package com.example.go4lunch.ui.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.go4lunch.R;
import com.example.go4lunch.ViewModels.CommunicationViewModel;
import com.example.go4lunch.databinding.FragmentWorkmatesBinding;
import com.example.go4lunch.models.User;
import com.example.go4lunch.ui.activity.MainActivity;
import com.example.go4lunch.views.WorkmatesAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class WorkmatesFragment extends BaseFragment {

    private List<User> mUserList;

    private WorkmatesAdapter mWorkmatesAdapter;

    private CommunicationViewModel mViewModel;

    private FragmentWorkmatesBinding binding;



    public WorkmatesFragment() {
        // Required empty public constructor
    }


    public static WorkmatesFragment newInstance() {
        return new WorkmatesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);

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



    private void configureOnSwipeRefresh() {
        binding.matesSwipeRefresh.setOnRefreshListener(this::updateUIWhenCreating);
    }



    private void configureOnClickRecyclerView() {




    }



    private void updateUIWhenCreating() {



    }



    private void configureRecyclerView() {
        this.mUserList = new ArrayList<>();
        this.mWorkmatesAdapter = new WorkmatesAdapter(this.mUserList);
        this.binding.matesRecyclerView.setAdapter(this.mWorkmatesAdapter);
        this.binding.matesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration (ContextCompat.getDrawable(getContext(), R.drawable.divider), 100 );
        //binding.matesRecyclerView.addItemDecoration(dividerItemDecoration);

        binding.matesRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), R.drawable.divider));

       binding.matesRecyclerView.setAdapter(this.mWorkmatesAdapter);



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


}