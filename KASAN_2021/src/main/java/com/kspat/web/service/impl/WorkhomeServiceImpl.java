package com.kspat.web.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.Workhome;
import com.kspat.web.domain.Workout;
import com.kspat.web.mapper.WorkhomeMapper;
import com.kspat.web.service.WorkhomeService;
@Service
public class WorkhomeServiceImpl implements WorkhomeService{

	@Autowired
	private WorkhomeMapper workhomeMapper;

	@Override
	public List<Workhome> getUserWorkhomeList(SearchParam searchParam) {
		return workhomeMapper.getUserWorkhomeList(searchParam);
	}

	@Override
	public Workhome insertWorkhome(Workhome workhome) {
		workhomeMapper.insertWorkhome(workhome);

		SearchParam searchParam = new SearchParam(workhome.getId());
		Workhome wh = workhomeMapper.getWorkhomeDetailById(searchParam);

		return wh;
	}

	@Override
	public Workhome getWorkhomeDetailById(SearchParam searchParam) {
		return workhomeMapper.getWorkhomeDetailById(searchParam);
	}

	@Override
	public Workhome updateWorkhome(Workhome workhome) {
		workhomeMapper.updateWorkhome(workhome);

		SearchParam searchParam = new SearchParam(workhome.getId());
		Workhome wh = workhomeMapper.getWorkhomeDetailById(searchParam);
		return wh;
	}

	@Override
	public int deleteWorkhome(Workhome workhome) {
		SearchParam searchParam = new SearchParam(workhome.getId());
		Workhome wh = workhomeMapper.getWorkhomeDetailById(searchParam);
		wh.setCrtdId(workhome.getCrtdId());

		int res = workhomeMapper.deleteWorkhome(workhome);
		//외근t삭제정보를 부서 매니져에게 메일발송
		//emailTempleatService.setWoekoutEmailTempleate(wh, "delete",workhome.getDeptCd());

		return res;
	}

}
