CREATE PROCEDURE AddNewGame
    @gameName VARCHAR(100),
    @gameSize INT,
    @version VARCHAR(20),
    @downloadURL VARCHAR(200),
    @gamePrice DECIMAL(10,2),
    @publisherID INT,
    @developerID INT -- DeveloperID passed as a parameter
AS
BEGIN
    -- Insert the new game into the Game table (gameID will auto-increment)
    INSERT INTO Game (gameName, gameSize, version, downloadURL, gamePrice, publisherID)
    VALUES (@gameName, @gameSize, @version, @downloadURL, @gamePrice, @publisherID);

    -- Retrieve the gameID of the last inserted game using SCOPE_IDENTITY
    DECLARE @gameID INT;
    SET @gameID = SCOPE_IDENTITY();  -- SCOPE_IDENTITY() gets the last inserted identity value

    -- Check if gameID was successfully retrieved
    IF @gameID IS NULL
    BEGIN
        PRINT 'Failed to retrieve gameID.';
        RETURN; -- Exit the procedure if no gameID is returned
    END

    -- Insert into Game_Developer table using the gameID and developerID
    INSERT INTO Game_Developer (gameID, developerID)
    VALUES (@gameID, @developerID);

    PRINT 'Game successfully added with gameID ' + CAST(@gameID AS VARCHAR(10));
END;