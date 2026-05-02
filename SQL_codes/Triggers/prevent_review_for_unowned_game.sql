-- Trigger: Prevent inserting a review for a game that the user doesn't own
CREATE TRIGGER prevent_review_for_unowned_game
ON Review
INSTEAD OF INSERT
AS
BEGIN
    DECLARE @userID VARCHAR(50), @gameID INT;

    -- Get the values from the inserted review row
    SELECT @userID = userID, @gameID = gameID FROM inserted;

    -- Check if the user owns the game
    IF NOT EXISTS (SELECT 1 FROM Library WHERE userID = @userID AND gameID = @gameID)
    BEGIN
        -- If the user doesn’t own the game, raise an error
        RAISERROR('You can only review games you own.', 16, 1);
    END
    ELSE
    BEGIN
        -- Proceed with inserting the review if the user owns the game
        INSERT INTO Review (reviewID, rating, description, reviewDate, userID, gameID)
        SELECT reviewID, rating, description, reviewDate, userID, gameID
        FROM inserted;
    END;
END;