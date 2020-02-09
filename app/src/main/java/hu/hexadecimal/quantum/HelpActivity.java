package hu.hexadecimal.quantum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import hu.hexadecimal.quantum.graphics.QuantumView;
import hu.hexadecimal.quantum.math.VisualOperator;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HelpActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        webView = new WebView(HelpActivity.this);
        ((ConstraintLayout) findViewById(R.id.help_parent)).addView(webView);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#171717")));
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#171717"));

        webView.postDelayed(() ->
                new Thread(() ->
                {
                    try {
                        String helpFileDir = getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir + "/appHelp";
                        String helpFileName = helpFileDir + "/v" + VisualOperator.helpVersion + ".html";
                        File f = new File(helpFileName);
                        if (!f.exists()) {
                            try {
                                String[] names = new File(helpFileDir).list();
                                for (String s : names) {
                                    File file = new File(helpFileDir, s);
                                    file.delete();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            new File(helpFileDir).mkdirs();
                            BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.info)));
                            StringBuilder total = new StringBuilder();
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
                            //html = html.replace("&lt;P6matrix&gt;", VisualOperator.PI6_GATE.toStringHtmlTable(VisualOperator.HTML_MODE_CAPTION | VisualOperator.HTML_MODE_FAT));

                            html = html.replace("&lt;CNOTmatrix&gt;", VisualOperator.CNOT.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                            html = html.replace("&lt;SWAPmatrix&gt;", VisualOperator.SWAP.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                            html = html.replace("&lt;CSWAPmatrix&gt;", VisualOperator.FREDKIN.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                            html = html.replace("&lt;CCXmatrix&gt;", VisualOperator.TOFFOLI.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                            html = html.replace("&lt;CHmatrix&gt;", VisualOperator.CH.toStringHtmlTable(VisualOperator.HTML_MODE_BASIC));
                            final String finalHtml = html;
                            runOnUiThread(() -> webView.loadDataWithBaseURL("file:///android_asset/info.html", finalHtml, "text/html", "UTF-8", null));
                            FileOutputStream fileOutputStream = new FileOutputStream(f);
                            fileOutputStream.write(html.getBytes(StandardCharsets.UTF_8));
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } else {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                            StringBuilder total = new StringBuilder();
                            for (String line; (line = bufferedReader.readLine()) != null; ) {
                                total.append(line).append('\n');
                            }
                            String html = total.toString();
                            runOnUiThread(() -> webView.loadDataWithBaseURL("file:///android_asset/info.html", html, "text/html", "UTF-8", null));
                            bufferedReader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> webView.loadUrl("file:///android_res/raw/info.html"));
                    }
                }).start(), 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i("dpWidth", "" + dpWidth);
        int padding = dpWidth > 450 ? dpWidth > 600 ? dpWidth >= 800 ? 40 : 30 : 20 : 10;
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins((int) QuantumView.pxFromDp(this, padding),
                (int) QuantumView.pxFromDp(this, padding / 2),
                (int) QuantumView.pxFromDp(this, padding),
                (int) QuantumView.pxFromDp(this, padding / 2));
        webView.setLayoutParams(lp);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i("dpWidth", "" + dpWidth);
        int padding = dpWidth > 400 ? dpWidth > 600 ? 30 : 20 : 10;
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins((int) QuantumView.pxFromDp(this, padding),
                (int) QuantumView.pxFromDp(this, padding / 2),
                (int) QuantumView.pxFromDp(this, padding),
                (int) QuantumView.pxFromDp(this, padding / 2));
        webView.setLayoutParams(lp);
    }

    /**
     * Source: https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview
     */
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 22) {
            return;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}
