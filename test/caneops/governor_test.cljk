(ns caneops.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [caneops.governor :as gov]
            [caneops.store :as store]))

(deftest hard-violations-no-field-id
  (testing "Hard violation: missing field-id"
    (let [req {}
          prop {:op :log-field-record :effect :propose}
          s (store/mem-store)
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (seq (:violations verdict)))
      (is (some #(= :field-not-registered (:rule %)) (:violations verdict))))))

(deftest hard-violations-unregistered-field
  (testing "Hard violation: field-id present but not registered"
    (let [req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose}
          s (store/mem-store)
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :field-not-registered (:rule %)) (:violations verdict))))))

(deftest hard-violations-effect-not-propose
  (testing "Hard violation: effect is not :propose"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :execute}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :no-execution (:rule %)) (:violations verdict))))))

(deftest hard-violations-field-equipment-blocked
  (testing "Hard violation: direct field-equipment operation is permanently blocked"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :operate-field-equipment :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :equipment-or-decision-blocked (:rule %)) (:violations verdict))))))

(deftest hard-violations-burn-decision-blocked
  (testing "Hard violation: finalizing a pre-harvest-burn decision is permanently blocked"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :finalize-burn-decision :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :equipment-or-decision-blocked (:rule %)) (:violations verdict))))))

(deftest hard-violations-pesticide-decision-blocked
  (testing "Hard violation: finalizing a pesticide-application decision is permanently blocked"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :finalize-pesticide-application :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :equipment-or-decision-blocked (:rule %)) (:violations verdict))))))

(deftest hard-violations-op-not-allowed
  (testing "Hard violation: op outside the closed allowlist"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :dispatch-drone-sprayer :effect :propose}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :op-not-allowed (:rule %)) (:violations verdict))))))

(deftest hard-violations-field-record-invalid
  (testing "Hard violation: non-positive acreage"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose :acreage 0 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :field-record-invalid (:rule %)) (:violations verdict))))))

(deftest hard-violations-ratoon-cycle-invalid
  (testing "Hard violation: negative ratoon-cycle"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose :acreage 40 :ratoon-cycle -1 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:hard? verdict))
      (is (some #(= :ratoon-cycle-invalid (:rule %)) (:violations verdict))))))

(deftest ok-field-logging
  (testing "OK: valid field record logging with a registered field"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose :acreage 40 :ratoon-cycle 1 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:hard? verdict)))
      (is (not (:escalate? verdict))))))

(deftest ok-field-logging-zero-ratoon-cycle
  (testing "OK: zero ratoon-cycle is a valid (fresh plant cane) observation"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose :acreage 40 :ratoon-cycle 0 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:hard? verdict))))))

(deftest escalation-crop-health-concern
  (testing "Escalation: crop pest (borer)/disease/drought-stress concern ALWAYS escalates, even at high confidence"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :flag-crop-health-concern :effect :propose
                :concern "サトウキビメイチュウ（stalk borer）の可能性" :confidence 0.95}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict))
      (is (:high-stakes? verdict)))))

(deftest escalation-low-confidence
  (testing "Escalation: confidence below the floor"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :log-field-record :effect :propose :acreage 40 :confidence 0.5}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict)))))

(deftest escalation-supply-order-high-cost
  (testing "Escalation: supply order over the (default) cost threshold"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :order-supplies :effect :propose :cost 1000 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict)))))

(deftest escalation-supply-order-category-specific-threshold
  (testing "Escalation: supply order over its category-specific threshold (equipment: 1000)"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :order-supplies :effect :propose :cost 1200 :confidence 0.9
                :value {:category "equipment"}}
          verdict (gov/check req nil prop s)]
      (is (:escalate? verdict))))

  (testing "OK: equipment order under its higher category threshold"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :order-supplies :effect :propose :cost 800 :confidence 0.9
                :value {:category "equipment"}}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:escalate? verdict))))))

(deftest ok-supply-order-low-cost
  (testing "OK: supply order under the cost threshold"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :order-supplies :effect :propose :cost 100 :confidence 0.9}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:escalate? verdict))))))

(deftest ok-schedule-field-operation
  (testing "OK: scheduling a field operation (planting/fertilizing/pre-harvest-burn/harvest) is a routine coordination op"
    (let [field {:id "cane-001" :name "Test Plantation North Block"}
          s (store/mem-store {:initial-fields {"cane-001" field}})
          req {:field-id "cane-001"}
          prop {:op :schedule-field-operation :effect :propose :confidence 0.85}
          verdict (gov/check req nil prop s)]
      (is (:ok? verdict))
      (is (not (:escalate? verdict))))))
