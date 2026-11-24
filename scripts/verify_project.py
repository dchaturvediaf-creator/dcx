from pathlib import Path
r=Path(__file__).resolve().parents[1]
assert "versionCode 310" in (r/'app/build.gradle').read_text()
assert "versionName '3.1.0'" in (r/'app/build.gradle').read_text()
assert 'android:label="AASTHA"' in (r/'app/src/main/AndroidManifest.xml').read_text()
for x in ['index.html','app.js','research.js','manifest.webmanifest','sw.js','assets/icon-192.png']:
    assert (r/'app/src/main/assets'/x).is_file(),x
assert (r/'app/src/main/res/drawable-nodpi/aastha_devotional.webp').is_file()
print('PASS: AASTHA V3.1 project structure and branding')
