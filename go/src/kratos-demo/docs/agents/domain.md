# Domain docs

- **Layout:** Single-context
- **CONTEXT.md:** At the repo root
- **ADRs:** `docs/adr/` at the repo root
- **Consumer rules:** See below

## Consumer rules for skills

Skills that read domain docs (`improve-codebase-architecture`, `diagnose`, `tdd`) follow these rules:

1. **CONTEXT.md is a glossary only.** It captures domain language and the relationships between concepts. It does NOT contain implementation details, specs, or scratch notes.
2. **ADRs capture architecturally significant decisions.** An ADR exists only when all three conditions are met: (a) hard to reverse, (b) surprising without context, (c) the result of a real trade-off.
3. **Cross-reference before assuming.** If a skill encounters a term that's undefined in CONTEXT.md, it should ask rather than guess.

## Skills that consume this

- `improve-codebase-architecture` — reads CONTEXT.md for domain language
- `diagnose` — reads CONTEXT.md + ADRs to understand intent before debugging
- `tdd` — reads CONTEXT.md to align test vocabulary with domain language

## Status

CONTEXT.md and docs/adr/ do not yet exist. They will be created lazily — the first skill that needs them will scaffold them.
