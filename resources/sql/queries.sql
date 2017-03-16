-- :name save-fixtures! :! :n
-- :doc Creates a new fixtures record using the fixtures request body and gameweek
INSERT INTO fixtures
(body, gameweek)
VALUES (:body, :gameweek)

-- :name get-fixtures :? :1
-- :doc retrieve a fixture given the gameweek
SELECT * FROM fixtures
WHERE gameweek = :gameweek
