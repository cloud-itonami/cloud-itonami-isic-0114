(ns caneops.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [caneops.store :as store]))

(deftest mem-store-creation
  (testing "Create empty store"
    (let [st (store/mem-store)]
      (is (some? st))
      (is (satisfies? store/Store st))))

  (testing "Create store with initial fields"
    (let [fields {"cane-001" {:id "cane-001" :name "Test Plantation North Block"}}
          st (store/mem-store {:initial-fields fields})]
      (is (some? st))
      (is (satisfies? store/Store st)))))

(deftest registered-field-retrieval
  (testing "Retrieve existing field"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          st (store/mem-store {:initial-fields {"cane-001" field}})]
      (is (= field (store/registered-field st "cane-001")))))

  (testing "Retrieve non-existent field"
    (let [st (store/mem-store)]
      (is (nil? (store/registered-field st "no-such-field")))))

  (testing "nil field-id returns nil (never falls through to a default)"
    (let [st (store/mem-store {:initial-fields {"cane-001" {:id "cane-001"}}})]
      (is (nil? (store/registered-field st nil))))))

(deftest add-field-test
  (testing "Register a new field"
    (let [st (store/mem-store)
          field-data {:id "cane-002" :name "New Block"}
          result (store/add-field st "cane-002" field-data)]
      (is (= field-data result))
      (is (= field-data (store/registered-field st "cane-002")))))

  (testing "Update an existing field"
    (let [st (store/mem-store {:initial-fields {"cane-001" {:id "cane-001"}}})
          updated {:id "cane-001" :name "Renamed Block"}
          result (store/add-field st "cane-001" updated)]
      (is (= updated result))
      (is (= updated (store/registered-field st "cane-001"))))))
