(ns auburn-reports.data
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [auburn-reports.utils :as u]))

(def custom-formatter (f/formatter "dd/MM/YYYY HH:mm"))

(def headers ["Exception"
              "Timestamp"
              "Type"
              "Worker"
              "Description"])

(def rand-count 20)
(def some-ints (u/random-ints rand-count 100))

(defn to-long [date-time] (c/to-long date-time))
(defn from-long [long-time] (c/from-long long-time))

(def now (t/now))
(def two-weeks-ago (t/plus now (t/weeks -2)))
(defn format-date [date-time] (f/unparse custom-formatter date-time))

;;
;; Given a number which will be between 0 and 99 inclusive, return what time this will be between two weeks
;; ago and now. We always work in time as the UTC number - conversions done only when displaying
;;
(defn scale-rand-to-long-date [rand-int]
  (let [from-world {:min 0 :max 99}
        to-world {:min (to-long two-weeks-ago) :max (to-long now)}
        res (u/scale from-world to-world rand-int)]
    res))

(def some-long-dates (sort (map scale-rand-to-long-date some-ints)))

(def some-formatted-dates (map (comp format-date from-long) some-long-dates))

(defn data-at [idx]
  {:timestamp (nth some-formatted-dates idx)
   :type "Fail Turn Up"
   :worker "Chris"
   :desription "Not clocked in by 9:20 for 9:00"})