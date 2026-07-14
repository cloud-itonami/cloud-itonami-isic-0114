(ns caneops.sim
  "Simple simulation/demo runner for the Sugar-Cane Plantation Operations
  Coordinator actor. Used to validate that the actor flow compiles and
  basic proposal flow works. Mirrors `riceops.sim` (cloud-itonami-isic-0112)."
  (:require [caneops.operation :as operation]
            [caneops.store :as store]))

(defn demo
  "Run a simple demo scenario: register a cane field, propose a
  field-record log (with ratoon-cycle), and check the disposition flow."
  []
  (let [;; Create store with a registered cane field
        st (store/mem-store
            {:initial-fields
             {"cane-001"
              {:id "cane-001"
               :name "Test Plantation North Block"
               :variety "hybrid"}}})

        ;; Build actor
        actor (operation/build st)

        ;; Create a request to log a field record
        request {:op :log-field-record
                 :field-id "cane-001"
                 :acreage 40
                 :ratoon-cycle 1
                 :variety "hybrid"
                 :record-type "ratooning"}

        ;; Context with phase 0 (simulation)
        context {:actor-id "cane-ops-01"
                 :role :plantation-operator
                 :phase :phase-0}]

    (println "=== Sugar-Cane Plantation Operations Coordinator Demo ===")
    (println "Demo field: cane-001")
    (println "Request: log-field-record")
    (println "Phase: phase-0 (simulation)")
    (println "Expected: escalate (phase-0 forces human review of all commits)")
    (println)
    (let [result (actor request context)]
      (println "Result disposition:" (:disposition result))
      result)))

(defn -main
  "clojure -M:run entrypoint."
  [& _args]
  (demo))

(comment
  ;; In a real REPL:
  (demo)
)
