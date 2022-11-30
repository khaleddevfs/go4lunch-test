package com.example.go4lunch.ui.activity;


import android.Manifest;
import android.app.AlertDialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.databinding.ActivityMainBinding;
import com.example.go4lunch.repository.BookingRepository;
import com.example.go4lunch.repository.UserRepository;
import com.example.go4lunch.ui.fragments.ListFragment;
import com.example.go4lunch.ui.fragments.WorkmatesFragment;
import com.example.go4lunch.ui.fragments.MapFragment;
import com.example.go4lunch.ViewModels.CommunicationViewModel;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;



public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIGN_OUT_TASK = 10;

    private ActivityMainBinding binding;
    protected CommunicationViewModel viewModel;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;


    private boolean isReadPermissionGranted = false;
    private boolean isLocationPermissionGranted = false;
    private boolean isRecordPermissionGranted = false;

    public static final int ACTIVITY_SETTINGS = 0;
    public static final int ACTIVITY_PLACE_DETAIL = 1 ;
    public static final int ACTIVITY_LOGIN = 2 ;

    public static final int  FRAGMENT_MAPVIEW = 0;
    public static final int  FRAGMENT_LISTVIEW = 1;
    public static final int  FRAGMENT_MATES = 2;

    //Default data to create user
    public static final int DEFAULT_ZOOM = 13;
    public static final int DEFAULT_SEARCH_RADIUS = 1000;
    public static final boolean DEFAULT_NOTIFICATION = false;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        this.initDrawer();
        //this.configureDrawerLayout();
        //this.initToolBar();
        this.configureNavigationView();
        this.configureBottomView();
        this. updateUIWhenCreating();
       // this.retrieveCurrentUser();
        this.showFragment(FRAGMENT_MAPVIEW);


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

    private void initToolBar() {
        setSupportActionBar(binding.simpleToolbar);
        getSupportActionBar().setTitle(R.string.hungry);
    }

    private void retrieveCurrentUser() {
        viewModel = new ViewModelProvider(this).get(CommunicationViewModel.class);
        this.viewModel.updateCurrentUserUID(getCurrentUser().getUid());
        UserRepository.getUsersCollection().document(getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot value, @javax.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("MAIN_ACTIVITY", "Listen failed.", error);
                    return;
                }

                if(value != null && value.exists()) {
                   Log.e("MAIN_ACTIVITY", "Current data:" + value.getData());
                   viewModel.updateCurrentUserZoom(Integer.parseInt(value.getData().get("defaultZoom").toString()));
                   viewModel.updateCurrentUserRadius(Integer.parseInt(value.getData().get("searchRadius").toString()));
                } else {
                    Log.e("MAIN_ACTIVITY", "Current data: null" );
                }
            }
        });
    }

    private void updateUIWhenCreating() {
        if (this.getCurrentUser() != null){
            View header = binding.activityMainNavView.getHeaderView(0);
            ImageView imageView = header.findViewById(R.id.drawer_image);
            ImageView imageView1 = header.findViewById(R.id.drawer_image_bk);//ImageView imageView1 = headerBinding.drawerImageBk;
            TextView textView = header.findViewById(R.id.drawer_name);//TextView textView = headerBinding.drawerName;
            TextView textView1 = header.findViewById(R.id.drawer_email);//TextView textView1 = headerBinding.drawerEmail;

            Glide.with(this)
                    .load(R.drawable.header)
                    .apply(RequestOptions.bitmapTransform((Transformation<Bitmap>) new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)))//modif
                    .into(imageView1);


            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() !=null ){
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
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
        if (this.binding.activityMainDrawerLayout.isDrawerOpen(GravityCompat.START)){
           this.binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
           super.onBackPressed();
        }
    }



     private void initDrawer(){
       DrawerLayout mDrawerLayout = binding.activityMainDrawerLayout;
         Toolbar mToolbar = binding.simpleToolbar;
         setSupportActionBar(mToolbar);
         getSupportActionBar().setTitle(R.string.hungry);
        configureDrawerLayout (mDrawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureDrawerLayout(DrawerLayout mDrawerLayout) {
        ActionBarDrawerToggle barDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, binding.simpleToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(barDrawerToggle);
        barDrawerToggle.syncState();
    }





   /* private void initClickListener() {

        binding.bottomNavigation.setOnClickListener(OnSuccessListener);

        binding.activityMainNavView.setOnClickListener(v -> {

        });

    }*/



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

    private void showActivity(int activityIdentifier){
        switch (activityIdentifier){
            case ACTIVITY_SETTINGS:
                launchActivity(SettingsActivity.class,null);
                break;
            case ACTIVITY_PLACE_DETAIL:
                BookingRepository.getBooking(getCurrentUser().getUid(),getTodayDate()).addOnCompleteListener(bookingTask -> {
                    if (bookingTask.isSuccessful()){
                        if (bookingTask.getResult().isEmpty()){
                            Toast.makeText(this, getResources().getString(R.string.drawer_no_restaurant_booked), Toast.LENGTH_SHORT).show();
                        }else{
                            Map<String,Object> extra = new HashMap<>();
                            for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                                extra.put("PlaceDetailResult",booking.getData().get("restaurantId"));
                            }
                            launchActivity(PlaceDetailsActivity.class,extra);
                        }

                    }
                });
                break;
            case ACTIVITY_LOGIN:
                launchActivity(LoginActivity.class,null);
                break;
        }
    }

    private void launchActivity(Class mClass, Map<String,Object> info){
        Intent intent = new Intent(this, mClass);
        if (info != null){
            for (Object key : info.keySet()) {
                String mKey = (String)key;
                String value = (String) info.get(key);
                intent.putExtra(mKey, value);
            }
        }
        startActivity(intent);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_lunch:
                showActivity(ACTIVITY_PLACE_DETAIL);
                break;

            case R.id.drawer_settings:
                showActivity(ACTIVITY_SETTINGS);
                break;

            case R.id.drawer_logout:
                this.signOutUserFromFirebase();
                break;

            default:
                break;
        }
        binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);

        return true;



    }

    private void signOutUserFromFirebase() {
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



        /*AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));*/

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
                               getSupportActionBar().setTitle(R.string.hungry);
                               showFragment(FRAGMENT_MAPVIEW);
                               break;
                           case R.id.list:
                               getSupportActionBar().setTitle(R.string.hungry);
                               showFragment(FRAGMENT_LISTVIEW);
                               break;
                           case R.id.mates:
                               getSupportActionBar().setTitle(R.string.available);
                               showFragment(FRAGMENT_MATES);
                               break;
                   }
                       return true;
               }
               });
    }


    // Create OnCompleteListener called after tasks ended    ADDED
   private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                        showActivity(ACTIVITY_LOGIN);
                        break;
                    default:
                        break;
                }
            }
        };
    }

}
