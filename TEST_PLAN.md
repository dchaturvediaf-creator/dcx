# AASTHA V3 Acceptance Plan
1. Verify app name AASTHA, devotional icon, and Home opening screen.
2. Navigate categories and search agents.
3. Generate DCX and LADS prompts; copy and open ChatGPT.
4. Add an agent, task, and schedule; restart and verify persistence.
5. Export JSON and restore it; confirm domain isolation policy.
6. Confirm integrations explicitly say not connected.
7. Check Android back, phone portrait, and permission prompts.
8. Verify APK on device and sign release consistently before any in-place upgrade.


## V3.1 tests
1. Android APK: invoke Speak, dictate 'search web for radar technology', verify external browser opens with encoded query. Verify microphone permission/provider availability.
2. Web: research search opens correct chosen engine; local AASTHA search filters agents without web results being imported.
3. Advisor: generate a small [Proposed] suggestion for an objective. Verify voice and regular typed commands.
4. Document: paste executed answer, save as PDF using Android Print/Save as PDF, verify A4 content.
5. DOCX: export and open in Microsoft Word; verify paragraphs, UTF-8 and no corruption.
6. XLSX: export, open with Excel or Google Sheets, verify each line is on its own row; no formulas inferred.
7. PNG: export text card and verify readability; not AI image generation.
8. Image request: generates prompt for ChatGPT; it does not generate an image in this app.
9. Confirm Family Office and corporate domains remain separate; backups restored without sharing data.
10. Confirm external links never load within the privileged WebView; APK must be installed only from your trusted private build.

Verification in this environment: Python project structure and Node.js syntax PASS, isolated Chromium DOM smoke test PASS. Android compilation, native document format opening, voice, external link and physical device validation NOT YET RUN.
