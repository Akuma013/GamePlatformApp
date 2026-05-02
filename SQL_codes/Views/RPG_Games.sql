CREATE VIEW RPG_Games AS
SELECT g.gameName, ge.genreName, STRING_AGG(d.developerName, ', ') AS developers
FROM Game g
JOIN Game_Genre gg ON g.gameID = gg.gameID
JOIN Genre ge ON gg.genreID = ge.genreID
JOIN Game_Developer gd ON g.gameID = gd.gameID
JOIN Developer d ON gd.developerID = d.developerID
WHERE ge.genreName = 'RPG'
GROUP BY g.gameName, ge.genreName;