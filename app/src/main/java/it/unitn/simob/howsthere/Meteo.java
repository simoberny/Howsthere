package it.unitn.simob.howsthere;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class Meteo extends Fragment {
    public Meteo() {}

    public static Meteo newInstance() {
        Meteo fragment = new Meteo();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_meteo, container, false);

        String html = "<iframe width=\"400\" height=\"260\" scrolling=\"no\" frameborder=\"no\" noresize=\"noresize\" src=\"http://www.ilmeteo.it/box/previsioni.php?citta=5913&type=day1&width=400&ico=1&lang=ita&days=6&font=Arial&fontsize=12&bg=FFFFFF&fg=000000&bgtitle=0099FF&fgtitle=FFFFFF&bgtab=F0F0F0&fglink=1773C2\"></iframe>";

        WebView webview;
        webview = (WebView) view.findViewById(R.id.meteo);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadData(html, "text/html", null);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
