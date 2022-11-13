package com.example.go4lunch.Utils;

import com.example.go4lunch.models.AutoComplete.AutoCompleteResult;
import com.example.go4lunch.models.PlacesInfo.MapPlacesInfo;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.example.go4lunch.models.PlacesInfo.PlacesDetails.PlaceDetailsResults;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlacesStreams {

    private static PlacesService placeInfo = PlacesService.retrofit.create(PlacesService.class);


    public static Observable<MapPlacesInfo> streamFetchNearbyPlaces(String location, int radius, String type, String key){
        return placeInfo.getNearbyPlace(location, radius, type, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<PlaceDetailsInfo> streamSimpleFetchPlaceInfo(String placeId, String key){
        return placeInfo.getPlaceDetails(placeId, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<AutoCompleteResult> streamFetchAutoComplete(String input, String location, int radius, String key){
        return placeInfo.getPlaceAutoComplete(input, location, radius, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }


    public static Observable<List<PlaceDetailsResults>> streamFetchPlaceInfo(String location, int radius, String type, String key){
        return placeInfo.getNearbyPlace(location, radius, type, key)
                .flatMapIterable(MapPlacesInfo::getResults)
                .flatMap(info -> placeInfo.getPlaceDetails(info.getPlaceId(), key))
                .map(PlaceDetailsInfo::getResult)
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<List<PlaceDetailsResults>> streamFetchAutoCompleteInfo(String input, String location, int radius, String key){
        return placeInfo.getPlaceAutoComplete(input, location, radius, key)
                .flatMapIterable(AutoCompleteResult::getPredictions)
                .flatMap(info -> placeInfo.getPlaceDetails(info.getPlaceId(), key))
                .map(PlaceDetailsInfo::getResult)
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }









}
