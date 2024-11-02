package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.model.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private Context context;
    private List<Step> stepList;

    public StepAdapter(Context context, List<Step> stepList) {
        this.context = context;
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = stepList.get(position);
        holder.tvStepNumber.setText(""+(position + 1));
        holder.tvStepDescription.setText(step.getDescription());
        // Load image using Picasso or any other image loading library
        Picasso.get().load(step.getImageUrl()).into(holder.imgStepImage);
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber;
        TextView tvStepDescription;
        ImageView imgStepImage;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            imgStepImage = itemView.findViewById(R.id.imgStepImage);
        }
    }
}
