CREATE TABLE results
(
        id SERIAL PRIMARY KEY,
        user_id VARCHAR(255) REFERENCES users(id),
        pick VARCHAR(127),
        gameweek INTEGER,
        date DATE,
        points INTEGER
);
