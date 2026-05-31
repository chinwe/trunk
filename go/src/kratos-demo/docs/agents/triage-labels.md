# Triage labels

Five canonical triage roles and their label strings on this repository.

| Role | Label | Description |
|---|---|---|
| Needs evaluation | `needs-triage` | Maintainer needs to evaluate the issue |
| Awaiting info | `needs-info` | Waiting on the reporter to provide more detail |
| Ready for agent | `ready-for-agent` | Fully specified; an AFK agent can pick it up |
| Ready for human | `ready-for-human` | Needs human judgement or implementation |
| Won't fix | `wontfix` | Will not be actioned |

## State machine

```
needs-triage ──→ needs-info ──→ ready-for-agent
     │                            │
     ├──→ ready-for-human ←───────┘
     └──→ wontfix
```

All labels are the default names — no custom mapping was configured.

## Skills that consume this

- `triage` — applies these labels during the triage workflow
- `to-issues` / `to-prd` — creates issues with the appropriate initial label
