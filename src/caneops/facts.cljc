(ns caneops.facts
  "Reference facts for sugar-cane plantation operations coordination: supply
  category cost policy, sugar-cane-variety classification, and field
  operation types. This namespace contains pure lookup functions for domain
  reference data -- the Governor and Advisor consult these instead of
  inventing thresholds. Mirrors `riceops.facts` (cloud-itonami-isic-0112) in
  shape, adapted to sugar cane specifics: a PERENNIAL crop grown through
  ratoon-cropping cycles (successive harvests from the same rootstock)
  rather than annual replanting, with pre-harvest field burning and
  brix (sugar-content) testing rather than paddy water-level management.")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (grower/ops-manager)."
  {"seed-cane"
   {:id "seed-cane" :name "苗茎（種苗）" :cost-threshold 500}

   "fertilizer"
   {:id "fertilizer" :name "肥料" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備（収穫機・灌漑ポンプ等含む）" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def sugar-cane-varieties
  "Sugar-cane varieties this actor's field records may cover (ISIC 0114:
  growing of sugar cane -- other crops are out of scope)."
  {"hybrid"       {:id "hybrid" :name "ハイブリッド品種（商業栽培主流）"}
   "noble"        {:id "noble" :name "ノーブルケーン（S. officinarum系原種）"}
   "energy-cane"  {:id "energy-cane" :name "エナジーケーン（バイオ燃料用高繊維品種）"}
   "chewing-cane" {:id "chewing-cane" :name "チューイングケーン（生食用）"}})

(defn sugar-cane-variety-by-id [id]
  (get sugar-cane-varieties id))

(def field-operation-types
  "Reference set of field-operation types this actor's
  schedule-field-operation proposals commonly cover, spanning both a fresh
  planting cycle and the successive ratoon-cropping cycles that follow it
  (harvest the standing cane, then let the rootstock regrow rather than
  replanting -- typically several ratoon cycles before the field is
  replanted). Informational only -- NOT a validated enum; the
  advisor/operator may propose other operation-type strings and the
  Governor does not reject unlisted values here."
  #{"planting" "fertilizing" "pre-harvest-burn" "harvest" "ratooning"})
