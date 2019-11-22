package hu.hexadecimal.quantum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView webView = findViewById(R.id.helpWebView);
        BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.info)));
        StringBuilder total = new StringBuilder();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String line; (line = in.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    String html = total.toString();
                    html = html.replace("&lt;Hmatrix&gt;", VisualOperator.HADAMARD.toStringHtmlTable());
                    html = html.replace("&lt;CSmatrix&gt;", VisualOperator.CS.toStringHtmlTable());
                    html = html.replace("&lt;CTmatrix&gt;", VisualOperator.CT.toStringHtmlTable());
                    html = html.replace("&lt;CZmatrix&gt;", VisualOperator.CZ.toStringHtmlTable());
                    html = html.replace("&lt;CYmatrix&gt;", VisualOperator.CY.toStringHtmlTable());
                    html = html.replace("&lt;Imatrix&gt;", VisualOperator.ID.toStringHtmlTable());
                    html = html.replace("&lt;Qmatrix&gt;", VisualOperator.SQRT_NOT.toStringHtmlTable());

                    html = html.replace("&lt;Xmatrix&gt;", VisualOperator.PAULI_X.toStringHtmlTable(true));
                    html = html.replace("&lt;Ymatrix&gt;", VisualOperator.PAULI_Y.toStringHtmlTable(true));
                    html = html.replace("&lt;Zmatrix&gt;", VisualOperator.PAULI_Z.toStringHtmlTable(true));
                    html = html.replace("&lt;Smatrix&gt;", VisualOperator.S_GATE.toStringHtmlTable(true));
                    html = html.replace("&lt;Tmatrix&gt;", VisualOperator.T_GATE.toStringHtmlTable(true));
                    html = html.replace("&lt;P6matrix&gt;", VisualOperator.PI6_GATE.toStringHtmlTable(true));

                    html = html.replace("&lt;CNOTmatrix&gt;", VisualOperator.CNOT.toStringHtmlTable());
                    html = html.replace("&lt;SWAPmatrix&gt;", VisualOperator.SWAP.toStringHtmlTable());
                    html = html.replace("&lt;CSWAPmatrix&gt;", VisualOperator.FREDKIN.toStringHtmlTable());
                    html = html.replace("&lt;CCXmatrix&gt;", VisualOperator.TOFFOLI.toStringHtmlTable());
                    html = html.replace("&lt;CHmatrix&gt;", VisualOperator.CH.toStringHtmlTable());
                    final String finalHtml = html;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadData(finalHtml, "text/html", "UTF-8");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("file:///android_res/raw/info.html");
                        }
                    });
                }
            }
        }).start();
    }
}
