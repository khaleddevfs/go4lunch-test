package com.example.go4lunch.views;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.databinding.FragmentListItemsBinding;
import com.example.go4lunch.models.PlacesInfo.Location;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.example.go4lunch.repository.ReservationRepository;
import com.example.go4lunch.ui.fragments.MapFragment;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private FragmentListItemsBinding binding;

    private static final String OPEN = "OPEN";
    private static final String CLOSED = "CLOSED";
    private static final String CLOSING_SOON = "CLOSING_SOON";
    private static final String OPENING_HOURS_NOT_KNOW = "OPENING_HOURS_NOT_KNOW";

    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/photo";
    public static final int MAX_WIDTH = 75;
    public static final int MAX_HEIGHT = 75;
    public static final int MAX_HEIGHT_LARGE = 250;

    public static final double MAX_RATING = 5;
    public static final double MAX_STAR = 3;


    private float[] distanceResults = new float[3];

    public RestaurantViewHolder(View itemView) {
        super(itemView);
    }

    public void updateWithData(PlaceDetailsResults results, String userLocation){
        RequestManager glide = Glide.with(itemView);

        // Display Name
        binding.itemTextviewName.setText(results.getName());

        // Display Distance
        getDistance(userLocation,results.getGeometry().getLocation());
        String distance = Integer.toString(Math.round(distanceResults[0]));
        binding.itemTextviewDistance.setText(itemView.getResources().getString(R.string.list_unit_distance, distance));

        // Display Address
        binding.itemTextviewAddress.setText(getAddress(results));

        // Display Rating
        displayRating(results);

        // Display Mates number & Icon
        ReservationRepository.getTodayBooking(results.getPlaceId(),getTodayDate()).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                if (restaurantTask.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : restaurantTask.getResult()) {
                        Log.e("TAG", document.getId() + " => " + document.getData());
                    }
                    binding.itemTextviewMates.setText(itemView.getResources().getString(R.string.restaurant_mates_number,restaurantTask.getResult().size()));
                    binding.itemImageviewMates.setImageResource(R.drawable.baseline_person_outline_black_36);
                    binding.itemImageviewMates.setVisibility(View.VISIBLE);
                }else{
                    binding.itemTextviewMates.setText("");
                    binding.itemImageviewMates.setVisibility(View.GONE);
                }
            }
        });


        // Display Opening Hours
        if (results.getOpeningHours() != null){
            if (results.getOpeningHours().getOpenNow().toString().equals("false")){
                displayOpeningHour(CLOSED,null);
            }else{
                getOpeningHoursInfo(results);
            }
        }else{
            displayOpeningHour(OPENING_HOURS_NOT_KNOW,null);
        }



        // Display Photos
        if (!(results.getPhotos() == null)){
            if (!(results.getPhotos().isEmpty())){
                glide.load(BASE_URL+"?maxwidth="+MAX_WIDTH+"&maxheight="+MAX_HEIGHT+"&photoreference="+results.getPhotos().get(0).getPhotoReference()+"&key="+ MapFragment.API_KEY).into(binding.itemImageviewMainPic);
            }
        }else{
            glide.load(R.drawable.ic_no_image_available).apply(RequestOptions.centerCropTransform()).into(binding.itemImageviewMainPic);
        }
    }

    private void displayRating(PlaceDetailsResults results){
        if (results.getRating() != null){
            double googleRating = results.getRating();
            double rating = googleRating / MAX_RATING * MAX_STAR;
            binding.itemRatingBar.setRating((float)rating);
            binding.itemRatingBar.setVisibility(View.VISIBLE);
        }else{
            binding.itemRatingBar.setVisibility(View.GONE);
        }
    }

    private String getAddress(PlaceDetailsResults results){
        String addressNumber;
        String addressStreet;

        switch (results.getAddressComponents().get(0).getTypes().get(0)) {
            case "street_number":
                addressNumber = results.getAddressComponents().get(0).getLongName();
                if (results.getAddressComponents().get(1).getTypes().get(0).equals("route")) {
                    addressStreet = results.getAddressComponents().get(1).getLongName();
                    return addressNumber + " " + addressStreet;
                } else {
                    return results.getVicinity();
                }
            case "route":
                addressStreet = results.getAddressComponents().get(0).getLongName();
                return addressStreet;
            default:
                return results.getVicinity();
        }
    }

    private void getDistance(String startLocation, Location endLocation){
        String[] separatedStart = startLocation.split(",");
        double startLatitude = Double.parseDouble(separatedStart[0]);
        double startLongitude = Double.parseDouble(separatedStart[1]);
        double endLatitude = endLocation.getLat();
        double endLongitude = endLocation.getLng();
        android.location.Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude,distanceResults);
    }

    private void getOpeningHoursInfo(PlaceDetailsResults results){
        int daysArray[] = {0,1,2,3,4,5,6};

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minOfDay = calendar.get(Calendar.MINUTE);
        if (minOfDay < 10){minOfDay = '0'+minOfDay;}
        String currentHourString = Integer.toString(hourOfDay)+Integer.toString(minOfDay);
        int currentHour = Integer.parseInt(currentHourString);

        for (int i=0;i < results.getOpeningHours().getPeriods().size();i++){
            if (results.getOpeningHours().getPeriods().get(i).getOpen().getDay() == daysArray[day]){
                String closeHour = results.getOpeningHours().getPeriods().get(i).getClose().getTime();
                if (currentHour < Integer.parseInt(closeHour) || daysArray[day] < results.getOpeningHours().getPeriods().get(i).getClose().getDay()){
                    int timeDifference = Integer.parseInt(closeHour) - currentHour;
                    //Log.e("TAG", "RestaurantName : " + results.getName() + " | CurrentHour : " + currentHour + " | CloseHour : " + Integer.parseInt(closeHour) + " | TimeDifference : " + timeDifference);
                    if (timeDifference <= 30 && daysArray[day] == results.getOpeningHours().getPeriods().get(i).getClose().getDay()){
                        displayOpeningHour(CLOSING_SOON, closeHour);
                    }else{
                        displayOpeningHour(OPEN,results.getOpeningHours().getPeriods().get(i).getClose().getTime());
                    }
                    break;
                }
            }
        }
    }

    private void displayOpeningHour(String type, String hour){
        switch (type){
            case OPEN:
                binding.itemTextviewOpening.setText(itemView.getResources().getString(R.string.restaurant_open_until,formatTime(itemView.getContext(),hour)));
                binding.itemTextviewOpening.setTextColor(itemView.getContext().getResources().getColor(R.color.colorOk));
                binding.itemTextviewOpening.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                break;
            case CLOSED:
                binding.itemTextviewOpening.setText(R.string.restaurant_closed);
                binding.itemTextviewOpening.setTextColor(itemView.getContext().getResources().getColor(R.color.colorError));
                binding.itemTextviewOpening.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                break;
            case CLOSING_SOON:
                binding.itemTextviewOpening.setText(itemView.getResources().getString(R.string.restaurant_closing_soon,formatTime(itemView.getContext(),hour)));
                binding.itemTextviewOpening.setTextColor(itemView.getContext().getResources().getColor(R.color.colorWarning));
                binding.itemTextviewOpening.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                break;
            case OPENING_HOURS_NOT_KNOW:
                binding.itemTextviewOpening.setText(R.string.restaurant_opening_not_know);
                binding.itemTextviewOpening.setTextColor(itemView.getContext().getResources().getColor(R.color.colorWarning));
                binding.itemTextviewOpening.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                break;
        }
    }

    private String formatTime(Context context, String date) {
        date = date.substring(0,2) + ":" +date.substring(2);
        try {
            Date date1 = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(date);
            if (android.text.format.DateFormat.is24HourFormat(context)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return dateFormat.format(date1);
            }else{
                SimpleDateFormat dateFormat = new SimpleDateFormat("h.mm a", Locale.getDefault());
                return dateFormat.format(date1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }




}
