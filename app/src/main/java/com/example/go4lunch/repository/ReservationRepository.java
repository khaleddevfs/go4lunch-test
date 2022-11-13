package com.example.go4lunch.repository;

import com.example.go4lunch.models.Reservation;
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

public class ReservationRepository {

    private static final String COLLECTION_NAME = "reservation";
    private static final String COLLECTION_CHOICE_NAME = "restaurantChoice";


    // COLLECTION REFERENCE
    public static CollectionReference getReservationCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static CollectionReference getChoiceCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_CHOICE_NAME);
    }

    // CREATE

    public static Task<DocumentReference> createReservation(String reservationDate, String userId, String restaurantId, String restaurantName) {
        Reservation reservationToCreate = new Reservation(reservationDate, userId, restaurantId, restaurantName);
        return ReservationRepository.getReservationCollection().add(reservationToCreate);
    }


    public static Task<Void> createChoice(String restaurantId, String userId){
        Map<String,Object> user = new HashMap<>();
        user.put(userId,true);
        return ReservationRepository.getChoiceCollection().document(restaurantId).set(user, SetOptions.merge());
    }





    /// ****** la difference entre QuerySnapshot et DocumentSnapshot ****** ///






    // GET

    public static Task<QuerySnapshot> getReservation(String userId, String reservationDate){
        return ReservationRepository.getReservationCollection().whereEqualTo("userId", userId).whereEqualTo("reservationDate", reservationDate).get();
    }

    public static Task<QuerySnapshot> getTodayBooking(String restaurantId, String bookingDate){
        return ReservationRepository.getReservationCollection().whereEqualTo("restaurantId", restaurantId).whereEqualTo("bookingDate", bookingDate).get();
    }



    public static Task<DocumentSnapshot> getChoiceForThisRestaurant(String restaurantId){
        return ReservationRepository.getChoiceCollection().document(restaurantId).get();
    }

    // DELETE
    public static Task<Void> deleteReservation(String reservationId){
        return ReservationRepository.getReservationCollection().document(reservationId).delete();
    }

    public static Boolean deleteChoice(String restaurantId, String userId){
        ReservationRepository.getChoiceForThisRestaurant(restaurantId).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                Map<String,Object> update = new HashMap<>();
                update.put(userId, FieldValue.delete());
                ReservationRepository.getChoiceCollection().document(restaurantId).update(update);
            }
        });
        return true;
    }

}
