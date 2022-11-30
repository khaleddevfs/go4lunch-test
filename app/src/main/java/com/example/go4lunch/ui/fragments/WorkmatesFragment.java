package com.example.go4lunch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.go4lunch.R;
import com.example.go4lunch.Utils.ItemClickSupport;
import com.example.go4lunch.ViewModels.CommunicationViewModel;
import com.example.go4lunch.databinding.FragmentWorkmatesBinding;
import com.example.go4lunch.models.User;
import com.example.go4lunch.repository.BookingRepository;
import com.example.go4lunch.ui.activity.PlaceDetailsActivity;
import com.example.go4lunch.views.WorkmatesAdapter;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class WorkmatesFragment extends BaseFragment {

    private List<User> mUserList;

    private WorkmatesAdapter mWorkmatesAdapter;

    private CommunicationViewModel mViewModel;

    private FragmentWorkmatesBinding binding;



    /*public WorkmatesFragment() {
        // Required empty public constructor
    }*/


    public static WorkmatesFragment newInstance() {
        return new WorkmatesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
              binding = FragmentWorkmatesBinding.inflate(getLayoutInflater());
              View view = binding.getRoot();

        setHasOptionsMenu(true);

        mViewModel = new ViewModelProvider(requireActivity()).get(CommunicationViewModel.class);
        mViewModel.currentUserUID.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String uid) {
                configureRecyclerView();
                updateUIWhenCreating();
                configureOnClickRecyclerView();
                configureOnSwipeRefresh();

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }



    private void configureRecyclerView() {
        this.mUserList = new ArrayList<>();
        this.mWorkmatesAdapter = new WorkmatesAdapter(this.mUserList);
        this.binding.matesRecyclerView.setAdapter(this.mWorkmatesAdapter);
        this.binding.matesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration dividerItemDecoration = new com.example.go4lunch.Utils.DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider), 100 );
        binding.matesRecyclerView.addItemDecoration(dividerItemDecoration);
        binding.matesRecyclerView.addItemDecoration(dividerItemDecoration);
    }


    private void configureOnSwipeRefresh() {
        binding.matesSwipeRefresh.setOnRefreshListener(this::updateUIWhenCreating);
    }



    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(binding.matesRecyclerView, R.layout.fragment_workmates_items)
                .setOnItemClickListener((recyclerView, position, v) -> {

                    User result = mWorkmatesAdapter.getMates(position);
                    retrieveBookedRestaurantByUser(result);
                });
    }

    private void retrieveBookedRestaurantByUser(User result) {
        BookingRepository.getBooking(result.getUid(),getTodayDate()).addOnCompleteListener(bookingTask ->{
            if (bookingTask.isSuccessful()){
                if (!(bookingTask.getResult().isEmpty())){
                    for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                        showBookingRestaurantByUser(booking.getData().get("restaurantId").toString());
                    }
                }else {
                    Toast.makeText(getContext(), getResources().getString(R.string.mates_hasnt_decided,result.getUsername()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showBookingRestaurantByUser(String restaurantId) {
        Intent intent = new Intent(getActivity(), PlaceDetailsActivity.class);
        intent.putExtra("PlaceDetailsResult", restaurantId);
        startActivity(intent);
    }


    private void updateUIWhenCreating() {



    }







}