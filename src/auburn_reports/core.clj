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

(def typical-cell-width 220)
(def headers [;{:id (nth d/headers 0) :text "Exception" :size 50 :alignment Element/ALIGN_CENTER}
              {:id (nth d/headers 1)
               :text "Timestamp"
               :size 110
               :header-alignment Element/ALIGN_CENTER
               :body-alignment Element/ALIGN_CENTER
               }
              {:id (nth d/headers 2)
               :text "Type"
               :size typical-cell-width
               :header-alignment Element/ALIGN_CENTER
               :body-alignment Element/ALIGN_LEFT
               }
              {:id (nth d/headers 3)
               :text "Worker"
               :size typical-cell-width
               :header-alignment Element/ALIGN_CENTER
               :body-alignment Element/ALIGN_CENTER
               }
              {:id (nth d/headers 4)
               :text "Description"
               :size (* 2 typical-cell-width)
               :header-alignment Element/ALIGN_LEFT
               :body-alignment Element/ALIGN_LEFT
               }
              ])

(def some-file-name "report.pdf")
(println "Now it is" (u/to-long u/now) ", and 2 weeks ago was: " (u/to-long d/two-weeks-ago))
(u/pr-seq d/some-formatted-dates "DATES")

(defn- set-alignment! [align-id cell heading]
  (.setHorizontalAlignment ^PdfPCell cell (align-id heading))
  ;is no op:
  ;(.setVerticalAlignment ^PdfPCell cell Element/ALIGN_TOP)
  )

(def set-header-alignment! (partial set-alignment! :header-alignment))
(def set-body-alignment! (partial set-alignment! :body-alignment))

(defn do-report! [doc]
  (let [middle-headers (u/mid-section headers)
        all-but-last (u/except-last headers)
        first-header (first headers)
        last-header (last headers)
        total-width (float (reduce + (map :size all-but-last)))
        ;header-widths (float-array (mapv #(-> % :size float) middle-headers))
        all-but-last-widths (float-array (mapv #(-> % :size float) all-but-last))
        total-num-cols (count all-but-last)
        headerTable (PdfPTable. total-num-cols)
        rowsTable (PdfPTable. total-num-cols)
        tables [headerTable rowsTable]
        ]
    (run! #(.setHorizontalAlignment ^PdfPTable % Element/ALIGN_LEFT) tables)
    (run! #(.setTotalWidth ^PdfPTable % total-width) tables)
    (run! #(.setLockedWidth ^PdfPTable % true) tables)
    ;(run! #(.setWidths ^PdfPTable % header-widths) [rowsTable])
    (run! #(.setWidths ^PdfPTable % all-but-last-widths) tables)

    (let [first-header (first headers)
          first-cell (PdfPCell. (Phrase. (:text first-header)))]
      (set-header-alignment! first-cell first-header)
      (.addCell headerTable first-cell))
    (doseq [header middle-headers]
      (let [header-text (:text header)
            ;_ (println "HDR:" header-text)
            cell (PdfPCell. (Phrase. header-text))]
        (set-header-alignment! cell header)
        (.addCell headerTable cell)))
    (doseq [i (range d/size)]
      (let [first-cell (PdfPCell. (Phrase. (d/data-at i (:id first-header))))]
        (.setRowspan first-cell 2)
        (set-body-alignment! first-cell first-header)
        (.addCell rowsTable first-cell))
      (doseq [header middle-headers]
        (let [text (d/data-at i (:id header))
              ;_ (println "txt" i " " header-id " is " text)
              cell (PdfPCell. (Phrase. text))]
          (set-body-alignment! cell header)
          (.addCell rowsTable cell)))
      (let [last-cell (PdfPCell. (Phrase. (d/data-at i (:id last-header))))]
        (.setGrayFill last-cell 0.95)
        (.setColspan last-cell total-num-cols)
        (set-body-alignment! last-cell last-header)
        (.addCell rowsTable last-cell)))
    (.add doc headerTable)
    (.add doc rowsTable)))

;;
;; At the moment if the PDF file is already being viewed then this crashes quite badly.
;; If this code ever used in any proper way lets handle DocumentException
;;
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(println "Hello, World!")
  (let [doc (Document.)
        os (FileOutputStream. some-file-name)
        _ (PdfWriter/getInstance doc os)]
    (.open doc)
    (do-report! doc)
    (.close doc)
    (println "See" some-file-name))
  )
