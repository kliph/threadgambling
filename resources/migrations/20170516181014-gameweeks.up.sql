CREATE TABLE gameweeks (
  id SERIAL PRIMARY KEY,
  gameweek INTEGER,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);
--;
INSERT INTO gameweeks
(id, gameweek)
VALUES (1, 38);
