## ADDED Requirements

### Requirement: Task type registry
The system SHALL support 8 task types (A-H) with extensible handler registration. Each task type SHALL have dedicated validation and completion logic.

#### Scenario: Register task handler
- **WHEN** system starts
- **THEN** system registers handlers for all task types: A (self_report), B (timer), C (health), D (chat), E (sign_in), F (photo), G (voice), H (scripted_event)
- **THEN** system validates handlerParams schema for each type

#### Scenario: Create A-type task
- **WHEN** system loads task configuration with taskType=A
- **THEN** system creates task instance with self_report handler
- **THEN** system enables "I completed" button with optional note field

#### Scenario: Create B-type task
- **WHEN** system loads task configuration with taskType=B
- **THEN** system creates task instance with timer handler
- **THEN** system displays embedded timer with durationSec from handlerParams

#### Scenario: Create C-type task
- **WHEN** system loads task configuration with taskType=C
- **THEN** system creates task instance with health handler
- **THEN** system displays health metric threshold and authorization button

### Requirement: Task state machine
The system SHALL implement unified task state machine: LOCKED → AVAILABLE → IN_PROGRESS → COMPLETED → (VERIFYING → COMPLETED_VERIFIED).

#### Scenario: Task becomes available
- **WHEN** task unlock conditions are met
- **THEN** system transitions task from LOCKED to AVAILABLE
- **THEN** system displays task in task list

#### Scenario: Start task
- **WHEN** user clicks task
- **THEN** system transitions task to IN_PROGRESS
- **THEN** system opens task detail page

#### Scenario: Complete A-type task
- **WHEN** user clicks "I completed" on A-type task
- **THEN** system transitions task to COMPLETED
- **THEN** system dispatches rewardBundleId (base reward)
- **THEN** system does NOT dispatch rewardVerifiedBundleId

#### Scenario: Complete B-type task with verification
- **WHEN** timer completes and user confirms
- **THEN** system transitions task to COMPLETED_VERIFIED
- **THEN** system dispatches rewardBundleId (base reward)
- **THEN** system dispatches rewardVerifiedBundleId (bonus reward)

### Requirement: Task reward tiering
The system SHALL support tiered rewards: base reward for completion, bonus reward for verification.

#### Scenario: Base reward only
- **WHEN** task has only rewardBundleId
- **THEN** system dispatches only base reward upon completion
- **THEN** system records reasonCode=TASK_COMPLETE

#### Scenario: Base + verified reward
- **WHEN** task has both rewardBundleId and rewardVerifiedBundleId
- **THEN** system dispatches base reward with reasonCode=TASK_COMPLETE
- **THEN** system dispatches verified reward with reasonCode=TASK_VERIFY
- **THEN** system only dispatches verified reward if verification succeeds

### Requirement: Task constraints and limits
The system SHALL enforce task completion limits and diminishing returns.

#### Scenario: Daily completion limit
- **WHEN** user attempts to complete task beyond maxCompletionsPerDay
- **THEN** system blocks completion with error message
- **THEN** system displays remaining completions for today

#### Scenario: Diminishing returns for A-type
- **WHEN** user completes A-type task multiple times in same day
- **THEN** system reduces reward for subsequent completions
- **THEN** system applies configured diminishing returns formula
- **THEN** system eventually grants near-zero reward

#### Scenario: Weekly task reset
- **WHEN** weekly rollover occurs
- **THEN** system resets progress for all WEEKLY group tasks
- **THEN** system refreshes weekly task pool

### Requirement: Task unlock conditions
The system SHALL support conditional task unlocking based on attributes, flags, or items.

#### Scenario: Unlock by minimum bond
- **WHEN** Bond reaches minBond threshold
- **THEN** system unlocks tasks with unlockCondition.minBond met
- **THEN** system displays newly available tasks

#### Scenario: Unlock by required item
- **WHEN** user acquires required item
- **THEN** system unlocks tasks with unlockCondition.requiredItems satisfied
- **THEN** system displays newly available tasks

### Requirement: Task weekly counting
The system SHALL support counting verified task completions for weekly rewards.

#### Scenario: Count verified completion
- **WHEN** task completes with verification (B/C/F/G)
- **THEN** system increments weekly.verifiedTaskCount
- **THEN** system updates weekly progress display

#### Scenario: Weekly reward claim
- **WHEN** user claims weekly reward
- **THEN** system validates weekly.verifiedTaskCount >= required threshold
- **THEN** system dispatches weekly reward bundle
- **THEN** system resets weekly counter

### Requirement: Task configuration validation
The system SHALL validate task configuration before loading.

#### Scenario: Validate reward bundle reference
- **WHEN** system loads task configuration
- **THEN** system validates rewardBundleId exists in RewardBundleDef
- **THEN** system validates rewardVerifiedBundleId exists if specified
- **THEN** system rejects invalid configuration with error

#### Scenario: Validate handler parameters
- **WHEN** system loads task configuration
- **THEN** system validates handlerParams contains only allowed keys for taskType
- **THEN** system warns on unknown parameters (does not fail)