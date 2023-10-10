package com.ctlev.app.ui.puninout.placeholder;

import com.ctlev.app.ui.puninout.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    public  final List<PlaceholderItem> ITEMS = new ArrayList<PlaceholderItem>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
//    public static final Map<String, PlaceholderItem> ITEM_MAP = new HashMap<String, PlaceholderItem>();

    private static final int COUNT = 25;

//    static {
        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createPlaceholderItem(i));
//        }
//    }

//    private static void addItem(PlaceholderItem item) {
//        ITEMS.add(item);
//        ITEM_MAP.put(item.startTime, item);
//    }



    public  List<PlaceholderItem> parseJsonData(String empData)
    {

        try {
            if(empData==null||empData.isEmpty() || !empData.startsWith("{")||!empData.endsWith("}")){
                return ITEMS;
            }

            JSONObject EmpTimeRecord = new JSONObject(empData);
            ArrayList<Date> dates = getDateBeginWeek();
            for (Date date : dates) {
                String keyTodayDate = Constants.getDateAsString(date, null);
                JSONObject todayData= new JSONObject(EmpTimeRecord.optString(keyTodayDate,"{}"));
                if(todayData==null||todayData.length()==0) continue;
                long startTime=todayData.optLong(Constants.StartTime, System.currentTimeMillis());
                long endTime=todayData.optLong(Constants.EndTime, System.currentTimeMillis());
                long diffTimeExtra=todayData.optLong(Constants.DiffOfDay, 0l);
                long  diffTime=endTime-startTime;//check if ninehr30min needed
               /* long hr= (diffTime/Constants.oneHR);
                long min= (diffTime/Constants.oneMin)-hr*60;
                long sec= ((diffTime-hr*Constants.oneHR -min*Constants.oneMin)/Constants.oneSec);
                long ms= (diffTime-hr*Constants.oneHR -min*Constants.oneMin-sec*Constants.oneSec)/Constants.oneMiliSec;
                String diffTimes=hr+" hr, "+min+" min, "+sec+" sec, "+ms+" ms \n Extra:"+(String.format("%.2f",(diffTime-Constants.nineHrthirtyMin)*1.0f/Constants.oneHR))+" hr";
                */
                String diffTimes = Constants.convertTimeDifferenceToString(diffTime);
                String dateFormat="dd-MM-yyyy HH:mm:ss (E)";
                try {
                    ITEMS.add(new PlaceholderItem(Constants.getDateAsString(new Date(startTime),dateFormat),Constants.getDateAsString(new Date(endTime),dateFormat),diffTimes));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ITEMS;
    }

    public  List<PlaceholderItem> parseJsonMonthlyData(String empData,int which)
    {

        try {
            if(empData==null||empData.isEmpty() || !empData.startsWith("{")||!empData.endsWith("}")){
                return ITEMS;
            }

            JSONObject EmpTimeRecord = new JSONObject(empData);
            Calendar c1 = Calendar.getInstance();
            if(which!=0)
                c1.add(Calendar.MONTH, which);//
            int dayMax=c1.getActualMaximum(Calendar.DAY_OF_MONTH); //last day of month
            int dayMin=c1.getActualMinimum(Calendar.DAY_OF_MONTH); //first day of month

            for (int i = dayMin; i <= dayMax; i++)  {
                c1.set(Calendar.DAY_OF_MONTH,i);
                Date date =c1.getTime();
                String keyTodayDate = Constants.getDateAsString(date, null);
                JSONObject todayData= new JSONObject(EmpTimeRecord.optString(keyTodayDate,"{}"));
                if(todayData==null||todayData.length()==0) continue;
                long startTime=todayData.optLong(Constants.StartTime, System.currentTimeMillis());
                long endTime=todayData.optLong(Constants.EndTime, System.currentTimeMillis());
                long diffTimeExtra=todayData.optLong(Constants.DiffOfDay, 0l);
                long  diffTime=endTime-startTime;
               /* long hr= (diffTime/Constants.oneHR);
                long min= (diffTime/Constants.oneMin)-hr*60;
                long sec= ((diffTime-hr*Constants.oneHR -min*Constants.oneMin)/Constants.oneSec);
                long ms= (diffTime-hr*Constants.oneHR -min*Constants.oneMin-sec*Constants.oneSec)/Constants.oneMiliSec;
                String diffTimes=hr+" hr, "+min+" min, "+sec+" sec, "+ms+" ms \n Extra:"+(String.format("%.2f",(diffTime-Constants.nineHrthirtyMin)*1.0f/Constants.oneHR))+" hr";
                */
                String diffTimes = Constants.convertTimeDifferenceToString(diffTime);
                String dateFormat="dd-MM-yyyy HH:mm:ss (E)";
                try {
                    ITEMS.add(new PlaceholderItem(Constants.getDateAsString(new Date(startTime),dateFormat),Constants.getDateAsString(new Date(endTime),dateFormat),diffTimes));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ITEMS;
    }

    public ArrayList<Date> getDateBeginWeek(){
        Calendar c1 = Calendar.getInstance();
        int year = c1.get(Calendar.YEAR);
        int month = c1.get(Calendar.MONTH)+1;
        int day = c1.get(Calendar.DAY_OF_MONTH);
        int dayMax=c1.getActualMaximum(Calendar.DAY_OF_MONTH); //last day of month
        int dayMin=c1.getActualMinimum(Calendar.DAY_OF_MONTH); //first day of month
        System.out.println("date begin of week = " +day+"-"+month+"-"+year);
        //first day of week is sunday that is 1 but i want as Monday so 2
        c1.set(Calendar.DAY_OF_WEEK, 2); //monday

        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH)+1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date begin of week = " +day1+"-"+month1+"-"+year1);

       Date date1= c1.getTime();
        //last day of week is saturday that is 7 but i want as Friday so 6
        c1.set(Calendar.DAY_OF_WEEK, 6); //friday

        int year7 = c1.get(Calendar.YEAR);
        int month7 = c1.get(Calendar.MONTH)+1;
        int day7 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date end of week = " +day7+"-"+month7+"-"+year7);
        Date date2=c1.getTime();

        c1.set(Calendar.DAY_OF_WEEK, 7); //saturday
        int day6 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date end of week = " +day6+"-"+month7+"-"+year7);
        Date date3=c1.getTime();
        //-------
        boolean isLastDayOfMonth=day==dayMax;

        boolean isLastDayOfWeek=day==day7|| day==day6;


        ArrayList<Date> dates=new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while(!cal1.after(cal2))
        {
            day = cal1.get(Calendar.DAY_OF_MONTH);
            isLastDayOfMonth=day==dayMax;
            isLastDayOfWeek=/*day==day7||*/ day==day6;
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
            if(isLastDayOfMonth || isLastDayOfWeek) break;

        }
        return dates;
    }

    public ArrayList<Date> getDateBeginMonth(int which){
        Calendar c1 = Calendar.getInstance();
        if(which!=0)
            c1.add(Calendar.MONTH, which);//

        int year = c1.get(Calendar.YEAR);
        int month = c1.get(Calendar.MONTH)+1;
        int day = c1.get(Calendar.DAY_OF_MONTH);

        int dayMax=c1.getActualMaximum(Calendar.DAY_OF_MONTH); //last day of month
        int dayMin=c1.getActualMinimum(Calendar.DAY_OF_MONTH); //first day of month
        System.out.println("date begin of week = " +day+"-"+month+"-"+year);
        //first day of week is sunday that is 1 but i want as Monday so 2
        for (int i = dayMin; i < dayMax; i+=7) {

        }
        c1.set(Calendar.DAY_OF_WEEK, 2); //monday

        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH)+1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date begin of week = " +day1+"-"+month1+"-"+year1);

        Date date1= c1.getTime();
        //last day of week is saturday that is 7 but i want as Friday so 6
        c1.set(Calendar.DAY_OF_WEEK, 6); //friday

        int year7 = c1.get(Calendar.YEAR);
        int month7 = c1.get(Calendar.MONTH)+1;
        int day7 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date end of week = " +day7+"-"+month7+"-"+year7);
        Date date2=c1.getTime();
        //-------
        boolean isLastDayOfMonth=day==dayMax;

        boolean isLastDayOfWeek=day==day7;


        ArrayList<Date> dates=new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while(!cal1.after(cal2))
        {
            day = cal1.get(Calendar.DAY_OF_MONTH);
            isLastDayOfMonth=day==dayMax;
            isLastDayOfWeek=day==day7;
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
            if(isLastDayOfMonth || isLastDayOfWeek) break;

        }
        return dates;
    }

    private static PlaceholderItem createPlaceholderItem(int position) {
        return new PlaceholderItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A placeholder item representing a piece of content.
     */
    public static class PlaceholderItem {
        public final String startTime;
        public final String endTime;
        public final String diffTime;

        public PlaceholderItem(String startTime, String endTime, String diffTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.diffTime = diffTime;
        }

        @Override
        public String toString() {
            return endTime+"-"+startTime+"="+diffTime;
        }
    }
}