package in.aastha.promptstudio;

import android.app.Activity;
import android.app.PrintManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.speech.RecognizerIntent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;

/** AASTHA: offline command centre with explicit external-browser and native-export handoffs. */
public class MainActivity extends Activity {
    private static final int SAVE_JSON=1001, PICK_JSON=1002, VOICE_REQUEST=1003, EXPORT_REQUEST=1004;
    private static final String LOCAL_PAGE="file:///android_asset/index.html";
    private WebView web, printWeb;
    private android.webkit.ValueCallback<Uri[]> fileCallback;
    private String pendingJson, exportFormat, exportTitle, exportText;

    @Override public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(220,238,255));
        web=new WebView(this);setContentView(web);
        configureWebView(web,true);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                String url=request.getUrl().toString();
                if(url.equals(LOCAL_PAGE))return false;
                // Never load untrusted sites into a WebView that exposes a native JS bridge.
                toast("External navigation blocked. Use Search Web or Open ChatGPT.");return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view,android.webkit.ValueCallback<Uri[]> callback,FileChooserParams params){
                if(fileCallback!=null)fileCallback.onReceiveValue(null);
                fileCallback=callback;
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/json");intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/json","text/plain"});
                try{startActivityForResult(intent,PICK_JSON);}catch(Exception ex){fileCallback.onReceiveValue(null);fileCallback=null;toast("File picker unavailable");}
                return true;
            }
        });
        web.loadUrl(LOCAL_PAGE);
    }
    private void configureWebView(WebView w,boolean bridge){
        WebSettings s=w.getSettings();s.setJavaScriptEnabled(bridge);s.setDomStorageEnabled(bridge);
        s.setAllowFileAccess(bridge);s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);
        s.setBlockNetworkLoads(true);s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        if(bridge)w.addJavascriptInterface(new Bridge(),"AasthaAndroid");
    }
    private void toast(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
    private void external(String url,boolean webSearch){
        try{
            Uri u=Uri.parse(url);if(!"https".equals(u.getScheme()))throw new Exception("Invalid scheme");
            String host=u.getHost();if(host==null)throw new Exception("Missing host");
            if(webSearch){
                if(!host.equals("www.google.com")&&!host.equals("www.bing.com")&&!host.equals("duckduckgo.com"))throw new Exception("Unapproved search service");
            }else if(!host.equals("chatgpt.com")&&!host.equals("www.chatgpt.com"))throw new Exception("Unapproved ChatGPT link");
            startActivity(new Intent(Intent.ACTION_VIEW,u));
        }catch(Exception e){toast("Unable to open external browser: "+e.getMessage());}
    }
    private void voice(String language){
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN".equals(language)?"hi-IN":"en-IN");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak your AASTHA command");
        try{startActivityForResult(i,VOICE_REQUEST);}catch(Exception e){toast("Speech recognition provider unavailable; use keyboard input");}
    }
    public class Bridge {
        @JavascriptInterface public void copyText(String text){
            ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("AASTHA",text));toast("Copied to clipboard");}
        @JavascriptInterface public void openChat(String url){runOnUiThread(()->external(url,false));}
        @JavascriptInterface public void openWebSearch(String url){runOnUiThread(()->external(url,true));}
        @JavascriptInterface public void startVoice(String language){runOnUiThread(()->voice(language));}
        @JavascriptInterface public void shareText(String text){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Share AASTHA text"));});}
        @JavascriptInterface public void saveJson(String fileName,String json){runOnUiThread(()->{
            pendingJson=json;Intent save=new Intent(Intent.ACTION_CREATE_DOCUMENT);save.setType("application/json");
            save.addCategory(Intent.CATEGORY_OPENABLE);save.putExtra(Intent.EXTRA_TITLE,fileName.replaceAll("[^a-zA-Z0-9_.-]","_"));
            try{startActivityForResult(save,SAVE_JSON);}catch(Exception ex){toast("Unable to open save dialog");}
        });}
        @JavascriptInterface public void exportDocument(String format,String title,String content){runOnUiThread(()->exportDocumentNative(format,title,content));}
    }
    private static String xml(String input){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<input.length();i++){
            char c=input.charAt(i);
            if(c=='&')sb.append("&amp;");else if(c=='<')sb.append("&lt;");else if(c=='>')sb.append("&gt;");else if(c=='\"')sb.append("&quot;");
            else if(c=='\'')sb.append("&apos;");else if(c=='\n'||c=='\t'||c=='\r'||c>=32)sb.append(c);
        }return sb.toString();
    }
    private static String safeName(String text){String n=text.replaceAll("[^a-zA-Z0-9 _.-]","_").trim();if(n.length()>72)n=n.substring(0,72);return n.isEmpty()?"AASTHA_Brief":n;}
    private void exportDocumentNative(String format,String title,String content){
        if(content==null||content.trim().isEmpty()){toast("Enter content before exporting");return;}
        if(content.length()>180000){toast("Document exceeds 180,000 characters");return;}
        if(format.equals("pdf")){printAsPdf(title,content);return;}
        if(!format.equals("docx")&&!format.equals("xlsx")&&!format.equals("png")){toast("Unsupported export format");return;}
        exportFormat=format;exportTitle=title;exportText=content;
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(format.equals("docx")?"application/vnd.openxmlformats-officedocument.wordprocessingml.document":format.equals("xlsx")?"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":"image/png");
        intent.putExtra(Intent.EXTRA_TITLE,safeName(title)+"."+format);
        try{startActivityForResult(intent,EXPORT_REQUEST);}catch(Exception e){toast("Unable to open document save dialog");}
    }
    private void printAsPdf(String title,String content){
        // Android Print → Save as PDF. Print UI owns pagination and destination.
        if(printWeb!=null){printWeb.destroy();printWeb=null;}
        printWeb=new WebView(this);configureWebView(printWeb,false);
        String html="<!doctype html><html><head><meta charset='utf-8'><style>@page{size:A4;margin:17mm}body{font-family:sans-serif;color:#19354b;font-size:11pt;line-height:1.55}h1{color:#174b78;font-size:18pt;border-bottom:1px solid #8bbbe0;padding-bottom:10px}pre{font:inherit;white-space:pre-wrap;overflow-wrap:anywhere}footer{font-size:8pt;color:#627c92;margin-top:25px}</style></head><body><h1>"+xml(title)+"</h1><pre>"+xml(content)+"</pre><footer>Generated locally in AASTHA · Source: user-provided text</footer></body></html>";
        printWeb.setWebViewClient(new WebViewClient(){boolean printed=false;
            @Override public void onPageFinished(WebView view,String url){if(printed)return;printed=true;
                PrintManager manager=(PrintManager)getSystemService(Context.PRINT_SERVICE);
                manager.print(safeName(title),view.createPrintDocumentAdapter(safeName(title)),new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());
                toast("In print dialog, select Save as PDF");
            }});
        printWeb.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
    }
    private static void z(ZipOutputStream zip,String path,String text)throws Exception{
        zip.putNextEntry(new ZipEntry(path));zip.write(text.getBytes(StandardCharsets.UTF_8));zip.closeEntry();
    }
    private static byte[] docx(String title,String content)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(bytes)){
            z(zip,"[Content_Types].xml","<?xml version='1.0'?><Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'><Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/><Default Extension='xml' ContentType='application/xml'/><Override PartName='/word/document.xml' ContentType='application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml'/></Types>");
            z(zip,"_rels/.rels","<?xml version='1.0'?><Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'><Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument' Target='word/document.xml'/></Relationships>");
            StringBuilder body=new StringBuilder();body.append("<w:p><w:r><w:rPr><w:b/><w:sz w:val='34'/></w:rPr><w:t>").append(xml(title)).append("</w:t></w:r></w:p>");
            for(String line:content.split("\\R",-1))body.append("<w:p><w:r><w:t xml:space='preserve'>").append(xml(line)).append("</w:t></w:r></w:p>");
            z(zip,"word/document.xml","<?xml version='1.0' encoding='UTF-8' standalone='yes'?><w:document xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'><w:body>"+body+"<w:sectPr><w:pgSz w:w='11906' w:h='16838'/><w:pgMar w:top='1134' w:right='1134' w:bottom='1134' w:left='1134'/></w:sectPr></w:body></w:document>");
        }return bytes.toByteArray();
    }
    private static byte[] xlsx(String title,String content)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(bytes)){
            z(zip,"[Content_Types].xml","<?xml version='1.0'?><Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'><Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/><Default Extension='xml' ContentType='application/xml'/><Override PartName='/xl/workbook.xml' ContentType='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml'/><Override PartName='/xl/worksheets/sheet1.xml' ContentType='application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml'/></Types>");
            z(zip,"_rels/.rels","<?xml version='1.0'?><Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'><Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument' Target='xl/workbook.xml'/></Relationships>");
            z(zip,"xl/workbook.xml","<?xml version='1.0'?><workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'><sheets><sheet name='Results' sheetId='1' r:id='rId1'/></sheets></workbook>");
            z(zip,"xl/_rels/workbook.xml.rels","<?xml version='1.0'?><Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'><Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet' Target='worksheets/sheet1.xml'/></Relationships>");
            StringBuilder rows=new StringBuilder();String[] lines=(title+"\n"+content).split("\\R",-1);
            for(int i=0;i<lines.length&&i<5000;i++){
                String value=lines[i];if(value.length()>30000)value=value.substring(0,30000);
                rows.append("<row r='").append(i+1).append("'><c r='A").append(i+1).append("' t='inlineStr'><is><t xml:space='preserve'>").append(xml(value)).append("</t></is></c></row>");
            }
            z(zip,"xl/worksheets/sheet1.xml","<?xml version='1.0'?><worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'><cols><col min='1' max='1' width='75' customWidth='1'/></cols><sheetData>"+rows+"</sheetData></worksheet>");
        }return bytes.toByteArray();
    }
    private static byte[] pngCard(String title,String content)throws Exception{
        int w=1080;Paint p=new Paint(3);p.setTextSize(31);p.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.NORMAL));
        ArrayList<String> lines=new ArrayList<>();for(String paragraph:content.split("\\R",-1)){
            String remaining=paragraph;while(remaining.length()>0){int cut=Math.min(75,remaining.length());while(cut>1&&p.measureText(remaining.substring(0,cut))>990)cut--;if(cut<remaining.length()&&remaining.charAt(cut)!=' '){int space=remaining.lastIndexOf(' ',cut);if(space>15)cut=space;}
                lines.add(remaining.substring(0,cut));remaining=remaining.substring(cut).trim();if(lines.size()>=100)break;}
            if(lines.size()>=100)break;if(paragraph.isEmpty())lines.add("");}
        int height=Math.min(7000,200+lines.size()*43);Bitmap bmp=Bitmap.createBitmap(w,Math.max(400,height),Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bmp);canvas.drawColor(Color.rgb(239,247,255));
        p.setColor(Color.rgb(23,75,120));p.setTextSize(43);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);canvas.drawText(safeName(title),44,77,p);
        p.setColor(Color.rgb(53,82,105));p.setTypeface(android.graphics.Typeface.DEFAULT);p.setTextSize(29);int y=145;
        for(String line:lines){if(y>height-45)break;canvas.drawText(line,44,y,p);y+=43;}
        ByteArrayOutputStream out=new ByteArrayOutputStream();bmp.compress(Bitmap.CompressFormat.PNG,100,out);bmp.recycle();return out.toByteArray();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==VOICE_REQUEST){if(result==RESULT_OK&&data!=null){ArrayList<String> found=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(found!=null&&!found.isEmpty())web.evaluateJavascript("window.aasthaVoiceResult("+JSONObject.quote(found.get(0))+ ")",null);}else toast("Voice request cancelled");return;}
        if(request==PICK_JSON){if(fileCallback!=null){Uri[] uris=(result==RESULT_OK&&data!=null&&data.getData()!=null)?new Uri[]{data.getData()}:null;fileCallback.onReceiveValue(uris);fileCallback=null;}return;}
        if(request==SAVE_JSON){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&pendingJson!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new Exception("No destination");out.write(pendingJson.getBytes(StandardCharsets.UTF_8));toast("Backup exported");}catch(Exception e){toast("Backup export failed");}}pendingJson=null;return;}
        if(request==EXPORT_REQUEST){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&exportText!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){
                if(out==null)throw new Exception("No destination");byte[] bytes=exportFormat.equals("docx")?docx(exportTitle,exportText):exportFormat.equals("xlsx")?xlsx(exportTitle,exportText):pngCard(exportTitle,exportText);out.write(bytes);toast("AASTHA "+exportFormat.toUpperCase(Locale.ROOT)+" saved");
                }catch(Exception e){toast("Export failed: "+e.getMessage());}}
            exportText=null;exportTitle=null;exportFormat=null;}
    }
    @Override protected void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("AasthaAndroid");web.destroy();}if(printWeb!=null)printWeb.destroy();super.onDestroy();}
}
