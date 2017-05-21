package it.ibashkimi.lockscheduler.model.source.local;

import android.provider.BaseColumns;

class PersistenceContract {

    // Table Names
    static final String TABLE_PROFILE = "profiles";
    static final String TABLE_CONDITION_HANDLER = "handlers";

    private PersistenceContract() {
    }

    static abstract class ProfileEntry implements BaseColumns {
        static final String COLUMN_NAME_PROFILE_ID = "profile_id";
        static final String COLUMN_NAME_PROFILE_REP = "profile_rep";
    }

    static abstract class ConditionHandlerEntry implements BaseColumns {
        static final String COLUMN_NAME_PROFILE_ID = "profile_id";
        static final String COLUMN_NAME_CONDITION_TYPE = "condition_type";
    }
}
