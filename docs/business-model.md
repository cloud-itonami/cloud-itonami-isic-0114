# Business Model: Sugar-Cane Plantation Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0114`
- ISIC Rev. 4: `0114`
- Industry: Growing of sugar cane
- Social impact: food-security, rural-employment, environmental-stewardship

## Customer

- Small-to-medium sugar-cane plantations (hybrid, noble, energy-cane,
  chewing-cane)
- Cane growers' cooperatives and mill-contracted growers
- Diversified plantation operations running mixed plant-cane/ratoon
  schedules
- Smallholder cane producers (extension-service integrations)

## Offer

- Cane field management and record-keeping (acreage, ratoon-cycle, yield,
  brix)
- Planting/fertilizing/pre-harvest-burn/harvest scheduling coordination
- Crop-health and pest (borer)/disease tracking
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-hectare-per-season pricing)
- Supply chain integration fees
- API access for agronomist/extension-service/mill partners
- Data analytics and reporting add-ons

## Trust Controls

- No direct field-equipment operation without human sign-off
- No finalized pre-harvest-burn or pesticide-application decisions by the actor
- All field-operation scheduling proposals are proposals, not commands
- Cane field registration is required before any operation
- All crop-health concerns are automatically escalated
- High-cost supply orders require approval
- Audit ledger is append-only and never editable

## What we do NOT do

- **Agronomic decisions** (what/when/how much to plant, fertilize, burn,
  harvest) — the grower/agronomist decides
- **Pre-harvest-burn decisions** — the grower/agronomist decides
- **Pesticide-application decisions** — the agronomist/grower decides
- **Direct field-equipment operation** — the robot manages records and
  logistics only
- **Economic decisions** (crop mix, marketing, land use, mill contracts) —
  remain human authority

## Supported Operations

### Field Record Logging
- Planting records (variety, acreage, date)
- Ratoon-cycle records (cycle count since last replanting)
- Yield records
- Brix-test records (sugar-content sampling)
- Field-condition notes (logging only, not decision-making)

### Field-Operation Scheduling
- Schedule planting, fertilizing, pre-harvest burn, and harvest windows
- Track equipment/labor/mill-slot availability
- Propose follow-up field visits (not order them directly)

### Crop-Health Concern Escalation
- Flag suspected borer (stalk borer) infestation
- Report disease symptoms or drought stress
- Automatic escalation to grower/agronomist

### Supply Procurement
- Seed-cane (planting sett) orders
- Fertilizer orders
- Equipment procurement (including harvest machinery and irrigation pumps)
- Cost threshold escalation for large orders
