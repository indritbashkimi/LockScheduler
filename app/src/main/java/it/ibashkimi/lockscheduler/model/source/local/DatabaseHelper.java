package it.ibashkimi.lockscheduler.model.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Profile.db";

    private static final String CREATE_TABLE_PROFILE =
            "CREATE TABLE " + PersistenceContract.TABLE_PROFILE + " (" +
                    PersistenceContract.ProfileEntry._ID + " TEXT PRIMARY KEY," +
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_ID + " TEXT," +
                    PersistenceContract.ProfileEntry.COLUMN_NAME_PROFILE_REP + " TEXT" +
                    " )";

    private static final String CREATE_TABLE_CONDITION_HANDLER =
            "CREATE TABLE " + PersistenceContract.TABLE_CONDITION_HANDLER + " (" +
                    PersistenceContract.ConditionHandlerEntry._ID + " TEXT PRIMARY KEY," +
                    PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_PROFILE_ID + " TEXT," +
                    PersistenceContract.ConditionHandlerEntry.COLUMN_NAME_CONDITION_TYPE + " TEXT" +
                    " )";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_CONDITION_HANDLER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}
