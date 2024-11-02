package com.example.recipe_sharing_app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipe_sharing_app.fragment.MyRecipesFragment;
import com.example.recipe_sharing_app.fragment.My_FavoritesFragment;
import com.example.recipe_sharing_app.fragment.SaveForLaterFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SaveForLaterFragment();  // Tab "Món Đã Lưu"
            case 1:
                return new My_FavoritesFragment();
            case 2:
                return new MyRecipesFragment();// Tab "Món Của Tôi"
            default:
                return new SaveForLaterFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;  // Số lượng tab
    }
}

