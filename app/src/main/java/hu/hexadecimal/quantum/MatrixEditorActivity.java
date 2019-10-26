package hu.hexadecimal.quantum;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.DialogCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MatrixEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppBarLayout a = new AppBarLayout(this);
        setContentView(R.layout.activity_matrix_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                AlertDialog.Builder adb = new AlertDialog.Builder(MatrixEditorActivity.this);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View v = layoutInflater.inflate(R.layout.edit_matrix, null);
                adb.setView(v);
                adb.setPositiveButton(R.string.add_gate, null);
                adb.setNegativeButton(R.string.cancel, null);
                AlertDialog d = adb.create();
                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.colorBar);
                        seekBar.setMax(256 * 7 - 1);
                        seekBar.setOnSeekBarChangeListener(stuff);
                        seekBar.setProgress((int) (256 * 3.5));
                        final Button okButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    }
                });
                d.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private final SeekBar.OnSeekBarChangeListener stuff = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int r = 0;
            int g = 0;
            int b = 0;

            if (progress < 256) {
                b = progress;
            } else if (progress < 256 * 2) {
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 3) {
                g = 255;
                b = progress % 256;
            } else if (progress < 256 * 4) {
                r = progress % 256;
                g = 256 - progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 5) {
                r = 255;
                g = 0;
                b = progress % 256;
            } else if (progress < 256 * 6) {
                r = 255;
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 7) {
                r = 255;
                g = 255;
                b = progress % 256;
            }

            seekBar.setBackgroundColor(Color.argb(255, r, g, b));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
