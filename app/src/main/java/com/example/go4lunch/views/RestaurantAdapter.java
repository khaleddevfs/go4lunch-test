package com.example.go4lunch.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {
    // FOR DATA
    private List<PlaceDetailsResults> mResults;
    private String mLocation;

    // CONSTRUCTOR
    public RestaurantAdapter(List<PlaceDetailsResults> result, String location) {
        this.mResults = result;
        this.mLocation = location;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.fragment_list_items,parent,false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {
        holder.updateWithData(this.mResults.get(position), this.mLocation);

    }


    public PlaceDetailsResults getRestaurant(int position){
     return this.mResults.get(position);

    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
