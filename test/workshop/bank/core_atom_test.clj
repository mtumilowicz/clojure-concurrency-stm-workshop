(ns workshop.bank.core-atom-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [workshop.bank.core-atom :refer :all]))

(defn transferToAndFro [account1 account2 amount]
  (do
    (transfer account1 account2 amount)
    (transfer account2 account1 amount))
  )

(defn rand-ints [size upperLimit]
  (gen/sample (gen/choose 0 upperLimit) size))

(deftest test-concurrent-transfers
  (let [checking (make-account 100)
        savings (make-account 100)]
    (is (thrown-with-msg? Exception #"Insufficient Funds" (doall (pmap (partial transferToAndFro checking savings) (rand-ints 100 25))) ))))