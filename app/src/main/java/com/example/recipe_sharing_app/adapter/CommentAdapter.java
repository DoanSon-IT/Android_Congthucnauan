package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.model.Comment;
import com.example.recipe_sharing_app.model.Rating;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> comments;

    // Constructor
    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        // Thiết lập tên người dùng
        holder.txtUserName.setText(comment.getUserName());

        // Thiết lập nội dung bình luận
        holder.txtCommentContent.setText(comment.getContent());

        // Thiết lập thời gian bình luận
        holder.txtCommentTime.setText(comment.getTime());
        // Thiết lập đánh giá RatingBar

        // Tải ảnh đại diện người dùng bằng Glide
        Glide.with(context).load(comment.getUserAvatar()).into(holder.imgUserAvatar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView txtUserName;
        TextView txtCommentContent;
        TextView txtCommentTime;

        public ViewHolder(View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtCommentContent = itemView.findViewById(R.id.txtCommentContent);
            txtCommentTime = itemView.findViewById(R.id.txtCommentTime);
        }
    }
}
