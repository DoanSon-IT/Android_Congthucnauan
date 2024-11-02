package com.example.recipe_sharing_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.adapter.ChefAdapter;
import com.example.recipe_sharing_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CacBanBepFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChefAdapter chefAdapter;
    private List<User> chefList;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cac_ban_bep, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChefs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chefList = new ArrayList<>();
        chefAdapter = new ChefAdapter(getContext(), chefList);
        recyclerView.setAdapter(chefAdapter);

        mAuth = FirebaseAuth.getInstance();

        loadChefsFromFirebase();

        return view;
    }

    private void loadChefsFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String currentUserEmail = currentUser.getEmail(); // Lấy email của người dùng hiện tại

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chefList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User chef = snapshot.getValue(User.class);
                    if (chef != null &&
                            !chef.getEmail().equals(currentUserEmail) &&
                            !chef.getEmail().equals("admin@gmail.com")) {
                        // Bỏ qua người dùng hiện tại và admin@gmail.com
                        chefList.add(chef);
                    }
                }
                chefAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView sau khi lấy dữ liệu
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
}
