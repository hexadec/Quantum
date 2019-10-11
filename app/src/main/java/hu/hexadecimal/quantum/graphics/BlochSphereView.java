package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import hu.hexadecimal.quantum.Complex;
import hu.hexadecimal.quantum.LinearOperator;
import hu.hexadecimal.quantum.Qubit;

public class BlochSphereView extends View {

    Paint mPaint, otherPaint, textPaint;
    int mPadding;

    Qubit qb;

    public BlochSphereView(Context context) {
        super(context);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xff333333);
        mPaint.setStrokeWidth(5);

        textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xff111111);
        textPaint.setTextSize(pxFromDp(context, 20));
        textPaint.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();
        otherPaint.setStyle(Paint.Style.STROKE);
        otherPaint.setColor(0xffcc0000);
        otherPaint.setStrokeWidth(12);

        mPadding = (int) pxFromDp(context, 32);
    }

    public void setQBit(Qubit qb) {
        this.qb = qb;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (qb == null) qb = new Qubit();

        Qubit qbh = LinearOperator.HADAMARD.operateOn(qb);
        float ProbZ = (float) Complex.multiply(qb.matrix[1], Complex.conjugate(qb.matrix[1])).real;
        float ProbX = (float) Complex.multiply(qbh.matrix[0], Complex.conjugate(qbh.matrix[0])).real;
        qbh = LinearOperator.transpose(LinearOperator.S_GATE).operateOn(qb);
        qbh.applyOperator(LinearOperator.HADAMARD);
        float ProbY = (float) Complex.multiply(qbh.matrix[1], Complex.conjugate(qbh.matrix[1])).real;

        int xpos = getWidth();
        int ypos = getHeight();
        if (getHeight() < getWidth()) {
            final int radius = xpos / 6;
            final int cx1 = xpos / 4;
            final int cx2 = xpos / 4 * 3;
            final int cy = ypos / 2;
            canvas.drawCircle(cx1, ypos / 2, radius, mPaint);
            canvas.drawText("⎥i-⟩", cx1 - radius - pxFromDp(super.getContext(), 40), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥i+⟩", cx1 + radius + pxFromDp(super.getContext(), 4), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥1⟩", cx1 - pxFromDp(super.getContext(), 13), cy + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText("⎥0⟩", cx1 - pxFromDp(super.getContext(), 13), cy - radius - pxFromDp(super.getContext(), 10), textPaint);

            canvas.drawCircle(cx2, ypos / 2, radius, mPaint);
            canvas.drawText("⎥i-⟩", cx2 - radius - pxFromDp(super.getContext(), 40), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥i+⟩", cx2 + radius + pxFromDp(super.getContext(), 4), cy + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥+⟩", cx2 - pxFromDp(super.getContext(), 13), cy + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText("⎥-⟩", cx2 - pxFromDp(super.getContext(), 13), cy - radius - pxFromDp(super.getContext(), 10), textPaint);

            canvas.drawCircle(cx1 - radius + radius * 2 * ProbY, cy - radius + radius * 2 * ProbZ, pxFromDp(super.getContext(), 4), otherPaint);
            canvas.drawCircle(cx2 - radius + radius * 2 * ProbY, cy - radius + radius * 2 * ProbX, pxFromDp(super.getContext(), 4), otherPaint);
        } else {
            final int radius = ypos / 6;
            final int cx = xpos / 2;
            final int cy1 = ypos / 4;
            final int cy2 = ypos / 4 * 3;
            canvas.drawCircle(cx, cy1, radius, mPaint);
            canvas.drawText("⎥0⟩", cx - pxFromDp(super.getContext(), 13), cy1 - radius - pxFromDp(super.getContext(), 10), textPaint);
            canvas.drawText("⎥1⟩", cx - pxFromDp(super.getContext(), 13), cy1 + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText("⎥i+⟩", cx + radius + pxFromDp(super.getContext(), 4), cy1 + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥i-⟩", cx - radius - pxFromDp(super.getContext(), 40), cy1 + pxFromDp(super.getContext(), 7), textPaint);

            canvas.drawCircle(cx, cy2, radius, mPaint);
            canvas.drawText("⎥-⟩", cx - pxFromDp(super.getContext(), 13), cy2 - radius - pxFromDp(super.getContext(), 10), textPaint);
            canvas.drawText("⎥+⟩", cx - pxFromDp(super.getContext(), 13), cy2 + radius + pxFromDp(super.getContext(), 20), textPaint);
            canvas.drawText("⎥i+⟩", cx + radius + pxFromDp(super.getContext(), 4), cy2 + pxFromDp(super.getContext(), 7), textPaint);
            canvas.drawText("⎥i-⟩", cx - radius - pxFromDp(super.getContext(), 40), cy2 + pxFromDp(super.getContext(), 7), textPaint);

            canvas.drawCircle(cx - radius + radius * 2 * ProbY, cy1 - radius + radius * 2 * ProbZ, pxFromDp(super.getContext(), 4), otherPaint);
            canvas.drawCircle(cx - radius + radius * 2 * ProbY, cy2 - radius + radius * 2 * ProbX, pxFromDp(super.getContext(), 4), otherPaint);
        }
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}