-- Trigger: Automatically update the playtime after a game is played
CREATE TRIGGER update_playtime
ON Library
AFTER UPDATE
AS
BEGIN
    -- Check if the playtime has increased
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN deleted d ON i.userID = d.userID AND i.gameID = d.gameID
        WHERE i.playTime > d.playTime
    )
    BEGIN
        -- Update the playtime for the game
        UPDATE Library
        SET playTime = (SELECT playTime FROM inserted WHERE userID = Library.userID AND gameID = Library.gameID)
        WHERE EXISTS (
            SELECT 1
            FROM inserted
            WHERE inserted.userID = Library.userID AND inserted.gameID = Library.gameID
        );
    END;
END;