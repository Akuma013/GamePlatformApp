CREATE VIEW GameWithPublisher AS
SELECT g.gameName, p.publisherName
FROM Game g
JOIN Publisher p ON g.publisherID = p.publisherID;