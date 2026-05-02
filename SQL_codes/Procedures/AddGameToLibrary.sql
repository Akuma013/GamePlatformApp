-- Stored Procedure: Add a game to the user's library
CREATE PROCEDURE AddGameToLibrary
    @userID VARCHAR(50),
    @gameID INT
AS
BEGIN
    -- Check if the user already owns the game
    IF EXISTS (SELECT 1 FROM Library WHERE userID = @userID AND gameID = @gameID)
    BEGIN
        PRINT 'User already owns this game.';
        RETURN;
    END
    
    -- Insert the game into the library with default values for playtime and favorite
    INSERT INTO Library (userID, gameID, playTime, favorite)
    VALUES (@userID, @gameID, 0, 0);
    
    PRINT 'Game successfully added to the library.';
END;