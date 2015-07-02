package com.cheatdatabase.helpers;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface SystemAndGameCountPrefs {

    // https://github.com/excilys/androidannotations/wiki/SharedPreferencesHelpers

    // NOT USED (30.06.2015) - CAN BE DELETED OR EDITED
    String systemsAndGameCount();
    long systemsAndGameCountLastUpdated();


}