CREATE VIEW ExpensiveGames AS
SELECT gameName, gamePrice
FROM Game
WHERE gamePrice > 50;