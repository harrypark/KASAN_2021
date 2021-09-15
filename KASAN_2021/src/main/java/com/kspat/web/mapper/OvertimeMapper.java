package com.kspat.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kspat.web.domain.Overtime;
import com.kspat.web.domain.SearchParam;

public interface OvertimeMapper {

	Overtime getReqDateOvertimeInfo(SearchParam searchParam);

	void insertOvertime(Overtime overtime);

	Overtime getOvertimeDetail(int id);

	List<Overtime> getOvertimeListById(SearchParam searchParam);

	List<Overtime> getOvertimeAdminList(SearchParam searchParam);

	List<Overtime> getRequestOvertimeList(String calDt);

	void updateReqUserOvertime(Overtime reqDateOvertimeInfo);

	int checkDuplicateReq(SearchParam searchParam);

	int checkOvertiomData(@Param("calDt") String calDt, @Param("crtdId") String crtdId);

	void updateOvertimeDailyStat(Overtime overtime);

	void insertOvertimeDailyStat(Overtime ot);


}
