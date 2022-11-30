package com.example.go4lunch.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.go4lunch.R;
import com.example.go4lunch.Utils.PlacesStreams;
import com.example.go4lunch.ViewModels.CommunicationViewModel;
import com.example.go4lunch.databinding.FragmentMapBinding;
import com.example.go4lunch.models.PlacesInfo.MapPlacesInfo;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.example.go4lunch.repository.BookingRepository;
import com.example.go4lunch.ui.activity.MainActivity;
import com.example.go4lunch.ui.activity.PlaceDetailsActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;


import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.EasyPermissions;



public class MapFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,  LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FragmentMapBinding binding;
    private Disposable disposable;

    private static final int PERMS_FINE_COARSE_LOCATION = 100;
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public static final String SEARCH_TYPE = "restaurant";

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private LocationRequest mLocationRequest;

    public static final String API_KEY = "AIzaSyA8LboW2ze3r3wLiC28QfSZoFWGnkshsqM";


    private CommunicationViewModel viewModel;


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(CommunicationViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setHasOptionsMenu(true);

        viewModel.currentUserPosition.observe(getViewLifecycleOwner(), latLng -> {
            executeHttpRequestWithRetrofit();
        });

        binding.map.onCreate(savedInstanceState);
        binding.map.onResume();
        this.configureMapView();
        this.configureGoogleApiClient();
        this.configureLocationRequest();
        this.configureLocationCallBack();

        return view;
    }

    private void configureLocationCallBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()){
                    handleNewLocation(location);
                }
            }
        };

    }

    private void handleNewLocation(Location location) {
        Log.e(TAG, "handleNewLocation: ");
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        this.viewModel.updateCurrentUserPosition(new LatLng(currentLatitude, currentLongitude));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(this.viewModel.getCurrentUserPosition()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.viewModel.getCurrentUserPosition(), viewModel.getCurrentUserZoom()));
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }


    private void configureLocationRequest() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(100 * 1000)
                .setFastestInterval(1000);
    }

    private void configureGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addApi(LocationServices.API)
                .enableAutoManage(getActivity(), (GoogleApiClient.OnConnectionFailedListener) this)
                .build();
    }

    private void configureMapView() {
        try {
            MapsInitializer.initialize(getActivity().getBaseContext());
        } catch (Exception e){
            e.printStackTrace();
        }
        binding.map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(true);
                }
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                View locationButton =((View) binding.map.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0,0,30,30);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.setOnMarkerClickListener(MapFragment.this::onMarkerClick);

            }
        });
    }

    private boolean checkLocationPermission() {
        if (EasyPermissions.hasPermissions(getContext(), perms)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
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
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2 ){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(query, viewModel.getCurrentUserPositionFormatted(), viewModel.getCurrentUserRadius(), API_KEY).subscribeWith(createObserver());
                }else{
                    Toast.makeText(getContext(), getResources().getString(R.string.search_too_short), Toast.LENGTH_LONG).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(newText, viewModel.getCurrentUserPositionFormatted(), viewModel.getCurrentUserRadius(), API_KEY).subscribeWith(createObserver());
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect(); }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.currentUserPosition.removeObservers(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.map.onDestroy();
        this.disposeWhenDestroy();
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.map.onLowMemory();

    }

    private void disposeWhenDestroy() {
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();

    }


    private void executeHttpRequestWithRetrofit() {
        String location = viewModel.getCurrentUserPositionFormatted();
        Log.e(TAG,"location :"+location);
        this.disposable = PlacesStreams.streamFetchNearbyPlaces(location, viewModel.getCurrentUserRadius(), SEARCH_TYPE, API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver <T> createObserver() {
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                updateUI(t);

            }

            @Override
            public void onError(Throwable e) {
                handleError(e);

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private <T> void updateUI(T results) {
        mMap.clear();
        if (results instanceof MapPlacesInfo){
            MapPlacesInfo placesInfo = ((MapPlacesInfo) results);
            Log.e(TAG, "updateUI : " + placesInfo.getResults().size());
            if (placesInfo.getResults().size() > 0){
                for (int i = 0; i < placesInfo.getResults().size(); i++){
                    int CurrentObject = i;
                    BookingRepository.getTodayBooking(placesInfo.getResults().get(CurrentObject).getPlaceId(),getTodayDate()).addOnCompleteListener(restaurantTask ->{
                        if (restaurantTask.isSuccessful()){
                            Double lat = placesInfo.getResults().get(CurrentObject).getGeometry().getLocation().getLat();
                            Double lng = placesInfo.getResults().get(CurrentObject).getGeometry().getLocation().getLng();
                            String title = placesInfo.getResults().get(CurrentObject).getName();

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(lat,lng));
                            markerOptions.title(title);

                            if (restaurantTask.getResult().isEmpty()){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_unbook_24));
                            }else {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_booked_24));
                            }
                            Marker marker = mMap.addMarker(markerOptions);
                            marker.setTag(placesInfo.getResults().get(CurrentObject).getPlaceId());
                        }
                    });
                }
            }else {
                Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_SHORT).show();
            }
        }else if (results instanceof ArrayList){
            Log.e(TAG, "updateUI : SEARCH_NUMBER : " + ((ArrayList)results).size());
            if (((ArrayList)results).size() > 0){
                for (Object result : ((ArrayList)results)){
                    PlaceDetailsResults placeDetails = ((PlaceDetailsResults) result);
                    BookingRepository.getTodayBooking(placeDetails.getPlaceId(), getTodayDate()).addOnCompleteListener(restaurantTask -> {
                        if (restaurantTask.isSuccessful()){
                            Double lat = placeDetails.getGeometry().getLocation().getLat();
                            Double lng = placeDetails.getGeometry().getLocation().getLng();
                            String title = placeDetails.getName();

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(lat,lng));
                            markerOptions.title(title);
                            if (restaurantTask.getResult().isEmpty()){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_unbook_24));
                            }else {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_booked_24));
                            }
                            Marker marker = mMap.addMarker(markerOptions);
                            marker.setTag(placeDetails.getPlaceId());

                        }

                    });
                }
            }else {
                Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_LONG).show();

            }
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {mMap.setMyLocationEnabled(true);}

    }


    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() != null){
            Log.e(TAG, "onMarkerClick:" + marker.getTag());
            Intent intent = new Intent(getActivity(), PlaceDetailsActivity.class);
            intent.putExtra("PlaceDetailsResults", marker.getTag().toString());
            startActivity(intent);
            return true;

        }else{
            Log.e(TAG, "onClickMarker: ERROR NO TAG" );
            return false;
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (EasyPermissions.hasPermissions(getContext(), perms)){
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                handleNewLocation(location);
                            }else {
                                if (EasyPermissions.hasPermissions(getContext(), perms)){
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                                }
                            }
                        }
                    });
        }else {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_perm_access), PERMS_FINE_COARSE_LOCATION, perms);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
