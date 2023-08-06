package com.ctlev.app.ui.puninout;

import android.content.Context;
import android.os.Bundle;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ctlev.app.R;
import com.ctlev.app.databinding.FragmentItemListBinding;
import com.ctlev.app.persistence.SharedPreferenceStorage;
import com.ctlev.app.ui.puninout.placeholder.PlaceholderContent;

import org.json.JSONObject;


/**
 * A fragment representing a list of Items.
 */
public class WeeklyTrackerFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String DATA = "Data";
    // TODO: Customize parameters
    private String empId = "";
    SharedPreferenceStorage pref;
    PlaceholderContent placeholderContent;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeeklyTrackerFragment() {
    }

    private FragmentItemListBinding binding;
    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WeeklyTrackerFragment newInstance(String data) {
        WeeklyTrackerFragment fragment = new WeeklyTrackerFragment();
        Bundle args = new Bundle();
        args.putString(DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            empId = getArguments().getString(DATA,"");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      //  View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onBackPressRegister();

        pref= SharedPreferenceStorage.getSharedPreferences ( getActivity());
        String empData=pref.getData(empId,"");
        placeholderContent=new PlaceholderContent();

       try {
           JSONObject root=new JSONObject(empData);
           String name= root.optString(Constants.EName,"");
           binding.empDetails.setText(name+"\nEmpid:"+empId);
           placeholderContent.parseJsonData(empData.trim().toString());

           //parse the
       }catch (Exception exception){
           goToHome();
       }
        Context context = binding.weeklyList.getContext();
        LinearLayoutManager  layoutManager=new LinearLayoutManager(context);
        binding.previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMonthlyData(-1);
            }
        });
        binding.currentMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMonthlyData(0);
            }
        });
        binding.weeklyList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        binding.weeklyList.addItemDecoration(dividerItemDecoration);
        binding.weeklyList.setItemAnimator(new DefaultItemAnimator());
        binding.weeklyList.setAdapter(new WeeklyTrackerRecyclerViewAdapter(placeholderContent.ITEMS));


    }

    void getMonthlyData(int month){
        placeholderContent.ITEMS.clear();
        binding.weeklyList.getAdapter().notifyDataSetChanged();
        String empData=pref.getData(empId,"");
        placeholderContent.parseJsonMonthlyData(empData,month);
        binding.weeklyList.getAdapter().notifyDataSetChanged();
    }



    public String getTwoDigitNumFormat(){
        return  null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }
    public void onBackPressRegister(){
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Log.d("TAG", "Fragment back pressed invoked");
                        // Do custom work here
                        goToHome();
                    }
                });
    }
    public void goToHome(){
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, PunchInOutFragment.newInstance())
                .commitNow();
    }
}