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

(def headers [{:id (nth d/headers 0) :text "Exception" :size 80 :alignment Element/ALIGN_CENTER}
              {:id (nth d/headers 1) :text "Timestamp" :size 80 :alignment Element/ALIGN_CENTER}
              {:id (nth d/headers 2) :text "Type" :size 60 :alignment Element/ALIGN_CENTER}
              {:id (nth d/headers 3) :text "Worker" :size 150 :alignment Element/ALIGN_CENTER}
              {:id (nth d/headers 4) :text "Description" :size 150 :alignment Element/ALIGN_CENTER}
              ])
(def header-widths (float-array (mapv #(-> % :size float) headers)))
(def header-ids (mapv #(-> % :id) headers))
(def total-width (float (reduce + (map :size headers))))
(def total-num-cols (count headers))

(def some-file-name "report.pdf")
(println "Now it is" (u/to-long u/now) ", and 2 weeks ago was: " (u/to-long d/two-weeks-ago))
(u/pr-seq d/some-formatted-dates "DATES")

(defn- set-alignment! [cell heading-id]
  (.setHorizontalAlignment ^PdfPCell cell (:alignment (first (filter #(= (:id %) heading-id) headers))))
  ;is no op:
  ;(.setVerticalAlignment ^PdfPCell cell Element/ALIGN_TOP)
  )

(defn do-report! [doc]
  (let [headerTable (PdfPTable. total-num-cols)
        rowsTable (PdfPTable. total-num-cols)
        tables [headerTable rowsTable]
        paragraph (Paragraph. "Joy to The World!")]
    (run! #(.setHorizontalAlignment ^PdfPTable % Element/ALIGN_LEFT) tables)
    (run! #(.setTotalWidth ^PdfPTable % total-width) tables)
    (run! #(.setLockedWidth ^PdfPTable % true) tables)
    (run! #(.setWidths ^PdfPTable % header-widths) tables)
    (doseq [header-id header-ids]
      (let [header-text (:text (first (filter #(= header-id (:id %)) headers)))
            _ (println "HDR:" header-text)
            cell (PdfPCell. (Phrase. header-text))]
        (set-alignment! cell header-id)
        (.addCell headerTable cell)
        (doseq [i (range d/size)]
          (let [text (d/data-at i header-id)
                _ (println "txt" i " " header-id " is " text)
                cell (PdfPCell. (Phrase. text))]
            (set-alignment! cell header-id)
            (.addCell rowsTable cell)))))
    (.add doc headerTable)
    (.add doc rowsTable)
    (.add doc paragraph)))

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
