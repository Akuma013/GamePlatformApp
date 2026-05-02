CREATE TRIGGER trg_CheckUserBalance
ON GamePlatformDB.dbo.Order_Game
INSTEAD OF INSERT
AS
BEGIN
    -- Check if any inserted purchase exceeds user's balance
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN GamePlatformDB.dbo.[Order] o ON i.orderID = o.orderID
        JOIN GamePlatformDB.dbo.[User] u ON o.userID = u.username
        JOIN GamePlatformDB.dbo.Game g ON i.gameID = g.gameID
        WHERE u.balance < g.gamePrice * o.quantity
    )
    BEGIN
        -- Stop transaction if balance is insufficient
        RAISERROR ('Not enough balance to complete the purchase.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Insert purchase if balance is sufficient
    INSERT INTO GamePlatformDB.dbo.Order_Game (gameID, orderID, priceAtPurchase)
    SELECT gameID, orderID, priceAtPurchase
    FROM inserted;

    -- Deduct total cost from user's balance
    UPDATE u
    SET u.balance = u.balance - (g.gamePrice * o.quantity)
    FROM GamePlatformDB.dbo.[User] u
    JOIN GamePlatformDB.dbo.[Order] o ON u.username = o.userID
    JOIN inserted i ON o.orderID = i.orderID
    JOIN GamePlatformDB.dbo.Game g ON i.gameID = g.gameID;
END;