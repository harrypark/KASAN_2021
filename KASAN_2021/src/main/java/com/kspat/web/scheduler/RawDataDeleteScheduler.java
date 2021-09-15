package com.kspat.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kspat.web.service.StatService;
/**
 * 202106 수정요청사항
 * RawData 14개월후 자동삭제 : 새벽4시 자동실행
 * @author parkh
 *
 */


@Component
public class RawDataDeleteScheduler {

	/** The logger.<br/> 로거. */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StatService statService;

	@Scheduled(cron="0 0 04 * * ?") //새벽4시 실행
	public void scheduleRawDataAutoDeleteTask() {

		logger.debug("row data 삭제 작업 시작");

		statService.deleteRawData();

		logger.debug("row data 삭제 작업 종료");
	}
}
