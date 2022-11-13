package com.example.go4lunch.ui.activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.databinding.ActivityMainBinding;
import com.example.go4lunch.databinding.NavigationHeaderBinding;
import com.example.go4lunch.repository.UserRepository;
import com.example.go4lunch.ui.fragments.ListFragment;
import com.example.go4lunch.ui.fragments.WorkmatesFragment;
import com.example.go4lunch.ui.fragments.MapFragment;
import com.example.go4lunch.ViewModels.CommunicationViewModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;


import java.util.ArrayList;
import java.util.List;

import java.util.Objects;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    protected NavigationHeaderBinding headerBinding;

    protected CommunicationViewModel viewModel;


    private DrawerLayout drawer;
    private Toolbar toolbar;


    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;
    private boolean isLocationPermissionGranted = false;
    private boolean isRecordPermissionGranted = false;




    public static final int TITLE_HUNGRY = R.string.hungry;


    public static final int  FRAGMENT_MAPVIEW = 0;
    public static final int  FRAGMENT_LISTVIEW = 1;
    public static final int  FRAGMENT_MATES = 2;

    //Default data to create user
    public static final int DEFAULT_ZOOM = 13;
    public static final int DEFAULT_SEARCH_RADIUS = 1000;
    public static final boolean DEFAULT_NOTIFICATION = false;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Fragment fragment = new MapFragment();
        getSupportFragmentManager().
                beginTransaction().replace(R.id.fragment_view,fragment)
                .commit();
        initDrawer();
       // initToolBar();
        initClickListener();
        configureNavigationView();
        configureBottomView();
        updateUIWhenCreating();
        //retrieveCurrentUser();
        showFragment(FRAGMENT_MAPVIEW);

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

            if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){

                isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));

            }

            if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null){

                isLocationPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));

            }

            if (result.get(Manifest.permission.RECORD_AUDIO) != null){

                isRecordPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));

            }

        });

        requestPermission();
    }

    private void retrieveCurrentUser() {
        viewModel = new ViewModelProvider(this).get(CommunicationViewModel.class);
        this.viewModel.updateCurrentUserUID(Objects.requireNonNull(getCurrentUser()).getUid());
        UserRepository.getUsersCollection().document(getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot value, @javax.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("MAIN_ACTIVITY", "Listen failed.", error);
                    return;
                }

                if(value != null && value.exists()) {
                   Log.e("MAIN_ACTIVITY", "Current data:" + value.getData());
                   viewModel.updateCurrentZoom(Integer.parseInt(value.getData().get("defaultZoom").toString()));
                   viewModel.updateCurrentRadius(Integer.parseInt(value.getData().get("searchRadius").toString()));
                } else {
                    Log.e("MAIN_ACTIVITY", "Current data: null" );
                }
            }
        });
    }

    private void updateUIWhenCreating() {
        if (this.getCurrentUser() != null){
            View header = binding.activityMainNavView.getHeaderView(0);
            ImageView imageView = headerBinding.drawerImage;
            ImageView imageView1 = headerBinding.drawerImageBk;
            TextView textView = headerBinding.drawerName;
            TextView textView1 = headerBinding.drawerEmail;

            Glide.with(this)
                    .load(R.drawable.header)
                    //.apply(RequestOptions.bitmapTransform(new BlurMaskFilter()))
                    .into(imageView1);


            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() !=null ){
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.centerCropTransform())
                        .into(imageView);
            }

            //Get email from Firebase
            String name = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();


            textView.setText(name);
            textView1.setText(email);



        }


    }

    private void requestPermission(){

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        isRecordPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<String>();

        if (!isReadPermissionGranted){

            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }

        if (!isLocationPermissionGranted){

            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);

        }

        if (!isRecordPermissionGranted){

            permissionRequest.add(Manifest.permission.RECORD_AUDIO);

        }

        if (!permissionRequest.isEmpty()){

            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));

        }


    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
           super.onBackPressed();
        }
    }



     private void initDrawer(){
       DrawerLayout mDrawerLayout = binding.activityMainDrawerLayout;
         Toolbar mToolbar = binding.simpleToolbar;
         setSupportActionBar(mToolbar);
         Objects.requireNonNull(getSupportActionBar()).setTitle(TITLE_HUNGRY);
        configureDrawerLayout (mDrawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }





    private void initClickListener() {
        binding.bottomNavigation.setOnClickListener(v -> {
        });

        binding.activityMainNavView.setOnClickListener(v -> {

        });

    }

    private void configureDrawerLayout(DrawerLayout mDrawerLayout) {
    }

    private void showFragment(int fragmentIdentifier){
        Fragment newFragment = new Fragment();
        switch (fragmentIdentifier){
            case MainActivity.FRAGMENT_MAPVIEW:
                newFragment = MapFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_MAPVIEW );
                break;
            case MainActivity.FRAGMENT_LISTVIEW:
                newFragment = ListFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_LISTVIEW );
                break;
            case MainActivity.FRAGMENT_MATES:
                newFragment = WorkmatesFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_MATES );
                break;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_view, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }






    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle Navigation Item Click
        int id = item.getItemId();
        if (id == R.id.drawer_logout){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you Want To Exit ?");
            builder.setCancelable(true);

            builder.setNegativeButton("NO", (dialog, which) -> dialog.cancel());
            builder.setPositiveButton("YES", (dialog, which) -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        return true;
    }

    private void configureNavigationView() {
        NavigationView navigationView = binding.activityMainNavView;
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureBottomView(){
       binding.bottomNavigation .setOnItemSelectedListener(
               new BottomNavigationView.OnNavigationItemSelectedListener() {
                   @Override
                   public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                       switch (menuItem.getItemId()) {

                           case R.id.map:
                               //  getSupportActionBar().setTitle(TITLE_HUNGRY);
                               showFragment(FRAGMENT_MAPVIEW);
                               break;
                           case R.id.list:
                               //getSupportActionBar().setTitle(TITLE_HUNGRY);
                               showFragment(FRAGMENT_LISTVIEW);
                               break;
                           case R.id.mates:
                               //getSupportActionBar().setTitle(TITLE_WORKMATES);
                               showFragment(FRAGMENT_MATES);
                               break;
                   }
                       return true;
               }
               });
    }
}
