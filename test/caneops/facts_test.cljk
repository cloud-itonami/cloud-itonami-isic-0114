(ns caneops.facts-test
  (:require [clojure.test :refer [deftest is are testing]]
            [caneops.facts :as facts]))

(deftest supply-category-lookup
  (testing "Lookup valid supply category"
    (let [c (facts/supply-category-by-id "seed-cane")]
      (is (= "seed-cane" (:id c)))
      (is (= "苗茎（種苗）" (:name c)))))

  (testing "Lookup invalid supply category"
    (is (nil? (facts/supply-category-by-id "unknown")))))

(deftest supply-category-cost-thresholds
  (testing "Category-specific cost thresholds"
    (are [id expected] (= expected (:cost-threshold (facts/supply-category-by-id id)))
      "seed-cane"   500
      "fertilizer"  500
      "equipment"   1000)))

(deftest default-cost-threshold-value
  (testing "Default fallback threshold matches the conservative baseline"
    (is (= 500 facts/default-cost-threshold))))

(deftest sugar-cane-variety-lookup
  (testing "Lookup valid sugar-cane variety"
    (are [id expected-name] (= expected-name (:name (facts/sugar-cane-variety-by-id id)))
      "hybrid"       "ハイブリッド品種（商業栽培主流）"
      "noble"        "ノーブルケーン（S. officinarum系原種）"
      "energy-cane"  "エナジーケーン（バイオ燃料用高繊維品種）"
      "chewing-cane" "チューイングケーン（生食用）"))

  (testing "Lookup invalid sugar-cane variety"
    (is (nil? (facts/sugar-cane-variety-by-id "unknown"))))

  (testing "Rice is out of scope (ISIC 0112, not this actor)"
    (is (nil? (facts/sugar-cane-variety-by-id "japonica")))))

(deftest field-operation-types-reference
  (testing "Planting/fertilizing/pre-harvest-burn/harvest/ratooning operation types are present as reference data"
    (is (contains? facts/field-operation-types "planting"))
    (is (contains? facts/field-operation-types "fertilizing"))
    (is (contains? facts/field-operation-types "pre-harvest-burn"))
    (is (contains? facts/field-operation-types "harvest"))
    (is (contains? facts/field-operation-types "ratooning"))))
