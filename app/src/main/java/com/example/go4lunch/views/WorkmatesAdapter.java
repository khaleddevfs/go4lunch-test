package com.example.go4lunch.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.models.User;

import java.util.List;



public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    // FOR DATA
    private List<User> mResults;


    // CONSTRUCTOR
    public WorkmatesAdapter(List<User> result) {this.mResults = result;}

    @Override
    public WorkmatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_workmates_items, parent,false);
        return new WorkmatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WorkmatesViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position));
    }

    public User getMates(int position){
        return this.mResults.get(position);
    }


    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
