package com.kspat.web.controller;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kspat.util.common.DateTimeUtil;
import com.kspat.util.common.DownloadUtil;
import com.kspat.util.common.SessionUtil;
import com.kspat.web.domain.AutoAnnual;
import com.kspat.web.domain.Calendar;
import com.kspat.web.domain.CodeData;
import com.kspat.web.domain.CodeGroup;
import com.kspat.web.domain.ComPenAnnual;
import com.kspat.web.domain.DailyRule;
import com.kspat.web.domain.Dept;
import com.kspat.web.domain.DeptMapping;
import com.kspat.web.domain.MailContent;
import com.kspat.web.domain.RawData;
import com.kspat.web.domain.Regulation;
import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.SessionInfo;
import com.kspat.web.domain.User;
import com.kspat.web.domain.YearlyRule;
import com.kspat.web.service.AutoAnnualService;
import com.kspat.web.service.CalendarService;
import com.kspat.web.service.CodeService;
import com.kspat.web.service.EmailService;
import com.kspat.web.service.ManagementService;
import com.kspat.web.service.RegulationService;
import com.kspat.web.service.RuleService;
import com.kspat.web.service.UserService;

@RequestMapping(value = "/management/*")
@Controller
public class ManagementController {

	private static final Logger logger = LoggerFactory.getLogger(ManagementController.class);

	@Autowired
	private ManagementService managementService;

	@Autowired
	private UserService userService;

	@Autowired
	private CodeService codeService;

	@Autowired
	private CalendarService calendarService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private AutoAnnualService autoAnnualService;

	@Autowired
	private RegulationService regulationService;

	@Autowired
	private EmailService emailService;


	//Formatter
	DateTimeFormatter fmt_ymdhms = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	DateTimeFormatter fmt_ymd = DateTimeFormat.forPattern("yyyy-MM-dd");



	@Value("#{file.baseDir}")
	private String baseDir;

	@RequestMapping(value = "/rawData")
	public String rawData(Model model, SearchParam searchParam,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		//logger.info("Welcome home! The client locale is {}.", locale);
		List<RawData> ulist = userService.getRawDataSearchList(searchParam);
		model.addAttribute("ulist", ulist);
		return "management/rawData";
	}

	@RequestMapping(value="/rawDataListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String rawDataListAjax(Model model, SearchParam searchParam,HttpServletRequest request, HttpServletResponse response, HttpSession session){
		//logger.debug(searchParam.toString());
		List<RawData> list = userService.getRawDataList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
		//return (new Gson().toJson(list));
	}

	@RequestMapping(value = "/code")
	public String code(Model model,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		return "management/code";
	}


	@RequestMapping(value = "/codeGroupListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeGroupListAjax(Model model,SearchParam searchParam) {

		List<CodeGroup> list = codeService.getCodeGroupList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}

	@RequestMapping(value = "/codeGroupInsertAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeGroupInsertAjax(Model model, CodeGroup codeGroup,HttpServletRequest request) {
		//logger.debug(codeGroup.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);
		codeGroup.setCrtdId(Integer.toString(info.getId()));
		codeGroup.setMdfyId(Integer.toString(info.getId()));

		codeGroup = codeService.insertCodeGroup(codeGroup);
		//logger.debug("return-->"+codeGroup.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(codeGroup);
	}

	@RequestMapping(value = "/codeGroupDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeGroupDetailAjax(Model model,CodeGroup codeGroup) {

		CodeGroup cg = codeService.getCodeGroupDetail(codeGroup);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(cg);
	}


	@RequestMapping(value = "/codeGroupEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeGroupEditAjax(Model model, CodeGroup codeGroup,HttpServletRequest request) {
		//logger.debug(codeGroup.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);
		codeGroup.setMdfyId(Integer.toString(info.getId()));

		codeGroup = codeService.updateCodeGroup(codeGroup);
		//logger.debug("return-->"+codeGroup.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(codeGroup);
	}

	/** Code Data */
	@RequestMapping(value = "/codeDataListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeDataListAjax(Model model,SearchParam searchParam) {

		List<CodeData> list = codeService.getCodeDataList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}

	@RequestMapping(value = "/codeDataInsertAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeDataInsertAjax(Model model, CodeData codeData, HttpServletRequest request) {
		//logger.debug(codeData.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);
		codeData.setCrtdId(Integer.toString(info.getId()));
		codeData.setMdfyId(Integer.toString(info.getId()));

		codeData = codeService.insertCodeData(codeData);
		//logger.debug("return-->"+codeData.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(codeData);
	}


	@RequestMapping(value = "/codeDataDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeDataDetailAjax(Model model,CodeData codeData) {

		CodeData cd = codeService.getCodeDataDetail(codeData);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(cd);
	}


	@RequestMapping(value = "/codeDataEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String codeDataEditAjax(Model model, CodeData codeData,HttpServletRequest request) {
		//logger.debug(codeData.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);
		codeData.setMdfyId(Integer.toString(info.getId()));

		codeData = codeService.updateCodeData(codeData);
		//logger.debug("return-->"+codeData.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(codeData);
	}


	/** ?????????(??????)?????? */

	@RequestMapping(value = "/rules")
	public String rules(Model model,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";
		//logger.info("Welcome home! The client locale is {}.", locale);

		return "management/rules";
	}


	/** ?????????, ?????? *****************************************************************************/


	/** ????????????????????????
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/holiday")
	public String holiday(Model model,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";
		//logger.info("Welcome home! The client locale is {}.", locale);

		return "management/holiday";
	}

	/** ??????(??????)??????
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/holidayListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String holidayListAjax(Model model,SearchParam searchParam) {

		//logger.debug(searchParam.toString());
		List<Calendar> list = calendarService.getHolidayList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	/** ??????????????????
	 * @param model
	 * @param searchDate
	 * @return
	 */
	@RequestMapping(value = "/holidayDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String holidayDetailAjax(Model model,String searchDate ) {
		Calendar calendar = new Calendar();
		calendar.setCalDate1(searchDate);
		/* CalDate1??? ???????????? ?????????????????? ??????
		if(searchDate != null){
			String[] dt = searchDate.split("-");
			calendar.setCalYear(dt[0]);
			calendar.setCalMonth(dt[1]);
			calendar.setCalDay(dt[2]);
		}
		*/
		Calendar cd = calendarService.getHolidayDetail(calendar);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(cd);
	}


	/** ????????????
	 * @param model
	 * @param calendar
	 * @return
	 */
	@RequestMapping(value = "/holidayEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String holidayEditAjax(Model model, Calendar calendar,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		calendar.setMdfyId(Integer.toString(info.getId()));
		//logger.debug(calendar.toString());

		calendar = calendarService.updateHoliday(calendar);
		//logger.debug("return-->"+calendar.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(calendar);
	}

	/** ???????????? ***********************************************************************/

	/** ???????????? ?????? ??????
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/user")
	public String user(Model model, SearchParam searchParam,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";


		//logger.info("Welcome home! The client locale is {}.", locale);
		List<CodeData> deptList = codeService.getCommonCodeList("DEPT");
		model.addAttribute("deptList", deptList);
		List<CodeData> positionList = codeService.getCommonCodeList("POSITION");
		model.addAttribute("positionList", positionList);
		List<CodeData> stateList = codeService.getCommonCodeList("USER_STATE");
		model.addAttribute("stateList", stateList);
		List<CodeData> authList = codeService.getCommonCodeList("AUTH");
		model.addAttribute("authList", authList);
		return "management/user";
	}

	/** User list (????????????)
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/userListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String userListAjax(Model model,SearchParam searchParam) {
		//logger.debug(searchParam.toString());
		List<User> list = userService.getUserList(searchParam);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/userDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String userDetailAjax(Model model,String searchId ) {
		User user = userService.getUserDetailById(searchId);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(user);
	}


	@RequestMapping(value = "/userInsertAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String userInsertAjax(Model model,User user ,HttpServletRequest request, HttpServletResponse response) {
		//session ?????? ??????
		SessionInfo info =SessionUtil.getSessionInfo(request);
		user.setCrtdId(Integer.toString(info.getId()));
		user.setMdfyId(Integer.toString(info.getId()));
		user = userService.insertUser(user);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(user);
	}

	@RequestMapping(value = "/userPwdInitAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String userPwdInitAjax(Model model,User user ,HttpServletRequest request, HttpServletResponse response) {
		String res = userService.initUserPassword(user.getId());
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(res);
	}

	@RequestMapping(value = "/userEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String userEditAjax(Model model,User user,HttpServletRequest request, HttpServletResponse response ) {
		//session ?????? ??????
		SessionInfo info =SessionUtil.getSessionInfo(request);
		user.setMdfyId(Integer.toString(info.getId()));
		user = userService.updateUser(user);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(user);
	}

	/** ??????????????? ??????user list
	 * @param model
	 * @param user
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getDeptUserAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getDeptUserAjax(Model model,SearchParam searchParam,HttpServletRequest request, HttpServletResponse response ) {
		logger.debug(searchParam.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);

		List<User> ulist = userService.getUserSearchList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(ulist);
	}

	/** ?????????-?????? ??????
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/managerDept")
	public String managerDept(Model model, SearchParam searchParam,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";


		//logger.info("Welcome home! The client locale is {}.", locale);
		List<CodeData> deptList = codeService.getCommonCodeList("DEPT");
		model.addAttribute("deptList", deptList);
//		List<CodeData> positionList = codeService.getCommonCodeList("POSITION");
//		model.addAttribute("positionList", positionList);
//		List<CodeData> stateList = codeService.getCommonCodeList("USER_STATE");
//		model.addAttribute("stateList", stateList);
//		List<CodeData> authList = codeService.getCommonCodeList("AUTH");
//		model.addAttribute("authList", authList);
		return "management/managerDept";
	}

	/** ????????? ??????
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/managerListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String managerListAjax(Model model,SearchParam searchParam) {
		//logger.debug(searchParam.toString());
		searchParam.setSearchAuth("002");//????????? ??????
		List<User> list = userService.getManagerList(searchParam);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/managerDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String managerDetailAjax(Model model,String searchId ) {
		User user = userService.getManagerDetailById(searchId);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
		return gson.toJson(user);
	}

	@RequestMapping(value = "/managerEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String managerEditAjax(Model model,User user,HttpServletRequest request, HttpServletResponse response ) {
		//session ?????? ??????
		SessionInfo info =SessionUtil.getSessionInfo(request);
		user.setCrtdId(Integer.toString(info.getId()));
		user.setMdfyId(Integer.toString(info.getId()));

		//logger.debug(user.toString());

		user = userService.updateManager(user);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(user);
	}


	/** ????????? ????????? ??????
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deptManagerCountAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String deptManagerCountAjax(Model model,HttpServletRequest request, HttpServletResponse response ) {
		List<DeptMapping> list = userService.getDeptManagerCount();

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}



	/** ????????????  */

	@RequestMapping(value = "/annual")
	public String annual(Model model, HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		//logger.info("Welcome home! The client locale is {}.", locale);
		List<User> userList = userService.getSearchUserList();
		model.addAttribute("userList", userList);
		return "management/annual";
	}



	/** Auto ????????????  */

	@RequestMapping(value = "/autoAannual")
	public String autoAnnual(Model model, HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		//logger.info("Welcome home! The client locale is {}.", locale);
		List<CodeData> deptList = codeService.getCommonCodeList("DEPT");
		model.addAttribute("deptList", deptList);
		List<CodeData> stateList = codeService.getCommonCodeList("USER_STATE");
		model.addAttribute("stateList", stateList);
		return "management/autoAnnual";
	}

	@RequestMapping(value = "/autoAnnualListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String autoAnnualListAjax(Model model,SearchParam searchParam) {

		//logger.debug(searchParam.toString());
		List<AutoAnnual> list = autoAnnualService.getAutoAnnualList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}



	@RequestMapping(value = "/autoAnnualManualCreateAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String autoAnnualManualCreateAjax(Model model,SearchParam searchParam,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		searchParam.setCrtdId(Integer.toString(info.getId()));

		logger.debug("=== Manual Auto Annual Create start==================");
		DateTime dateTime = new DateTime();
	    logger.debug("Current time - " + dateTime.toString(fmt_ymd));

	    searchParam.setSearchDate(dateTime.toString(fmt_ymd));

		List<AutoAnnual> list = autoAnnualService.manualCreateAutoAnnual(searchParam);



		// ?????? 06-25, 10-25 ???????????????????????? ????????????
		// ???????????? ????????? ????????? ?????????. ????????? ????????? ??????
		//autoAnnualService.sendRemainingAnnualMail(searchParam);

		logger.debug("=== Manual Auto Annual Create End==================");
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/getUserComPenAnnualAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getUserComPenAnnualAjax(Model model,AutoAnnual autoAnnual,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);


		ComPenAnnual cpa = autoAnnualService.getUserComPenAnnual(autoAnnual);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(cpa);
	}


	@RequestMapping(value = "/editComPenAnnualAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String editComPenAnnualAjax(Model model,ComPenAnnual cpa,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		cpa.setCrtdId(Integer.toString(info.getId()));
		cpa.setMdfyId(Integer.toString(info.getId()));

		logger.debug(cpa.toString());
		AutoAnnual aa = autoAnnualService.editComPenAnnual(cpa);
		logger.debug(aa.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(aa);
	}






	/** ???????????? *******************************************/

	@RequestMapping(value = "/dailyRuleListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String dailyRuleListAjax(Model model,SearchParam searchParam) {

		logger.debug(searchParam.toString());
		List<DailyRule> list = ruleService.getDailyRuleList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/dailyRuleDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String dailyRuleDetailAjax(Model model,SearchParam searchParam) {

		logger.debug(searchParam.toString());
		DailyRule rule = ruleService.getDailyRuleDetail(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}


	@RequestMapping(value = "/dailyRuleEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String dailyRuleEditAjax(Model model,DailyRule dailyRule,HttpServletRequest request, HttpServletResponse response) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		dailyRule.setCrtdId(Integer.toString(info.getId()));
		dailyRule.setMdfyId(Integer.toString(info.getId()));

		logger.debug(dailyRule.toString());
		DailyRule rule = ruleService.updateDailyRule(dailyRule);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}


	@RequestMapping(value = "/dailyRuleInsertAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String dailyRuleInsertAjax(Model model,DailyRule dailyRule,HttpServletRequest request, HttpServletResponse response) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		dailyRule.setCrtdId(Integer.toString(info.getId()));
		dailyRule.setMdfyId(Integer.toString(info.getId()));

		logger.debug(dailyRule.toString());
		DailyRule rule = ruleService.insertDailyRule(dailyRule);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}

	@RequestMapping(value = "/yearlyRuleListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String yearlyRuleListAjax(Model model,SearchParam searchParam) {

		//logger.debug(searchParam.toString());
		List<YearlyRule> list = ruleService.getYearlyRuleList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/yearlyRuleDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String yearlyRuleDetailAjax(Model model,SearchParam searchParam) {

		//logger.debug(searchParam.toString());
		YearlyRule rule = ruleService.getyearlyRuleDetail(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}


	@RequestMapping(value = "/yearlyRuleEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String yearlyRuleEditAjax(Model model,YearlyRule yearlyRule,HttpServletRequest request, HttpServletResponse response) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		yearlyRule.setCrtdId(Integer.toString(info.getId()));
		yearlyRule.setMdfyId(Integer.toString(info.getId()));

		//logger.debug(yearlyRule.toString());
		YearlyRule rule = ruleService.updateYearlyRule(yearlyRule);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}

	/** ??????(regulation)??????
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/regulation")
	public String regulation(Model model, HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";


		//List<User> userList = userService.getSearchUserList();
		//model.addAttribute("userList", userList);
		return "management/regulation";
	}

	@RequestMapping(value = "/fileUpload", method = RequestMethod.POST,  produces="text/plain;charset=UTF-8")
	public @ResponseBody String fileUpload(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IllegalStateException, IOException {
		SessionInfo info =SessionUtil.getSessionInfo(request);

		Regulation regulation = new Regulation();
		regulation.setCrtdId(Integer.toString(info.getId()));
		Regulation rg = null;

		MultipartHttpServletRequest multipartRequest =  (MultipartHttpServletRequest)request;  //???????????? ?????????
		MultipartFile file = multipartRequest.getFile("file");
		String orgFileName = new String(file.getOriginalFilename().getBytes("8859_1"),"utf-8");
		orgFileName = orgFileName.replaceAll("\\p{Space}", "_");

//		logger.debug("orgFileName:"+orgFileName);
//		logger.debug("size:"+file.getSize());
//		logger.debug("type:"+file.getContentType());

		String sysFileName=null;
		String path=null;
		  // ?????????
		if(file !=null){
			sysFileName = DateTimeUtil.getSystemFileName(orgFileName) ;
			path=baseDir+sysFileName;

			regulation.setOrgName(orgFileName);
			regulation.setSysName(sysFileName);
			regulation.setPath(path);
			regulation.setSize(file.getSize());
			regulation.setType(file.getContentType());


//			logger.debug("... StoredFilseName=>"+sysFileName);
//			logger.debug("... path=>"+path);
			File f=new File(path);
			file.transferTo(f);

			rg = regulationService.insertFile(regulation);
		}
//		logger.debug(regulation.toString());
//		logger.debug(file.toString());
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rg);

	}


	/** ????????????
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/regulationListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String regulationListAjax(Model model,SearchParam searchParam) {

		List<Regulation> list = regulationService.getRegulationList(searchParam);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}

	@RequestMapping(value = "/fileDeleteAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String fileDeleteAjax(Model model,int fileId) {
		//logger.debug("fileId:{}",fileId);

		int res = regulationService.deleteFile(fileId);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(res);
	}

	@RequestMapping(value = "/fileDownloadAjax",  produces="text/plain;charset=UTF-8")
	public void fileDownloadAjax(HttpServletRequest request, HttpServletResponse response, Model model,int fileId) {
		logger.debug("fileId:{}",fileId);

		Regulation regulation = new Regulation();

		if(fileId <= 0){//????????????????????? ??????????????? ??????
			regulation = regulationService.getLastRegulationDetail();
		}else{//??????????????? ?????? id??????
			regulation = regulationService.getRegulationDetail(fileId);
		}

		if(regulation != null){

			String regulationFile = baseDir+regulation.getSysName();
			String orgName = regulation.getOrgName();
			String result = null;
			try {
				result = DownloadUtil.download(request, response, new File(regulationFile),orgName);

			} catch (ServletException e) {
				logger.debug(result);
				e.printStackTrace();
			} catch ( IOException e){
				logger.debug(result);
				e.printStackTrace();
			}
		}
	}

	/** ??????????????????
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sendMail")
	public String sendMail(Model model, HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		return "management/sendMail";
	}


	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getMailSendTypeAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getMailSendTypeAjax(Model model,HttpServletRequest request) {

		String res = emailService.getMailSendType();

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(res);
	}

	/**
	 * @param model
	 * @param sendType
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateMailSendTypeAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String updateMailSendTypeAjax(Model model,String sendType, HttpServletRequest request) {

		emailService.updateMailSendType(sendType);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(sendType);
	}

	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getMailDefaultAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getMailDefaultAjax(Model model,HttpServletRequest request) {

		String res = emailService.getMailDefault();

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(res);
	}



	/**
	 * @param model
	 * @param sendType
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateMailDefaultAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String updateMailDefaultAjax(Model model,String lateDefaultAddr, HttpServletRequest request) {

		emailService.updateMailDefault(lateDefaultAddr);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(lateDefaultAddr);
	}



	/** ?????? - ?????? ??????
	 * @param model
	 * @param searchParam
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deptDepts")
	public String deptDepts(Model model, SearchParam searchParam,HttpServletRequest request) {
		SessionInfo info =SessionUtil.getSessionInfo(request);
		if(!"003".equals(info.getAuthCd())) return "redirect:/";

		List<CodeData> deptList = codeService.getCommonCodeList("DEPT");
		model.addAttribute("deptList", deptList);
		return "management/deptDepts";
	}

	/** ?????? ??????
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/deptDeptsListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String deptDeptsListAjax(Model model,SearchParam searchParam) {
		//logger.debug(searchParam.toString());
		searchParam.setSearchAuth("002");//????????? ??????
		List<Dept> list = managementService.getDeptDeptsList(searchParam);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}


	@RequestMapping(value = "/deptDeptsDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String deptDeptsDetailAjax(Model model,String searchCode ) {
		Dept dept = managementService.getDeptDeptsDetailByCode(searchCode);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
		return gson.toJson(dept);
	}

	@RequestMapping(value = "/deptDeptsEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String deptDeptsEditAjax(Model model,Dept dept,HttpServletRequest request, HttpServletResponse response ) {
		//session ?????? ??????
		SessionInfo info =SessionUtil.getSessionInfo(request);
		dept.setCrtdId(Integer.toString(info.getId()));
		dept.setMdfyId(Integer.toString(info.getId()));

		//logger.debug(dept.toString());

		dept = managementService.updateDeptDepts(dept);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(dept);
	}

	//?????? ???????????? ???????????? ??????
	@RequestMapping(value = "/getAnnualRuleAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getAnnualRuleAjax(Model model,HttpServletRequest request) {

		String res = ruleService.getAnnualRule();//?????? ????????????????????????

		String applyDt = ruleService.getAnnualApplyDt();

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Map map = new HashMap();
		map.put("rule", res);
		map.put("applyDt", applyDt);



		return gson.toJson(map);
	}

	//?????? ???????????? ???????????? ????????????
	/**
	 * @param model
	 * @param sendType
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateAnnualRuleAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String updateAnnualRuleAjax(Model model,String rule, HttpServletRequest request) {

		ruleService.updateAnnualRule(rule);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(rule);
	}

	/** ?????? ???????????? ?????? ????????? update
	 * @param model
	 * @param rule
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateAnnualApplyDtAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String updateAnnualApplyDtAjax(Model model,String applyDt, HttpServletRequest request) {

		ruleService.updateAnnualApplyDt(applyDt);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(applyDt);
	}


	/** ???????????? ??????
	 * @param model
	 * @param searchParam
	 * @return
	 */
	@RequestMapping(value = "/getMailContentListAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getMailContentListAjax(Model model,SearchParam searchParam) {
		//logger.debug(searchParam.toString());
		List<MailContent> list = emailService.getMailContentList(searchParam);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(list);
	}

	/** ??????????????????
	 * @param model
	 * @param content
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/mailContentInsertAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String mailContentInsertAjax(Model model, MailContent content,HttpServletRequest request) {
		//logger.debug(codeGroup.toString());
		SessionInfo info =SessionUtil.getSessionInfo(request);

		content = emailService.insertMailContent(content);
		logger.debug("return-->"+content.toString());

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(content);
	}

	/** ??????????????????
	 * @param model
	 * @param searchId
	 * @return
	 */
	@RequestMapping(value = "/getMailContentDetailAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String getMailContentDetailAjax(Model model,int searchId ) {
		MailContent mailContent = new MailContent(searchId);

		mailContent = emailService.getMailContentDetail(mailContent);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
		return gson.toJson(mailContent);
	}


	@RequestMapping(value = "/mailContentEditAjax",  produces="text/plain;charset=UTF-8")
	public @ResponseBody String mailContentEditAjax(Model model,MailContent mailContent,HttpServletRequest request, HttpServletResponse response ) {
		//session ?????? ??????
		SessionInfo info =SessionUtil.getSessionInfo(request);
		mailContent = emailService.updateMailContent(mailContent);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(mailContent);
	}



}

