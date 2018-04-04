package com.example.Crofun.Hmilab;

/**
 * Created by Angel on 2017/4/7.
 */


import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class ActivityCollecter {

    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishall() {
        Log.d("ActivityCollecter", "finishall() is used.");
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
                Log.d("ActivityCollecter", "" + activity + " is finished.");
            }
        }
    }

}
