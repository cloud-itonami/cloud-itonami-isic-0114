(ns caneops.store-contract-test
  "MemStore ≡ DatomicStore parity for the Store protocol. Mirrors
  `cerealops.store-contract-test` (cloud-itonami-isic-0111)."
  (:require [clojure.test :refer [deftest is]]
            [caneops.store :as store]))

(defn- exercise [s]
  (store/add-field s "cane-x" {:id "cane-x" :name "X Block" :variety "hybrid"})
  ;; re-registering (update) exercises the identity-upsert path on
  ;; DatomicStore (:field/id is :db.unique/identity) the same way
  ;; MemStore's plain `assoc` re-registration does.
  (store/add-field s "cane-x" {:id "cane-x" :name "X Block (renamed)" :variety "hybrid"})
  (store/append-ledger! s {:t :committed :op :log-field-record :subject "cane-x"})
  (store/append-ledger! s {:t :approval-requested :op :flag-crop-health-concern :subject "cane-x"})
  {:field  (store/registered-field s "cane-x")
   :absent (store/registered-field s "no-such-field")
   :ledger (store/ledger s)})

(deftest mem-and-datomic-parity
  (let [mem (store/mem-store)
        dat (store/datomic-store)
        m (exercise mem)
        d (exercise dat)]
    (is (= (:field m) (:field d)))
    (is (= "X Block (renamed)" (:name (:field m))) "re-registration upserts, not forks history")
    (is (nil? (:absent m)))
    (is (nil? (:absent d)))
    (is (= 2 (count (:ledger m))))
    (is (= 2 (count (:ledger d))))
    (is (= (:ledger m) (:ledger d)))))

(deftest datomic-store-seeded-fields
  (let [dat (store/datomic-store {:initial-fields
                                   {"cane-y" {:id "cane-y" :name "Y Block"}}})]
    (is (= {:id "cane-y" :name "Y Block"} (store/registered-field dat "cane-y")))
    (is (empty? (store/ledger dat)))))
