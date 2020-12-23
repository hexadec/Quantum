package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.UIHelper;
import hu.hexadecimal.quantum.math.Complex;

import static hu.hexadecimal.quantum.graphics.QuantumView.MAX_QUBITS;

public class ExecutionResultDialog {

    Activity context;
    AlertDialog.Builder adb;

    final String separator;
    final boolean scientific;

    Complex[] stateVector;
    float[] probabilities;

    public ExecutionResultDialog(Activity context, Complex[] stateVector, float[] probabilities) {
        adb = new AlertDialog.Builder(context);
        this.context = context;
        this.stateVector = stateVector;
        this.probabilities = probabilities;
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        separator = pref.getString("separator", ",");
        scientific = pref.getBoolean("sci_form", false);
    }

    public void setTitle(String title) {
        adb.setTitle(title);
    }

    public AlertDialog create(QuantumView qv, int shots, ExecutionProgressDialog progressDialog) {
        ScrollView scrollView = new ScrollView(context);
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setPadding(0, (int) UIHelper.pxFromDp(context, 10), 0, (int) UIHelper.pxFromDp(context, 10));
        short[] measuredQubits = qv.getMeasuredQubits();
        scrollView.addView(tableLayout);
        adb.setView(scrollView);
        AlertDialog ad = adb.create();
        ad.setOnShowListener((DialogInterface dialogInterface) -> {
            float dpWidth = ad.getWindow().getDecorView().getWidth() / context.getResources().getDisplayMetrics().density;
            Log.i("Debug", "Dialog dpwidth: " + dpWidth);
            int decimalPoints = dpWidth > 280 ? dpWidth > 300 ? dpWidth > 365 ? dpWidth > 420 ? dpWidth > 450 ? dpWidth > 520 ? 10 : 8 : 7 : 6 : 5 : 4 : 3;
            String decimals = new String(new char[decimalPoints]).replace("\0", "#");
            NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
            NumberFormat nf2 = NumberFormat.getInstance(Locale.US);
            DecimalFormat df = (DecimalFormat) nf1;
            DecimalFormat sf = (DecimalFormat) nf2;
            df.applyPattern(stateVector == null ? "0.########" : "0." + decimals);
            sf.applyPattern(stateVector == null ? "0.########" : "0." + (decimalPoints < 4 ? decimals.substring(2) : decimals.substring(3)) + "E0");
            outerFor:
            for (int i = 0; i < probabilities.length; i++) {
                if (shots == 1 && probabilities[i] == 0) {
                    continue;
                }
                if (probabilities.length > Math.pow(2, 7) && probabilities[i] == 0) {
                    continue;
                }
                TableRow tr = new TableRow(context);
                tr.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                tr.setDividerDrawable(context.getDrawable(R.drawable.vertical_divider));
                for (int j = 0; j < measuredQubits.length; j++) {
                    if (measuredQubits[j] < 1 && (i >> j) % 2 == 1) {
                        continue outerFor;
                    }
                }
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                params.setMargins((int) UIHelper.pxFromDp(context, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0, (int) UIHelper.pxFromDp(context, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0);
                AppCompatTextView[] textView = new AppCompatTextView[]{
                        new AppCompatTextView(context),
                        new AppCompatTextView(context),
                        new AppCompatTextView(context)};
                textView[0].setTypeface(Typeface.DEFAULT_BOLD);
                textView[0].setText(String.format("%" + MAX_QUBITS + "s", Integer.toBinaryString(i)).replace(' ', '0'));
                textView[0].setLayoutParams(params);
                textView[1].setTypeface(Typeface.MONOSPACE);
                if (probabilities[i] * Math.pow(10, decimalPoints) < 1 && probabilities[i] != 0)
                    textView[1].setText(sf.format(probabilities[i]));
                else
                    textView[1].setText(df.format(probabilities[i]));
                textView[1].setLayoutParams(params);
                textView[2].setTypeface(Typeface.MONOSPACE);
                textView[2].setText((stateVector != null ? stateVector[i].toString(decimalPoints) : ""));
                textView[2].setLayoutParams(params);
                if (dpWidth < 330 && stateVector != null) {
                    textView[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                }
                tr.addView(textView[0]);
                tr.addView(textView[1]);
                tr.addView(textView[2]);
                tableLayout.addView(tr);
            }

            try {
                progressDialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ad.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener((View v) -> {
                View layout = context.getLayoutInflater().inflate(R.layout.graph_layout, null);
                int size = (int) Math.ceil(Math.pow(2, qv.getLastUsedQubit() + 1));
                float[] arguments = null;
                if (stateVector != null) {
                    arguments = new float[stateVector.length];
                    for(int i = 0; i < stateVector.length; i++) {
                        arguments[i] = (float) stateVector[i].arg();
                    }
                }
                ((GraphView) layout.findViewById(R.id.graphView)).setData(probabilities, size == 0 ? 2 : size, arguments);
                layout.findViewById(R.id.closeButton).setOnClickListener((View button) -> {
                    ad.dismiss();
                });
                ad.setContentView(layout);
            });
        });
        return ad;
    }

    public void setupDialog() {
        adb.setCancelable(false);
        adb.setPositiveButton("OK", null);
        adb.setNegativeButton(R.string.graph, null);
        adb.setNeutralButton(R.string.export_csv, (DialogInterface dialogInterface, int i) -> {
            try {
                Uri uri = context.getContentResolver().getPersistedUriPermissions().get(0).getUri();
                DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
                if (!pickedDir.exists()) {
                    context.getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    pickedDir = null;
                }
                StringBuilder sb = new StringBuilder();
                DecimalFormat df = new DecimalFormat(scientific ? "0.########E0" : "0.##########", new DecimalFormatSymbols(Locale.UK));
                for (int k = 0; k < probabilities.length; k++) {
                    if (k != 0) sb.append("\r\n");
                    sb.append(k);
                    sb.append(separator);
                    sb.append(String.format("%" + MAX_QUBITS + "s", Integer.toBinaryString(k)).replace(' ', '0'));
                    sb.append(separator);
                    sb.append(df.format(probabilities[k]));
                    if (stateVector != null) {
                        sb.append(separator);
                        sb.append(stateVector[k].toString(10));
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("'results'_yyyy-MM-dd_HH-mm-ss'.csv'", Locale.UK);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String filename = sdf.format(new Date());
                DocumentFile newFile = pickedDir.createFile("text/csv", filename);
                OutputStream out = context.getContentResolver().openOutputStream(newFile.getUri());
                out.write(sb.toString().getBytes());
                out.close();
                Snackbar.make(context.findViewById(R.id.parent2), filename + " \n" + context.getString(R.string.successfully_exported), Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(context.findViewById(R.id.parent2), R.string.choose_save_location_settings, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(0xffD81010);
                snackbar.show();
            }
        });
    }
}
