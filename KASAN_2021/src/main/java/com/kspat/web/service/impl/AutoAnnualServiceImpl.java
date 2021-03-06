package com.kspat.web.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kspat.util.AutoAnnualUtil;
import com.kspat.web.domain.AutoAnnual;
import com.kspat.web.domain.ComPenAnnual;
import com.kspat.web.domain.Score;
import com.kspat.web.domain.SearchParam;
import com.kspat.web.mapper.AutoAnnualMapper;
import com.kspat.web.service.AutoAnnualService;
import com.kspat.web.service.EmailTempleatService;
import com.kspat.web.service.StatService;

@Service
public class AutoAnnualServiceImpl implements AutoAnnualService {
	private static DateTimeFormatter fmt = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
	private static DateTimeFormatter fmt_yyyy = org.joda.time.format.DateTimeFormat.forPattern("yyyy");

	@Value("#{checkDay}")
	String[] CHECK_DAY;

	@Autowired
	private AutoAnnualMapper autoAnnualMapper;

	@Autowired
	private StatService statService;

	@Autowired
	private EmailTempleatService emailTempleatService;

	@Override
	public List<AutoAnnual> manualCreateAutoAnnual(SearchParam searchParam) {

		List<AutoAnnual> list = autoAnnualMapper.getUserHeirDtList(searchParam);
		List<AutoAnnual> newList = new ArrayList<AutoAnnual>();
		//기존연차정보 삭제
		autoAnnualMapper.deleteAllAutoAnnual();

		//연차 update
		if(list !=null){
			for(AutoAnnual aa : list){
				AutoAnnual na = getUserCurrentAnnual(aa, searchParam.getSearchDate());
				na.setCrtdId(searchParam.getCrtdId());
				na.setMdfyId(searchParam.getCrtdId());
				newList.add(na);
				//System.out.println(na.toString());
				//autoAnnualMapper.deleteNowAnnual(na);
				autoAnnualMapper.upsertAutoAnnual(na);
				//System.out.println(aa.toString());
			}
		}
		return newList;
	}

	/** toDay 기준 연차 계산
	 * @param aa
	 * @param toDay
	 * @return
	 */
	private AutoAnnual getUserCurrentAnnual(AutoAnnual aa,String toDay) {

		DateTime hireDt = DateTime.parse(aa.getHireDt(), fmt);
		DateTime currDt = DateTime.parse(toDay, fmt);

		int hireYear = hireDt.getYear();
		int currYear = currDt.getYear();

		String term = null;
		double availAnnual= 0;//사용가능연차=자동계산연차(autoAnnual)+보정연차
		double autoAnnual = 0;//자동계산된연차
		String startDt=null, endDt=null;
		String type=null;

		//입사일이 1년이 안될때
		int realYear = AutoAnnualUtil.getRealYearDateTime(hireDt,currDt);//만 근무년
		int year = AutoAnnualUtil.getYear(String.valueOf(hireYear),String.valueOf(currYear));
		double [] BDAnnualArray = {0,0,15,15,16,16,17,17,18,18,19,19,20,20,21,21,22,22,23,23,24,24,25,25};

		/**
		 * 2018.06.02 연차계산로직이 기준일(applyDtType)을 기준으로 분기가되었는데,
		 * 모두 기준일이 후 'after'로직으로 계산. (서버의 기준일은  1999-12-31로 변경 : 최초입사자보다 빠른날)
		 *
		 * 법의개정으로 2년차(AB)직원의 전년도 사용연차 차감로직은 삭제.
		 *
		 * 2018.06.15 기준일 2018.01.01로 변경 before로직사용함.
		 *
		 */


		if("before".equals(aa.getApplyDtType())){//과거로직 사용안함.
//			System.out.println("before");
//			System.out.println("만 나이  계산(realYear):"+ realYear);
//			System.out.println("한국나이 계산(year):"+ year);
			if(hireYear == currYear){
//				System.out.println("C로직");
				type = "BC";
				//String tempYear = hireDt.plusYears(2).toString(fmt_yyyy);
				//System.out.println(hireDt.plusYears(2).toString(fmt));
				//System.out.println(DateTime.parse(tempYear+"-12-31", fmt).toString(fmt));
				DateTime yearLastDay = DateTime.parse(hireYear+"-12-31", fmt);
				int  termDay = Days.daysBetween(hireDt, yearLastDay).getDays();
//				System.out.println("termDay: "+ termDay);
				double annual = ((double)termDay*15.0)/365.0;
//				System.out.println("년차 d: "+ annual);
				double annual2 = Math.ceil(annual);
				autoAnnual = (int) annual2;
				startDt = hireDt.toString(fmt);
				endDt = yearLastDay.toString(fmt);;
				term = startDt +" ~ "+ endDt;
//				System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"("+termDay+")		"+availAnnual+"		"+term);
			}else{
				//System.out.println("만 3년이상");
				type = "BD";

				double annual=0;
				if(year < BDAnnualArray.length){
					annual = BDAnnualArray[year];
				}else{
					annual = 25;
				}


				if(annual > 25){
					annual =25;
				}
				//System.out.println("연차 :"+annual);
				String tempYear = currDt.toString(fmt_yyyy);
				//System.out.println();
				//System.out.println("기간:"+DateTime.parse(tempYear+"-01-01", fmt).toString(fmt) +" ~ "+ DateTime.parse(tempYear+"-12-31", fmt).toString(fmt));

				autoAnnual = annual;
				startDt = DateTime.parse(tempYear+"-01-01", fmt).toString(fmt);
				endDt = DateTime.parse(tempYear+"-12-31", fmt).toString(fmt);
				term = startDt +" ~ "+ endDt;
				//System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"		"+availAnnual+"		"+term);
			}



		}else if("after".equals(aa.getApplyDtType())){
			//System.out.println("after");
			if(realYear < 1){
				type = "AA";
				autoAnnual = Months.monthsBetween(hireDt, currDt).getMonths();
				startDt = hireDt.toString(fmt);
				endDt = hireDt.plusMonths(12).minusDays(1).toString(fmt);
				term = startDt +" ~ "+ endDt;
//					System.out.println("만1년 미만");
//					System.out.println("년차:"+ ann);
//					System.out.println("기간:"+ term);
//					System.out.println("Day		CurrentDT		realYear		year		state		annual		term");
				//System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"		"+availAnnual+"		"+term);

			}else if(realYear < 2){
				/**
				 * 2018.06.02 법이 바뀌어 전년도 사용연차 마이너스 로직 삭제.
				 * 무조건 연차 15개 부여
				 */
				type = "AB";
				//double usedAnnual = getTypeAAUsedAnnual(aa);
				//System.out.println("usedAnnual:"+usedAnnual);

				//autoAnnual = 15-usedAnnual;
				autoAnnual = 15;
				startDt = hireDt.plusMonths(12).toString(fmt);
				endDt = hireDt.plusMonths(24).minusDays(1).toString(fmt);
				term = startDt +" ~ "+ endDt;

//					System.out.println("만2년 미만");
//					System.out.println("년차:"+ ann);
//					System.out.println("기간:"+term);
				//System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"		"+availAnnual+"		"+term);

			}else{
//				String hierDt_MM_DD = hireDt.toString(fmt_MM_dd);
//				boolean dtCheck = "01-01".equals(hierDt_MM_DD);//만 2년을 딱 채웠나?

				//if(year == 3 && dtCheck == false){
				if(year == 3){
					type = "AC";
					String tempYear = hireDt.plusYears(2).toString(fmt_yyyy);
					//System.out.println(hireDt.plusYears(2).toString(fmt));
					//System.out.println(DateTime.parse(tempYear+"-12-31", fmt).toString(fmt));
					DateTime yearLastDay = DateTime.parse(tempYear+"-12-31", fmt);
					int  termDay = Days.daysBetween(hireDt.plusYears(2), yearLastDay).getDays();
//					System.out.println("id: "+ aa.getId());
//					System.out.println("termDay: "+ termDay);
					/*
					 * 2022s년3월7일 AC구간 15에서 16으로 변경요청 반영
					 * double annual = (termDay*15.0)/365.0;
					 */
					double annual = (termDay*16.0)/365.0;
//					System.out.println("년차 d: "+ annual);
					//autoAnnual = Math.ceil(annual);//올림으로 변경[2018.06.04]
					autoAnnual = Math.floor(annual);//내림으로 변경[2022.03.23]
//					System.out.println("년차 d 올림: "+ autoAnnual);
					startDt = hireDt.plusMonths(24).toString(fmt);
					endDt = yearLastDay.toString(fmt);
					term = startDt +" ~ "+ endDt;
					//System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"("+termDay+")		"+availAnnual+"		"+term);
				}else{
					//System.out.println("만 3년이상");
					type = "AD";
					//term = hireDt.plusMonths(12).toString(fmt) +" ~ "+ hireDt.plusMonths(24).minusDays(1).toString(fmt);
					double annual = 15;
					annual  = annual + (year/2 -2) +1;

					if(annual > 25){
						annual =25;
					}
					//System.out.println("연차 :"+annual);
					String tempYear = currDt.toString(fmt_yyyy);
					//System.out.println();
					//System.out.println("기간:"+DateTime.parse(tempYear+"-01-01", fmt).toString(fmt) +" ~ "+ DateTime.parse(tempYear+"-12-31", fmt).toString(fmt));

					autoAnnual = annual;
					startDt = DateTime.parse(tempYear+"-01-01", fmt).toString(fmt);
					endDt = DateTime.parse(tempYear+"-12-31", fmt).toString(fmt);
					term = startDt +" ~ "+ endDt;
					//System.out.println("		"+currDt.toString(fmt)+"		"+realYear+"		"+year+"		"+type+"		"+availAnnual+"		"+term);
				}
			}
		}
		aa.setAutoAnnual(autoAnnual);
		aa.setType(type);
		aa.setStartDt(startDt);
		aa.setEndDt(endDt);
		aa.setYear(year);

		//연차 보정체크
		ComPenAnnual cpa =  getUserComPenAnnual(aa);

		//System.out.println("comAnnual :"+comAnnual + "/ "+aa.toString());
		aa.setComAnnual(cpa==null?0:cpa.getComAnnual());
		availAnnual = autoAnnual+aa.getComAnnual();

		aa.setAvailCount(availAnnual);

//		if(aa.getId() == 43 || aa.getId() == 59 || aa.getId() == 68) {
//			System.out.println("====>id:"+aa.getId());
//			System.out.println("====>realYear:"+realYear);
//			System.out.println("====>year:"+year);
//			System.out.println("====>aa:"+aa.toString());
//
//		}
		return aa;

	}



	private double getTypeAAUsedAnnual(AutoAnnual aa) {

		String startDt = aa.getHireDt();
		aa.setStartDt(startDt);
		String endDt = DateTime.parse(aa.getHireDt(), fmt).plusMonths(12).minusDays(1).toString(fmt);
		aa.setEndDt(endDt);
		return autoAnnualMapper.getTypeAAUsedAnnual(aa);
	}

	@Override
	public List<AutoAnnual> getAutoAnnualList(SearchParam searchParam) {
		return autoAnnualMapper.getAutoAnnualList(searchParam);
	}

	@Override
	public ComPenAnnual getUserComPenAnnual(AutoAnnual autoAnnual) {
		ComPenAnnual cpa = autoAnnualMapper.getUserComPenAnnual(autoAnnual);
		if(cpa == null){
			cpa = new ComPenAnnual(autoAnnual.getId(),autoAnnual.getStartDt(),autoAnnual.getEndDt(),0);
		}

		return cpa;
	}

	@Override
	public AutoAnnual editComPenAnnual(ComPenAnnual cpa) {
		//연차보정
		autoAnnualMapper.upsertComPenAnnual(cpa);

		return calculateUserAutoAnnual(cpa);
	}

	private AutoAnnual calculateUserAutoAnnual(ComPenAnnual cpa) {
		AutoAnnual aa = new AutoAnnual();

		//해당 id 연차 다시 계산
		SearchParam searchParam = new SearchParam(cpa.getId(),cpa.getStartDt(),cpa.getEndDt());

		DateTime dateTime = new DateTime();
	    searchParam.setSearchDate(dateTime.toString(fmt));

		List<AutoAnnual> list = manualCreateAutoAnnual(searchParam);


		return autoAnnualMapper.getUserAutoAnnualDetail(searchParam);
	}

	@Override
	public void sendRemainingAnnualMail(SearchParam searchParam) {
		String today = searchParam.getSearchDate();
		boolean send = false;

		/* 연차확인 메일은 서버 처음 적용하는 2020-02-18일 테스트로 전직원 한번 발송
		 * 나머지는 목록에 있는 날짜만 발송
		 */
		if("2020-02-18".equals(today)) {// 최초 테스트용
			send = true;
		}else { // 이후 적용 로직
			send = Arrays.asList(CHECK_DAY).contains(today.substring(5));
		}

		if(send) {
			List<Score> slist = statService.getAnnualEmailSendScoreList(searchParam);
			//메일 발송 성공 개발자 확인용 추가
			slist.add(new Score(0,"개발자","관리팀","2020-02-18","2020-02-18","bbaga93@daum.net",10.0));

			for(Score s : slist) {
				//System.out.println(s.toString());
				//logger.debug("아이디 {} : {} 님은 {}/{} {}~{} 사용가능연차:{} 입니다.",s.getId(),s.getCapsName(),s.getDeptName(),s.getEmail(),s.getStartDt(),s.getEndDt(),s.getCurrCount());
				emailTempleatService.setRemainingAnnualEmailTempleate(s);
			}

		}

	}


}
