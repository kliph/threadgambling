CREATE TABLE fixtures (
  id SERIAL PRIMARY KEY,
  body VARCHAR NOT NULL,
  gameweek INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);
--;;
CREATE INDEX gameweek_ix on fixtures(gameweek);
