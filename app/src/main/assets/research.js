/* AASTHA V3.1 — local voice, web handoff, advisory and native export. */
'use strict';
const ra = (id)=>document.getElementById(id);
function showResearch(){go('research')}
function beginVoice(target){
  window.aasthaVoiceTarget=target||'globalCommand';
  if(window.AasthaAndroid && typeof window.AasthaAndroid.startVoice==='function'){
    window.AasthaAndroid.startVoice(ra('voiceLanguage')?.value||'en-IN'); return;
  }
  const SpeechRecognition=window.SpeechRecognition||window.webkitSpeechRecognition;
  if(!SpeechRecognition){alert('Voice recognition is unavailable in this browser. Use the Android app or type your command.');return}
  const rec=new SpeechRecognition();rec.lang=ra('voiceLanguage')?.value||'en-IN';rec.interimResults=false;rec.maxAlternatives=1;
  rec.onresult=(event)=>window.aasthaVoiceResult(event.results[0][0].transcript);
  rec.onerror=()=>toast('Voice recognition unavailable; type your command');rec.start();
}
window.aasthaVoiceResult=function(text){
  const target=ra(window.aasthaVoiceTarget||'globalCommand')||ra('globalCommand');target.value=text;
  if(target.id==='globalCommand')executeCommand();else toast('Dictation entered');
};
function executeCommand(){
 const q=ra('globalCommand').value.trim();if(!q)return toast('Enter a command');
 const n=q.toLowerCase();
 quickIdea(q);
 if(/\b(pdf|word|docx|excel|xlsx|document)\b|पीडीएफ|दस्तावेज/.test(n)&&/\b(export|download|create|make|generate)\b/.test(n)){showResearch();ra('researchResult').focus();toast('Paste the result to export');return;}
 if(/\b(schedules?|reminders?|calendar)\b/.test(n)){go('schedules');return}
 if(/\b(nistha|delegat|approval)\b/.test(n)){go('nistha');return}
 if(/\b(agent|skills?|dcx|smriti|kuber)\b/.test(n)){go('agents');ra('agentSearch').value=q;renderAgents();return}
 if(/\b(search|google|bing|web|internet|find online)\b|खोज|इंटरनेट|वेब/.test(n)){showResearch();ra('webQuery').value=q.replace(/^(search (the )?(web|internet)( for)?|google|find online|bing)\s*/i,'');searchWeb();return}
 if(/\b(advice|advise|recommend|next steps?|what next)\b|सलाह|सुझाव/.test(n)){showResearch();ra('adviceQuery').value=q;adviseNext();return}
 go('prompts');ra('promptDetails').value=q;generatePrompt();toast('Draft prompt prepared');
}
function researchLocal(){const q=ra('webQuery').value.trim();if(!q)return toast('Enter keywords');go('agents');ra('agentSearch').value=q;renderAgents();}
function searchWeb(){
 const q=ra('webQuery').value.trim();if(!q)return toast('Enter your search query');
 const base={google:'https://www.google.com/search?q=',bing:'https://www.bing.com/search?q=',duckduckgo:'https://duckduckgo.com/?q='};
 const url=base[ra('searchProvider').value]+encodeURIComponent(q);
 ra('researchStatus').textContent='Opening worldwide search in your browser; results are not copied into AASTHA automatically.';
 if(window.AasthaAndroid?.openWebSearch)window.AasthaAndroid.openWebSearch(url);
 else window.open(url,'_blank','noopener,noreferrer');
}
function adviseNext(){
 const q=ra('adviceQuery').value.trim();if(!q)return toast('Describe an objective');const lower=q.toLowerCase();
 let tip='Clarify the desired outcome and deadline before selecting an action.';
 if(/fat|drone|uav|acceptance|test/.test(lower))tip='Confirm the configuration baseline, acceptance criteria, safety controls and evidence required for each test.';
 else if(/email|mail|correspondence|dcx/.test(lower))tip='Separate actionable correspondence from information-only mail, then verify owners and response deadlines.';
 else if(/investment|portfolio|ipo|finance|expense/.test(lower))tip='Reconcile source statements first; identify exposure, risk and evidence gaps before deciding on transactions.';
 else if(/research|web|market|vendor|technology/.test(lower))tip='Compare at least two independent sources, record their dates, and distinguish specifications from marketing claims.';
 else if(/schedule|deadline|meeting/.test(lower))tip='Identify time-critical commitments, dependencies and who must confirm each milestone.';
 const output='[Proposed — Local AASTHA guidance]\n1. Goal: '+q+'\n2. First check: '+tip+'\n3. Next: identify evidence, responsible owner and a realistic due date.\n4. Verify with connected sources or ChatGPT before acting.\n\nThis is deterministic guidance, not a live AI analysis.';
 ra('adviceContent').textContent=output;return output;
}
function importPromptText(){ra('researchResult').value=ra('promptPreview').value||'';toast('Current prompt inserted. This is NOT an executed answer.');}
function importAdviceText(){ra('researchResult').value=ra('adviceContent').textContent||'';toast('Advice inserted');}
async function copyResearchOutput(){const text=ra('researchResult').value;if(!text)return toast('Enter content first');
 if(window.AasthaAndroid?.copyText)window.AasthaAndroid.copyText(text);else if(navigator.clipboard)await navigator.clipboard.writeText(text);else{ra('researchResult').select();document.execCommand('copy')};toast('Copied')}
function exportOutput(fmt){const title=ra('documentTitle').value.trim()||'AASTHA Brief', content=ra('researchResult').value;
 if(!content.trim())return toast('Paste or enter the result before exporting');
 if(window.AasthaAndroid?.exportDocument){window.AasthaAndroid.exportDocument(fmt,title,content);return}
 if(fmt==='pdf'){const child=window.open('','_blank');if(!child)return alert('Allow pop-ups to print this document');
 child.document.write('<!doctype html><meta charset="utf-8"><title>'+htmlEscape(title)+'</title><style>body{font:12pt/1.5 sans-serif;padding:18mm}pre{white-space:pre-wrap;overflow-wrap:anywhere}</style><h1>'+htmlEscape(title)+'</h1><pre>'+htmlEscape(content)+'</pre>');child.document.close();child.focus();child.print();return;}
 toast('Word, Excel and PNG exports are available in the Android APK.');
}
function htmlEscape(s){return String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}

function quickIdea(q){
 const n=q.toLowerCase();let idea='Identify the outcome, source evidence, owner and deadline before proceeding.';
 if(/email|mail|dcx/.test(n))idea='Review urgency and response ownership before drafting replies.';
 else if(/drone|fat|uav|test/.test(n))idea='Confirm the test baseline and acceptance criteria before selecting procedures.';
 else if(/research|search|web/.test(n))idea='Compare sources and record the publication date before relying on a finding.';
 else if(/finance|investment|portfolio/.test(n))idea='Reconcile records and check risk before making any decision.';
 else if(/document|pdf|report/.test(n))idea='Capture the completed answer, verify its sources, then export the preferred format.';
 const e=ra('quickAdvice');if(e){e.style.display='block';e.textContent='💡 AASTHA suggestion [Proposed]: '+idea;}
}
function prepareImagePrompt(){
 const subject=ra('researchResult').value.trim()||ra('adviceQuery').value.trim()||ra('webQuery').value.trim();
 if(!subject)return toast('Describe the image in the results or objective field first');
 go('prompts');ra('promptPreview').value='Create a professional image based on the following request. Use clear composition, appropriate typography, accurate subject details, and a clean AASTHA visual style.\n\nImage request: '+subject+'\n\nReturn the rendered image, not tool parameters.';
 toast('Image-generation prompt prepared. Copy and open ChatGPT.');
}
