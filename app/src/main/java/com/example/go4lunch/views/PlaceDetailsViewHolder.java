package com.example.go4lunch.views;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.databinding.ActivityPlaceDetailsItemsBinding;
import com.example.go4lunch.models.User;

public class PlaceDetailsViewHolder extends RecyclerView.ViewHolder {

    private ActivityPlaceDetailsItemsBinding binding;


    public PlaceDetailsViewHolder(View itemView) {
        super(itemView);
    }

    public void updateWithData(User user){
        RequestManager glide = Glide.with(itemView);
        if(!(user.getUrlPicture() == null)) {
            glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(binding.detailsPicture);
        }else {
            glide.load(R.drawable.ic_no_image_available).apply(RequestOptions.circleCropTransform()).into(binding.detailsPicture);
        }

        this.binding.detailsUserName.setText(itemView.getResources().getString(R.string.restaurant_detail_recyclerview, user.getUsername()));
        this.changeTextColor(R.color.Black);
    }

    private void changeTextColor(int color) {
        int mColor = itemView.getContext().getResources().getColor(color);
        this.binding.detailsUserName.setTextColor(mColor);
    }
}
