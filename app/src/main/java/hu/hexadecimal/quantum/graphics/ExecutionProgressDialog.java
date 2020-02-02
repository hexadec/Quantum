package hu.hexadecimal.quantum.graphics;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import hu.hexadecimal.quantum.R;

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
        progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_style));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) QuantumView.pxFromDp(context, 6));
        layoutParams.setMargins((int) QuantumView.pxFromDp(context, 2),
                (int) QuantumView.pxFromDp(context, 8),
                (int) QuantumView.pxFromDp(context, 2),
                (int) QuantumView.pxFromDp(context, 8));
        progressBar.setLayoutParams(layoutParams);
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
        progressBar.setSecondaryProgress(max);
        progressText.setText(nf.format(progress) + "/" + nf.format(max));
    }

    public void setSecondaryProgress(int progress, int max) {
        progressBar.setMax(max);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(progress);
        progressText.setText(R.string.assembling_experiment);
    }
    
    
}
