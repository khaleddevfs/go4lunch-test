package com.example.go4lunch.models;

public class Booking {

    private String bookingDate;
    private String userId;
    private String restaurantId;
    private String restaurantName;

    public Booking (String reservationDate, String userId, String restaurantId, String restaurantName) {

        this.bookingDate = reservationDate;

        this.userId = userId;

        this.restaurantId = restaurantId;

        this.restaurantName = restaurantName;

    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {this.bookingDate = bookingDate;}

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
