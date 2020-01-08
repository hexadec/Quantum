package hu.hexadecimal.quantum.graphics;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

public class ExecutionProgressDialog extends AlertDialog {

    private ProgressBar progressBar;
    private TextView progressText;
    
    public ExecutionProgressDialog(Context context, String text) {
        super(context);
        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding((int) QuantumView.pxFromDp(context, 10),
                (int) QuantumView.pxFromDp(context, 15),
                (int) QuantumView.pxFromDp(context, 10),
                (int) QuantumView.pxFromDp(context, 5));
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(16);
        l.addView(tv);
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        l.addView(progressBar);
        progressText = new TextView(context);
        progressText.setTextSize(16);
        l.addView(progressText);
        Button cancel = new Button(context, null, android.R.attr.buttonBarPositiveButtonStyle);
        cancel.setText(android.R.string.cancel);
        cancel.setOnClickListener((View view) -> cancel());
        l.addView(cancel);
        setView(l);
        setCancelable(false);
    }

    public void setProgress(int progress, int max) {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        progressBar.setMax(max);
        progressBar.setProgress(progress);
        progressText.setText(nf.format(progress) + "/" + nf.format(max));
    }
    
    
}
