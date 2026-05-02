/* =========================
   3.1 CREATE DATABASE
   ========================= */



USE GamePlatformDB;



/* =========================
   3.2 DDL - CREATE TABLES
   ========================= */

CREATE TABLE [User] (
    username VARCHAR(50) PRIMARY KEY,
    nickname VARCHAR(50),
    email VARCHAR(100),
    balance DECIMAL(10,2)
);

CREATE TABLE Publisher (
    publisherID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for publisherID
    publisherName VARCHAR(100),
    webSite VARCHAR(200)
);

CREATE TABLE Developer (
    developerID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for developerID
    developerName VARCHAR(100),
    email VARCHAR(100)
);

CREATE TABLE Game (
    gameID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for gameID
    gameSize INT,
    version VARCHAR(20),
    downloadURL VARCHAR(200),
    gameName VARCHAR(100),
    gamePrice DECIMAL(10,2),
    publisherID INT,
    FOREIGN KEY (publisherID) REFERENCES Publisher(publisherID)
);

CREATE TABLE Genre (
    genreID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for genreID
    genreName VARCHAR(50)
);

CREATE TABLE Game_Genre (
    gameID INT,
    genreID INT,
    PRIMARY KEY (gameID, genreID),
    FOREIGN KEY (gameID) REFERENCES Game(gameID),
    FOREIGN KEY (genreID) REFERENCES Genre(genreID)
);

CREATE TABLE Game_Developer (
    developerID INT,
    gameID INT,
    PRIMARY KEY (developerID, gameID),
    FOREIGN KEY (developerID) REFERENCES Developer(developerID),
    FOREIGN KEY (gameID) REFERENCES Game(gameID)
);

CREATE TABLE Friend (
    userID_1 VARCHAR(50),
    userID_2 VARCHAR(50),
    PRIMARY KEY (userID_1, userID_2),
    FOREIGN KEY (userID_1) REFERENCES [User](username),
    FOREIGN KEY (userID_2) REFERENCES [User](username)
);

CREATE TABLE Library (
    userID VARCHAR(50),
    gameID INT,
    playTime INT,
    favorite BIT,
    PRIMARY KEY (userID, gameID),
    FOREIGN KEY (userID) REFERENCES [User](username),
    FOREIGN KEY (gameID) REFERENCES Game(gameID)
);

CREATE TABLE Wishlist (
    userID VARCHAR(50),
    gameID INT,
    PRIMARY KEY (userID, gameID),
    FOREIGN KEY (userID) REFERENCES [User](username),
    FOREIGN KEY (gameID) REFERENCES Game(gameID)
);

CREATE TABLE Review (
    reviewID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for reviewID
    rating INT,
    description TEXT,
    reviewDate DATE,
    userID VARCHAR(50),
    gameID INT,
    FOREIGN KEY (userID) REFERENCES [User](username),
    FOREIGN KEY (gameID) REFERENCES Game(gameID)
);

CREATE TABLE [Order] (
    orderID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for orderID
    quantity INT,
    orderDate DATE,
    userID VARCHAR(50),
    FOREIGN KEY (userID) REFERENCES [User](username)
);

CREATE TABLE Order_Game (
    gameID INT,
    orderID INT,
    priceAtPurchase DECIMAL(10,2),
    PRIMARY KEY (gameID, orderID),
    FOREIGN KEY (gameID) REFERENCES Game(gameID),
    FOREIGN KEY (orderID) REFERENCES [Order](orderID)
);

CREATE TABLE Payment (
    paymentID INT IDENTITY(1,1) PRIMARY KEY,  -- Set IDENTITY for paymentID
    orderID INT,
    amount DECIMAL(10,2),
    paymentMethod VARCHAR(50),
    FOREIGN KEY (orderID) REFERENCES [Order](orderID)
);