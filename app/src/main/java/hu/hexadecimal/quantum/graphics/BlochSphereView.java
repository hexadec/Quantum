package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hu.hexadecimal.quantum.Complex;
import hu.hexadecimal.quantum.GeneralOperator;
import hu.hexadecimal.quantum.LinearOperator;
import hu.hexadecimal.quantum.QBit;

public class BlochSphereView extends View {

    Paint mPaint, otherPaint, textPaint;
    RectF mRectF;
    int mPadding;
    Path mPath;

    final int STEP = 60;
    final int GATE_SIZE = 18;
    final float UNIT = pxFromDp(super.getContext(), 1);
    final float START_Y = pxFromDp(super.getContext(), 20);

    QBit qb;

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
        otherPaint.setStrokeWidth(8);

        mPadding = (int) pxFromDp(context, 32);


        DisplayMetrics displayMetrics = new DisplayMetrics();

        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);


        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        Point p1 = new Point((int) pxFromDp(context, 80) + (screenWidth / 2), (int) pxFromDp(context, 40));
        Point p2 = new Point((int) pxFromDp(context, 40) + (screenWidth / 2), (int) pxFromDp(context, 80));
        Point p3 = new Point((int) pxFromDp(context, 120) + (screenWidth / 2), (int) pxFromDp(context, 80));

        mPath = new Path();
        mPath.moveTo(p1.x, p1.y);
        mPath.lineTo(p2.x, p2.y);
        mPath.lineTo(p3.x, p3.y);
        mPath.close();

        mRectF = new RectF(screenWidth / 4, screenHeight / 3, screenWidth / 6, screenHeight / 2);


        OnTouchListener click = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
                float x = event.getX();
                float y = event.getY();
                Toast.makeText(BlochSphereView.super.getContext(), "x: " + x + " y: " + y, Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        this.setOnTouchListener(click);
    }

    public void setQBit(QBit qb) {
        this.qb = qb;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (qb == null) qb = new QBit();
        //canvas.drawRect(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding, mPaint);
        //canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 75, 45, true, mPaint);
        QBit qbh = LinearOperator.HADAMARD.operateOn(qb);
        float ProbZ = (float) Complex.multiply(qb.matrix[1], Complex.conjugate(qb.matrix[1])).real;
        float ProbX = (float) Complex.multiply(qbh.matrix[0], Complex.conjugate(qbh.matrix[0])).real;
        qbh = LinearOperator.S_GATE.operateOn(qbh);
        float ProbY = (float) Complex.multiply(qbh.matrix[0], Complex.conjugate(qbh.matrix[0])).real;

        //canvas.drawPath(mPath, mPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 4, getWidth() / 3, mPaint);
        canvas.drawText("⎥0⟩", getHeight() / 4, getWidth() / 2 - getWidth() / 3 - pxFromDp(super.getContext(), 20), textPaint);
        canvas.drawText("⎥1⟩", getHeight() / 4, getWidth() / 2 + getWidth() / 3 + pxFromDp(super.getContext(), 10), textPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 4 * 3, getWidth() / 3, mPaint);

        canvas.drawCircle(getWidth() / 2 - getWidth() / 3 + getWidth() / 3 * 2 * ProbY, getHeight() / 4 - getWidth() / 3 + getWidth() / 3 * 2 * ProbZ, pxFromDp(super.getContext(), 5), otherPaint);
        canvas.drawCircle(getWidth() / 2 - getWidth() / 3 + getWidth() / 3 * 2 * ProbY, getHeight() / 4 * 3 - getWidth() / 3 + getWidth() / 3 * 2 * ProbX, pxFromDp(super.getContext(), 5), otherPaint);

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}