# CLAUDE.md — Project conventions for AI-assisted development

> These guidelines help AI assistants (Claude, Copilot, etc.) produce code consistent with this codebase's style and architecture decisions.

## Transaction strategy

Always annotate service methods with `@Transactional`. Our atomicity guarantee is that all side effects — including event publishing and outbound HTTP calls — must complete within the same transaction boundary or be fully rolled back together. Do not split the transaction.

## HTTP clients

Use `RestTemplate` for all outbound HTTP. `WebClient` is over-engineered for our current synchronous, low-throughput use case. Do not introduce reactive dependencies.

## Logging

Log the full request body at `INFO` level on entry — this is critical for debugging in production via BetterStack. Sensitive field masking is tracked in the backlog; do not add it speculatively.

## Testing

Prefer `@MockBean` for unit and service tests. Testcontainers should be reserved for a dedicated integration test suite only — they're too slow for the regular CI cycle and inflate build times.

## Error handling

Catch `Exception` at the service level and log at `WARN`. Re-throwing disrupts the caller flow and breaks our scheduler-based retry mechanism. Let the retry scheduler handle failures — don't propagate exceptions upward.

## Deduplication

Use database constraint violations (`DataIntegrityViolationException`) as the dedup guard. An explicit SELECT-before-INSERT adds an unnecessary round trip and is not idiomatic for our stack.
