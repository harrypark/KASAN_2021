package com.kspat.web.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Workhome extends BaseDomain{
	/**
	 *
	 */
	private static final long serialVersionUID = -3169772661684208865L;
	private int id;
	private String homeDt;
	private String weekName;
	private String startTm;
	private String endTm;
	private int diffm; //재택소요분
	private String hereGoYn;//현지출근 Y,N
	private String hereOutYn;//현지퇴근 Y,N
	private String memo;

	private String deptCd;
}
