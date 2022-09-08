/*
 * Copyright (c) 2022, CTL and/or its affiliates. All rights reserved.
 * Created by nkjha on 06-09-2022.
 */
package com.ctlev.app.ui.puninout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {
    public static final long oneMiliSec= 1;
    public static final long oneSec=  1000*oneMiliSec;
    public static final long oneMin=  60*oneSec;
    public static final long oneHR=  60*oneMin;
    public static final long nineHrthirtyMin= ((9*oneHR)+ (oneHR/2)); //34200000
    public static final long twoHrthirtyMin= ((2*oneHR)+ (oneHR/2)); //  9000000
    //public static final long divideBy=getTimeDivider(showTimeUnit);
    public static final String StartTime="StartTime";
    public static final String EndTime="EndTime";
    public static final String ExtraTillNow="extraTillnow";
    public static final String DiffOfDay="diffOfDay";
    public static final  String EName="ename";
    public static final String LastEId="lasteid";
    public static String getDateAsString(Date date, String format){
        if(format==null || format.isEmpty()) format="dd-MM-yyyy";
        // Parsing the date and time
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
        String mStartTime = mSimpleDateFormat.format(date);
        return mStartTime;
        //  Date mEndTime = mSimpleDateFormat.parse(endTime);
    }
    public static String convertTimeDifferenceToString(long diffTime){
        long hr= (diffTime/Constants.oneHR);
        long min= (diffTime/Constants.oneMin)-hr*60;
        long sec= ((diffTime-hr*Constants.oneHR -min*Constants.oneMin)/Constants.oneSec);
        long ms= (diffTime-hr*Constants.oneHR -min*Constants.oneMin-sec*Constants.oneSec)/Constants.oneMiliSec;
        String diffTimes=hr+" hr, "+min+" min, "+sec+" sec, "+ms+" ms \nExtra:"+(String.format("%.2f",(diffTime-Constants.nineHrthirtyMin)*1.0f/Constants.oneHR))+" hr";
        return diffTimes;
    }
}
