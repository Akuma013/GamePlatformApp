package com.gameplatform.model;
import java.util.List;

public class Game {
    private final int gameID;
    private final String gameName;
    private final double price;
    private final String version;
    private final int gameSize;
    private final String publisher;
    private final double avgRating;
    private final String imagePath;
    private final List<String> genres;

    public Game(int gameID, String gameName, double price, String version,
                int gameSize, String publisher, double avgRating,
                String imagePath, List<String> genres) {       // ← new param
        this.gameID = gameID;
        this.gameName = gameName;
        this.price = price;
        this.version = version;
        this.gameSize = gameSize;
        this.publisher = publisher;
        this.avgRating = avgRating;
        this.imagePath = imagePath;
        this.genres = genres == null ? List.of() : genres;
    }

    public int getGameID()       { return gameID; }
    public String getGameName()  { return gameName; }
    public double getPrice()     { return price; }
    public String getVersion()   { return version; }
    public int getGameSize()     { return gameSize; }
    public String getPublisher() { return publisher; }
    public double getAvgRating() { return avgRating; }
    public String getImagePath() { return imagePath; }
    public List<String> getGenres() { return genres; }
    @Override
    public String toString() {
        return String.format("Game[%d, %s, $%.2f, ★%.2f]",
                gameID, gameName, price, avgRating);
    }
}