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
