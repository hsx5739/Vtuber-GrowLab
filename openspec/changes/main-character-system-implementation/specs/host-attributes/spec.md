## ADDED Requirements

### Requirement: Host attributes management
The system SHALL manage host attributes including Fortune (气运), Vitality (能量), Mood (心境), Bond (羁绊), and Focus (专注). Each attribute SHALL have a value range [min, max] with default 0-100.

#### Scenario: Initialize host attributes
- **WHEN** user first launches the app
- **THEN** system initializes all attributes to default values (50 for Vitality/Mood, 0 for Fortune/Bond/Focus)

#### Scenario: Apply attribute change
- **WHEN** system receives a statDelta from reward bundle
- **THEN** system applies the change and clamps the result to [min, max] range
- **THEN** system triggers UI feedback and companion dialogue based on new values

#### Scenario: Daily rollover for Fortune
- **WHEN** daily rollover occurs
- **THEN** system partially resets or decays Fortune value according to configured rules
- **THEN** system records the change in ledger

#### Scenario: Weekly rollover for Focus
- **WHEN** weekly rollover occurs (Monday)
- **THEN** system partially clears or decays Focus value
- **THEN** system resets weekly task progress

### Requirement: Attribute buff system
The system SHALL support buff modifiers that affect attribute values. Buffs SHALL have duration and merge policies.

#### Scenario: Apply buff to attribute
- **WHEN** a buff is activated (e.g., from fruit item)
- **THEN** system adds buff to activeBuffs list with duration
- **THEN** system recalculates effective attribute values with buff modifiers
- **THEN** system notifies UI to display buff status

#### Scenario: Expire buff on rollover
- **WHEN** daily rollover occurs
- **THEN** system removes expired buffs from activeBuffs
- **THEN** system recalculates attribute values without expired buffs

#### Scenario: Buff merge policy
- **WHEN** a new buff with same buffId is applied
- **THEN** system applies merge policy (REFRESH_DURATION or STACK_INTENSITY_MAX_3)
- **THEN** system updates buff duration or intensity accordingly

### Requirement: Attribute-driven companion presentation
The system SHALL derive companion presentation (pose, dialogue, idle variant) from host attribute values and equipped skin.

#### Scenario: Low Vitality companion pose
- **WHEN** Vitality < 30
- **THEN** system displays companion in tired pose (pose_tired)
- **THEN** system selects gentle, comforting dialogue

#### Scenario: High Fortune companion behavior
- **WHEN** Fortune > 80
- **THEN** system adds special effects to gacha animation
- **THEN** companion uses playful dialogue and system-style jokes

#### Scenario: High Bond companion features
- **WHEN** Bond > 70
- **THEN** system unlocks additional idle voice lines
- **THEN** system enables special称呼 and intimate dialogue options

### Requirement: Attribute change tracking
The system SHALL track attribute changes with reason codes and timestamps for audit and debugging.

#### Scenario: Log attribute change
- **WHEN** any attribute value changes
- **THEN** system logs the change with: attribute key, delta, reasonCode, timestamp, traceId
- **THEN** system stores log in ledger for audit

#### Scenario: Query attribute history
- **WHEN** user views attribute details panel
- **THEN** system displays recent changes with reasons
- **THEN** system shows companion explanation for each change