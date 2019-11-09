package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

public class BigPreference extends Preference {
    public BigPreference(Context context) {
        super(context);
    }

    public BigPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View v = super.onCreateView(parent);
        v.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        ((TextView)v.findViewById(android.R.id.summary)).setMaxLines(15);
        return v;
    }
}
