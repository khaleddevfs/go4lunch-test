package com.example.go4lunch.models;

public class Reservation {

    private String reservationDate;
    private String userId;
    private String restaurantId;
    private String restaurantName;

    public Reservation (String reservationDate, String userId, String restaurantId, String restaurantName) {

        this.reservationDate = reservationDate;

        this.userId = userId;

        this.restaurantId = restaurantId;

        this.restaurantName = restaurantName;

    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {this.reservationDate = reservationDate;}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
