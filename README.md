# AASTHA V3.0 — Unified Executive Command Centre (interactive foundation)

This is a working, responsive, offline-first **frontend prototype**, not a live ChatGPT account controller. It provides a dynamic dashboard, searchable editable agent catalogue, prompt generation and ChatGPT copy/open handoff, local actions, schedule catalogue, integration registry, NISTHA workflow launcher and backup/import.

## How to run

For immediate preview, open `index.html` in a modern desktop/mobile browser (some browser features are restricted on `file://` URLs). For service-worker offline installation and the most reliable behaviour, serve the folder over HTTPS or localhost: `python -m http.server 8080` (from the project directory), then open `http://localhost:8080` on that machine. When on a phone, use a secured HTTPS deployment; do not expose a personal computer's web server to the internet.

## What is real vs proposed

- **Working locally:** tiles, navigation, search, prompt generation, edit and save, clipboard handoff, custom agents, task register, schedule catalogue, JSON backup and import.
- **Not integrated:** live ChatGPT account history, automatically discovered installed skills, live Automations, running MCP servers, connected mailbox data, live NISTHA execution, scheduled background runs, notifications and cross-device sync.
- Integration cards deliberately show `Not connected to this prototype`. A separate authenticated backend with an approved connector registry is required.

## Security and domain separation

Never embed ChatGPT cookies, API tokens, MCP secrets or OAuth refresh tokens in frontend HTML, JavaScript, mobile APK or JSON export. The local JSON backup contains user-entered data and should be stored securely. Avoid putting sensitive corporate and Family Office records in shared browser storage; a production app needs workspace-scoped databases, encryption, sign-in and role controls. No external messages, purchases or record modifications happen automatically.

## Proposed production architecture

Android / PWA → authenticated API gateway → agent and skill registry / prompt library / scheduler state / approvals and audit ledger → supported API connectors or MCP adapters (Gmail, Calendar, Drive, GitHub, Plaud, etc.) → explicit ChatGPT/OpenAI handoff via supported API. A server-owned agent runtime could use the OpenAI Agents SDK or API with scoped MCP tools, but would be separate from the user's consumer ChatGPT chat history.

## Recommended implementation order

1. **UX foundation** (this project), confirm small-card interaction and workspace navigation on Android.
2. **Identity & persistence:** authenticated, encrypted workspace-specific records, audit trails and incremental migrations.
3. **Live integrations:** OAuth and MCP adapters, read-only first; permissioned writes with explicit review screens.
4. **Automation:** background job runner, scheduling, approvals, status and notifications.
5. **Android release:** WebView/PWA Trusted Web Activity or native Compose shell; same digital branding, signed builds and installation QA.

No app installation or live credentials are bundled.


## Android packaging
This folder contains an Android WebView wrapper. `app/src/main/assets/` contains the interactive frontend. Build with GitHub Actions (workflow_dispatch) in a new private repository or local Gradle. The APK is not yet compiled or device tested.


## V3.1 Voice, Web & Document Studio
- Android voice dictation via OS speech recognizer (availability and privacy depend on installed speech provider), plus browser speech recognition when supported. No continuous listening.
- Worldwide search opens Google/Bing/DuckDuckGo in external browser. No result ingestion or search API integration yet.
- Local deterministic next-action guidance clearly marked [Proposed], with no claim of AI reasoning.
- AASTHA can export only text it holds: native PDF via Android printing, DOCX and XLSX via native OOXML packaging, PNG as text card. DOCX/XLSX are basic text-oriented layouts, not reproductions of complex ChatGPT formatting.
- ChatGPT-generated answers must be pasted into AASTHA until a supported authenticated execution backend exists.
- Google Sheets can open/import XLSX; direct Google Sheets write remains a future authenticated integration. Image generation likewise needs a connected AI image service.
- Keep domain isolation and explicit approvals; never store API keys within WebView.
