package com.gameplatform.model;

import java.time.LocalDate;

public class Review {
    private final int reviewID;
    private final int rating;
    private final String description;
    private final LocalDate reviewDate;
    private final String userID;

    public Review(int reviewID, int rating, String description,
                  LocalDate reviewDate, String userID) {
        this.reviewID = reviewID;
        this.rating = rating;
        this.description = description;
        this.reviewDate = reviewDate;
        this.userID = userID;
    }

    public int getReviewID()         { return reviewID; }
    public int getRating()           { return rating; }
    public String getDescription()   { return description; }
    public LocalDate getReviewDate() { return reviewDate; }
    public String getUserID()        { return userID; }
}