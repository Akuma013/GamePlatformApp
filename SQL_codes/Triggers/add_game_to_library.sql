-- Trigger: Automatically add the game to the user's library after an order is placed
CREATE TRIGGER add_game_to_library
ON Order_Game
AFTER INSERT
AS
BEGIN
    -- Insert the game into the Library table if it’s not already owned
    INSERT INTO Library (userID, gameID, playTime, favorite)
    SELECT o.userID, og.gameID, 0, 0
    FROM [Order] o
    JOIN Order_Game og ON o.orderID = og.orderID
    WHERE og.orderID IN (SELECT orderID FROM inserted)
    AND NOT EXISTS (
        SELECT 1 
        FROM Library l
        WHERE l.userID = o.userID AND l.gameID = og.gameID
    );
END;