package com.ctlev.app.ui.puninout;


import static com.ctlev.app.ui.puninout.PunchInOutFragment.DisplayTimeIn.*;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ctlev.app.R;
import com.ctlev.app.databinding.FragmentPunInOutBinding;
import com.ctlev.app.persistence.SharedPreferenceStorage;
import com.ctlev.app.receiver.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PunchInOutFragment extends Fragment {

    private GeofencingClient geofencingClient;
    PendingIntent geofencePendingIntent;
    ArrayList<Geofence> geofenceList;
    private PunInOutViewModel mViewModel;
    private FragmentPunInOutBinding binding;
    SharedPreferenceStorage pref;
    //Declare timer
    CountDownTimer cTimer = null;
    JSONObject empData;
    DisplayTimeIn showTimeUnit=MIN;
    int clickedId=-1;

    enum DisplayTimeIn{
        HOUR("hr"),MIN("min"),SEC("sec"),MILISEC("ms");

        public String getData() {
            return data;
        }

        String data;
        DisplayTimeIn(String data){
            this.data=data;
        }
    }
    String keyTodayDate = getDateAsString(Calendar.getInstance().getTime(), null);
    boolean isTimmerOn = false;
    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();

    public static PunchInOutFragment newInstance() {
        return new PunchInOutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPunInOutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this.getContext(), GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this.getContext(), 0, intent, PendingIntent.
                FLAG_MUTABLE);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: nineHrthirtyMin(34200000):"+nineHrthirtyMin+",twoHrthirtyMin(9000000):"+twoHrthirtyMin);
        mViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getActivity().getApplication())).get(PunInOutViewModel.class);
        geofencingClient = LocationServices.getGeofencingClient(this.getContext());
        geofenceList=new ArrayList<>();
        //you area of fence
        BAY_AREA_LANDMARKS.put("BajajAutoWorkPlace", new LatLng(18.66083333, 73.78277778)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoM", new LatLng(18.654348, 73.782258)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoHR", new LatLng(18.655720, 73.785952)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoCorpOff", new LatLng(18.655261, 73.782160)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoYamuna", new LatLng(18.66305556, 73.78611111)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoRND", new LatLng(18.660831, 73.7808896)); //change to office
        BAY_AREA_LANDMARKS.put("BajajAutoRNDParking", new LatLng(18.6605845, 73.7813563)); //change to office
        // San Francisco International Airport.
        BAY_AREA_LANDMARKS.put("SFO", new LatLng(37.621313, -122.378955)); //change to office
        // Googleplex.
        BAY_AREA_LANDMARKS.put("GOOGLE", new LatLng(37.422611,-122.0840577));
        populateGeofenceList();
        pref=SharedPreferenceStorage.getSharedPreferences ( getActivity());

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
           // return; //change this flow
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                        Log.i(TAG, "onSuccess: Geofences added");
                    }
                })
                .addOnFailureListener(this.getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                        Log.i(TAG, "onFailure: Failed to add geofences");
                    }
                });
        //to remove the geofencing
//        removeGeofences();

//        long currentTimeMillis=System.currentTimeMillis();
//        long punchInTime=pref.getData(StartTime,currentTimeMillis);
        binding.btnIn.setEnabled(false);
        binding.btnOut.setEnabled(false);
        binding.etId.setEnabled(true);
        binding.etId.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                binding.etId.setEnabled(true);
                binding.etName.setEnabled(true);
                return false;
            }
        });
        binding.circularDeterminativePb.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                String empid=binding.etId.getText().toString();
                if(empid==null || empid.isEmpty()) return;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, WeeklyTrackerFragment.newInstance(empid))
                        .commitNow();
             }
        });


        String empid=pref.getData(LastEId,"");
        String empname=pref.getData(EName,"");
        binding.etName.setText(empname);
        if(!empid.isEmpty()) {
            binding.etId.setText(empid);
            String empDatap=pref.getData(empid,"");
            binding.etId.setEnabled(false);
            checkEmpid(empDatap);
        }
        binding.tvIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedId=v.getId();
                getUserSelectedDateFromCal();
            }
        });
        binding.tvOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedId=v.getId();
                getUserSelectedDateFromCal();
            }
        });


        binding.rgTimeUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbtnTimeUnithr:
                        showTimeUnit=HOUR;
                    break;
                    case R.id.rbtnTimeUnitmin:
                        showTimeUnit=MIN;
                    break;
                    case R.id.rbtnTimeUnitsec:
                        showTimeUnit=SEC;
                    break;
                    case R.id.rbtnTimeUnitms:
                        showTimeUnit=MILISEC;
                    break;
                    default:
                        showTimeUnit=MIN;
                        break;
                }
                divideBy=getTimeDivider(showTimeUnit);
                if(empData!=null){
                    cancelTimer();
                    checkEmpid(empData.toString());
                }
            }
        });
        binding.etId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (binding.etId.getRight() - binding.etId.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here save N fetch json of employee
                        binding.etId.setEnabled(false);
                        binding.etName.setEnabled(false);
                        String id =binding.etId.getText().toString().trim();
                        int zeroIdChk=Integer.parseInt(id);
                        if(id.isEmpty() || id.equals("0") ||zeroIdChk==0||!id.matches("\\d+")) {
                            binding.etName.setError(getString(R.string.txt_error_id));
                            binding.etName.setText(R.string.txt_error_id);
                            binding.etId.setEnabled(true);
                            binding.etName.setEnabled(true);
                            return true;
                        }

                        String isDataExist=pref.getData(id,"");
                        if(isDataExist.isEmpty()) { // no record of empid in preference
                            String name=binding.etName.getText().toString();

                            if(name.isEmpty() ||!name.matches("[a-zA-Z. ]+")) {
                                binding.etName.setHint(R.string.txt_hint_name);
                                binding.etName.setError(getString(R.string.txt_hint_name));
                                binding.tvIn.setError(getString(R.string.txt_error_name));
                                binding.etName.setEnabled(true);
                                return true;
                            }
                            JSONObject root = new JSONObject();
                            try {
                                root.put(EName,getNameRemoveGreet(name));
                                root.put(ExtraTillNow,0l);
                                root.put(keyTodayDate, "{}");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            pref.setData(id,root.toString());
                            binding.tvIn.setText(R.string.txt_success_record_msg);
                            empData=root;
                            binding.btnIn.setEnabled(true);
                            binding.btnOut.setEnabled(false);
                        }else {//  record of empid in preference
                            checkEmpid(isDataExist);
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        binding.btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnIn.setEnabled(false);
                String id =binding.etId.getText().toString();
                if(id.isEmpty()) {
                    binding.etId.setText(R.string.txt_hint_id);
                    binding.etId.setError(getString(R.string.txt_hint_id));
                    binding.btnIn.setEnabled(true);
                    return;
                }
                String name=binding.etName.getText().toString();
                if(name.isEmpty()) {
                    binding.etName.setError(getString(R.string.txt_hint_name));
                    binding.btnIn.setEnabled(true);
                    return;
                }
                long currentTimeMillis=System.currentTimeMillis();
                JSONObject todayData= empData.optJSONObject(keyTodayDate);
                if(todayData==null) todayData=new JSONObject();
                try {
                    todayData.put(StartTime,currentTimeMillis);
                    empData.put(EName,getNameRemoveGreet(name));
                    empData.put(keyTodayDate,todayData.toString());
                    pref.setData(id,empData.toString());
                    //save the data
                    long punchInTime=currentTimeMillis;
                    long extraTillnow=empData.optLong(ExtraTillNow,0l);

                    binding.tvIn.setText(getString(R.string.app_tvin,getDateAsString(new Date(punchInTime))
                           ,(extraTillnow*1.0f/(divideBy)),getTimeUnit(showTimeUnit)));
                    if(extraTillnow>threeHrthirtyMin)
                        extraTillnow=threeHrthirtyMin;
                    long diffTime=punchInTime+nineHrthirtyMin-extraTillnow;
                    binding.tvOut.setText(getString(R.string.app_tvout,getDateAsString(new Date(diffTime))));
                    startTimer(diffTime-System.currentTimeMillis());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                binding.btnOut.setEnabled(true);

                inClicked();
            }
        });
        binding.btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnOut.setEnabled(false);
                binding.btnIn.setEnabled(false);
                outClicked(System.currentTimeMillis());
            }
        });
        //deleting the record of previous month
        binding.btnOut.postDelayed(new Runnable() {
            @Override
            public void run() {
               String empid= pref.getData(LastEId,"");
               if(empid.isEmpty()||true)
                empid= binding.etId.getText().toString();
                if(empid.isEmpty()) return;
                String empDatap=pref.getData(empid,"");
                if (empDatap.isEmpty() || !empDatap.startsWith("{")|| !empDatap.endsWith("}")) return;
                try {
                    JSONObject root=new JSONObject(empDatap);
                    //current month
                    Calendar calmonth=Calendar.getInstance();
                    //get past month
                    calmonth.add(Calendar.MONTH, -2);//

                    int min = calmonth.getActualMinimum(Calendar.DAY_OF_MONTH);
                    calmonth.set(Calendar.DAY_OF_MONTH, min);  //previous Month first day
                    Date preMonFirtDate=calmonth.getTime();

                    int dayMax = calmonth.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calmonth.set(Calendar.DAY_OF_MONTH, dayMax);//previousMonlast day
                    Date preMonLastDate=calmonth.getTime();


                    //ArrayList<Date> dates=new ArrayList<>();
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(preMonFirtDate);


                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(preMonLastDate);

                    while(!cal1.after(cal2))
                    {
                       int day = cal1.get(Calendar.DAY_OF_MONTH);
                        String keyTodayDate= Constants.getDateAsString(cal1.getTime(),null);
                        Log.i(TAG, "run: keyTodayDate:"+keyTodayDate+",root.has:"+root.has(keyTodayDate));
                        if (root.has(keyTodayDate)) {
                            root.remove(keyTodayDate);
                        }
                       boolean isLastDayOfMonth=day==dayMax;
                       //dates.add(cal1.getTime());
                       cal1.add(Calendar.DATE, 1);
                       if(isLastDayOfMonth ) break;
                    }
                    pref.setData(empid,root.toString());

                }
                catch (Exception e){
                    Log.i(TAG, "run: ex while delete the record of time tracker");
                }
            }
        },10);
     //   AddCalendarEvent();//only for test
    }

    public String getNameRemoveGreet(String etData){
        if(etData.contains("Welcome")&&etData.length()>12){
            etData=etData.substring(13);
        }
        return etData;
    }
    public void startRecord(){
        if (empData.optJSONObject(keyTodayDate)==null) {
            long currentTimeMillis=System.currentTimeMillis();
            long punchInTime=currentTimeMillis;
            long extraTillnow=pref.getData(ExtraTillNow,0);
//            binding.tvIn.setText(getString(R.string.app_tvin, getDateAsString(new Date(punchInTime)))+" ,\nSo far extra time you have covered is "+(extraTillnow*1.0/(60*1000))+" min");
            binding.tvIn.setText(getString(R.string.app_tvin, getDateAsString(new Date(punchInTime)),(extraTillnow*1.0/(divideBy)),getTimeUnit(showTimeUnit)) );
            long diffTime=punchInTime+nineHrthirtyMin-currentTimeMillis;
            binding.tvOut.setText(getString(R.string.app_tvout,getDateAsString(new Date(punchInTime+nineHrthirtyMin))));
            startTimer(diffTime-System.currentTimeMillis());
        }
    }
    public long restExtraTime(long extra){
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
        //last day of week is saturday that is 7 but i want as Friday so 6
        c1.set(Calendar.DAY_OF_WEEK, 6); //friday

        int year7 = c1.get(Calendar.YEAR);
        int month7 = c1.get(Calendar.MONTH)+1;
        int day7 = c1.get(Calendar.DAY_OF_MONTH);
        System.out.println("date end of week = " +day7+"-"+month7+"-"+year7);
        // reset logic
        boolean isLastDayOfMonth=day==dayMax;
        boolean isFirstDayOfMonth=day==dayMin;
        boolean isLastDayOfWeek=day==day7;
        boolean isFirstDayOfWeek=day==day1;
        if (extra>sevenHrthirtyMin){
            extra=sevenHrthirtyMin;
        }
        //if(isFirstDayOfWeek||isLastDayOfWeek||isFirstDayOfMonth || isLastDayOfMonth ){
        if(isLastDayOfWeek|| isLastDayOfMonth ){
            extra=0;
        }
       /* if(day7-day1<0 ){ //reset
            extra=0; }*/
        return extra;
    }
    public void checkEmpid(String empidData ){
        if(!empidData.isEmpty() && empidData.startsWith("{")&& empidData.endsWith("}")){
            try {
                JSONObject root=new JSONObject(empidData);
                empData=root;
                long extraTillNow=root.optLong(ExtraTillNow,0l);
                String name=root.getString(EName);
                binding.etName.setText(getString(R.string.app_etname,name));
                binding.etName.setEnabled(false);
                JSONObject todayData=new JSONObject(root.optString(keyTodayDate,"{}"));
//                if (todayData!=null){
                    long currentTimeMillis=System.currentTimeMillis();
                    long punchInTime=todayData.optLong(StartTime,currentTimeMillis);
                  //  long punchOutTime=todayData.optLong(EndTime,currentTimeMillis);
                    if(punchInTime!=currentTimeMillis){
                        //set the time
                        binding.tvIn.setText(getString(R.string.app_tvin, getDateAsString(new Date(punchInTime)),(extraTillNow*1.0/(divideBy)),getTimeUnit(showTimeUnit)));
                        if(extraTillNow>threeHrthirtyMin)
                            extraTillNow=threeHrthirtyMin;
                        long diffTime=punchInTime+nineHrthirtyMin-extraTillNow;
                        binding.tvOut.setText(getString(R.string.app_tvout, getDateAsString(new Date(diffTime))));
                        startTimer(diffTime-System.currentTimeMillis());
                        binding.btnIn.setEnabled(false);
                        binding.btnOut.setEnabled(true);
                    }
                    else
                    {
                        binding.btnIn.setEnabled(true);
                        binding.tvIn.setText(getString(R.string.txt_start_punch_in));
                    }
//                }
//                else
//                {
//                    binding.btnIn.setEnabled(true);
//                    binding.tvIn.setText(getString(R.string.txt_start_punch_in));
//                    Log.i(TAG, "checkEmpid: check why its today is null here data is there");
//                }
            } catch (JSONException e) {
                Log.i(TAG, "checkEmpid: check why its JSONException here data is there");
                e.printStackTrace();
                binding.tvIn.setError(getString(R.string.error_tvin));
                binding.tvIn.setText(R.string.error_tvin);
                binding.etId.setEnabled(true);
            }
        }else
        {
            binding.tvIn.setError(getString(R.string.error_tvin));
            binding.tvIn.setText(R.string.error_tvin);
            binding.etId.setEnabled(true);
            Log.i(TAG, "checkEmpid: check why its empty here data is there");
        }

    }
    String getTimeUnit(DisplayTimeIn format){
        String time;
        switch (format){
            case HOUR:
                time=HOUR.getData();
                break;
                case SEC:
                    time=SEC.getData();
                break;
            case MILISEC:
                time=MILISEC.getData();
                break;
            case MIN:
            default:
                time=MIN.getData();
                break;
        }
        return time;
    }
    long getTimeDivider(DisplayTimeIn format){
        long time=oneMin;
        switch (format){
            case HOUR:
                time=oneHR;
                break;
                case SEC:
                    time=oneSec;
                break;
            case MILISEC:
                time=oneMiliSec;
                break;
            case MIN:
            default:
                time=oneMin;
                break;
        }
        return time;
    }

    private static final String TAG = "PunchInOutFragment";
    public void inClicked(){
        AddCalendarEvent();
       // startRecord();
    }
    public void outClicked(long endTime){
        try {
            //long endTime = System.currentTimeMillis();
            long extraTillNow=empData.optLong(ExtraTillNow,0l);
            JSONObject todayData = new JSONObject(empData.optString(keyTodayDate));
            long punchInTime = todayData.optLong(StartTime, endTime);
            if (punchInTime == endTime) {
                binding.tvOut.setText("you forget to punch in");
            } else {
                String msg = "Today you have ";
                long diff = punchInTime + nineHrthirtyMin - endTime;
                if (diff == 0) {
                    msg += " completed your 9hr 30min only";
                    extraTillNow=0;
                } else if (diff > 0) {
                    msg += "not completed your 9hr 30min, less by " + String.format("%.2f",(diff *1.0/ divideBy)) + " "+getTimeUnit(showTimeUnit);
                   if(diff<fourHrfortyFive||diff<sixHr)
                       extraTillNow=0;
                    else
                    extraTillNow=-diff;
                } else {
                    //diff = endTime- (punchInTime + nineHrthirtyMin); -1(-diff)=+diff
                    extraTillNow=-1*(diff);
                    msg += "completed your 9hr 30min, extra by " + String.format ("%.2f",(extraTillNow *1.0/ divideBy)) + " "+getTimeUnit(showTimeUnit);

                }

                binding.tvOut.setText(msg);

                todayData.put(EndTime, endTime);
                todayData.put(DiffOfDay, diff);

                if(extraTillNow>twoHrthirtyMin)extraTillNow=twoHrthirtyMin;

                // add all previous diff
                extraTillNow=calculateExtra();
                extraTillNow=restExtraTime(extraTillNow);
                empData.put(ExtraTillNow, extraTillNow);
                empData.put(keyTodayDate, todayData.toString());

                pref.setData(binding.etId.getText().toString(), empData.toString());
                cancelTimer();
            }
        }
        catch (Exception e){
           e.printStackTrace();
        }
    }

    private long calculateExtra(){
        long extra=0l;
        try {
            ArrayList<Date> dates = getDateBeginWeek();
            JSONObject EmpTimeRecord = new JSONObject(empData.toString());
            for (Date date : dates) {
                String keyTodayDate = Constants.getDateAsString(date, null);
                JSONObject todayData = new JSONObject(EmpTimeRecord.optString(keyTodayDate, "{}"));
                if (todayData == null || todayData.length() == 0) continue;
                long startTime = todayData.optLong(Constants.StartTime, System.currentTimeMillis());
                long endTime = todayData.optLong(Constants.EndTime, System.currentTimeMillis());
                long diffTimeExtra = todayData.optLong(Constants.DiffOfDay, 0l);
                long diffTime = endTime - (startTime+nineHrthirtyMin);
                if(diffTime<fourHrfortyFive||diffTime<sixHr)
                    diffTime=0;
                if(diffTime>twoHrthirtyMin)diffTime=twoHrthirtyMin;
                extra+=diffTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extra;
    }

    /* do similar change in the getDateBeginWeek class or make it common by utils class or singleton */
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

        boolean isLastDayOfWeek=day==day7 || day==day6;


        ArrayList<Date> dates=new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date3);

        while(!cal1.after(cal2))
        {
            day = cal1.get(Calendar.DAY_OF_MONTH);
            isLastDayOfMonth=day==dayMax;
            isLastDayOfWeek=/*day==day7 ||*/ day==day6;
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
            if(isLastDayOfMonth || isLastDayOfWeek) break;

        }
        return dates;
    }

    @Override
    public void onPause() {
        super.onPause();
        pref.setData(LastEId,binding.etId.getText().toString());
        pref.setData(EName,getNameRemoveGreet(binding.etName.getText().toString()));
    }

    @Override
    public void onResume() {
        super.onResume();
        pref.remove(LastEId);
        pref.remove(EName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimer();
//        removeGeofences();
    }
    void startTimer(long remainTime) {
        if (isTimmerOn) {
            return;
        }
        binding.circularDeterminativePb.setMax(100);
        binding.circularDeterminativePb.setProgress(100);
       // cancelTimer();
        cTimer = new CountDownTimer(remainTime, divideBy) {
            public void onTick(long millisUntilFinished) {
                int progress=(int) (millisUntilFinished*100/nineHrthirtyMin);
                double showPer=  (millisUntilFinished*(millisUntilFinished>divideBy?1:1.0)/divideBy);//binding.circularDeterminativePb.getProgress();
                Log.i(TAG, "onTick: "+showPer+" "+getTimeUnit(showTimeUnit)+",  "+progress+"% ");
              //  if (showPer>progress) {
                    binding.circularDeterminativePb.setProgress(progress);
                    binding.progressTv.setText(progress+"%\nleft\n"+String.format("%.2f",showPer)+"\n"+ getTimeUnit(showTimeUnit));

              //  }

            }
            public void onFinish() {
                //send notification for punchout
//                binding.circularDeterminativePb.setMax(100);
                binding.circularDeterminativePb.setProgress(0);
                binding.progressTv.setText("done");
            }
        };
        cTimer.start();
        isTimmerOn=true;
    }
    //cancel timer
    void cancelTimer() {
        if(cTimer!=null){
            cTimer.cancel();
            isTimmerOn=false;
        }
    }


    public String getDateAsString( Date date){
        // Parsing the date and time
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        String mStartTime = mSimpleDateFormat.format(date);
        return mStartTime;
      //  Date mEndTime = mSimpleDateFormat.parse(endTime);
    }
    public String getDateAsString( Date date,String format){
        if(format==null || format.isEmpty()) format="dd-MM-yyyy";
        // Parsing the date and time
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
        String mStartTime = mSimpleDateFormat.format(date);
        return mStartTime;
      //  Date mEndTime = mSimpleDateFormat.parse(endTime);
    }
    public Date getDateFromString(String date){
        // Parsing the date and time
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        Date mStartTime = null;
        try {
            mStartTime = mSimpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            mStartTime=Calendar.getInstance().getTime();

        }
        return mStartTime;
      //  Date mEndTime = mSimpleDateFormat.parse(endTime);
    }

    long oneMiliSec= 1;
    long oneSec=  1000*oneMiliSec;
    long oneMin=  60*oneSec;
    long oneHR=  60*oneMin;
    long nineHrthirtyMin= ((9*oneHR)+ (oneHR/2)); //34200000
    long twoHrthirtyMin= ((2*oneHR)+ (oneHR/2)); //  9000000
    long threeHrthirtyMin= ((3*oneHR)+ (oneHR/2)); //
    long sixHr= (6*oneHR); //  25200000
    long fourHrfortyFive=( (4*oneHR)+ (oneHR/2)+(15*oneMin)); //  25200000
    long sevenHrthirtyMin= ((7*oneHR)+ (oneHR/2)); //  25200000
    long divideBy=getTimeDivider(showTimeUnit);
    final String StartTime="StartTime";
    final String EndTime="EndTime";
    final String ExtraTillNow="extraTillnow";
    final String DiffOfDay="diffOfDay";
    final  String EName="ename";
    final String LastEId="lasteid";
    DatePickerDialog startTime;
    TimePickerDialog pickTime;

    private void getSelectedTimeInmilis() {
        Calendar currentDate = Calendar.getInstance();
//        Calendar date = Calendar.getInstance();
        if (pickTime == null){
            pickTime = new TimePickerDialog(this.getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String id =binding.etId.getText().toString();
                    if(id==null||id.isEmpty()) return;
                    Calendar date = Calendar.getInstance();
                    date.setTime(userSeleted);
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    date.set(Calendar.MINUTE, minute);

                    keyTodayDate=getDateAsString(date.getTime(),null);
                    long currentTimeMillis=date.getTime().getTime();
                    Log.i(TAG, "onTimeSet: selected date time :"+date.toString()+",keyTodayDate:"+keyTodayDate+",currentTimeMillis:"+currentTimeMillis);



                    try {
                        JSONObject todayData;
                        String s = empData.optString(keyTodayDate);
                        if(s!=null&&s.isEmpty()&&s.startsWith("{")&&s.endsWith("}"))
                             todayData = new JSONObject(s);
                        else{
                            todayData = new JSONObject();
                        }

                        if(clickedId==R.id.tvIn){
                            //in
                            todayData.put(StartTime,currentTimeMillis);
                            empData.put(keyTodayDate,todayData.toString());
                            pref.setData(id,empData.toString());
                        } if(clickedId==R.id.tvOut){
                            //out
                            todayData.put(EndTime,currentTimeMillis);
                            outClicked(currentTimeMillis);
                        }
                        else
                        {
                          //other cases
                        }



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    date.setTimeInMillis(System.currentTimeMillis());
                    Log.v(TAG, "Reset calender " + date.getTime());
                }
            }, currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE),
                    false);
        }
        if(!pickTime.isShowing())
            pickTime.show();

    }
    Date userSeleted;
    private void getUserSelectedDateFromCal(){
//        show calender to user
        if(startTime==null) {
            Calendar newCalendar = Calendar.getInstance();
            startTime = new DatePickerDialog(this.getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year, monthOfYear, dayOfMonth);
                            userSeleted=newDate.getTime();
                            keyTodayDate=getDateAsString(newDate.getTime(),null);
                            newDate.setTimeInMillis(System.currentTimeMillis());
                            getSelectedTimeInmilis();
                        }

                    },
                    newCalendar.get(Calendar.YEAR),
                    newCalendar.get(Calendar.MONTH),
                    newCalendar.get(Calendar.DAY_OF_MONTH));

        }
        if(!startTime.isShowing())
            startTime.show();

    }
    public void AddCalendarEvent() {
        Calendar calendarEvent = Calendar.getInstance();
        Intent i = new Intent(Intent.ACTION_EDIT);
        i.setType("vnd.android.cursor.item/event");
        i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, pref.getData(StartTime,System.currentTimeMillis()));
        i.putExtra(CalendarContract.Events.DESCRIPTION, "Working hour to remind");
        i.putExtra("time", true);
        //        i.putExtra("allDay", true);
//        i.putExtra("rule", "FREQ=YEARLY");
//        i.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
//        //Every 20 minutes from 8:00 AM to 8:40 AM and from 5:00 PM to 5:40 PM every day:
//        i.putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY;BYHOUR=8,17;BYMINUTE=0,20,40");
        // Every 9 hours from 9:00 AM to 5:00 PM on a specific day: FREQ=HOURLY;INTERVAL=1;BYHOUR=9;BYMINUTE=30;COUNT=1
         i.putExtra(CalendarContract.Events.RRULE, "FREQ=HOURLY;INTERVAL=1;BYHOUR=9;BYMINUTE=30;COUNT=1");
//        i.putExtra("rule", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
        i.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, pref.getData(EndTime,System.currentTimeMillis()+nineHrthirtyMin));
        i.putExtra(CalendarContract.Events.TITLE, "Punch In and Out");
        i.putExtra(CalendarContract.Events.EVENT_LOCATION, "WFH or WFO");
        try {
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : BAY_AREA_LANDMARKS.entrySet()) {

            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

    public void removeGeofences(){
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                        Log.i(TAG, "onSuccess: Geofences removed ");
                    }
                })
                .addOnFailureListener(this.getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                        Log.i(TAG, "onFailure: Failed to remove geofences");
                    }
                });
    }



}