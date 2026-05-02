-- Create logins
CREATE LOGIN admin1 WITH PASSWORD = 'Admin123!';
CREATE LOGIN user1 WITH PASSWORD = 'User123!';
CREATE LOGIN analyst1 WITH PASSWORD = 'Analyst123!';


USE GamePlatformDB;
-- create users
CREATE USER admin1 FOR LOGIN admin1;
CREATE USER user1 FOR LOGIN user1;
CREATE USER analyst1 FOR LOGIN analyst1;

-- create roles
CREATE ROLE UserRole;
CREATE ROLE AnalystRole;

-- grant permissions

-- User
GRANT SELECT ON Game TO UserRole;
GRANT SELECT ON Genre TO UserRole;
GRANT SELECT ON Library TO UserRole;
GRANT SELECT ON Wishlist TO UserRole;
GRANT SELECT ON Review TO UserRole;
GRANT SELECT ON [Order] TO UserRole;
GRANT SELECT ON Publisher TO UserRole;
GRANT SELECT ON Developer TO UserRole;
GRANT SELECT ON Game_Genre TO UserRole;

GRANT INSERT ON Library TO UserRole;
GRANT INSERT ON Wishlist TO UserRole;
GRANT INSERT ON Review TO UserRole;
GRANT INSERT ON [Order] TO UserRole;

GRANT UPDATE ON Library TO UserRole;
GRANT UPDATE ON Review TO UserRole;

GRANT DELETE ON Wishlist TO UserRole;
GRANT DELETE ON Library TO UserRole;

-- Analyst
GRANT SELECT ON Game TO AnalystRole;
GRANT SELECT ON Genre TO AnalystRole;
GRANT SELECT ON [User] TO AnalystRole;
GRANT SELECT ON Library TO AnalystRole;
GRANT SELECT ON Wishlist TO AnalystRole;
GRANT SELECT ON Review TO AnalystRole;
GRANT SELECT ON [Order] TO AnalystRole;
GRANT SELECT ON Order_Game TO AnalystRole;
GRANT SELECT ON Payment TO AnalystRole;
GRANT SELECT ON Publisher TO AnalystRole;
GRANT SELECT ON Developer TO AnalystRole;

-- add users to roles

-- Admin (full control)
ALTER ROLE db_owner ADD MEMBER admin1;

ALTER ROLE UserRole ADD MEMBER user1;
ALTER ROLE AnalystRole ADD MEMBER analyst1;