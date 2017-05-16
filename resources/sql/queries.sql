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


-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id
