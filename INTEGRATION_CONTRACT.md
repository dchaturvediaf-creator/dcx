# AASTHA V3 — Controlled Integration Contract (proposed)

**Status:** Architecture specification. Nothing below is connected or deployed.

## Core API routes

- `GET /api/v1/catalogue/agents`: agent and skill registry scoped by workspace and user.
- `POST /api/v1/prompts/prepare`: structured prompt generation from reviewed templates.
- `GET /api/v1/actions`: evidence-linked task register filtered by workspace.
- `GET /api/v1/schedules`: read-only schedule state from supported providers; do not infer that a schedule is live from a local card.
- `GET /api/v1/connections`: provider, scope, connection status, token last-verified timestamp; never return secrets.
- `POST /api/v1/operations/plan`: prepare an operation, list proposed tool calls and required approvals.
- `POST /api/v1/operations/{id}/approve`: explicit user approval for a precisely scoped action.
- `GET /api/v1/audit`: immutable evidence and result record with owner, timestamp and outcome.

## Read/write boundary

1. Read-only commands should request the minimum provider scope.
2. External writes (send, forward, delete, purchases, order, account mutation) require an exact consent preview and explicit confirmation.
3. Family Office, LEWL, UDPL, LADS, Nabhoman and other workspaces use separate server-side access controls and storage.
4. Local Android storage never holds API keys, OAuth refresh tokens or confidential source documents in plain text.
5. Connections are managed server-side with OAuth or authorised MCP flows; audit all tool requests and outputs.
6. The consumer ChatGPT account is not a general public API; integration only through documented supported methods.
7. ChatGPT automations and connected services may require distinct permissions and APIs.

## Releases

- `V3.0` local UX foundation: supplied.
- `V3.1` authenticated service and database: proposed.
- `V3.2` agent/skill registry and discovery provider: proposed.
- `V3.3` MCP/OAuth live read-only adapters: proposed.
- `V3.4` NISTHA review/approvals, scheduling, event status: proposed.
- `V3.5` notification, encrypted backups and signed Android production release: proposed.


### V3.1 backend requirements
- Search service: authenticated HTTPS endpoint, source attribution, date, per-domain constraints, usage limits.
- AI execution: store prompt, model, execution time, sources, output and provenance with explicit project scope.
- Exports: only execute on captured result; PDF/DOCX/XLSX are on-device formats and must be verified on hardware.
- Images: generated through authorised image service; native text-card PNG is not AI image generation.
- Voice: push-to-talk; explicit recording activation; do not promise always-on wake word.
