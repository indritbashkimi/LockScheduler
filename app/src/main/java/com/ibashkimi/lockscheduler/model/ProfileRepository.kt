package com.ibashkimi.lockscheduler.model

interface ProfileRepository {
    fun add(profile: Profile)

    fun get(id: String): Profile?

    fun getAll(): List<Profile>

    fun remove(id: String)

    fun removeAll()

    fun swap(profile1: Profile, profile2: Profile)

    fun update(profile: Profile)
}