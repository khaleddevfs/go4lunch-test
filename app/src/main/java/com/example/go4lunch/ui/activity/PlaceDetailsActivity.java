package com.example.go4lunch.ui.activity;


import static com.example.go4lunch.views.RestaurantViewHolder.MAX_RATING;
import static com.example.go4lunch.views.RestaurantViewHolder.MAX_STAR;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;



import com.example.go4lunch.R;
import com.example.go4lunch.Utils.PlacesStreams;
import com.example.go4lunch.databinding.ActivityPlaceDetailsBinding;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.example.go4lunch.models.User;
import com.example.go4lunch.repository.BookingRepository;
import com.example.go4lunch.repository.UserRepository;
import com.example.go4lunch.ui.fragments.MapFragment;
import com.example.go4lunch.views.PlaceDetailsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class PlaceDetailsActivity extends BaseActivity implements View.OnClickListener {

    private ActivityPlaceDetailsBinding binding;
    private PlaceDetailsResults detailsResults;

    private Disposable mDisposable;

    private List<User> mDetailUsers;
    private PlaceDetailsAdapter adapter;
    private RequestManager mGlide;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        this.configureRecyclerView();
        this.retrieveObject();
        this.setFloatingActionButtonOnClickListener();
        this.configureButtonClickListener();
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
        disposeWhenDestroy();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }


    private void disposeWhenDestroy(){
        if(this.mDisposable != null && !this.mDisposable.isDisposed())
            this.mDisposable.dispose();
    }

    private void configureRecyclerView(){
        this.mDetailUsers = new ArrayList<>();
        this.adapter = new PlaceDetailsAdapter(this.mDetailUsers);
        this.binding.restaurantRecyclerView.setAdapter(this.adapter);
        this.binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void retrieveObject(){
        String result = getIntent().getStringExtra("PlaceDetailResult");
        Log.e("TAG", "retrieveObject: " + result );
        this.executeHttpRequestWithRetrofit(result);
    }

    private void executeHttpRequestWithRetrofit(String placeId) {
        this.mDisposable = PlacesStreams.streamSimpleFetchPlaceInfo(placeId, MapFragment.API_KEY).subscribeWith(createObserver());

    }

    private <T> DisposableObserver<T> createObserver() {
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof PlaceDetailsInfo){
                    detailsResults = ((PlaceDetailsInfo) t).getResult();
                    updateUI(((PlaceDetailsResults) t));
                }else {
                    Log.e("TAG", "onNext: " + t.getClass() );

                }
            }

            @Override
            public void onError(Throwable e) {
                Exception exception = new Exception(e);
            }

            @Override
            public void onComplete() {

            }
        };
    }



    private void updateUI(PlaceDetailsResults results) {
        if (results != null){
            if (getCurrentUser() !=null){
                this.checkIfUserAlreadyBookedRestaurant(getCurrentUser().getUid(), detailsResults.placeId, detailsResults.getName(), false);
                this.checkIfUserLikeThisRestaurant();
            }else {
                binding.likeButton.setText(R.string.restaurant_item_like);
                this.displayFab((R.drawable.baseline_check_circle_black_24), getResources().getColor(R.color.colorGreen));
                Toast.makeText(this, getResources().getString(R.string.restaurant_error_retrieving_info), Toast.LENGTH_SHORT).show();
            }

            RequestManager glide = mGlide;
            mGlide= glide;

            if (results.getPhotos() != null && !results.getPhotos().isEmpty()){
                Glide.with(this)
                        .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference=" + results.getPhotos().get(0).getPhotoReference() + "&key=" + MapFragment.API_KEY)
                        .apply(RequestOptions.centerCropTransform())
                        .into(binding.restImg);
            }else{
                binding.restImg.setImageResource(R.drawable.ic_no_image_available);
            }
            binding.restaurantName.setText(results.getName());
            binding.restaurantAddress.setText(results.getVicinity());
            this.displayRating(results);
            this.updateUIWithRecyclerView(results.placeId);
        }
    }



    private void checkIfUserLikeThisRestaurant() {
        BookingRepository.getAllLikeByUserId(getCurrentUser().getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.e("TAG", "checkIfUserLikeThisRestaurant: " + task.getResult().getDocuments());
                if (task.getResult().isEmpty()){
                    binding.likeButton.setText(getResources().getString(R.string.restaurant_item_like));
            }else{
                    for (DocumentSnapshot restaurant : task.getResult()){
                        if(restaurant.getId().equals(detailsResults.getPlaceId())){
                            binding.likeButton.setText(getResources().getString(R.string.restaurant_item_dislike));
                            break;
                        }else {
                            binding.likeButton.setText(getResources().getString(R.string.restaurant_item_like));
                        }
                    }
                }
            }
        });
    }


    private void setFloatingActionButtonOnClickListener(){
        binding.floatingButton.setOnClickListener(view -> bookThisRestaurant());
    }

    private void likeThisRestaurant() {
        if (detailsResults != null && getCurrentUser() != null) {
            BookingRepository.createLike(detailsResults.getPlaceId(), getCurrentUser().getUid()).addOnCompleteListener(likeTask -> {
                if (likeTask.isSuccessful()) {
                    Toast.makeText(this, getResources().getString(R.string.restaurant_like_ok), Toast.LENGTH_SHORT).show();
                    binding.likeButton.setText(getResources().getString(R.string.restaurant_item_dislike));
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.restaurant_like_ko), Toast.LENGTH_SHORT).show();
        }
    }

    private void dislikeThisRestaurant() {
        if (detailsResults != null && getCurrentUser() != null){
            BookingRepository.deleteLike(detailsResults.getPlaceId(), getCurrentUser().getUid());
            binding.likeButton.setText(getResources().getString(R.string.restaurant_item_like));
            Toast.makeText(this, getResources().getString(R.string.restaurant_dislike_ok), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.restaurant_like_ko), Toast.LENGTH_SHORT).show();
        }
    }


    private void bookThisRestaurant() {
        if (this.getCurrentUser() != null){
            String userId = getCurrentUser().getUid();
            String restaurantId = detailsResults.getPlaceId();
            String restaurantName = detailsResults.getName();
            this.checkIfUserAlreadyBookedRestaurant(userId, restaurantId, restaurantName , true);
        }
    }

    private void checkIfUserAlreadyBookedRestaurant(String userId, String restaurantId, String restaurantName, boolean tryingToBook) {
        BookingRepository.getBooking(userId, getTodayDate()).addOnCompleteListener(restaurantTask->{
            if (restaurantTask.isSuccessful()){
                if (restaurantTask.getResult().size() == 1 ){

                    // already booked a restaurant today

                    for(QueryDocumentSnapshot restaurant : restaurantTask.getResult()){
                        if (restaurant.getData().get("restaurantName").equals(restaurantName)){
                            this.displayFab((R.drawable.baseline_clear_black_24),getResources().getColor(R.color.colorError));
                            if (tryingToBook){
                                this.manageBooking(userId, restaurantId, restaurantName, restaurant.getId(), false, false, true );
                                Toast.makeText(this, getResources().getString(R.string.restaurant_cancel_booking), Toast.LENGTH_SHORT).show();
                            }

                            // trying to book an other restaurant for today

                        } else {
                            this.displayFab((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
                            if (tryingToBook){
                                this.manageBooking(userId, restaurantId, restaurantName, restaurant.getId(), false, true, false);
                                Toast.makeText(this, getResources().getString(R.string.restaurant_change_booking), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                    // No restaurant booked for this user today
                } else {
                    this.displayFab((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
                    if (tryingToBook){
                        this.manageBooking(userId, restaurantId, restaurantName, null, true, false, false);
                        Toast.makeText(this, getResources().getString(R.string.restaurant_new_booking), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    private void manageBooking(String userId, String restaurantId, String restaurantName, String bookingId, boolean toCreate, boolean toUpdate, boolean toDelete) {
        if (toUpdate){
            BookingRepository.deleteBooking(bookingId);
            BookingRepository.createBooking(this.getTodayDate(), userId, restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
            this.displayFab((R.drawable.baseline_clear_black_24), getResources().getColor(R.color.colorError));
        } else if (toCreate){
            BookingRepository.createBooking(this.getTodayDate(), userId, restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
            this.displayFab((R.drawable.baseline_clear_black_24), getResources().getColor(R.color.colorError));
        } else if (toDelete){
            BookingRepository.deleteBooking(bookingId);
            this.displayFab((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
        }

        updateUIWithRecyclerView(detailsResults.getPlaceId());

    }

    private void updateUIWithRecyclerView(String placeId) {
        mDetailUsers.clear();
        BookingRepository.getTodayBooking(placeId, getTodayDate()).addOnCompleteListener(restaurantTask ->{
            if(restaurantTask.isSuccessful()){
                if(restaurantTask.getResult().isEmpty()){
                    adapter.notifyDataSetChanged();
                }else {
                    for(QueryDocumentSnapshot restaurant : restaurantTask.getResult()){
                        Log.e("TAG", "DETAIL_ACTIVITY | Restaurant : " + restaurant.getData() );
                        UserRepository.getUser(restaurant.getData().get("userId").toString()).addOnCompleteListener(userTask->{
                            if(userTask.isSuccessful()){
                                Log.e("TAG", "DETAIL_ACTIVITY | User : " + userTask.getResult() );
                                String uid = userTask.getResult().getData().get("uid").toString();
                                String userName = userTask.getResult().getData().get("userName").toString();
                                String urlPicture = userTask.getResult().getData().get("urlPicture").toString();
                                User userToAdd = new User(uid, userName, urlPicture, MainActivity.DEFAULT_SEARCH_RADIUS, MainActivity.DEFAULT_ZOOM, false);
                                mDetailUsers.add(userToAdd);
                            }
                            adapter.notifyDataSetChanged();
                        });

                    }
                }
            }
        });
    }

    private void displayFab(int icon, int color) {
        Drawable mDrawable = ContextCompat.getDrawable(getBaseContext(), icon).mutate();
        mDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        binding.floatingButton.setBackgroundDrawable(mDrawable);
    }

    private void displayRating(PlaceDetailsResults results){
        if (results.getRating() != null){
            double googleRating = results.getRating();
            double rating = googleRating / MAX_RATING * MAX_STAR;
            this.binding.ratingBar.setRating((float) rating);
            this.binding.ratingBar.setVisibility(View.VISIBLE);
        } else {
            this.binding.ratingBar.setVisibility(View.GONE);
        }
    }

    private void configureButtonClickListener() {
        binding.callButton.setOnClickListener(this);
        binding.websiteButton.setOnClickListener(this);
        binding.likeButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.callButton:
                if (detailsResults.getFormattedPhoneNumber() != null ){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + detailsResults.getFormattedPhoneNumber()));
                    startActivity(intent);
                }else{
                    Toast.makeText(this, getResources().getString(R.string.restaurant_detail_no_phone) ,Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.likeButton:
                if (binding.likeButton.getText().equals(getResources().getString(R.string.restaurant_item_like))){
                    this.likeThisRestaurant();
                }else{
                    this.dislikeThisRestaurant();
                }
                break;

            case R.id.websiteButton:
                if (detailsResults.getWebsite() != null){
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra("Website",  detailsResults.getWebsite());
                    startActivity(intent);
                }else{
                    Toast.makeText(this, getResources().getString(R.string.restaurant_detail_no_website), Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}