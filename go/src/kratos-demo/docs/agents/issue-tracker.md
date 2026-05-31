# Issue tracker

- **Type:** GitHub Issues
- **Repository:** `github.com/chinwe/trunk`
- **CLI:** `gh` (GitHub CLI)

## Conventions

- Issues are created and managed via the `gh` CLI.
- Labels follow the triage vocabulary defined in `triage-labels.md`.
- When creating an issue, always include context linking back to the originating conversation or decision.

## Skills that consume this

- `to-issues` — reads issues from GitHub Issues
- `to-prd` — writes PRDs as GitHub Issues
- `triage` — applies labels and moves issues through the triage state machine
- `qa` — references issues during verification
- `diagnose` — links bug reports to existing issues

## Overriding

To switch to a different issue tracker (GitLab, local markdown, Jira), re-run the `setup-matt-pocock-skills` skill.
