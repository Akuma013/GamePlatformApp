-- Add index on publisherID in Game table (to optimize JOIN)
CREATE INDEX idx_publisherID ON GamePlatformDB.dbo.Game(publisherID);

-- Query to retrieve all games along with their publisher names
SELECT g.gameName, p.publisherName
FROM GamePlatformDB.dbo.Game g
JOIN GamePlatformDB.dbo.Publisher p ON g.publisherID = p.publisherID;