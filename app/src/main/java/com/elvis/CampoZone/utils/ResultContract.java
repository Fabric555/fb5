package com.elvis.CampoZone.utils;

import android.provider.BaseColumns;

public final class ResultContract {

    private ResultContract(){}

    public static final class ResultEntry implements BaseColumns {

        public final static String _ID = BaseColumns._ID;
        public final static String SUGGEST_COLUMN_TEXT_1 = "name";
        public final static String SUGGEST_COLUMN_TEXT_2 = "location";
        public final static String SUGGEST_COLUMN_ICON_1 = "img";
        public final static String SUGGEST_COLUMN_INTENT_EXTRA_DATA = "title";

    }
}