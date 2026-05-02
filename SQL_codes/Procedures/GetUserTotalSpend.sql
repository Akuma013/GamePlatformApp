-- Stored Procedure: Get total amount spent by a user on games
CREATE PROCEDURE GetUserTotalSpend
    @userID VARCHAR(50)
AS
BEGIN
    -- Calculate the total amount spent by the user on all their orders
    SELECT SUM(og.priceAtPurchase) AS TotalAmountSpent
    FROM [Order] o
    JOIN Order_Game og ON o.orderID = og.orderID
    WHERE o.userID = @userID;
END;