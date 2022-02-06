package com.kspat.web.domain;

import lombok.Data;

@Data
public class AvailableReplaceInfo {
	private int currCount;//이번월 사용가능 대체근무수
	private int nextCount;//다음월 사용가능 대체근무수
	private int availCount; //남은 대체근무 일수
	private int todayMin;
	private String availStartTm;
	private String hasHl; //선택된날짜에 반휴 신청이 있는지 체크

}
