package com.example.go4lunch.Utils;



import com.example.go4lunch.models.AutoComplete.AutoCompleteResult;
import com.example.go4lunch.models.PlacesInfo.MapPlacesInfo;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {

    @GET("nearbySearch/json?")
    Observable<MapPlacesInfo> getNearbyPlace(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    @GET("details/json?")
    Observable<PlaceDetailsInfo> getPlaceDetails(@Query("placeId") String placeId, @Query("key") String key);

    @GET("autocomplete/json?strictBounds&types=establishment")
    Observable<AutoCompleteResult> getPlaceAutoComplete(@Query("input") String input, @Query("location") String location, @Query("radius") int radius, @Query("key") String key);


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)).build())
            .build();
}
