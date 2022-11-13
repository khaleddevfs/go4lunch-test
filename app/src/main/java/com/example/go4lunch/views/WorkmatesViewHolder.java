package com.example.go4lunch.views;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.databinding.FragmentWorkmatesItemsBinding;
import com.example.go4lunch.models.User;


public class WorkmatesViewHolder extends RecyclerView.ViewHolder{

    private FragmentWorkmatesItemsBinding binding;


    public WorkmatesViewHolder(View itemView) {
        super(itemView);
    }

   public void updateWithData(User results){
        RequestManager glide = Glide.with(itemView);
        if (!(results.getUrlPicture() == null)){
            glide.load(results.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(binding.matesMainPicture);
        }else{
            glide.load(R.drawable.no_picture).apply(RequestOptions.circleCropTransform()).into(binding.matesMainPicture);
        }


    }
}
