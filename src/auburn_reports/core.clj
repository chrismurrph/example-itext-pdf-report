(ns auburn-reports.core
  (:import (com.itextpdf.text Document DocumentException Element Phrase Paragraph)
           (com.itextpdf.text.pdf PdfPCell PdfPTable PdfWriter)
           (java.io FileOutputStream))
  (:require [auburn-reports.data :as d]
            [clj-time.format :as f]
            [auburn-reports.utils :as u])
  (:gen-class)
  )

;(def side-header-width 140)

(def headers [{:text (nth d/headers 0) :size 40}
              {:text (nth d/headers 1) :size 80}
              {:text (nth d/headers 2) :size 60}
              {:text (nth d/headers 3) :size 150}])
(def header-widths (float-array (into [(float side-header-width)] (map #(-> % :size float) headers))))
(def header-names (into ["Exception"] (map #(-> % :text) headers)))
(def total-width (reduce + (map :size headers)))
(def total-num-cols (inc (count headers)))
(def header-width (float (+ side-header-width total-width)))

(def some-file-name "report.pdf")
(println "Now it is" (d/to-long d/now) ", and 2 weeks ago was: " (d/to-long d/two-weeks-ago))
(u/pr-seq d/some-formatted-dates "DATES")

(defn do-report [doc]
  (let [headerTable (PdfPTable. total-num-cols)
        rowsTable (PdfPTable. total-num-cols)
        tables [headerTable rowsTable]
        paragraph (Paragraph. "Joy to The World!")]
    (run! #(.setHorizontalAlignment ^PdfPTable % Element/ALIGN_LEFT) tables)
    (run! #(.setTotalWidth ^PdfPTable % header-width) tables)
    (run! #(.setLockedWidth ^PdfPTable % true) tables)
    (run! #(.setWidths ^PdfPTable % header-widths) tables)
    (doseq [header-name header-names]
      (let [_ (println "HDR:" header-name)
            cell (PdfPCell. (Phrase. header-name))]
        (.addCell headerTable cell)))
    (.add doc headerTable)
    (.add doc paragraph)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(println "Hello, World!")
  (let [doc (Document.)
        os (FileOutputStream. some-file-name)
        _ (PdfWriter/getInstance doc os)]
    (.open doc)
    (do-report doc)
    (.close doc)
    (println "See" some-file-name))
  )
