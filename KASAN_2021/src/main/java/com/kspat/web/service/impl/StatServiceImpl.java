package com.kspat.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kspat.util.common.Constants;
import com.kspat.util.common.DailyStatUtil;
import com.kspat.util.common.DateTimeUtil;
import com.kspat.web.domain.DailyRule;
import com.kspat.web.domain.DailyStat;
import com.kspat.web.domain.DayInfo;
import com.kspat.web.domain.LatePoint;
import com.kspat.web.domain.MailContent;
import com.kspat.web.domain.Overtime;
import com.kspat.web.domain.Score;
import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.User;
import com.kspat.web.domain.UserState;
import com.kspat.web.domain.LateStatPoint;
import com.kspat.web.mapper.CalendarMapper;
import com.kspat.web.mapper.DashboardMapper;
import com.kspat.web.mapper.OvertimeMapper;
import com.kspat.web.mapper.RuleMapper;
import com.kspat.web.mapper.StatMapper;
import com.kspat.web.mapper.UserMapper;
import com.kspat.web.service.EmailService;
import com.kspat.web.service.EmailTempleatService;
import com.kspat.web.service.OvertimeService;
import com.kspat.web.service.StatService;

@Service
public class StatServiceImpl implements StatService {
	private static final Logger logger = LoggerFactory.getLogger(StatServiceImpl.class);

	@Autowired
	private StatMapper statMapper;

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	private CalendarMapper calendarMapper;

	@Autowired
	private RuleMapper ruleMapper;

	@Autowired
	private OvertimeMapper overtimeMapper;

	@Autowired
	private OvertimeService overtimeService;

	@Autowired
	private EmailTempleatService emailTempleatService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserMapper userMapper;

	@Value("#{overtime.ruleChangeDate}")
	String overtimeRuleChangeDate;



	/* ?????? ????????????
	 * 1. ?????? ?????? ??????
	 * ------------
	 * 2.
	 * (non-Javadoc)
	 * @see com.kspat.web.service.StatService#manualCreateDailyStat(com.kspat.web.domain.SearchParam)
	 */
	@Override
	public List<DailyStat> manualCreateDailyStat(SearchParam searchParam) {
		List<DailyStat> statList = new ArrayList<DailyStat>();
		//searchParam.setSearchDt("2019-09-25");//test ???
		String calDt = searchParam.getSearchDt();
		Boolean overtimeRuleChange = DateTimeUtil.isCalDtBefore(calDt,overtimeRuleChangeDate);


		DayInfo dayInfo = calendarMapper.getDayInfo(calDt);
		DailyRule dailyRule = ruleMapper.getCurrentDailyRule(calDt);


		List<UserState> list =  dashboardMapper.dashUserStateList(searchParam);
		if(list.size()>0){
			list = DailyStatUtil.getNowWorking(list,calDt, dayInfo,dailyRule);

			list = DailyStatUtil.getDailyStat(list,calDt, dayInfo,dailyRule);

			statList = setDailyStat(list, calDt, dayInfo,dailyRule);

			//??????????????? ????????? ????????? ??????
			statMapper.deleteDayilStat(calDt);
			for(DailyStat ds : statList){
				statMapper.insertDailyStat(ds);
				if(overtimeRuleChange == true) {
					//logger.debug("$$$$$$$$$$$$$ true");
					checkUpdateOvertime(calDt,ds);
				}
			}

			//???????????? ????????? update - ?????????????????? ?????? ???????????? 2021-07-16 ????????? ??????.
			// ???????????????????????? ??????
			if(overtimeRuleChange == false) {
				//logger.debug("%%%%%%%%%%%%%%%%%%%% false");
				updateOvertime(calDt, statList);
			}
		}

		SearchParam param = new SearchParam(calDt,calDt,"all","all");
		statList = statMapper.getDailyStatList(param);

		logger.debug("#######################################"+(overtimeRuleChange==true?"????????? ???????????? ????????????":"????????? ???????????? ??????"));

		return statList;
	}

	/** ???????????? ?????? ?????? ?????? 2021-07-09 ??????
	 * @param calDt
	 * @param ds
	 */
	private void checkUpdateOvertime(String calDt,DailyStat ds) {
		if("N".equals(ds.getHolidayYn()) && StringUtils.isNotEmpty(ds.getOutTm()) && StringUtils.isNotEmpty(ds.getExpOutTm())) {
			//1. ???????????? ?????????????????? ??????
			logger.debug(ds.toString());
			int dl = DateTimeUtil.getOutTmAndExpOutTmdiffMin(ds.getOutTm(),ds.getExpOutTm());
			if (dl >= 120) {
				logger.debug("========================>???????????? ????????????"+ dl);
				int res = overtimeMapper.checkOvertiomData(calDt,Integer.toString(ds.getId()) );

				Overtime ot = new Overtime();
				ot.setCrtdId(Integer.toString(ds.getId()));
				ot.setMdfyId(Integer.toString(ds.getId()));
				ot.setReqDt(calDt);
				ot.setExpOutTm(ds.getExpOutTm());
				ot.setOutTm(ds.getOutTm());
				ot.setOvertimeMin(dl);
				ot.setResult("001");

				if(res == 0) {
					logger.debug("????????????");
					overtimeMapper.insertOvertimeDailyStat(ot);
				}else {
					logger.debug("???????????? ??????");
					overtimeMapper.updateOvertimeDailyStat(ot);
				}

			}
		}
	}


	/** ???????????? update
	 * @param calDt
	 * @param statList
	 */
	private void updateOvertime(String calDt, List<DailyStat> statList) {
		List<Overtime> overtimeList = overtimeMapper.getRequestOvertimeList(calDt);

		if(overtimeList.size() > 0){
			SearchParam searchParam = new SearchParam();
			searchParam.setSearchDt(calDt);

			for(Overtime ot: overtimeList){
				searchParam.setCrtdId(ot.getCrtdId());
				Overtime checkOvertime = overtimeService.getReqDateOvertimeInfo(searchParam);

				if(checkOvertime != null){
					//overtime id set
					checkOvertime.setId(ot.getId());

					if(checkOvertime.isHolidayYn() == true){
						checkOvertime.setResult("003");
						checkOvertime.setMemo("??????");
					}
					if(checkOvertime.isDataErrorYn() == true){
						checkOvertime.setResult("003");
						checkOvertime.setMemo("????????? ??????");
					}
					//?????? ?????????  ????????? sql?????? ??????
					if(checkOvertime.getExpOutTm() == null || checkOvertime.getOutTm() == null){
						checkOvertime.setResult("003");
						checkOvertime.setMemo("????????????(?????????) ??????.");
						checkOvertime.setExpOutTm("00:00");
						checkOvertime.setOutTm("00:00");
					}

				}

				overtimeMapper.updateReqUserOvertime(checkOvertime);
			}
		}

	}


	private List<DailyStat> setDailyStat(List<UserState> list, String calDt,
			DayInfo dayInfo, DailyRule dailyRule) {

		List<DailyStat> dsList = new ArrayList<DailyStat>();
		for(UserState us: list){
			DailyStat ds= new DailyStat(us.getId(),us.getCapsName(),calDt,dayInfo.getCalWeekName(),dayInfo.getIsHoliday(),dayInfo.getDataError());
//			logger.info("***********************");
//			logger.info(ds.toString());
//			logger.info("***********************");


			if("Y".equals(dayInfo.getDataError())){
				/*
				 * ??????, ??????????????????,?????? ?????? ?????? --> ?????????????????? (?????? ???????????? ??????)
				 * ??????, ????????? ?????? ??????????????? ??????.
				 *
				 */
				if("Y".equals(dayInfo.getIsHoliday())){
					ds.setGoTm(null);
					ds.setOutTm(null);
					ds.setExpOutTm(null);
					ds.setLateTm(null);
					ds.setCalWorkTmMin(0);
					ds.setWorkTmMin(0);

					ds.setStLeave(0.0);
					ds.setStHlLeave(0.0);
					ds.setStOffcial("N");
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					ds.setStFailWorkTm(0);
					ds.setStAbsence(0);
					ds.setMemo("Data Error");
				}else{
					ds.setGoTm(null);
					ds.setOutTm(null);
					ds.setExpOutTm(null);
					ds.setLateTm(null);
					ds.setCalWorkTmMin(0);
					ds.setWorkTmMin(0);
					//?????? ??????
					if(us.getHlLeave()!=null){
						if("Y".equals(us.getHlLeave().getOffcial())){
							ds.setStHlLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStHlLeave(0.5);
							ds.setStOffcial("N");
						}
					}else{
						ds.setStHlLeave(0.0);
					}
					//????????????
					if(us.getLeave()!=null){
						if("Y".equals(us.getLeave().getOffcial())){
							ds.setStLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStLeave(1.0);
							ds.setStOffcial("N");
						}

					}else{
						ds.setStLeave(0.0);
						ds.setStOffcial(null);
					}
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					ds.setStFailWorkTm(0);
					ds.setStAbsence(0);
					ds.setMemo("Data Error");
				}
			}else{
				if("Y".equals(dayInfo.getIsHoliday())){
					if(us.getBtrip() != null){
						logger.debug("??????");
					}
					ds.setGoTm(us.getCalHereGo());
					ds.setOutTm(us.getCalHereOut());
					ds.setExpOutTm(us.getExpHereOut());
					ds.setLateTm(us.getLateTm());
					ds.setCalWorkTmMin(us.getCalWorkTmMin());
					ds.setWorkTmMin(us.getWorkTmMin());

					//?????? ??????
					if(us.getHlLeave()!=null){
						if("Y".equals(us.getHlLeave().getOffcial())){
							ds.setStHlLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStHlLeave(0.5);
							ds.setStOffcial("N");
						}
					}else{
						ds.setStHlLeave(0.0);
						ds.setStOffcial("N");
					}
					//????????????
					if(us.getLeave()!=null){
						if("Y".equals(us.getLeave().getOffcial())){
							ds.setStLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStLeave(1.0);
							ds.setStOffcial("N");
						}

					}else{
						ds.setStLeave(0.0);
						ds.setStOffcial("N");
					}
					// ??????????????? ?????????????????? ??????
					if(us.getSupple()!=null && us.getSupple().size()>0){//????????????
						if(us.getDiffWorkTmMin()<0){
							ds.setStFailWorkTm(0.5);
						}else{
							ds.setStFailWorkTm(0);
						}
						if(us.getCalHereGo()==null || us.getCalHereOut()==null){//??????????????? ???????????? ???????????? ????????????.
							ds.setStFailWorkTm(0.5);//???????????? 0.5??????
						}
						ds.setStAbsence(0);
					}else{
						ds.setStAbsence(0);
					}
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					ds.setMemo(null);

				}else if(us.getBtrip() != null){
					ds.setGoTm(us.getCalHereGo());
					ds.setOutTm(us.getCalHereOut());
					ds.setExpOutTm(us.getExpHereOut());
					ds.setLateTm(us.getLateTm());
					ds.setCalWorkTmMin(us.getCalWorkTmMin());
					ds.setWorkTmMin(us.getWorkTmMin());

					ds.setStLeave(0.0);
					ds.setStHlLeave(0.0);
					ds.setStOffcial("N");
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					ds.setStFailWorkTm(0);
					ds.setStAbsence(0);
					ds.setMemo("????????????");

				}else if(us.getWorkhome() != null){
					ds.setStWorkhome(1);
					ds.setWorkTmMin(us.getWorkTmMin());
					ds.setGoTm(us.getCalHereGo());
					ds.setOutTm(us.getCalHereOut());
					ds.setExpOutTm(us.getExpHereOut());
					ds.setLateTm(us.getLateTm());
					ds.setCalWorkTmMin(us.getCalWorkTmMin());

					if(us.getHlLeave() != null) {

						//????????????
						if("Y".equals(us.getHlLeave().getOffcial())){
							ds.setStHlLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStHlLeave(0.5);
							ds.setStOffcial("N");
						}
						ds.setStLeave(0.0);
						ds.setStShortLate(0);
						ds.setStLongLate(0);

						//??????????????????
						//????????????????????????????????? ???????????? ????????????(2022.05.19 ????????? ??????????????? ???????????? ??????????????? ????????????)
						/*
						if(us.getCalHereGo()==null || us.getCalHereOut()==null){
//							logger.debug("us.getCalWorkTmMin():"+us.getCalWorkTmMin());
//							logger.debug("us.getWorkTmMin():"+us.getWorkTmMin());
//							logger.debug("us.getDiffWorkTmMin():"+us.getDiffWorkTmMin());
//							logger.debug("us.getWorkTmMin():"+ (us.getCalWorkTmMin()-us.getWorkTmMin()));
							if(us.getCalWorkTmMin() !=0 && "Y".equals(us.getDashState())){//????????? ?????? 20170612
								ds.setStAbsence(0);
							}else{
								ds.setStAbsence(0);
							}

						}else{
							ds.setStAbsence(0);
						}
						*/
						ds.setMemo("??????????????? ????????????");
					}
					//???????????????????????????
					if(us.getDiffWorkTmMin()<0 && "Y".equals(us.getDashState())){ //????????? ?????? 20170612
						ds.setStFailWorkTm(0.5);
					}else{
						ds.setStFailWorkTm(0);
					}


				}else if(us.getLeave() != null){
					ds.setGoTm(null);
					ds.setOutTm(null);
					ds.setExpOutTm(null);
					ds.setLateTm(null);
					ds.setCalWorkTmMin(0);
					ds.setWorkTmMin(0);

					//????????????
					if("Y".equals(us.getLeave().getOffcial())){
						ds.setStLeave(0.0);
						ds.setStOffcial("Y");
					}else{
						ds.setStLeave(1.0);
						ds.setStOffcial("N");
					}

					ds.setStHlLeave(0.0);
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					ds.setStFailWorkTm(0);
					ds.setStAbsence(0);
					ds.setMemo("????????????");
				}else if(us.getHlLeave() != null){
					ds.setGoTm(us.getCalHereGo());
					ds.setOutTm(us.getCalHereOut());
					ds.setExpOutTm(us.getExpHereOut());
					ds.setLateTm(us.getLateTm());
					ds.setCalWorkTmMin(us.getCalWorkTmMin());
					ds.setWorkTmMin(us.getWorkTmMin());

					//????????????
					if("Y".equals(us.getHlLeave().getOffcial())){
						ds.setStHlLeave(0.0);
						ds.setStOffcial("Y");
					}else{
						ds.setStHlLeave(0.5);
						ds.setStOffcial("N");
					}
					ds.setStLeave(0.0);
					ds.setStShortLate(0);
					ds.setStLongLate(0);
					//???????????????????????????
					if(us.getDiffWorkTmMin()<0 && "Y".equals(us.getDashState())){ //????????? ?????? 20170612
						ds.setStFailWorkTm(0.5);
					}else{
						ds.setStFailWorkTm(0);
					}
					//??????????????????
					if(us.getCalHereGo()==null || us.getCalHereOut()==null){
//						logger.debug("us.getCalWorkTmMin():"+us.getCalWorkTmMin());
//						logger.debug("us.getWorkTmMin():"+us.getWorkTmMin());
//						logger.debug("us.getDiffWorkTmMin():"+us.getDiffWorkTmMin());
//						logger.debug("us.getWorkTmMin():"+ (us.getCalWorkTmMin()-us.getWorkTmMin()));
						if(us.getCalWorkTmMin() !=0 && "Y".equals(us.getDashState())){//????????? ?????? 20170612
							ds.setStAbsence(1);
						}else{
							ds.setStAbsence(0);
						}

					}else{
						ds.setStAbsence(0);
					}
					ds.setMemo("????????????");
				}else{
					ds.setGoTm(us.getCalHereGo());
					ds.setOutTm(us.getCalHereOut());
					ds.setExpOutTm(us.getExpHereOut());
					ds.setLateTm(us.getLateTm());
					ds.setCalWorkTmMin(us.getCalWorkTmMin());
					ds.setWorkTmMin(us.getWorkTmMin());

					//?????? ??????
					if(us.getHlLeave()!=null){
						if("Y".equals(us.getHlLeave().getOffcial())){
							ds.setStHlLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStHlLeave(0.5);
							ds.setStOffcial("N");
						}
					}else{
						ds.setStHlLeave(0.0);
					}
					//????????????
					if(us.getLeave()!=null){
						if("Y".equals(us.getLeave().getOffcial())){
							ds.setStLeave(0.0);
							ds.setStOffcial("Y");
						}else{
							ds.setStLeave(1.0);
							ds.setStOffcial("N");
						}

					}else{
						ds.setStLeave(0.0);
						ds.setStOffcial(null);
					}
					//????????????
					if("short".equals(us.getLate())){
						ds.setStShortLate(1);
						ds.setStLongLate(0);
					}else if("long".equals(us.getLate())){
						ds.setStShortLate(0);
						ds.setStLongLate(1);
					}else{
						ds.setStShortLate(0);
						ds.setStLongLate(0);
					}
                    //???????????????????????????
					if(us.getDiffWorkTmMin()<0 ){
						ds.setStFailWorkTm(0.5);
					}else{
						ds.setStFailWorkTm(0);
					}
					//??????????????????
					if(us.getCalHereGo()!=null && us.getCalHereOut()!=null){
						ds.setStAbsence(0);
					}else{
						if(us.getCalWorkTmMin() <= 0){
							ds.setStAbsence(1);
						}else{
							ds.setStAbsence(0);
						}
					}
					/* 2017-04-20 ??? ???????????? ??????
					if(us.getCalHereGo()==null || us.getCalHereOut()==null){
						ds.setStAbsence(1);
					}else{
						ds.setStAbsence(0);
					}
					*/
					ds.setMemo("????????????");
				}
				//????????? ?????? ?????? ????????????
			}

			//?????? ??????????????? ???????????? ????????? ???????????? ?????????.
			if(ds.getStAbsence()==1){
				ds.setStFailWorkTm(0);
			}
			//??????????????? ????????? N
			ds.setStAdjust("N");
			/*
			 * ????????? ?????? 20170821
			 * ???????????? ????????????, ???????????? ????????? ????????? ??????
			 */
			if("N".equals(us.getDashState())){
				ds.setStFailWorkTm(0);
				ds.setStAbsence(0);
			}

			//?????? list??? ??????
			dsList.add(ds);
		}


		return dsList;
	}



	@Override
	public List<DailyStat> getDailyStatList(SearchParam searchParam) {
		return statMapper.getDailyStatList(searchParam);
	}



	@Override
	public DailyStat getUserStatDetail(SearchParam searchParam) {
		return statMapper.getUserStatDetail(searchParam);
	}



	/* (non-Javadoc)
	 * ????????????
	 * @see com.kspat.web.service.StatService#updateAdjust(com.kspat.web.domain.DailyStat)
	 */
	@Override
	public DailyStat updateAdjust(DailyStat ds) {
		statMapper.updateAdjust(ds);

		SearchParam searchParam = new SearchParam();
		searchParam.setSearchUser(Integer.toString(ds.getId()));
		searchParam.setSearchDt(ds.getStDt());
		DailyStat stat = statMapper.getUserStatDetail(searchParam);
		return stat;
	}



	/* (non-Javadoc)
	 * ????????????
	 * @see com.kspat.web.service.StatService#batchDailyStat(java.lang.String)
	 */
	@Override
	public int batchDailyStat(String calDt) {
		logger.debug("  ?????????: "+calDt);
		List<DailyStat> statList = new ArrayList<DailyStat>();
		Boolean overtimeRuleChange = DateTimeUtil.isCalDtBefore(calDt,overtimeRuleChangeDate);


		DayInfo dayInfo = calendarMapper.getDayInfo(calDt);
		DailyRule dailyRule = ruleMapper.getCurrentDailyRule(calDt);

		SearchParam searchParam = new SearchParam();
		searchParam.setSearchDt(calDt);

		List<UserState> list =  dashboardMapper.dashUserStateList(searchParam);

		int userCount = list.size();

		if(userCount>0){
			list = DailyStatUtil.getNowWorking(list,calDt, dayInfo,dailyRule);

			list = DailyStatUtil.getDailyStat(list,calDt, dayInfo,dailyRule);

			statList = setDailyStat(list, calDt, dayInfo,dailyRule);

			//??????????????? ????????? ????????? ??????
			statMapper.deleteDayilStat(calDt);
			for(DailyStat ds : statList){
				statMapper.insertDailyStat(ds);
				if(overtimeRuleChange == true) {
					checkUpdateOvertime(calDt,ds);
				}
			}

			//???????????? ????????????
			if(overtimeRuleChange == false) {
				updateOvertime(calDt, statList);
			}

			logger.debug("#######################################"+(overtimeRuleChange==true?"????????? ???????????? ????????????":"????????? ???????????? ??????"));
		}


		return userCount;
	}



	/* (non-Javadoc)
	 * ???????????????
	 * @see com.kspat.web.service.StatService#getUserScore(com.kspat.web.domain.SearchParam)
	 */
	@Override
	public Score getUserScore(SearchParam param) {

		return statMapper.getUserScore(param);
	}



	@Override
	public List<Score> getScoreList(SearchParam param) {
		return statMapper.getScoreList(param);
	}



	@Override
	public List<LatePoint> getLatePointList(SearchParam param) {
		return statMapper.getLatePointList(param);
	}



	/* ???????????? update
	 * @see com.kspat.web.service.StatService#updateLateStat(java.lang.String)
	 */
	@Override
	public int[] updateLateStat(String targetYear) {
		logger.debug("targetYear:{}",targetYear);
		List<LateStatPoint> list = statMapper.getLateStatPointList(targetYear);

		//mail content
		MailContent content = emailService.getMailContentDetail(new MailContent(Constants.MAIL_CONTENT_LATE_POINT_ID));

		//lateDefaultAddr
		String lateAddr = emailService.getMailDefault();
		//String[] lateDefaultAddr = lateAddr.split(",");
	    StringTokenizer tokens = new StringTokenizer( lateAddr, "," );
	    //???????????????
	    String[] lateDefaultAddress = null;
	    if(tokens.countTokens()>0){
	    	lateDefaultAddress = new String[tokens.countTokens()];
		    for( int i = 0; tokens.hasMoreElements(); i++ ){
		    	lateDefaultAddress[i]=tokens.nextToken();
		    }
	    }

		/*
		 * ?????? ????????? ??????
		 */
		int[] result = new int[3];
		int insertCnt=0;
		int mailSendCnt=0;
		int updateCnt=0;

		for(LateStatPoint p: list){
			//logger.debug(p.toString());
			if(p.getKlpLatePoint()==null){//???????????? ???????????? ???????????? ?????? ?????? insert(????????????)
				//logger.debug("insert");
				LatePoint lp = new LatePoint(p.getId(),p.getYear(),p.getShortLateSum(),p.getLongLateSum(),p.getOrgLatePoint(),p.getLatePoint());
				statMapper.insertLatePoint(lp);
				insertCnt += 1;
			}else{
				if(p.getLatePoint() > p.getKlpLatePoint()){//??????????????? ????????? ????????? ???????????? ???????????? ???????????? ?????? mail?????? and update
					User user = userMapper.getUserDetailById(String.valueOf(p.getId()));
					if("N".equals(user.getDashState())){//???????????????????????? N ?????? (?????????)???????????? ?????? ?????????.
						//logger.debug("update");
						LatePoint lp = new LatePoint(p.getId(),p.getYear(),p.getShortLateSum(),p.getLongLateSum(),p.getOrgLatePoint(),p.getLatePoint());

						statMapper.updateLatePoint(lp);
						updateCnt += 1;
					}else{
						//logger.debug("mail?????? & update");
						//??????????????????
						String [] sendTo = makeLateMailSendAddrs(user,lateDefaultAddress);

						LatePoint lp = new LatePoint(p.getId(),p.getYear(),p.getShortLateSum(),p.getLongLateSum(),p.getOrgLatePoint(),p.getLatePoint(),p.getKlpMailCount(),p.getKlpLastMailSendDt());
						emailTempleatService.setLatePointEmailTempleate(user,lp,content.getContent().replace("\n","<br/>"),sendTo);
						statMapper.updateLatePointWithMailSendDt(lp);

						mailSendCnt += 1;
					}

				}else{//????????? ????????? update???
					//logger.debug("update");
					LatePoint lp = new LatePoint(p.getId(),p.getYear(),p.getShortLateSum(),p.getLongLateSum(),p.getOrgLatePoint(),p.getLatePoint());

					statMapper.updateLatePoint(lp);
					updateCnt += 1;
				}
			}
		}

		result[0]=insertCnt;
		result[1]=mailSendCnt;
		result[2]=updateCnt;
		return result;

	}



	/** ??????????????????
	 * @param user
	 * @param lateDefaultAddr
	 * @return
	 */
	private String[] makeLateMailSendAddrs(User user, String[] lateDefaultAddress) {

		//??????????????? Emails
		String[] managersEmail = userMapper.mailSendManagerListByDeptcd(user.getDeptCd());
		//???????????? ????????????= ??????2(??????,?????????????????????)+ ??????????????????
		int arraySize=0;
		if(lateDefaultAddress != null && lateDefaultAddress.length>0){
			arraySize = 1+lateDefaultAddress.length+managersEmail.length;
		}else{
			arraySize = 1+managersEmail.length;
		}

		//?????????????????????
		String[] sendTo = new String[arraySize];

		sendTo[0]=user.getEmail();
		for(int i=0;i<managersEmail.length;i++){
			//logger.debug(managersEmail[i]);
			sendTo[i+1]=managersEmail[i];
		}

		if(lateDefaultAddress != null && lateDefaultAddress.length>0){
			for( int i = 0; i< lateDefaultAddress.length; i++ ){
				sendTo[i+1+managersEmail.length]=lateDefaultAddress[i];
		    }
		}

		/* ????????????
		for(int i=0;i<sendTo.length;i++){
			logger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%?????????????????????:"+sendTo[i]);;
		}
		String[] sendTo2 = {"bbaga93@naver.com","bbaga93@gmail.com"};
		 */
		return sendTo;
	}



	/* (non-Javadoc)
	 * ?????? 06-25, 10-25 ???????????????????????? ????????????
	 * @see com.kspat.web.service.StatService#getAnnualEmailSendScoreList(com.kspat.web.domain.SearchParam)
	 */
	@Override
	public List<Score> getAnnualEmailSendScoreList(SearchParam searchParam) {
		return statMapper.getAnnualEmailSendScoreList(searchParam);
	}



	@Override
	public void deleteRawData() {
		statMapper.deleteRawData();

	}




}
