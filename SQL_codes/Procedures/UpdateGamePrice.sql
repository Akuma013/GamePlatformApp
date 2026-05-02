-- Stored Procedure: Update the price of a game
CREATE PROCEDURE UpdateGamePrice
    @gameID INT,
    @newPrice DECIMAL(10, 2)
AS
BEGIN
    -- Update the price of the game
    UPDATE Game
    SET gamePrice = @newPrice
    WHERE gameID = @gameID;
    
    PRINT 'Game price updated successfully.';
END;