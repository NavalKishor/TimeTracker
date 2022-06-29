package com.ctlev.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ctlev.app.ui.puninout.PunchInOutFragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PunchInOutFragment.newInstance())
                    .commitNow();
        }
    }
}