package com.example.go4lunch.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.go4lunch.models.*;


public class UserRepository {

    private static final String COLLECTION_NAME = "users";



    // Collection Reference

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // Create User

    public static Task<Void> createUser(String uid, String username, String urlPicture, int searchRadius, int defaultZoom, boolean isNotificationOn) {
        User userToCreate = new User(uid, username, urlPicture, searchRadius, defaultZoom, isNotificationOn);
        return UserRepository.getUsersCollection().document(uid).set(userToCreate);
    }

    // Get User

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserRepository.getUsersCollection().document(uid).get();
    }

    // Update User

    public static Task<Void> updateUserSettings(String userId, int zoom, int radius, boolean notification){
        return  UserRepository.getUsersCollection().document(userId)
                .update(
                        "notificationOn",notification,
                        "defaultZoom",zoom,
                        "searchRadius", radius

                );
    }
}
