package it.unitn.lpmt.howsthere;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Radar extends AppCompatActivity {
    String content;
    boolean pronto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

       /* AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try {
                    document = Jsoup.connect("http://www.protezionecivile.gov.it/jcms/it/mappa_radar.wp").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content = document.getElementById("animImage").outerHtml();
                pronto = true;
            }
        });



        while(pronto == false){

        }
        WebView webview = (WebView)this.findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");*/

        final WebView htmlWebView = (WebView) findViewById(R.id.webView);
        htmlWebView.setWebViewClient(new WebViewClient());
        WebSettings webSetting = htmlWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.supportZoom();
        //webSetting.setDisplayZoomControls(true);

        htmlWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                htmlWebView.loadUrl("javascript:buttonCheck();");

                htmlWebView.loadUrl("javascript:(function() { " +
                        "var head = document.getElementsByClassName('breadcrumbs').style.display='none'; " +
                        "})()");

            }
        });


        htmlWebView.loadUrl("http://www.protezionecivile.gov.it/jcms/it/mappa_radar.wp");

        //htmlWebView.loadUrl("javascript:buttonCheck()");

        //htmlWebView.evaluateJavascript("buttonCheck()", null);
    }
}

