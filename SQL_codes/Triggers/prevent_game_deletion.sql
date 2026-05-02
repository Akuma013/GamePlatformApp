-- Trigger: Prevent deletion of a game if it is in any user's library
CREATE TRIGGER prevent_game_deletion
ON Game
INSTEAD OF DELETE
AS
BEGIN
    DECLARE @gameID INT;
    SELECT @gameID = gameID FROM deleted;

    -- Check if the game is in the Library table
    IF EXISTS (SELECT 1 FROM Library WHERE gameID = @gameID)
    BEGIN
        -- If the game is owned by users, raise an error and prevent deletion
        RAISERROR('Cannot delete the game as it is owned by users.', 16, 1);
    END
    ELSE
    BEGIN
        -- If the game is not owned by any user, proceed with deletion
        DELETE FROM Game WHERE gameID = @gameID;
    END;
END;