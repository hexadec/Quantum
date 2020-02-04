package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import androidx.core.graphics.PaintCompat;
import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.math.VisualOperator;
import hu.hexadecimal.quantum.math.Qubit;

public class BlochSphereView extends View {

    final Paint mPaint, otherPaint, textPaint, textPaintW;

    Qubit qb;
    boolean hasMulti;

    public BlochSphereView(Context context) {
        super(context);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xff333333);
        mPaint.setStrokeWidth(pxFromDp(getContext(), 2));

        textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xff111111);
        textPaint.setTextSize(pxFromDp(context, 20));
        textPaint.setTypeface(Typeface.MONOSPACE);

        textPaintW = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaintW.setColor(0xff333333);
        textPaintW.setTextSize(pxFromDp(context, 12));
        textPaintW.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();
        otherPaint.setStyle(Paint.Style.STROKE);
    }

    public void setQBit(Qubit qb, boolean hasMultiQubitGate) {
        this.qb = qb;
        hasMulti = hasMultiQubitGate;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (qb == null) qb = new Qubit();

        Qubit qbh = VisualOperator.HADAMARD.operateOn(new Qubit[]{qb})[0];
        float ProbZ = (float) Complex.multiply(qb.matrix[1], Complex.conjugate(qb.matrix[1])).real;
        float ProbX = (float) Complex.multiply(qbh.matrix[0], Complex.conjugate(qbh.matrix[0])).real;
        qbh = VisualOperator.transpose(VisualOperator.S_GATE).operateOn(new Qubit[]{qb})[0];
        qbh = VisualOperator.HADAMARD.operateOn(new Qubit[]{qbh})[0];
        float ProbY = (float) Complex.multiply(qbh.matrix[1], Complex.conjugate(qbh.matrix[1])).real;

        int xpos = getWidth();
        int ypos = getHeight();
        if (hasMulti) {
            canvas.drawText(getContext().getString(R.string.state_of_qubit_warning),
                    getWidth() / 2f - textPaintW.measureText(getContext().getString(R.string.state_of_qubit_warning)) / 2f,
                    pxFromDp(getContext(), 15),
                    textPaintW);
        }
        char verticalBar = PaintCompat.hasGlyph(textPaint, "⎥") ? '⎥' : '|';
        if (verticalBar == '|') textPaint.setTextSize(pxFromDp(super.getContext(), 17));
        if (getHeight() < getWidth()) {
            final int radius = (int) (ypos / 4 * (xpos / ypos) / 1.4);
            final int cx1 = xpos / 4;
            final int cx2 = xpos / 4 * 3;
            final int cy = ypos / 2;
            canvas.drawCircle(cx1, cy, radius, mPaint);
            otherPaint.setColor(0xff666666);
            otherPaint.setStrokeWidth(pxFromDp(super.getContext(), 1));
            canvas.drawLine(cx1 - radius, cy, cx1 + radius, cy, otherPaint);
            canvas.drawLine(cx1, cy - radius, cx1, cy + radius, otherPaint);
            canvas.drawText(verticalBar + "i-⟩", cx1 - radius - pxFromDp(super.getContext(), 40), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "i+⟩", cx1 + radius + pxFromDp(super.getContext(), 4), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "1⟩", cx1 - pxFromDp(super.getContext(), 13), cy + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText(verticalBar + "0⟩", cx1 - pxFromDp(super.getContext(), 13), cy - radius - pxFromDp(super.getContext(), 10), textPaint);

            canvas.drawCircle(cx2, ypos / 2, radius, mPaint);
            canvas.drawLine(cx2 - radius, cy, cx2 + radius, cy, otherPaint);
            canvas.drawLine(cx2, cy - radius, cx2, cy + radius, otherPaint);
            canvas.drawText(verticalBar + "i-⟩", cx2 - radius - pxFromDp(super.getContext(), 40), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "i+⟩", cx2 + radius + pxFromDp(super.getContext(), 4), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "+⟩", cx2 - pxFromDp(super.getContext(), 13), cy + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText(verticalBar + "-⟩", cx2 - pxFromDp(super.getContext(), 13), cy - radius - pxFromDp(super.getContext(), 10), textPaint);

            otherPaint.setColor(0xffcc0000);
            otherPaint.setStrokeWidth(pxFromDp(super.getContext(), 6));
            canvas.drawCircle(cx1 - radius + radius * 2 * ProbY, cy - radius + radius * 2 * ProbZ, pxFromDp(super.getContext(), 6), otherPaint);
            canvas.drawCircle(cx2 - radius + radius * 2 * ProbY, cy - radius + radius * 2 * ProbX, pxFromDp(super.getContext(), 6), otherPaint);
        } else {
            final int radius = ypos / 6;
            final int cx = xpos / 2;
            final int cy1 = ypos / 4;
            final int cy2 = ypos / 4 * 3;
            canvas.drawCircle(cx, cy1, radius, mPaint);
            otherPaint.setColor(0xff666666);
            otherPaint.setStrokeWidth(pxFromDp(super.getContext(), 1));
            canvas.drawLine(cx - radius, cy1, cx + radius, cy1, otherPaint);
            canvas.drawLine(cx, cy1 - radius, cx, cy1 + radius, otherPaint);
            canvas.drawText(verticalBar + "0⟩", cx - pxFromDp(super.getContext(), 13), cy1 - radius - pxFromDp(super.getContext(), 10), textPaint);
            canvas.drawText(verticalBar + "1⟩", cx - pxFromDp(super.getContext(), 13), cy1 + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText(verticalBar + "i+⟩", cx + radius + pxFromDp(super.getContext(), 4), cy1 + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "i-⟩", cx - radius - pxFromDp(super.getContext(), 40), cy1 + pxFromDp(super.getContext(), 7), textPaint);

            canvas.drawCircle(cx, cy2, radius, mPaint);
            canvas.drawLine(cx - radius, cy2, cx + radius, cy2, otherPaint);
            canvas.drawLine(cx, cy2 - radius, cx, cy2 + radius, otherPaint);
            canvas.drawText(verticalBar + "-⟩", cx - pxFromDp(super.getContext(), 13), cy2 - radius - pxFromDp(super.getContext(), 10), textPaint);
            canvas.drawText(verticalBar + "+⟩", cx - pxFromDp(super.getContext(), 13), cy2 + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText(verticalBar + "i+⟩", cx + radius + pxFromDp(super.getContext(), 4), cy2 + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText(verticalBar + "i-⟩", cx - radius - pxFromDp(super.getContext(), 40), cy2 + pxFromDp(super.getContext(), 7), textPaint);

            otherPaint.setColor(0xffcc0000);
            otherPaint.setStrokeWidth(pxFromDp(super.getContext(), 6));
            canvas.drawCircle(cx - radius + radius * 2 * ProbY, cy1 - radius + radius * 2 * ProbZ, pxFromDp(super.getContext(), 6), otherPaint);
            canvas.drawCircle(cx - radius + radius * 2 * ProbY, cy2 - radius + radius * 2 * ProbX, pxFromDp(super.getContext(), 6), otherPaint);
        }
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}