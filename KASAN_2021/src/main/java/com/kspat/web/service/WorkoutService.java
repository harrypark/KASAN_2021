package com.kspat.web.service;

import java.util.List;

import com.kspat.web.domain.SearchParam;
import com.kspat.web.domain.Workout;

public interface WorkoutService {

	Workout insertWorkout(Workout workout);

	List<Workout> getUserWorkoutList(SearchParam searchParam);

	Workout getWorkoutDetailById(SearchParam searchParam);

	Workout updateWorkout(Workout workout);

	int deleteWorkout(Workout workout);

	Workout getWorkoutAvailableTime();


}
