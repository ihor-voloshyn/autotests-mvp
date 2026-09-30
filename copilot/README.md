# Copilot Analysis

This directory stores GitHub Copilot analysis, diagnostics, review notes, and suggested fixes for the autotest framework.

## Purpose

Copilot results are treated as **diagnostic input**, not as authoritative changes. The workflow is:

1. Copilot analysis is saved here.
2. The analysis is reviewed against the current repository state.
3. Confirmed problems are fixed in source/tests.
4. Changes are validated with tests/CI.
5. The corresponding analysis file is kept as traceability.

## File naming

Use:

`YYYY-MM-DD-<short-topic>.md`

Examples:
- `2026-09-30-health-extension.md`
- `2026-09-30-reporting-null-overload.md`

## Recommended analysis format

Each analysis should contain:

- Date
- Commit / branch if known
- Copilot finding
- Affected files
- Error or symptom
- Root-cause hypothesis
- Suggested fix
- Validation status
- Notes from manual review

Do not store secrets, tokens, passwords, private credentials, or production data here.
