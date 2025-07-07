package com.atom.bluetoothfitnessapplication.presentation.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.atom.bluetoothfitnessapplication.presentation.fragments.FitnessMainFragment;
import com.atom.bluetoothfitnessapplication.presentation.fragments.FitnessStatsFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdapter extends FragmentStateAdapter {

    private List<Fragment> fragments;

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new ArrayList<>();
        fragments.add(FitnessMainFragment.newInstance("", ""));
        fragments.add(FitnessStatsFragment.newInstance("", ""));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
