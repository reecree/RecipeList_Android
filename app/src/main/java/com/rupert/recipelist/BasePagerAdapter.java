package com.rupert.recipelist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BasePagerAdapter extends FragmentPagerAdapter {
    private Context _context;
    public BasePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setContext(Context c) {
        _context = c;
    }

    @Override
    public Fragment getItem(int i) {
        BasePageObjectFragment fragment = new BasePageObjectFragment();
        fragment.setContext(_context);
        Bundle args = new Bundle();
        args.putSerializable(Globals.BASE_ID_KEY, Globals.Tabs.values()[i]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return Globals.TAB_LIST.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Globals.TAB_LIST[position];
    }
}