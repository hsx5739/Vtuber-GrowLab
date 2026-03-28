## ADDED Requirements

### Requirement: Currency management
The system SHALL manage multiple currencies including Star Dust (星尘), Moon Glow (月华), and Gacha Tickets. Each currency SHALL have independent balance and transaction history.

#### Scenario: Initialize wallet
- **WHEN** user first launches the app
- **THEN** system initializes wallet with zero balances for all currencies

#### Scenario: Add currency from reward
- **WHEN** reward bundle contains currency grant
- **THEN** system adds amount to corresponding currency balance
- **THEN** system records transaction with reasonCode and traceId in ledger

#### Scenario: Deduct currency for gacha
- **WHEN** user performs gacha pull
- **THEN** system validates sufficient currency balance
- **THEN** system deducts cost amount from currency
- **THEN** system records transaction with reasonCode=GACHA

#### Scenario: Currency balance query
- **WHEN** UI displays currency balance
- **THEN** system returns current balance for each currency
- **THEN** system updates UI in real-time on balance change

### Requirement: Currency transaction ledger
The system SHALL maintain a ledger of all currency transactions for audit and debugging.

#### Scenario: Record transaction
- **WHEN** any currency balance changes
- **THEN** system records transaction with: currencyId, amount (+/-), reasonCode, timestamp, traceId
- **THEN** system stores transaction in persistent ledger

#### Scenario: Query transaction history
- **WHEN** user or admin queries transaction history
- **THEN** system returns filtered transaction list by currency, date range, or reasonCode
- **THEN** system supports pagination for large datasets

### Requirement: Economic self-regulation
The system SHALL implement soft inflation control and hard sinks to maintain economic balance.

#### Scenario: Diminishing returns for A-type tasks
- **WHEN** user completes multiple A-type tasks in same day
- **THEN** system reduces Star Dust reward for subsequent completions
- **THEN** system applies configured diminishing returns formula

#### Scenario: Weekly diversity requirement
- **WHEN** user claims weekly reward
- **THEN** system validates completion of diverse task types (B/C/F/G verified)
- **THEN** system only grants large reward bundle if diversity requirement met

#### Scenario: Currency sinks
- **WHEN** user spends currency on gacha, shop, or event options
- **THEN** system deducts currency from wallet
- **THEN** system ensures total sinks match total sources over time

### Requirement: Currency validation
The system SHALL validate all currency operations to prevent negative balances or invalid amounts.

#### Scenario: Validate sufficient balance
- **WHEN** operation requires currency deduction
- **THEN** system checks if balance >= required amount
- **THEN** system rejects operation with error if insufficient

#### Scenario: Validate non-negative amount
- **WHEN** reward bundle contains currency grant
- **THEN** system validates amount >= 0
- **THEN** system rejects invalid reward bundle configuration

### Requirement: Multi-currency reward bundle support
The system SHALL support multiple currencies in a single reward bundle.

#### Scenario: Grant multiple currencies
- **WHEN** reward bundle contains STAR_DUST: 100 and MOON_GLOW: 10
- **THEN** system adds 100 to Star Dust balance
- **THEN** system adds 10 to Moon Glow balance
- **THEN** system records both transactions with same traceId