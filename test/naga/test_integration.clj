(ns naga.test-integration
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [naga.cli :refer :all]
            [cheshire.core :as j])
  (:import [java.io StringWriter]))

(defn capture-output
  [f & args]
  (with-open [out-buffer (StringWriter.)
              err-buffer (StringWriter.)]
    (binding [*out* out-buffer
              *err* err-buffer]
      (let [a (if (= 1 (count args))
                (remove empty? (s/split (first args) #"\s"))
                args)]
        (apply f a)
        [(.toString out-buffer) (.toString err-buffer)]))))

(def family-out
  "INPUT DATA\nsibling(fred, barney).\nparent(fred, mary).\nsibling(mary, george).\ngender(george, male).\n\nNEW DATA\nuncle(fred, george).\nbrother(mary, george).\nparent(barney, mary).\nuncle(barney, george).\nsibling(barney, fred).\n")

(def family-2nd-out
  "INPUT DATA\nsibling(fred, barney).\nparent(fred, mary).\nsibling(mary, george).\ngender(george, male).\nowl:SymmetricProperty(sibling).\n\nNEW DATA\nuncle(fred, george).\nbrother(mary, george).\nsibling(george, mary).\nparent(barney, mary).\nuncle(barney, george).\nsibling(barney, fred).\n")

(deftest test-basic-program
  (let [[out err] (capture-output -main "pabu/family.lg")]
    (is (= out family-out))
    (is (= err "")))
  (let [[out err] (capture-output -main "pabu/family-2nd-ord.lg")]
    (is (= out family-2nd-out))
    (is (= err ""))))

(def json-out
  [{:id "barney", :parent "mary", :uncle "george", :sibling "fred"}
   {:id "fred", :sibling "barney", :parent "mary", :uncle "george"}
   {:id "george", :type "male", :sibling "mary"}
   {:id "mary", :sibling "george", :brother "george"}])

(deftest test-json-flow
  (let [[out err] (capture-output -main "--json data/in.json --out data/out.json data/family.lg")
        json-result (j/parse-string (slurp "data/out.json") keyword)]
    (is (empty? out))
    (is (empty? err))
    (is (= (sort-by :id json-result) json-out))))

