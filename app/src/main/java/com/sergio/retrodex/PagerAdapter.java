package com.sergio.retrodex;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return CharacterListFragment.newInstance(CharacterListFragment.MODE_DECADE);
        } else {
            return CharacterListFragment.newInstance(CharacterListFragment.MODE_CATEGORY);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
