package hu.hexadecimal.quantum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView webView = new WebView(HelpActivity.this);
        ((ConstraintLayout) findViewById(R.id.help_parent)).addView(webView);

        webView.postDelayed(() ->
                new Thread(() ->
                {
                    try {
                        String helpFileName = getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir + "/help" + VisualOperator.helpVersion + ".html";
                        File f = new File(helpFileName);
                        if (!f.exists()) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.info)));
                            StringBuilder total = new StringBuilder();
                            Log.e("1", "X");
                            for (String line; (line = in.readLine()) != null; ) {
                                total.append(line).append('\n');
                            }
                            String html = total.toString();
                            Log.e("2", "X");
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
                            Log.e("3", "X");
                            runOnUiThread(() -> webView.loadData(finalHtml, "text/html", "UTF-8"));
                            FileOutputStream fileOutputStream = new FileOutputStream(f);
                            fileOutputStream.write(html.getBytes(StandardCharsets.UTF_8));
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            Log.e("4", "X");
                        } else {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                            StringBuilder total = new StringBuilder();
                            Log.e("10", "X");
                            for (String line; (line = bufferedReader.readLine()) != null; ) {
                                total.append(line).append('\n');
                            }
                            String html = total.toString();
                            runOnUiThread(() -> webView.loadData(html, "text/html", "UTF-8"));
                            bufferedReader.close();
                            Log.e("20", "X");
                        }
                        Log.e("5", "X");
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> webView.loadUrl("file:///android_res/raw/info.html"));
                    }
                }).start(), 100);
    }



    /**
     * Source: https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview
     * @param overrideConfiguration
     */
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 22) {
            return;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}
