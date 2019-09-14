package hu.hexadecimal.quantum;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    /*static {
        System.loadLibrary("native-lib");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(Complex.exponent(new Complex(2, 5), new Complex (4, 7)).toString());
        tv.setText(tv.getText() + "\n---\n" + Complex.sin(new Complex(-2, 0.5)).toString());
        tv.setText(tv.getText() + "\n---\n" + Complex.cos(new Complex(-2, 0.5)).toString());
        /* QBit q = new QBit();
        q.applyOperator(LinearOperator.HADAMARD);
        tv.setText(tv.getText() + "\n----\n" + q.toString());

        double value = 0;

        for (int i = 0; i < 200; i++) {
            QBit[] qs = TwoQBitOperator.CNOT.operateOn(q, q);
            value += qs[1].measureZ() ? 1 : 0;
        }
        tv.setText(tv.getText() + "\n----\n" + value / 200);
        List<String> gates = GateView.getPredefinedGateNames();
        for (String s : gates) {
            Log.d("?", s);
        }*/

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
}
