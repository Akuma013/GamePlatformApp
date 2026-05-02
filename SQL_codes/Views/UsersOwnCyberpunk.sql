CREATE VIEW UsersOwnCyberpunk AS
SELECT u.username
FROM [User] u
JOIN Library l ON u.username = l.userID
JOIN Game g ON l.gameID = g.gameID
WHERE g.gameName = 'Cyberpunk 2077';