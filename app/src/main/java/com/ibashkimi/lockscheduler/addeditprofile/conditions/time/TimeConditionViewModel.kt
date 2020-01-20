package com.ibashkimi.lockscheduler.addeditprofile.conditions.time

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ibashkimi.lockscheduler.model.condition.DaysOfWeek
import com.ibashkimi.lockscheduler.model.condition.Time

class TimeConditionViewModel : ViewModel() {

    val daysLiveData = MutableLiveData<DaysOfWeek>()

    val startTimeLiveData = MutableLiveData<Time>()

    val endTimeLiveData = MutableLiveData<Time>()
}