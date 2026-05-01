package com.gameplatform.model;

public class Game {
    private final int gameID;
    private final String gameName;
    private final double price;
    private final String version;
    private final int gameSize;
    private final String publisher;
    private final double avgRating;
    private final String imagePath;

    public Game(int gameID, String gameName, double price, String version, int gameSize, String publisher, double avgRating, String imagePath) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.price = price;
        this.version = version;
        this.gameSize = gameSize;
        this.publisher = publisher;
        this.avgRating = avgRating;
        this.imagePath = imagePath;
    }

    public int getGameID()       { return gameID; }
    public String getGameName()  { return gameName; }
    public double getPrice()     { return price; }
    public String getVersion()   { return version; }
    public int getGameSize()     { return gameSize; }
    public String getPublisher() { return publisher; }
    public double getAvgRating() { return avgRating; }
    public String getImagePath() { return imagePath; }
    @Override
    public String toString() {
        return String.format("Game[%d, %s, $%.2f, ★%.2f]",
                gameID, gameName, price, avgRating);
    }
}