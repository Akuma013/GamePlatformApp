-- Add index on gamePrice in Game table (to optimize WHERE filtering)
CREATE INDEX idx_gamePrice ON GamePlatformDB.dbo.Game(gamePrice);

-- Query to retrieve all games that cost more than $50
SELECT gameName, gamePrice
FROM GamePlatformDB.dbo.Game
WHERE gamePrice > 50;