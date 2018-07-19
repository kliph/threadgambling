(ns threadstreaks.api-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [threadstreaks.web :as web]
            [stubadub.core :refer [with-stub calls-to]]
            [compojure.handler :refer [site]]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]
            [luminus-migrations.core :as migrations]
            [threadstreaks.db.core :refer [*db*] :as db]
            [ring.mock.request :as mock]))

(use-fixtures :once
  (fn [f]
    (mount/start
     #'threadstreaks.db.core/*db*)
    (migrations/migrate ["migrate"] {:database-url (env :database-url)})
    (f)))


  (def sample-response "{\"count\":10,\"filters\":{\"matchday\":\"1\"},\"competition\":{\"id\":2021,\"area\":{\"id\":2072,\"name\":\"England\"},\"name\":\"Premier League\",\"code\":null,\"plan\":\"TIER_ONE\",\"lastUpdated\":\"2018-06-04T23:52:26Z\"},\"matches\":[{\"id\":233028,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-10T19:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":66,\"name\":\"Manchester United FC\"},\"awayTeam\":{\"id\":338,\"name\":\"Leicester City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233026,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T11:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":67,\"name\":\"Newcastle United FC\"},\"awayTeam\":{\"id\":73,\"name\":\"Tottenham Hotspur FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233024,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":63,\"name\":\"Fulham FC\"},\"awayTeam\":{\"id\":354,\"name\":\"Crystal Palace FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233029,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":394,\"name\":\"Huddersfield Town AFC\"},\"awayTeam\":{\"id\":61,\"name\":\"Chelsea FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233031,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":346,\"name\":\"Watford FC\"},\"awayTeam\":{\"id\":397,\"name\":\"Brighton & Hove Albion FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233032,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":1044,\"name\":\"AFC Bournemouth\"},\"awayTeam\":{\"id\":715,\"name\":\"Cardiff City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233023,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T16:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":76,\"name\":\"Wolverhampton Wanderers FC\"},\"awayTeam\":{\"id\":62,\"name\":\"Everton FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233025,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T12:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":64,\"name\":\"Liverpool FC\"},\"awayTeam\":{\"id\":563,\"name\":\"West Ham United FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233030,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T12:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":340,\"name\":\"Southampton FC\"},\"awayTeam\":{\"id\":328,\"name\":\"Burnley FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233027,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":57,\"name\":\"Arsenal FC\"},\"awayTeam\":{\"id\":65,\"name\":\"Manchester City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]}]}")

;;; Change the last game's goals.
(def sample-response-with-changed-data "{\"count\":10,\"filters\":{\"matchday\":\"1\"},\"competition\":{\"id\":2021,\"area\":{\"id\":2072,\"name\":\"England\"},\"name\":\"Premier League\",\"code\":null,\"plan\":\"TIER_ONE\",\"lastUpdated\":\"2018-06-04T23:52:26Z\"},\"matches\":[{\"id\":233028,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-10T19:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":66,\"name\":\"Manchester United FC\"},\"awayTeam\":{\"id\":338,\"name\":\"Leicester City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233026,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T11:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":67,\"name\":\"Newcastle United FC\"},\"awayTeam\":{\"id\":73,\"name\":\"Tottenham Hotspur FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233024,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":63,\"name\":\"Fulham FC\"},\"awayTeam\":{\"id\":354,\"name\":\"Crystal Palace FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233029,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":394,\"name\":\"Huddersfield Town AFC\"},\"awayTeam\":{\"id\":61,\"name\":\"Chelsea FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233031,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":346,\"name\":\"Watford FC\"},\"awayTeam\":{\"id\":397,\"name\":\"Brighton & Hove Albion FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233032,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T14:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":1044,\"name\":\"AFC Bournemouth\"},\"awayTeam\":{\"id\":715,\"name\":\"Cardiff City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233023,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-11T16:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":76,\"name\":\"Wolverhampton Wanderers FC\"},\"awayTeam\":{\"id\":62,\"name\":\"Everton FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233025,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T12:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":64,\"name\":\"Liverpool FC\"},\"awayTeam\":{\"id\":563,\"name\":\"West Ham United FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233030,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T12:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":340,\"name\":\"Southampton FC\"},\"awayTeam\":{\"id\":328,\"name\":\"Burnley FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":null,\"awayTeam\":null},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]},{\"id\":233027,\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":null},\"utcDate\":\"2018-08-12T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-07-07T16:01:35Z\",\"homeTeam\":{\"id\":57,\"name\":\"Arsenal FC\"},\"awayTeam\":{\"id\":65,\"name\":\"Manchester City FC\"},\"score\":{\"winner\":null,\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":4,\"awayTeam\":3},\"halfTime\":{\"homeTeam\":null,\"awayTeam\":null},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[]}]}")

(def sample-finished-response
"{\"count\":10,\"filters\":{\"matchday\":\"1\"},\"competition\":{\"id\":2019,\"area\":{\"id\":2114,\"name\":\"Italy\"},\"name\":\"Serie A\",\"code\":null,\"plan\":\"TIER_ONE\",\"lastUpdated\":\"2018-06-05T00:14:16Z\"},\"matches\":[{\"id\":204396,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-19T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":109,\"name\":\"Juventus FC\"},\"awayTeam\":{\"id\":104,\"name\":\"Cagliari Calcio\"},\"score\":{\"winner\":\"HOME_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":3,\"awayTeam\":0},\"halfTime\":{\"homeTeam\":2,\"awayTeam\":0},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":10977,\"name\":\"Fabio Maresca\",\"nationality\":null},{\"id\":10978,\"name\":\"Alfonso Marrazzo\",\"nationality\":null},{\"id\":10979,\"name\":\"Rodolfo Di Vuolo\",\"nationality\":null},{\"id\":10980,\"name\":\"Federico La Penna\",\"nationality\":null}]},{\"id\":204397,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-19T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":450,\"name\":\"Hellas Verona FC\"},\"awayTeam\":{\"id\":113,\"name\":\"SSC Napoli\"},\"score\":{\"winner\":\"AWAY_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":1,\"awayTeam\":3},\"halfTime\":{\"homeTeam\":0,\"awayTeam\":2},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":10985,\"name\":\"Michael Fabbri\",\"nationality\":null},{\"id\":10986,\"name\":\"Salvatore Longo\",\"nationality\":null},{\"id\":10987,\"name\":\"Filippo Valeriani\",\"nationality\":null},{\"id\":10988,\"name\":\"Gianluca Rocchi\",\"nationality\":null}]},{\"id\":204398,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":102,\"name\":\"Atalanta BC\"},\"awayTeam\":{\"id\":100,\"name\":\"AS Roma\"},\"score\":{\"winner\":\"AWAY_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":0,\"awayTeam\":1},\"halfTime\":{\"homeTeam\":0,\"awayTeam\":1},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":10990,\"name\":\"Piero Giacomelli\",\"nationality\":null},{\"id\":10991,\"name\":\"Giorgio Peretti\",\"nationality\":null},{\"id\":10992,\"name\":\"Stefano Alassio\",\"nationality\":null},{\"id\":10993,\"name\":\"Riccardo Pinzani\",\"nationality\":null}]},{\"id\":204399,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":108,\"name\":\"FC Internazionale Milano\"},\"awayTeam\":{\"id\":99,\"name\":\"ACF Fiorentina\"},\"score\":{\"winner\":\"HOME_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":3,\"awayTeam\":0},\"halfTime\":{\"homeTeam\":2,\"awayTeam\":0},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":10999,\"name\":\"Paolo Tagliavento\",\"nationality\":null},{\"id\":11000,\"name\":\"Fabrizio Posado\",\"nationality\":null},{\"id\":11001,\"name\":\"Mauro Vivenzi\",\"nationality\":null},{\"id\":11002,\"name\":\"Luca Pairetto\",\"nationality\":null}]},{\"id\":204400,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":110,\"name\":\"SS Lazio\"},\"awayTeam\":{\"id\":1107,\"name\":\"SPAL 2013\"},\"score\":{\"winner\":\"DRAW\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":0,\"awayTeam\":0},\"halfTime\":{\"homeTeam\":0,\"awayTeam\":0},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11006,\"name\":\"Rosario Abisso\",\"nationality\":null},{\"id\":11007,\"name\":\"Alessandro Costanzo\",\"nationality\":null},{\"id\":11008,\"name\":\"Damiano Di Iorio\",\"nationality\":null},{\"id\":11009,\"name\":\"Francesco Paolo Saia\",\"nationality\":null}]},{\"id\":204401,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":115,\"name\":\"Udinese Calcio\"},\"awayTeam\":{\"id\":106,\"name\":\"AC Chievo Verona\"},\"score\":{\"winner\":\"AWAY_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":1,\"awayTeam\":2},\"halfTime\":{\"homeTeam\":1,\"awayTeam\":1},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11015,\"name\":\"Claudio Gavillucci\",\"nationality\":null},{\"id\":11016,\"name\":\"Gianluca Vuoto\",\"nationality\":null},{\"id\":11017,\"name\":\"Daniele Bindoni\",\"nationality\":null},{\"id\":11018,\"name\":\"Antonio Rapuano\",\"nationality\":null}]},{\"id\":204402,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":584,\"name\":\"UC Sampdoria\"},\"awayTeam\":{\"id\":1106,\"name\":\"Benevento Calcio\"},\"score\":{\"winner\":\"HOME_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":2,\"awayTeam\":1},\"halfTime\":{\"homeTeam\":1,\"awayTeam\":1},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11021,\"name\":\"Fabrizio Pasqua\",\"nationality\":null},{\"id\":11022,\"name\":\"Riccardo Di Fiore\",\"nationality\":null},{\"id\":11023,\"name\":\"Emanuele Prenna\",\"nationality\":null},{\"id\":11024,\"name\":\"Daniele Minelli\",\"nationality\":null}]},{\"id\":204403,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":103,\"name\":\"Bologna FC 1909\"},\"awayTeam\":{\"id\":586,\"name\":\"Torino FC\"},\"score\":{\"winner\":\"DRAW\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":1,\"awayTeam\":1},\"halfTime\":{\"homeTeam\":1,\"awayTeam\":1},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11029,\"name\":\"Davide Massa\",\"nationality\":null},{\"id\":11030,\"name\":\"Claudio La Rocca\",\"nationality\":null},{\"id\":11031,\"name\":\"Stefano Del Giovane\",\"nationality\":null},{\"id\":11032,\"name\":\"Niccolò Baroni\",\"nationality\":null}]},{\"id\":204404,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":471,\"name\":\"US Sassuolo Calcio\"},\"awayTeam\":{\"id\":107,\"name\":\"Genoa CFC\"},\"score\":{\"winner\":\"DRAW\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":0,\"awayTeam\":0},\"halfTime\":{\"homeTeam\":0,\"awayTeam\":0},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11036,\"name\":\"Antonio Damato\",\"nationality\":null},{\"id\":11037,\"name\":\"Lorenzo Manganelli\",\"nationality\":null},{\"id\":11038,\"name\":\"Lucas Mondin\",\"nationality\":null},{\"id\":11039,\"name\":\"Marco Piccinini\",\"nationality\":null}]},{\"id\":204405,\"season\":{\"id\":21,\"startDate\":\"2017-08-19\",\"endDate\":\"2018-05-20\",\"currentMatchday\":38},\"utcDate\":\"2017-08-20T18:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"stage\":\"REGULAR_SEASON\",\"group\":\"Regular Season\",\"lastUpdated\":\"2018-06-22T10:03:34Z\",\"homeTeam\":{\"id\":472,\"name\":\"FC Crotone\"},\"awayTeam\":{\"id\":98,\"name\":\"AC Milan\"},\"score\":{\"winner\":\"AWAY_TEAM\",\"duration\":\"REGULAR\",\"fullTime\":{\"homeTeam\":0,\"awayTeam\":3},\"halfTime\":{\"homeTeam\":0,\"awayTeam\":3},\"extraTime\":{\"homeTeam\":null,\"awayTeam\":null},\"penalties\":{\"homeTeam\":null,\"awayTeam\":null}},\"referees\":[{\"id\":11043,\"name\":\"Maurizio Mariani\",\"nationality\":null},{\"id\":11044,\"name\":\"Giacomo Paganessi\",\"nationality\":null},{\"id\":11045,\"name\":\"Giorgio Schenone\",\"nationality\":null},{\"id\":11046,\"name\":\"Daniele Martinelli\",\"nationality\":null}]}]}"
)

(deftest renders-index
  (with-stub slurp :returns "stub"
    (let [req (web/handler (mock/request :get "/"))]
      (is (= 200 (-> req
                     :status)))
      (is (= "stub" (-> req
                        :body)))
      (is (= 1 (count (calls-to slurp))))
      (is (s/includes? (-> slurp
                           calls-to
                           first
                           first
                           .toString)
                       "index.html")))))

(deftest renders-admin
  (with-stub slurp :returns "stub"
    (let [req (web/handler (mock/request :get "/admin"))]
      (is (= 200 (-> req
                     :status)))
      (is (= "stub" (-> req
                        :body)))
      (is (= 1 (count (calls-to slurp))))
      (is (s/includes? (-> slurp
                           calls-to
                           first
                           first
                           .toString)
                       "admin.html")))))

(deftest renders-404
  (let [req (web/handler (mock/request :get "/an-invalid-path"))]
    (is (= 404 (-> req
                   :status)))
    (is (= "<h1>404 Not found</h1>" (-> req
                                        :body)))))

(deftest updates-user-account-details
  (with-stub db/update-user! :returns 1
    (let [name "Foo"
          team "South Philly Kittens"
          req (web/handler (mock/request :post "/account"
                                         {:headers {"Accept" "application/json"}
                                          :form-params {:name name
                                                        :team team}}))]
      (is (= 1 (count (calls-to db/update-user!)))))))

(deftest stores-fetched-fixtures-in-database
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (db/clear-fixtures! t-conn)
    (with-stub db/get-gameweek :returns {:gameweek 1}
      (testing "storing"
        (with-stub clj-http.client/get :returns {:body sample-response}
          (with-stub web/save-fixtures! :returns {:body sample-response}
            (let [req (web/handler (mock/request :get "/fixtures"))]
              (is (= 1 (count (calls-to web/save-fixtures!))))))))
      (testing "updates fixtures when it is changed"
        (db/save-fixtures! t-conn {:body sample-response
                                   :gameweek 1})
        (is (= {:body sample-response
                :gameweek 1}
               (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 1})
                   (select-keys [:body :gameweek]))))
        (with-stub clj-http.client/get :returns {:body sample-response-with-changed-data}
          (let [req (web/handler (mock/request :get "/fixtures"))]
            (is (= {:body sample-response-with-changed-data
                    :gameweek 1}
                   (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 1})
                       (select-keys [:body :gameweek]))))))))
    (db/clear-fixtures! t-conn)))

(deftest returns-json-fixture-response
  (with-stub clj-http.client/get :returns {:body sample-response}
    (let [req (web/handler (mock/request :get "/fixtures"))]
      (is (= 200 (-> req
                     :status)))
      (is (= sample-response (-> req
                                 :body))))))

(deftest fixtures-updated?
  (is (= false (web/fixtures-updated? sample-response
                                      {:body sample-response})))
  (is (= true (web/fixtures-updated? sample-response
                                     nil)))
  (is (= true (web/fixtures-updated? sample-response
                                     {:body sample-response-with-changed-data}))))

(deftest consumes-idtoken-form-param
  (let [user {:id "187"
              :name "Test User"
              :email "foo@example.com"
              :team ""}]
    (with-stub web/create-user-from-token-info! :returns {:status 200
                                                          :headers {}
                                                          :body {:user user}}
      (with-stub web/fetch-token-info :returns {:sub (:id user)
                                                :name (:name user)
                                                :email (:email user)}
        (with-stub web/aud-contains-client-id? :returns true
          (let [token "12345"
                req (web/handler (-> (mock/request :post "/tokensignin")
                                     (mock/content-type "application/x-www-form-urlencoded")
                                     (mock/body (str "idtoken=" token))))]
            (is (= 200 (-> req
                           :status)))
            (is (= user (-> req
                            :body
                            :user
                            (select-keys [:id :name :team :email]))))))))))



(deftest validates-the-passed-idtoken
  (is (= true
         (web/aud-contains-client-id? {:aud "abc123"}
                                      "abc123"))))

(deftest returns-error-status-when-aud-is-invalid
  (with-stub web/aud-contains-client-id? :returns false
    (let [invalid-token-info {:iss "https://accounts.google.com"
                              :sub "110169484474386276334"
                              :azp "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com"
                              :aud "invalid"
                              :iat "1433978353"
                              :exp "1433981953"
                              :email "testuser@gmail.com"
                              :email_verified "true"
                              :name  "Test User",
                              :picture "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg"
                              :given_name "Test"
                              :family_name "User"
                              :locale "en"}]
      (is (= 403
             (:status
              (web/verify-token-info invalid-token-info)))))))

(deftest all-fixtures-finished?
  (let [finished-fixtures [{:status "FINISHED"}
                           {:status "FINISHED"}]
        finished-with-canceled-and-postponed [{:status "FINISHED"}
                                              {:status "POSTPONED"}
                                              {:status "CANCELED"}]
        unfinished-fixtures [{:status "FINISHED"}
                             {:status "IN-PLAY"}]]
    (is (= true
           (web/all-finished? finished-fixtures)))
    (is (= true
           (web/all-finished? finished-with-canceled-and-postponed)))
    (is (= false
           (web/all-finished? unfinished-fixtures)))))

(deftest score-finished-week
  (let [expected-scores [{:date "2018-07-19T14:00:00Z"
                          :gameweek 1
                          :user_id "456"
                          :pick "FC Crotone"
                          :current_streak 0
                          :points 0}
                         {:date "2018-07-19T14:00:00Z"
                          :gameweek 1
                          :user_id "789"
                          :pick "AC Milan"
                          :current_streak 3
                          :points 3}]]
    (with-stub db/update-current-streak! :returns 1
      (with-stub db/update-points! :returns 1
        (with-stub db/get-user :returns {:points 2
                                         :current_streak 2}
          (with-stub db/get-gameweek :returns {:gameweek 1}
            (with-stub db/get-all-current-picks :returns [{:id "1234"
                                                           :current_pick "Tottenham Hotspur FC"
                                                           :current_streak 0}
                                                          {:id "456"
                                                           :current_pick "FC Crotone"
                                                           :current_streak 0}
                                                          {:id "789"
                                                           :current_pick "AC Milan"
                                                           :current_streak 2}]
              ;; Simulate having previously scored results.  We shouldn't
              ;; see half-scored weeks in practice, but this makes sure we
              ;; don't end up with multiple rows corresponding to the same
              ;; unique gameweek and user id
              (with-stub db/get-gameweek-results :returns [{:user_id "1234"}]
                (with-stub db/create-result! :returns 2
                  (web/score-finished-week! sample-finished-response)
                  (is (= (count (calls-to db/update-points!))
                         2))
                  (is (= (count (calls-to db/update-current-streak!))
                         2))
                  (is (= (->>  expected-scores
                               (mapv #(update-in % [:date] (fn [d]
                                                             (f/unparse (f/formatters :year-month-day) (f/parse d))))))

                         (->> (map first (calls-to db/create-result!))
                              (mapv #(update-in % [:date] (fn [d]
                                                            (f/unparse (f/formatters :year-month-day) (c/from-sql-time d)))))))))))))))))

(deftest respond-success-with-user
  (let [user {:id "187"
              :name "Test User"
              :email "foo@example.com"
              :last_login (java.sql.Timestamp. 1000)
              :created_at (java.sql.Timestamp. 1000)
              :team ""}
        json-body (json/write-str
                   {:user
                    (select-keys user
                                 [:id :name :email :team])})]
    (is (= 200
           (:status (web/respond-success-with-user user))))
    (is (= (json/read-str json-body)
           (json/read-str
            (:body (web/respond-success-with-user user)))))))


#_(deftest get-user-from-token-info
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [token-info {"iss" "https://accounts.google.com"
                      "sub" "110169484474386276334"
                      "azp" "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com"
                      "aud" "valid"
                      "iat" "1433978353"
                      "exp" "1433981953"
                      "email" "testuser@gmail.com"
                      "email_verified" "true"
                      "name"  "Test User",
                      "picture" "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg"
                      "given_name" "Test"
                      "family_name" "User"
                      "locale" "en"}
          user {:id (get token-info "sub")}]
      (db/create-user! t-conn
                       (-> user
                           (assoc :email "foo@example.com")
                           (assoc :name "Test User")
                           (assoc :team "South Philly Kittens"))
                       {:connection t-conn})
      (is (= user (-> (web/get-user-from-token-info token-info)
                      (select-keys [:id])))))))
