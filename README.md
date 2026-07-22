# cloud-itonami-isic-0114

Open Occupation Blueprint for **ISIC Rev. 4 0114**: Growing of sugar cane.

This repository implements a forkable OSS **sugar-cane plantation
operations coordinator**: a field-management and record-keeping robot
manages planting/ratoon-cycle/yield/brix-test logging, cane-field-operation
scheduling (planting, fertilizing, pre-harvest burn, harvest), and supply
procurement under a governor-gated actor, so a sugar-cane plantation keeps
its own operational records and maintains full transparency over decisions.

**Maturity: `:implemented`.** `src/caneops/` implements the
`CaneOpsAdvisor` (`caneops.advisor`) and the independent `CaneOperations
Governor` (`caneops.governor`), composed by `caneops.operation` following
the itonami actor pattern (ADR-2607011000): `intake -> advise -> govern ->
decide -> commit | request-approval -> commit | hold`, compiled to a real
`langgraph-clj` `StateGraph` (`langgraph.graph/state-graph` +
`compile-graph`, mirroring `cerealops.operation`, cloud-itonami-isic-0111)
with `interrupt-before #{:request-approval}` and checkpoint-based
human-in-the-loop resume for escalated operations. Every commit/hold/
approval-rejected decision fact is appended to `caneops.store`'s
append-only audit ledger (`ledger`/`append-ledger!`), implemented on both
`MemStore` and a `DatomicStore` (backed by `langchain.db` via
`kotoba-lang/langchain-store`) that pass the same store-contract test
(`test/caneops/store_contract_test.cljc`). 37 tests / 116 assertions
green (`clojure -M:dev:test`); the demo runner (`clojure -M:dev:run`)
drives the compiled graph end-to-end through a commit path, an
escalate→approve→commit path, an escalate→reject→hold path, and a
hard-hold path, printing the resulting audit ledger.

## Perennial crop, ratoon-cropping

Unlike an annual field crop (cereals: ISIC 0111, rice: ISIC 0112), sugar
cane is a **perennial** crop: after the first harvest of a freshly planted
("plant cane") stand, the same rootstock regrows and is harvested again
across successive **ratoon** cycles (`ratooning`) before the field is
eventually replanted. `caneops.facts/field-operation-types` reflects both
the initial `"planting"` and the recurring `"ratooning"` cycle, and
`:log-field-record` proposals carry a `:ratoon-cycle` count that the
Governor independently validates (see `ratoon-cycle-invalid` below).

## What this does NOT do

This actor coordinates **back-office logistics only**. It explicitly does **NOT**:

- **Direct field-equipment operation** — remains the grower's exclusive
  authority
- **Pre-harvest-burn decisions** — remains the grower/agronomist authority
- **Pesticide-application decisions** — remains the agronomist/grower authority
- **Agronomic decision authority** (what/when/how much to plant, fertilize,
  burn, or harvest) — remains human authority; this actor only coordinates
  the logistics around those decisions
- **Direct execution of any kind** — any proposal for direct field-equipment
  control or finalizing a pre-harvest-burn or pesticide-application decision
  is a hard block

## HARD invariants (always hold, never overridable)

1. **field-not-registered** — the request's `field-id` must resolve to a
   registered cane field in the Store before any proposal can proceed
2. **no-execution** — every proposal's `:effect` must be `:propose` (the
   governor never directly operates field equipment, never finalizes a
   pre-harvest-burn or pesticide-application decision)
3. **equipment-or-decision-blocked** — `:operate-field-equipment`,
   `:finalize-burn-decision`, and `:finalize-pesticide-application`
   proposals are unconditionally, permanently blocked
4. **op-not-allowed** — any op outside the closed allowlist below is rejected
5. **field-record-invalid** — `:log-field-record` with a non-positive acreage
   is rejected
6. **ratoon-cycle-invalid** — `:log-field-record` with a negative
   ratoon-cycle count is rejected (zero, a freshly planted "plant cane"
   cycle, is valid)

## Always-escalate operations (human sign-off, regardless of confidence)

- `:flag-crop-health-concern` — any pest (borer)/disease/drought-stress
  concern → automatic escalation
- `:order-supplies` over its category cost threshold (default 500 currency
  units; see `caneops.facts/supply-categories`)
- Any proposal with confidence below the Governor's floor (0.7)

## Operational requests (closed allowlist, all `:effect :propose`)

```text
:log-field-record
  — record planting/ratoon-cycle/yield/brix-test data
  — requires a registered cane field; non-positive acreage or negative
    ratoon-cycle is rejected

:schedule-field-operation
  — propose a planting/fertilizing/pre-harvest-burn/harvest scheduling
    operation
  — does NOT make agronomic decisions

:flag-crop-health-concern
  — surface a pest (borer), disease, or drought-stress concern
  — ALWAYS escalates for human review

:order-supplies
  — procurement for seed-cane, fertilizer, equipment (including harvest
    machinery / irrigation pumps)
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs the
physical domain work**. Here a field-management robot handles:

- Cane field record logging and entry (acreage, ratoon-cycle, yield, brix)
- Field-operation scheduling and reminders (planting/fertilizing/
  pre-harvest-burn/harvest)
- Supply inventory and ordering
- Audit ledger maintenance

The **CaneOperationsGovernor** is the independent safety layer that gates all
proposals before a robot action is executed. The governor never dispatches
hardware directly; `:high`/`:safety-critical` actions (such as escalated
crop-health concerns or high-cost supply orders) require human sign-off.

## Core Contract

```text
operational request (log, schedule, concern, order)
        |
        v
CaneOpsAdvisor -> CaneOperationsGovernor -> phase gate -> commit, or request-approval for human sign-off
        |
        v
robot actions (gated) + operating records + append-only audit ledger
```

No automated operation can dispatch a robot action the governor refuses, suppress an
operating record, or hide a crop-health concern without governor approval and audit
evidence.

## Module structure

Mirrors `cloud-itonami-isic-0111` (`cerealops.*`) module-for-module, with the
cereal-specific acreage-only check extended by a perennial-crop-specific
ratoon-cycle check:

- `caneops.facts` — reference data: supply-category cost thresholds,
  sugar-cane varieties, field-operation types
- `caneops.registry` — pure independent verification functions
  (cost/acreage/ratoon-cycle/confidence)
- `caneops.store` — `Store` protocol: cane-field registration lookup +
  append-only audit ledger, implemented by `MemStore` (in-memory, default)
  and `DatomicStore` (`langchain.db`-backed, via `kotoba-lang/langchain-store`)
- `caneops.advisor` — `Advisor` protocol + `MockAdvisor` (the sealed LLM/
  decision node; a real-LLM `Advisor` implementation is the documented next
  seam, same as every sibling cloud-itonami actor's advisor)
- `caneops.governor` — `CaneOperationsGovernor`: hard invariants + escalation
  gates
- `caneops.phase` — 0→3 rollout phase gate
- `caneops.operation` — compiles the `langgraph-clj` `StateGraph`: advise →
  govern → decide → commit | request-approval → commit | hold, with
  `interrupt-before` + checkpoint-based resume for escalated operations
- `caneops.sim` — demo runner (`clojure -M:dev:run`)

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISIC Rev. 4 `0114`). Required capabilities:

- :robotics
- :identity
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Testing

```bash
clojure -M:dev:test   # run the test suite (langgraph/langchain-store resolved via local sibling checkouts)
clojure -M:lint       # clj-kondo, 0 errors / 0 warnings
clojure -M:dev:run    # demo runner -- drives the compiled StateGraph end-to-end
```

`:dev` pins the transitive `langchain` dependency to the in-monorepo local
checkout (`../../kotoba-lang/langchain`) for offline workspace development;
a standalone fork should override `deps.edn`'s `:local/root` coordinates
with git coordinates (see `deps.edn`'s own comment).

## License

AGPL-3.0-or-later.
