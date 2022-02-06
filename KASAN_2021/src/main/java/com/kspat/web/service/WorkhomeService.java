package com.kspat.web.service;

import java.util.List;

import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.Workhome;

public interface WorkhomeService {

	List<Workhome> getUserWorkhomeList(SearchParam searchParam);

	Workhome insertWorkhome(Workhome workhome);

	Workhome getWorkhomeDetailById(SearchParam searchParam);

	Workhome updateWorkhome(Workhome workhome);

	int deleteWorkhome(Workhome workhome);

}
