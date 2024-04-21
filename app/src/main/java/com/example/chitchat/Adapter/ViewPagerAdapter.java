package com.example.chitchat.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chitchat.Fragment.Notification2Fragment;
import com.example.chitchat.Fragment.RequestFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if (position == 1) {
            return new RequestFragment();
        }
        return new Notification2Fragment();
    }

    @Override
    public int getCount() {
        return 2;                                      // because size is two i.e. notification and request
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        String title = null;
        if(position == 0){
            title = "NOTIFICATION";
        } else if (position==1) {
            title = "REQUEST";
        }
        return title;
    }
}
