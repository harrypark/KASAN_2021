package com.kspat.web.mapper;

import java.util.List;

import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.Workhome;

public interface WorkhomeMapper {

	List<Workhome> getUserWorkhomeList(SearchParam searchParam);

	void insertWorkhome(Workhome workhome);

	Workhome getWorkhomeDetailById(SearchParam searchParam);

	void updateWorkhome(Workhome workhome);

	int deleteWorkhome(Workhome workhome);

}
