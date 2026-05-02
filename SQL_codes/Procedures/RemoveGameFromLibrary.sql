-- Stored Procedure: Remove a game from the user's library
CREATE PROCEDURE RemoveGameFromLibrary
    @userID VARCHAR(50),
    @gameID INT
AS
BEGIN
    -- Check if the user owns the game
    IF NOT EXISTS (SELECT 1 FROM Library WHERE userID = @userID AND gameID = @gameID)
    BEGIN
        PRINT 'User does not own this game.';
        RETURN;
    END
    
    -- Remove the game from the library
    DELETE FROM Library WHERE userID = @userID AND gameID = @gameID;
    
    PRINT 'Game successfully removed from the library.';
END;