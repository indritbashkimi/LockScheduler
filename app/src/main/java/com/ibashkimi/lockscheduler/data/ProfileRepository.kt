package com.ibashkimi.lockscheduler.data

import com.ibashkimi.lockscheduler.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun add(profile: Profile)

    suspend fun get(id: String): Profile?

    suspend fun getAll(): List<Profile>

    suspend fun remove(id: String)

    suspend fun removeAll()

    suspend fun swap(profile1: Profile, profile2: Profile)

    suspend fun update(profile: Profile)
}