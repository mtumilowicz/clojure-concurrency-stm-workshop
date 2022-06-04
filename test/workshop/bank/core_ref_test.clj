(ns workshop.bank.core-ref-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [workshop.bank.core-ref :refer :all]))

(defn transferToAndFro [account1 account2 amount]
  (do
    (transfer account1 account2 amount)
    (transfer account2 account1 amount))
  )

(defn rand-ints [size upperLimit]
  (gen/sample (gen/choose 0 upperLimit) size))

(defn simulate-transfers [account1 account2]
  (doall (pmap (partial transferToAndFro account1 account2) (rand-ints 100 25))))

(deftest test-concurrent-transfers
  (let [account1 (make-account 100)
        account2 (make-account 100)]
    (simulate-transfers account1 account2)
    (is (= 200 (+ (balance account1) (balance account2))))))