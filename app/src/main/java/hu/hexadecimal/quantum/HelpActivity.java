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

        new Thread(() ->
        {
            try {
                for (String line; (line = in.readLine()) != null; ) {
                    total.append(line).append('\n');
                }
                String html = total.toString();
                html = html.replace("&lt;Hmatrix&gt;", VisualOperator.HADAMARD.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CSmatrix&gt;", VisualOperator.CS.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CTmatrix&gt;", VisualOperator.CT.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CZmatrix&gt;", VisualOperator.CZ.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CYmatrix&gt;", VisualOperator.CY.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;Imatrix&gt;", VisualOperator.ID.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;Qmatrix&gt;", VisualOperator.SQRT_NOT.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));

                html = html.replace("&lt;Xmatrix&gt;", VisualOperator.PAULI_X.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));
                html = html.replace("&lt;Ymatrix&gt;", VisualOperator.PAULI_Y.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));
                html = html.replace("&lt;Zmatrix&gt;", VisualOperator.PAULI_Z.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));
                html = html.replace("&lt;Smatrix&gt;", VisualOperator.S_GATE.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));
                html = html.replace("&lt;Tmatrix&gt;", VisualOperator.T_GATE.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));
                html = html.replace("&lt;P6matrix&gt;", VisualOperator.PI6_GATE.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));

                html = html.replace("&lt;CNOTmatrix&gt;", VisualOperator.CNOT.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;SWAPmatrix&gt;", VisualOperator.SWAP.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CSWAPmatrix&gt;", VisualOperator.FREDKIN.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CCXmatrix&gt;", VisualOperator.TOFFOLI.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                html = html.replace("&lt;CHmatrix&gt;", VisualOperator.CH.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                final String finalHtml = html;
                runOnUiThread(() -> webView.loadData(finalHtml, "text/html", "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> webView.loadUrl("file:///android_res/raw/info.html"));
            }
        }).start();
    }
}
