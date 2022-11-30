package com.example.go4lunch.ui.activity;


import static android.content.ContentValues.TAG;
import static com.example.go4lunch.ui.activity.MainActivity.DEFAULT_NOTIFICATION;
import static com.example.go4lunch.ui.activity.MainActivity.DEFAULT_SEARCH_RADIUS;
import static com.example.go4lunch.ui.activity.MainActivity.DEFAULT_ZOOM;

import com.example.go4lunch.ViewModels.CommunicationViewModel;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.go4lunch.R;
import com.example.go4lunch.repository.UserRepository;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 1000 ;

    private CommunicationViewModel viewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(CommunicationViewModel.class);
        if (!this.isCurrentUserLogged()){
            this.startSignInActivity();
        }else{
            this.viewModel.updateCurrentUserUID(getCurrentUser().getUid());
            launchMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
       this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    /*private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.TwitterBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),      // GOOGLE
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))         // FACEBOOK
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.meal_v3_final)
                        .build(),
                RC_SIGN_IN);
    }*/


     private void startSignInActivity() {

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                        new AuthUI.IdpConfig.TwitterBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.meal_v3_final)
                        .build(),
                RC_SIGN_IN);
    }


    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
              this.createUserInFirestore();
                launchMainActivity();
            } else { // ERRORS
                if (response == null) {
                    Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show();
                    startSignInActivity();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void createUserInFirestore() {
        if(this.getCurrentUser() != null){
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: LOGGED" );
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String userName = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            viewModel.updateCurrentUserUID(uid);
            viewModel.updateCurrentUserRadius(DEFAULT_SEARCH_RADIUS);
            viewModel.updateCurrentUserZoom(DEFAULT_ZOOM);
            UserRepository.createUser(uid, userName,urlPicture, DEFAULT_SEARCH_RADIUS, DEFAULT_ZOOM, DEFAULT_NOTIFICATION).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "ok", e);
                }
            });
        }else {
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: NOT LOGGED");
        }
    }


    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    }


