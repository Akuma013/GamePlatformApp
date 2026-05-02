-- Add index on orderID in Order_Game table (to optimize JOIN)
CREATE INDEX idx_orderID_order_game ON GamePlatformDB.dbo.Order_Game(orderID);

-- Add index on userID in Order table (to optimize JOIN)
CREATE INDEX idx_userID_order ON GamePlatformDB.dbo.[Order](userID);

-- Query to retrieve the total amount spent by each user on games
SELECT o.userID, SUM(og.priceAtPurchase) AS total_spent
FROM GamePlatformDB.dbo.[Order] o
JOIN GamePlatformDB.dbo.Order_Game og ON o.orderID = og.orderID
GROUP BY o.userID
HAVING SUM(og.priceAtPurchase) > 40;