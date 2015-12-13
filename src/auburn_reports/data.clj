(ns auburn-reports.data
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [auburn-reports.utils :as u]))

(def ^:private custom-formatter (f/formatter "dd/MM/YYYY HH:mm"))

(def ^:private rand-count 20)
(def ^:private some-ints (u/random-ints rand-count 100))

(defn- from-long [long-time] (c/from-long long-time))

(defn- format-date [date-time] (f/unparse custom-formatter date-time))
(defn- parse-date [formatted-date] (f/parse custom-formatter formatted-date))

(def two-weeks-ago (t/plus u/now (t/weeks -2)))

;; Use this for time of last roster
(defn one-week-ago-from [date-time] (t/plus date-time (t/weeks -1)))

(defn one-week-ahead-from [date-time] (t/plus date-time (t/weeks 1)))
(defn two-days-ahead-from [date-time] (t/plus date-time (t/days 2)))

;;
;; Given a number which will be between 0 and 99 inclusive, return what time this will be between two weeks
;; ago and now. We always work in time as the UTC number - conversions done only when displaying
;;
(defn- scale-rand-to-long-date [rand-int]
  (let [from-world {:min 0 :max 99}
        to-world {:min (u/to-long two-weeks-ago) :max (u/to-long u/now)}
        res (u/scale from-world to-world rand-int)]
    res))

(def ^:private some-long-dates (reverse (sort (map scale-rand-to-long-date some-ints))))

(def some-formatted-dates (map (comp format-date from-long) some-long-dates))

(def headers [:exception
              :timestamp
              :type
              :worker
              :description])

(def size (count some-formatted-dates))
(def library-name "Lidcombe Library")

;(println (-> "28/11/2015 21:03" parse-date one-week-ahead-from format-date))

(defn- desc-from-type [chosen-type formatted-date]
  ;(println "IN: " formatted-date)
  (case chosen-type :flex-expired "Flex day expired when roster came out"
                    :flex-warning (str "Will loose Flex day if not used before next roster due out on "
                                       (-> formatted-date parse-date two-days-ahead-from format-date))
                    :understaffed (str "Will be understaffed by one at circulation desk on "
                                       (-> formatted-date parse-date one-week-ago-from format-date))
                    :fail-turn-up "Not clocked on by 9:20am for 9:00am reference desk shift"
                    :over-budget "Published roster is over budget"))

(defn- by-id-and-type [chosen-type idx worker-needed?]
  (let [formatted-date (nth some-formatted-dates idx)]
    {:exception nil
     :timestamp formatted-date
     :type (case chosen-type :flex-expired "Flex Expired"
                             :flex-warning "Flex Warning"
                             :understaffed "Understaffed"
                             :fail-turn-up "Fail Turn Up"
                             :over-budget "Over Budget")
     :worker (when worker-needed? (u/random-of ["Chris Murphy" "Efren Katague" "Tomek Manko"]))
     :description (desc-from-type chosen-type formatted-date)}))

(defn data-at [idx]
  (let [chosen-type (u/random-of [:flex-expired :flex-warning :understaffed :fail-turn-up :over-budget])
        worker-not-needed? (or (= chosen-type :over-budget) (= chosen-type :understaffed))]
    (by-id-and-type chosen-type idx (not worker-not-needed?))))