package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import androidx.annotation.NonNull;
import hu.hexadecimal.quantum.UIHelper;
import hu.hexadecimal.quantum.math.VisualOperator;

public class GateView extends View {

    public final float UNIT;
    public final float PADDING;
    public final int GATE_SIZE;
    public final VisualOperator visualOperator;

    final Paint otherPaint, whiteTextPaint;

    public GateView(Context context, @NonNull VisualOperator v, int size) {
        super(context);
        UNIT = UIHelper.pxFromDp(super.getContext(), 1);
        PADDING = UIHelper.pxFromDp(super.getContext(), 1);
        GATE_SIZE = size;
        visualOperator = v;

        whiteTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        whiteTextPaint.setColor(0xffffffff);
        whiteTextPaint.setTextSize(UIHelper.pxFromDp(context, 24));
        whiteTextPaint.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();
        otherPaint.setStyle(Paint.Style.FILL);
        otherPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        setMinimumWidth(minSize());
        setMinimumHeight(minSize());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        otherPaint.setColor(visualOperator.getColor());

        RectF bounds = new RectF();
        bounds.left = getWidth() / 2 - UIHelper.pxFromDp(super.getContext(), GATE_SIZE);
        bounds.top = getHeight() / 2 - UIHelper.pxFromDp(super.getContext(), GATE_SIZE);
        RectF areaRect = new RectF(bounds.left,
                bounds.top,
                bounds.left + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2),
                bounds.top + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2));
        String symbol = visualOperator.getSymbols()[visualOperator.getSymbols().length - 1];
        switch (symbol.length()) {
            case 1:
            case 2:
                whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 24));
                break;
            case 3:
                whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 18));
                break;
            case 4:
                whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 14));
                break;
            default:
                whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 12));
        }

        bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
        bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2, otherPaint);
        canvas.drawText(symbol, bounds.left, bounds.top - whiteTextPaint.ascent(), whiteTextPaint);
    }

    public int minSize() {
        return (int) UIHelper.pxFromDp(getContext(), GATE_SIZE * 2 + PADDING);
    }
}
