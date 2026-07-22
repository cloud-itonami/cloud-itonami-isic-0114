(ns caneops.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a registered cane field
  through a clean phase-3 auto-commit, an always-escalate crop-health
  concern (human approves), a high-cost supply order (human rejects),
  and a hard-hold (unregistered field), then prints the resulting
  audit ledger. Mirrors `cerealops.sim` (cloud-itonami-isic-0111)."
  (:require [langgraph.graph :as g]
            [caneops.operation :as operation]
            [caneops.store :as store]))

(def grower {:actor-id "grower-01" :role :plantation-operator :phase :phase-3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "grower-01"}}
          {:thread-id tid :resume? true}))

(defn- reject! [actor tid]
  (g/run* actor {:approval {:status :rejected :by "grower-01"}}
          {:thread-id tid :resume? true}))

(defn demo
  "Run the compiled StateGraph through a commit path, an
  escalate->approve->commit path, an escalate->reject->hold path, and
  a hard-hold path; print each result and the final audit ledger."
  []
  (let [st (store/mem-store
            {:initial-fields
             {"cane-001"
              {:id "cane-001"
               :name "Test Plantation North Block"
               :variety "hybrid"}}})
        actor (operation/build st)]

    (println "=== Sugar-Cane Plantation Operations Coordinator Demo ===")

    (println "\n== log-field-record cane-001 (phase-3, governor-clean -> commit) ==")
    (println (exec-op actor "t1"
                      {:op :log-field-record :field-id "cane-001"
                       :acreage 40 :ratoon-cycle 1 :variety "hybrid"
                       :record-type "ratooning"}
                      grower))

    (println "\n== flag-crop-health-concern cane-001 (ALWAYS escalates -- grower approves) ==")
    (let [r (exec-op actor "t2"
                     {:op :flag-crop-health-concern :field-id "cane-001"
                      :concern "サトウキビメイチュウ（stalk borer）の可能性"}
                     grower)]
      (println r)
      (println "-- grower/agronomist approves --")
      (println (approve! actor "t2")))

    (println "\n== order-supplies cane-001 over cost threshold (escalates -- grower rejects) ==")
    (let [r (exec-op actor "t3"
                     {:op :order-supplies :field-id "cane-001"
                      :category "seed-cane" :cost 900}
                     grower)]
      (println r)
      (println "-- grower rejects --")
      (println (reject! actor "t3")))

    (println "\n== log-field-record cane-999 (unregistered -> HARD hold, no interrupt) ==")
    (println (exec-op actor "t4"
                      {:op :log-field-record :field-id "cane-999"
                       :acreage 25 :ratoon-cycle 0 :variety "noble"}
                      grower))

    (println "\n== audit ledger ==")
    (doseq [f (store/ledger st)] (println f))

    {:ledger (store/ledger st)}))

(defn -main
  "clojure -M:run entrypoint."
  [& _args]
  (demo))

(comment
  ;; In a real REPL:
  (demo)
  )
