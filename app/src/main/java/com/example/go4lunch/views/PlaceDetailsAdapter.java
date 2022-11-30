package com.example.go4lunch.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.models.User;

import java.util.List;

public class PlaceDetailsAdapter extends RecyclerView.Adapter<PlaceDetailsViewHolder> {

    private List<User> mResults;



    public PlaceDetailsAdapter(List<User> result) {
        this.mResults = result;
    }

    @Override
    public PlaceDetailsViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view =inflater.inflate(R.layout.activity_place_details_items, parent , false);
        return new PlaceDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceDetailsViewHolder holder, int position) {
        holder.updateWithData(this.mResults.get(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults !=null) itemCount = mResults.size();
        return itemCount;
    }
}
