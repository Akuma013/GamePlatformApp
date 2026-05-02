-- Add index on gameName in Game table (to optimize WHERE filtering)
CREATE INDEX idx_gameName ON GamePlatformDB.dbo.Game(gameName);

-- Add index on userID in Library table (to optimize JOIN)
CREATE INDEX idx_userID_library ON GamePlatformDB.dbo.Library(userID);

-- Query to retrieve usernames of users who own "Cyberpunk 2077"
SELECT u.username
FROM GamePlatformDB.dbo.[User] u
JOIN GamePlatformDB.dbo.Library l ON u.username = l.userID
JOIN GamePlatformDB.dbo.Game g ON l.gameID = g.gameID
WHERE g.gameName = 'Cyberpunk 2077';