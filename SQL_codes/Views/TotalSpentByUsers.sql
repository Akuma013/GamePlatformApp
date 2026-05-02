CREATE VIEW TotalSpentByUsers AS
SELECT o.userID, SUM(og.priceAtPurchase) AS total_spent
FROM [Order] o
JOIN Order_Game og ON o.orderID = og.orderID
GROUP BY o.userID;