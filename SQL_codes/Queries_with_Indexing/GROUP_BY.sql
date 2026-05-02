-- Add index on userID in Library table (to optimize GROUP BY and JOIN)
CREATE INDEX idx_userID_library ON GamePlatformDB.dbo.Library(userID);

-- Query to get the total number of games owned by each user
SELECT u.username, COUNT(l.gameID) AS total_games
FROM GamePlatformDB.dbo.[User] u
JOIN GamePlatformDB.dbo.Library l ON u.username = l.userID
GROUP BY u.username;