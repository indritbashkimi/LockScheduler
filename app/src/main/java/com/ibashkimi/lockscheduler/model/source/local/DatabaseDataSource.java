package com.ibashkimi.lockscheduler.model.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ibashkimi.lockscheduler.App;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.condition.Condition;
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource;
import com.ibashkimi.lockscheduler.model.source.serializer.ProfileSerializer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseDataSource implements ProfilesDataSource {

    private static DatabaseDataSource INSTANCE;

    private SQLiteDatabase mDb;

    private DatabaseDataSource(@NonNull Context context) {
        DatabaseHelper mDbHelper = new DatabaseHelper(context);
        mDb = mDbHelper.getWritableDatabase();
    }

    public static synchronized DatabaseDataSource getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DatabaseDataSource(App.getInstance());
        return INSTANCE;
    }


    @Override
    public void beginTransaction() {
        mDb.beginTransaction();
    }

    @Override
    public void endTransaction() {
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    @Override
    public List<Profile> getProfiles() {
        List<Profile> profiles = new ArrayList<>();
        try {
            String[] projection = {
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID,
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP
            };

            Cursor c = mDb.query(
                    PersistenceContract.TABLE_PROFILE, projection, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    //String id = c.getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID));
                    String rep = c
                            .getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP));

                    Profile profile = ProfileSerializer.parseJson(rep);
                    profiles.add(profile);
                }
            }
            if (c != null) {
                c.close();
            }
        } catch (IllegalStateException | JSONException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    @Nullable
    @Override
    public Profile getProfile(@NonNull String profileId) {
        try {
            String[] projection = {
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID,
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP
            };

            String selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?";
            String[] selectionArgs = {profileId};

            Cursor c = mDb.query(
                    PersistenceContract.TABLE_PROFILE, projection, selection, selectionArgs, null, null, null);

            Profile profile = null;

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                //String id = c.getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID));
                String rep = c
                        .getString(c.getColumnIndexOrThrow(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP));

                profile = ProfileSerializer.parseJson(rep);
            }
            if (c != null) {
                c.close();
            }

            return profile;
        } catch (IllegalStateException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Profile> getConditionProfiles(@Condition.Type int conditionType) {
        List<String> profileIds = new ArrayList<>();
        try {
            String[] projection = {
                    PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID,
                    PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE
            };

            String selection = PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE + " LIKE ?";
            String[] selectionArgs = {conditionToString(conditionType)};

            Cursor c = mDb.query(
                    PersistenceContract.TABLE_CONDITION_HANDLER, projection, selection, selectionArgs, null, null, null);

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    String id = c
                            .getString(c.getColumnIndexOrThrow(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID));
                    profileIds.add(id);
                }
            }
            if (c != null) {
                c.close();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        List<Profile> profiles = new ArrayList<>(profileIds.size());
        for (String profileId : profileIds) {
            Profile profile = getProfile(profileId);
            profiles.add(profile);
        }
        return profiles;
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        try {
            ContentValues values = new ContentValues();
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, profile.getId());
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP, ProfileSerializer.toJson(profile));

            mDb.insert(PersistenceContract.TABLE_PROFILE, null, values);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveCondition(@NonNull String profileId, @Condition.Type int conditionType) {
        try {
            ContentValues values = new ContentValues();
            values.put(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID, profileId);
            values.put(PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE, conditionToString(conditionType));

            mDb.insert(PersistenceContract.TABLE_CONDITION_HANDLER, null, values);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private String conditionToString(@Condition.Type int conditionType) {
        switch (conditionType) {
            case Condition.Type.PLACE:
                return "place";
            case Condition.Type.TIME:
                return "time";
            case Condition.Type.WIFI:
                return "wifi";
            case Condition.Type.POWER:
                return "power";
        }
        throw new IllegalArgumentException("Unhandled condition: " + conditionType);
    }

    @Override
    public void deleteProfiles() {
        try {
            mDb.delete(PersistenceContract.TABLE_PROFILE, null, null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteConditions() {
        try {
            mDb.delete(PersistenceContract.TABLE_CONDITION_HANDLER, null, null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteProfile(@NonNull String profileId) {
        try {
            String selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?";
            String[] selectionArgs = {profileId};
            mDb.delete(PersistenceContract.TABLE_PROFILE, selection, selectionArgs);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCondition(@NonNull String profileId, @Condition.Type int conditionType) {
        try {
            String selection = PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID + "=? and " + PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE + "=? ";
            String[] selectionArgs = {profileId, conditionToString(conditionType)};
            mDb.delete(PersistenceContract.TABLE_CONDITION_HANDLER, selection, selectionArgs);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProfile(@NonNull Profile profile) {
        try {
            ContentValues values = new ContentValues();
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID, profile.getId());
            values.put(PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP, ProfileSerializer.toJson(profile));

            String selection = PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " LIKE ?";
            String[] selectionArgs = {profile.getId()};

            mDb.update(PersistenceContract.TABLE_PROFILE, values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void swapProfiles(@NonNull String id1, @NonNull String id2) {
        Profile p1 = getProfile(id1);
        Profile p2 = getProfile(id2);
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Invalid profile ids.");
        }

        Profile profile1 = new Profile(id2, p1.getName(), p1.getConditions(), p1.getEnterActions(), p1.getExitActions());
        Profile profile2 = new Profile(id1, p2.getName(), p2.getConditions(), p2.getEnterActions(), p2.getExitActions());

        updateProfile(profile1);
        updateProfile(profile2);
    }
}
