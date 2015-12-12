(ns auburn-reports.utils
  (:require
    [clj-time.core :as t]
    [clj-time.coerce :as c]))

(def now (t/now))
(defn to-long [date-time] (c/to-long date-time))

(defn str-seq
  ([seq msg]
   (letfn [(lineify-seq [items]
             (apply str (interpose "\n" items)))]
     (str "\n--------start--------\n"
          msg "\nCOUNT: " (count seq) "\n"
          (lineify-seq seq) "\n---------end---------")))
  ([seq]
   (str-seq nil seq)))

(defn pr-seq
  ([seq msg]
   (println (str-seq seq msg)))
  ([seq]
   (pr-seq nil seq)))

;;
;; Note that there will be a tendency to not quite go to the sample-size due to the de-duping effect of creating
;; a set. In those cases we recurse until the difference is made up.
;; Yes it would be better if it were lazy!
;;
(defn random-ints
  [sample-size pop-size]
  (let [res (set (take sample-size (repeatedly #(rand-int pop-size))))
        diff (- sample-size (count res))]
    (if (> diff 0)
      (set (concat res (random-ints diff pop-size)))
      res)))

;;
;; from-world and to-world are maps of type {:min _ :max _}
;; These max and min are inclusive, so the exact middle when :min 0 and :max 10 is 5
;; Note that we need to do precision-scaling at the end, as there needs to be an exact
;; pixel location where to put circle on the graph
;;
(defn scale [from-world to-world from-val]
  (let [from-min (:min from-world)
        from-diff (- (:max from-world) from-min)
        to-diff (- (:max to-world) (:min to-world))
        from-proportion (/ (- from-val from-min) from-diff)
        res (+ (:min to-world) (* to-diff from-proportion))
        rounded-res (long res)
        ;_ (println rounded-res " | " res)
        ]
    rounded-res))

(defn fmap
  [f m]
  (->>
    (map (fn [[k v]]
           {k (f v)})
         m)
    (into {})))
