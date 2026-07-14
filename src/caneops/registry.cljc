(ns caneops.registry
  "Pure validation functions for sugar-cane plantation operations. These are
  called by the Governor to independently verify proposal parameters -- the
  LLM advisor's confidence is NOT sufficient to override these checks.
  Mirrors `riceops.registry` (cloud-itonami-isic-0112) in shape, replacing
  the paddy-specific water-level check with a ratoon-cycle check (this
  crop's own perennial-cultivation-specific measure: sugar cane is
  harvested and regrown from the same rootstock across successive
  ratoon cycles rather than replanted annually)."
  )

(defn cost-exceeds-threshold?
  "Independently verify a proposed spend against its category/default
  threshold. Inclusive at the boundary (exactly-at-threshold does not
  escalate)."
  [cost threshold]
  (> cost threshold))

(defn acreage-non-positive?
  "A logged planting/harvest acreage of zero or negative is not a real
  observation -- reject it as a HARD violation rather than silently
  accepting bad data into the field record."
  [acreage]
  (<= acreage 0))

(defn ratoon-cycle-invalid?
  "A logged ratoon-cycle count below zero is not a physically valid
  observation -- reject it as a HARD violation (mirrors
  `riceops.registry/water-level-negative?`). Zero (a freshly planted/virgin
  \"plant cane\" cycle, before any ratoon) is valid; 1, 2, 3... count
  successive ratoon regrowth cycles from the same rootstock."
  [ratoon-cycle]
  (< ratoon-cycle 0))

(defn confidence-below-floor?
  "Independently verify a proposal's stated confidence against the
  Governor's confidence floor."
  [confidence floor]
  (< confidence floor))
