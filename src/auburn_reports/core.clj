(ns auburn-reports.core
  (:import (com.itextpdf.text Document DocumentException Element Phrase Paragraph)
           (com.itextpdf.text.pdf PdfPCell PdfPTable PdfWriter)
           (java.io FileOutputStream)
  (:gen-class)))

(def side-header-width 140)

(def headers [{:text "Timestamp" :size 60}
              {:text "Type" :size 60}
              {:text "Worker" :size 60}
              {:text "Description" :size 60}])
(def header-widths (float-array (into [(float side-header-width)] (map #(-> % :size float) headers))))
(def total-width (reduce + (map :size headers)))
(def total-num-cols (inc (count headers)))
(def header-width (float (+ side-header-width total-width)))
(println header-widths)

(def some-file-name "report.pdf")

(defn do-report [doc]
  (let [headerTable (PdfPTable. total-num-cols)
        paragraph (Paragraph. "Joy to The World!")]
    (.setHorizontalAlignment headerTable Element/ALIGN_LEFT)
    (.setTotalWidth headerTable header-width)
    (.setLockedWidth headerTable true)
    (.setWidths headerTable header-widths)
    (.add doc headerTable)
    (.add doc paragraph)
    )

  )

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
