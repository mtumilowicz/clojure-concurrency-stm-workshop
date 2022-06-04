(ns workshop.bank.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [workshop.bank.core :refer :all]))

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
    (doall (pmap (partial transferToAndFro checking savings) (rand-ints 100 25)))
    (is (= 200 (+ (balance savings) (balance checking))))))