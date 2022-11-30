package com.example.go4lunch.ui.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.go4lunch.R;
import com.example.go4lunch.Utils.DividerItemDecoration;
import com.example.go4lunch.Utils.ItemClickSupport;
import com.example.go4lunch.Utils.PlacesStreams;
import com.example.go4lunch.ViewModels.CommunicationViewModel;
import com.example.go4lunch.databinding.FragmentListBinding;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.example.go4lunch.ui.activity.MainActivity;
import com.example.go4lunch.ui.activity.PlaceDetailsActivity;
import com.example.go4lunch.views.RestaurantAdapter;



import java.util.ArrayList;
import java.util.List;


import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


public class ListFragment extends BaseFragment {

    private Disposable disposable;
    private CommunicationViewModel viewModel;
    private RestaurantAdapter adapter;

    private List<PlaceDetailsResults> mResults;

    private FragmentListBinding binding;



    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(CommunicationViewModel.class);// trouver une solution pour le rouge
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

                               binding = FragmentListBinding.inflate(getLayoutInflater());
                               View view = binding.getRoot();  // View view = inflater.inflate(R.layout.fragment_list, container, false); ButterKnife.bind(this, view);


        setHasOptionsMenu(true);

        viewModel.currentUserPosition.observe(getViewLifecycleOwner(), latLng -> {
                    executeHttpRequestWithRetrofit();
                    configureRecyclerView();
                });
        this.configureOnClickRecyclerView();
        this.configureOnSwipeRefresh();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.currentUserPosition.removeObservers(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.toolbar_menu, menu);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);

        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);
        searchView.setQueryHint(getResources().getString(R.string.toolbar_search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(((MainActivity) getContext()).getComponentName()));

        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2 ){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(query,viewModel.getCurrentUserPositionFormatted(),viewModel.getCurrentUserRadius(),MapFragment.API_KEY).subscribeWith(createObserver());
                }else{
                    Toast.makeText(getContext(), getResources().getString(R.string.search_too_short), Toast.LENGTH_LONG).show();
                }
                return true;

            }
            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 2){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(query,viewModel.getCurrentUserPositionFormatted(),viewModel.getCurrentUserRadius(),MapFragment.API_KEY).subscribeWith(createObserver());
                }
                return false;
            }
        });
    }

    private void configureRecyclerView() {
        this.mResults = new ArrayList<>();
        this.adapter = new RestaurantAdapter(this.mResults, viewModel.getCurrentUserPositionFormatted());
        this.binding.listRecyclerView.setAdapter(this.adapter);
        this.binding.listRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider), 100);
        binding.listRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void configureOnSwipeRefresh(){
        binding.listSwipeRefresh.setOnRefreshListener(this::executeHttpRequestWithRetrofit);
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(binding.listRecyclerView, R.layout.fragment_list_items)
                .setOnItemClickListener((recyclerView, position, v) -> {

                    PlaceDetailsResults result = adapter.getRestaurant(position);
                    Intent intent = new Intent(getActivity(),PlaceDetailsActivity.class);
                    intent.putExtra("PlaceDetailResult", result.getPlaceId());
                    startActivity(intent);
                });
    }

    private void executeHttpRequestWithRetrofit() {
        binding.listSwipeRefresh.setRefreshing(true);
        this.disposable = PlacesStreams.streamFetchPlaceInfo(viewModel.getCurrentUserPositionFormatted(), viewModel.getCurrentUserRadius(), MapFragment.SEARCH_TYPE,MapFragment.API_KEY).subscribeWith(createObserver());
    }


    private <T> DisposableObserver<T> createObserver(){
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof ArrayList){
                    updateUI((ArrayList) t);
                }
            }
            @Override
            public void onError(Throwable e) {
                getActivity().runOnUiThread(() -> binding.listSwipeRefresh.setRefreshing(false));
                handleError(e);}
            @Override
            public void onComplete() { }
        };
    }


    private void updateUI(List<PlaceDetailsResults> results){
        binding.listSwipeRefresh.setRefreshing(false);
        mResults.clear();
        if (results.size() > 0){
            mResults.addAll(results);
        }else{
            Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }


    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }
}
