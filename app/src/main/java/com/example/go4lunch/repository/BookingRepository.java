package com.example.go4lunch.repository;

import com.example.go4lunch.models.Booking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BookingRepository {

    private static final String COLLECTION_NAME = "booking";
    private static final String COLLECTION_LIKE_NAME = "restaurantLiked";


    // COLLECTION REFERENCE
    public static CollectionReference getBookingCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static CollectionReference getLikedCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_LIKE_NAME);
    }

    // CREATE

    public static Task<DocumentReference> createBooking(String bookingDate, String userId, String restaurantId, String restaurantName) {
        Booking bookingToCreate = new Booking(bookingDate, userId, restaurantId, restaurantName);
        return BookingRepository.getBookingCollection().add(bookingToCreate);
    }


    public static Task<Void> createLike(String restaurantId, String userId){
        Map<String,Object> user = new HashMap<>();
        user.put(userId,true);
        return BookingRepository.getLikedCollection().document(restaurantId).set(user, SetOptions.merge());
    }





    /// ****** la difference entre QuerySnapshot et DocumentSnapshot ****** ///






    // GET

    public static Task<QuerySnapshot> getBooking(String userId, String reservationDate){
        return BookingRepository.getBookingCollection().whereEqualTo("userId", userId).whereEqualTo("reservationDate", reservationDate).get();
    }

    public static Task<QuerySnapshot> getTodayBooking(String restaurantId, String bookingDate){
        return BookingRepository.getBookingCollection().whereEqualTo("restaurantId", restaurantId).whereEqualTo("bookingDate", bookingDate).get();
    }

    public static Task<QuerySnapshot> getAllLikeByUserId(String userId){
        return BookingRepository.getLikedCollection().whereEqualTo(userId,true).get();
    }



    public static Task<DocumentSnapshot> getLikeForThisRestaurant(String restaurantId){
        return BookingRepository.getLikedCollection().document(restaurantId).get();
    }

    // DELETE
    public static Task<Void> deleteBooking(String reservationId){
        return BookingRepository.getBookingCollection().document(reservationId).delete();
    }

    public static Boolean deleteLike(String restaurantId, String userId){
        BookingRepository.getLikeForThisRestaurant(restaurantId).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                Map<String,Object> update = new HashMap<>();
                update.put(userId, FieldValue.delete());
                BookingRepository.getLikedCollection().document(restaurantId).update(update);
            }
        });
        return true;
    }

}
