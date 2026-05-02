-- Stored Procedure: Get all games owned by a user
CREATE PROCEDURE GetGamesOwnedByUser
    @userID VARCHAR(50)
AS
BEGIN
    -- Retrieve all games owned by the user
    SELECT g.gameName, g.gamePrice, l.playTime
    FROM Game g
    JOIN Library l ON g.gameID = l.gameID
    WHERE l.userID = @userID;
END;