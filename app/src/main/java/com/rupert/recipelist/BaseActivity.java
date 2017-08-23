package com.rupert.recipelist;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class BaseActivity extends FragmentActivity {
    BasePagerAdapter _basePagerAdapter;
    ViewPager _viewPager;
    TabLayout _tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        _basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager());
        _basePagerAdapter.setContext(this);
        _viewPager = findViewById(R.id.pager);
        _viewPager.setAdapter(_basePagerAdapter);

        _tabLayout = findViewById(R.id.tabs);
        _tabLayout.setupWithViewPager(_viewPager);
    }
}