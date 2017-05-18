-- :name save-fixtures! :! :n
-- :doc Creates a new fixtures record using the fixtures request body and gameweek
INSERT INTO fixtures
(body, gameweek)
VALUES (:body, :gameweek)

-- :name get-fixtures-by-gameweek :? :1
-- :doc retrieve the most recent fixture given the gameweek
SELECT * FROM fixtures
WHERE gameweek = :gameweek
ORDER BY created_at desc

-- :name clear-fixtures! :! :n
-- :doc delete all of the fixtures in the fixtures table
DELETE FROM fixtures;

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, name, team, email)
VALUES (:id, :name, :team, :email)

-- :name update-user! :! :n
-- :doc updates a user record
UPDATE users
SET name = :name,
    team = :team
WHERE id = :id

-- :name update-picks! :! :n
-- :doc updates a user's picks
UPDATE users
SET picks = :picks
WHERE id = :id

-- :name update-current-pick! :! :n
-- :doc updates a user's picks
UPDATE users
set current_pick = :current_pick
WHERE id = :id

-- :name update-current-streak! :! :n
-- :doc updates a user's streak
UPDATE users
set current_streak = :current_streak
WHERE id = :id

-- :name update-points! :! :n
-- :doc updates a user's streak
UPDATE users
set points = :points
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

-- :name get-gameweek :? :1
-- :doc retrieve the current gameweek
SELECT * FROM gameweeks
WHERE id = :id

-- :name update-gameweek! :! :n
-- :doc retrieve the current gameweek
UPDATE gameweeks
set gameweek = :gameweek
WHERE id = :id

-- :name get-standings :? :*
-- :doc retrieve listing of standings
SELECT name, team, points, current_streak, current_pick
FROM users
ORDER BY points DESC;
