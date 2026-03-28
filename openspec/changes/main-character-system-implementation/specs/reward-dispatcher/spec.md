## ADDED Requirements

### Requirement: Unified reward dispatching
The system SHALL dispatch all rewards through RewardDispatcher using RewardBundle structure. All reward sources (tasks, events, gacha, sign-in, shop, item use) MUST use rewardBundleId.

#### Scenario: Dispatch reward bundle
- **WHEN** system receives rewardBundleId with reasonCode and traceId
- **THEN** system loads RewardBundleDef configuration
- **THEN** system dispatches currencies: adds amounts to wallet
- **THEN** system applies statDelta: modifies host attributes with clamping
- **THEN** system grants items: adds to inventory with count and grantType
- **THEN** system applies buffs: adds to activeBuffs with duration
- **THEN** system records all operations in ledger with traceId

#### Scenario: Idempotent dispatch
- **WHEN** same rewardBundleId with same traceId is dispatched multiple times
- **THEN** system checks ledger for existing transaction with this traceId
- **THEN** system skips dispatch if already processed
- **THEN** system returns success without duplicate rewards

### Requirement: Reward bundle structure
The system SHALL support RewardBundle with currencies, statDelta, items, and buffs.

#### Scenario: Currency reward
- **WHEN** reward bundle contains currencies: {STAR_DUST: 100, MOON_GLOW: 10}
- **THEN** system adds 100 to Star Dust balance
- **THEN** system adds 10 to Moon Glow balance

#### Scenario: Stat delta reward
- **WHEN** reward bundle contains statDelta: {VITALITY: 5, MOOD: -2}
- **THEN** system adds 5 to Vitality (clamped to max)
- **THEN** system subtracts 2 from Mood (clamped to min)

#### Scenario: Item reward
- **WHEN** reward bundle contains items: [{itemId: "skill_barrier", count: 1, grantType: "UNIQUE"}]
- **THEN** system adds skill card to inventory
- **THEN** system sets grantType to UNIQUE (no stacking)

#### Scenario: Buff reward
- **WHEN** reward bundle contains buffs: [{buffId: "buff_vitality_boost", durationSec: 3600}]
- **THEN** system adds buff to activeBuffs
- **THEN** system sets buff expiration time to current + 3600 seconds

### Requirement: Reason code tracking
The system SHALL assign reasonCode to all reward dispatches for audit and analysis.

#### Scenario: Task completion reward
- **WHEN** task completes
- **THEN** system dispatches reward with reasonCode=TASK_COMPLETE

#### Scenario: Task verification reward
- **WHEN** task verification succeeds
- **THEN** system dispatches reward with reasonCode=TASK_VERIFY

#### Scenario: Sign-in reward
- **WHEN** user signs in
- **THEN** system dispatches reward with reasonCode=SIGN_IN

#### Scenario: Gacha reward
- **WHEN** user performs gacha pull
- **THEN** system dispatches reward with reasonCode=GACHA

#### Scenario: Event branch reward
- **WHEN** user selects event branch
- **THEN** system dispatches reward with reasonCode=EVENT_BRANCH

#### Scenario: Shop purchase
- **WHEN** user purchases from shop
- **THEN** system dispatches reward with reasonCode=SHOP

#### Scenario: Item use
- **WHEN** user uses consumable item
- **THEN** system dispatches reward with reasonCode=ITEM_USE

### Requirement: Trace ID generation
The system SHALL generate unique traceId for each reward dispatch operation.

#### Scenario: Generate trace ID
- **WHEN** reward dispatch is initiated
- **THEN** system generates unique traceId (UUID or similar)
- **THEN** system associates traceId with all ledger entries for this dispatch

#### Scenario: Query by trace ID
- **WHEN** system queries ledger by traceId
- **THEN** system returns all operations performed for that dispatch
- **THEN** system supports debugging and rollback by traceId

### Requirement: Reward bundle validation
The system SHALL validate reward bundle configuration before dispatching.

#### Scenario: Validate currency references
- **WHEN** system loads reward bundle
- **THEN** system validates all currencyId exist in currency registry
- **THEN** system validates all amounts >= 0

#### Scenario: Validate stat references
- **WHEN** system loads reward bundle
- **THEN** system validates all statKey exist in attribute registry
- **THEN** system validates delta values are within allowed range

#### Scenario: Validate item references
- **WHEN** system loads reward bundle
- **THEN** system validates all itemId exist in ItemDef
- **THEN** system validates count >= 1

#### Scenario: Validate buff references
- **WHEN** system loads reward bundle
- **THEN** system validates all buffId exist in BuffDef
- **THEN** system validates durationSec >= 0

### Requirement: Batch reward dispatch
The system SHALL support dispatching multiple reward bundles in a single transaction.

#### Scenario: Batch dispatch
- **WHEN** system needs to dispatch multiple reward bundles
- **THEN** system processes all bundles in single transaction
- **THEN** system uses single traceId for entire batch
- **THEN** system rolls back all changes if any bundle fails

#### Scenario: Transaction rollback
- **WHEN** any reward bundle in batch fails validation
- **THEN** system rolls back all changes from previous bundles in batch
- **THEN** system returns error without applying any rewards