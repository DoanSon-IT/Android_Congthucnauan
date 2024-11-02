package com.example.recipe_sharing_app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipe_sharing_app.fragment.CacBanBepFragment;
import com.example.recipe_sharing_app.fragment.KhoCamHungFragment;


public class PageAdapter extends FragmentStateAdapter {

    public PageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CacBanBepFragment();
            case 1:
                return new KhoCamHungFragment();
            default:
                return new CacBanBepFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}

