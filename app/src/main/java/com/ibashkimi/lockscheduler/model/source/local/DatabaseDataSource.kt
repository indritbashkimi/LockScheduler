package com.ibashkimi.lockscheduler.model.source.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource
import com.ibashkimi.lockscheduler.model.source.toJson
import com.ibashkimi.lockscheduler.model.source.toProfile
import org.json.JSONException
import java.util.*

object DatabaseDataSource : ProfilesDataSource {

    private val mDb: SQLiteDatabase

    init {
        val mDbHelper = DatabaseHelper(App.getInstance())
        mDb = mDbHelper.writableDatabase
    }


    override fun beginTransaction() {
        mDb.beginTransaction()
    }

    override fun endTransaction() {
        mDb.setTransactionSuccessful()
        mDb.endTransaction()
    }

    override fun getProfiles(): List<Profile>? {
        val profiles = ArrayList<Profile>()
        try {
            val projection = arrayOf(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP)

            val c = mDb.query(
                    PersistenceContract.TABLE_PROFILE, projection, null, null, null, null, null)

            if (c != null && c.count > 0) {
                while (c.moveToNext()) {
                    //String id = c.getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID));
                    val rep = c
                            .getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP))

                    val profile = rep.toProfile()
                    profiles.add(profile)
                }
            }
            c?.close()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return null
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }

        return profiles
    }

    override fun getProfile(profileId: String): Profile? {
        try {
            val projection = arrayOf(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP)

            val selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?"
            val selectionArgs = arrayOf(profileId)

            val c = mDb.query(
                    PersistenceContract.TABLE_PROFILE, projection, selection, selectionArgs, null, null, null)

            var profile: Profile? = null

            if (c != null && c.count > 0) {
                c.moveToFirst()
                //String id = c.getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID));
                val rep = c
                        .getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP))

                profile = rep.toProfile()
            }
            c?.close()

            return profile
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    override fun getConditionProfiles(conditionType: Condition.Type): List<Profile> {
        val profileIds = ArrayList<String>()
        try {
            val projection = arrayOf(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID, PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE)

            val selection = PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE + " LIKE ?"
            val selectionArgs = arrayOf(conditionToString(conditionType))

            val c = mDb.query(
                    PersistenceContract.TABLE_CONDITION_HANDLER, projection, selection, selectionArgs, null, null, null)

            if (c != null && c.count > 0) {
                while (c.moveToNext()) {
                    val id = c
                            .getString(c.getColumnIndexOrThrow(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID))
                    profileIds.add(id)
                }
            }
            c?.close()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        val profiles = ArrayList<Profile>(profileIds.size)
        for (profileId in profileIds) {
            getProfile(profileId)?.let { profiles.add(it) }
        }
        return profiles
    }

    override fun saveProfile(profile: Profile) {
        try {
            val values = ContentValues()
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, profile.id)
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP, profile.toJson().toString())

            mDb.insert(PersistenceContract.TABLE_PROFILE, null, values)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun saveCondition(profileId: String, conditionType: Condition.Type) {
        try {
            val values = ContentValues()
            values.put(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID, profileId)
            values.put(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE, conditionToString(conditionType))

            mDb.insert(PersistenceContract.TABLE_CONDITION_HANDLER, null, values)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun conditionToString(conditionType: Condition.Type): String {
        if (conditionType === Condition.Type.PLACE) {
            return "place"
        } else if (conditionType === Condition.Type.TIME) {
            return "time"
        } else if (conditionType === Condition.Type.WIFI) {
            return "wifi"
        } else if (conditionType === Condition.Type.POWER) {
            return "power"
        }
        throw IllegalArgumentException("Unhandled condition: $conditionType")
    }

    override fun deleteProfiles() {
        try {
            mDb.delete(PersistenceContract.TABLE_PROFILE, null, null)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun deleteConditions() {
        try {
            mDb.delete(PersistenceContract.TABLE_CONDITION_HANDLER, null, null)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun deleteProfile(profileId: String) {
        try {
            val selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?"
            val selectionArgs = arrayOf(profileId)
            mDb.delete(PersistenceContract.TABLE_PROFILE, selection, selectionArgs)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun deleteCondition(profileId: String, conditionType: Condition.Type) {
        try {
            val selection = PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID + "=? and " + PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE + "=? "
            val selectionArgs = arrayOf(profileId, conditionToString(conditionType))
            mDb.delete(PersistenceContract.TABLE_CONDITION_HANDLER, selection, selectionArgs)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun updateProfile(profile: Profile) {
        try {
            val values = ContentValues()
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, profile.id)
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP, profile.toJson().toString())

            val selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?"
            val selectionArgs = arrayOf(profile.id)

            mDb.update(PersistenceContract.TABLE_PROFILE, values, selection, selectionArgs)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun swapProfiles(id1: String, id2: String) {
        val p1 = getProfile(id1)
        val p2 = getProfile(id2)
        if (p1 == null || p2 == null) {
            throw IllegalArgumentException("Invalid profile ids.")
        }

        val profile1 = Profile(id2, p1.name, p1.conditions, p1.enterExitActions)
        val profile2 = Profile(id1, p2.name, p2.conditions, p2.enterExitActions)

        updateProfile(profile1)
        updateProfile(profile2)
    }
}
