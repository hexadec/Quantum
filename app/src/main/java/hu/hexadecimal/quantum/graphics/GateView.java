package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import androidx.annotation.NonNull;
import hu.hexadecimal.quantum.math.VisualOperator;

import static hu.hexadecimal.quantum.graphics.QuantumView.pxFromDp;

public class GateView extends View {

    public final float UNIT;
    public final float PADDING;
    public static final int GATE_SIZE = 18;
    public final VisualOperator visualOperator;

    final Paint otherPaint, whiteTextPaint;

    public GateView(Context context, @NonNull VisualOperator v) {
        super(context);
        UNIT = pxFromDp(super.getContext(), 1);
        PADDING = pxFromDp(super.getContext(), 1);
        visualOperator = v;

        whiteTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        whiteTextPaint.setColor(0xffffffff);
        whiteTextPaint.setTextSize(pxFromDp(context, 24));
        whiteTextPaint.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();
        otherPaint.setStyle(Paint.Style.FILL);

        setMinimumWidth(minSize());
        setMinimumHeight(minSize());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        otherPaint.setColor(visualOperator.getColor());

        RectF bounds = new RectF();
        bounds.left = getWidth() / 2 - pxFromDp(super.getContext(), GATE_SIZE);
        bounds.top = getHeight() / 2 - pxFromDp(super.getContext(), GATE_SIZE);
        RectF areaRect = new RectF(bounds.left,
                bounds.top,
                bounds.left + pxFromDp(super.getContext(), GATE_SIZE * 2),
                bounds.top + pxFromDp(super.getContext(), GATE_SIZE * 2));
        String symbol = visualOperator.getSymbols()[visualOperator.getSymbols().length - 1];
        switch (symbol.length()) {
            case 1:
            case 2:
                whiteTextPaint.setTextSize(pxFromDp(super.getContext(), 24));
                break;
            case 3:
                whiteTextPaint.setTextSize(pxFromDp(super.getContext(), 18));
                break;
            case 4:
                whiteTextPaint.setTextSize(pxFromDp(super.getContext(), 14));
                break;
            default:
                whiteTextPaint.setTextSize(pxFromDp(super.getContext(), 12));
        }

        bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
        bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        canvas.drawRect(areaRect, otherPaint);
        canvas.drawText(symbol, bounds.left, bounds.top - whiteTextPaint.ascent(), whiteTextPaint);
    }

    public int minSize() {
        return (int) pxFromDp(getContext(), GATE_SIZE * 2 + PADDING);
    }
}
