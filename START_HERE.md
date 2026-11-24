# AASTHA V3.1 — Fresh GitHub Build Starter

**Generated:** 23 Nov 2026 (date only; creation time not verified)  
**Revision:** 1.0  
**Source:** Existing AASTHA V3.1 Android Source package from this conversation.

## Build the Android APK

1. Create a **new private GitHub repository** (suggested name `AASTHA-Control-Centre`). Do not initialise it with a README.
2. Extract this ZIP on your computer. Upload **everything inside the extracted folder**, not the ZIP file, to the repository root. Keep hidden `.github/workflows/build-apk.yml` and the nested `app/` directories intact.
3. Verify the repository shows `app/`, `scripts/`, `build.gradle`, `settings.gradle`, and `.github/workflows/`.
4. Open **Actions → Build AASTHA V3 Android APK → Run workflow** on the default `main` branch.
5. Open the workflow run. If successful, download artifact `AASTHA-V3-Debug-APK`, extract it and locate `app-debug.apk`.
6. On your Android phone, install **only** the APK from your own verified GitHub build. Back up any existing app data first.
7. Device-test voice input, global web browser search, prompt generation, copied results, PDF/DOCX/XLSX/PNG export and saved records.

## Verification and limitations

- Android app identity: `in.aastha.promptstudio`, launcher name **AASTHA**, version **3.1.0** (code 310).
- Project structure test passed: `python3 scripts/verify_project.py`.
- JavaScript syntax checks passed: `node --check app/src/main/assets/app.js` and `research.js`.
- ZIP integrity passed. **No APK compilation or phone acceptance test was performed in this environment.**
- Live ChatGPT account access, consumer chat history, third-party MCP tool execution, AI web synthesis, and connected NISTHA scheduling are **not included** in this offline-first APK. They require a separately authenticated service and connection permissions.
- The installed app exports only results it holds; it cannot automatically collect responses from a separate ChatGPT app.

## Important GitHub workflow path

`.github/workflows/build-apk.yml` — this hidden folder must be present at the repository root. If the GitHub Actions workflow is not visible, verify that path and inspect the workflow YAML.

## Privacy and security

Keep repository private; do not add API keys or account passwords to code or issue comments. Export and safeguard local backups before deleting or updating an app. Separate private Family Office records from corporate workspaces.
