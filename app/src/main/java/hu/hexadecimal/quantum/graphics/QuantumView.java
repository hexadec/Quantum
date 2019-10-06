package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import hu.hexadecimal.quantum.GeneralOperator;
import hu.hexadecimal.quantum.LinearOperator;
import hu.hexadecimal.quantum.QBit;

public class QuantumView extends View {

    Paint mPaint, otherPaint, outerPaint, mTextPaint, whiteTextPaint;
    RectF mRectF;
    int mPadding;

    float arcLeft;

    Path mPath;

    QBit[] qbits;
    private List<GeneralOperator> gos;
    private List<Integer> qid;

    public QuantumView(Context context) {
        super(context);
        qbits = new QBit[6];
        gos = new ArrayList<>();
        qid = new ArrayList<>();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(5);


        mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.DKGRAY);
        mTextPaint.setTextSize(pxFromDp(context, 24));

        whiteTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        whiteTextPaint.setColor(0xffffffff);
        whiteTextPaint.setTextSize(pxFromDp(context, 24));
        whiteTextPaint.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();

        outerPaint = new Paint();
        outerPaint.setStyle(Paint.Style.FILL);
        outerPaint.setColor(Color.YELLOW);

        mPadding = 100;


        DisplayMetrics displayMetrics = new DisplayMetrics();

        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);


        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        arcLeft = pxFromDp(context, 20);


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
                Toast.makeText(QuantumView.super.getContext(), "x: " + x + " y: " + y, Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        this.setOnTouchListener(click);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawRoundRect(mRectF, 10, 10, otherPaint);
        //canvas.clipRect(mRectF, Region.Op.DIFFERENCE);
        //canvas.drawPaint(outerPaint);
        mPaint.setColor(0xff888888);

        otherPaint.setStyle(Paint.Style.FILL);
        otherPaint.setColor(0xffBA2121);
        final int STEP = 60;
        final int GATE_SIZE = 18;
        for (int i = (int) pxFromDp(super.getContext(), 20); i < getHeight() - 2 * mPadding - pxFromDp(super.getContext(), 20) && i <= pxFromDp(super.getContext(), 300); i += (int) pxFromDp(super.getContext(), STEP)) {
            canvas.drawLine(mPadding, mPadding + i, getWidth() - mPadding, mPadding + i, mPaint);
            canvas.drawLine(getWidth() - mPadding - pxFromDp(super.getContext(), 4), mPadding + i - pxFromDp(super.getContext(), 4), getWidth() - mPadding, mPadding + i, mPaint);
            canvas.drawLine(getWidth() - mPadding - pxFromDp(super.getContext(), 4), mPadding + i + pxFromDp(super.getContext(), 4), getWidth() - mPadding, mPadding + i, mPaint);
            canvas.drawRect(mPadding,
                    mPadding + i - pxFromDp(super.getContext(), GATE_SIZE),
                    mPadding + pxFromDp(super.getContext(), GATE_SIZE * 2),
                    mPadding + i + pxFromDp(super.getContext(), GATE_SIZE),
                    otherPaint);
            canvas.drawText("q" + Math.round((i + pxFromDp(super.getContext(), 20)) / pxFromDp(super.getContext(), STEP)), mPadding - pxFromDp(super.getContext(), 30), mPadding + i + pxFromDp(super.getContext(), 6), mTextPaint);
            canvas.drawText("\uD835\uDFD8", mPadding + pxFromDp(super.getContext(), 10f), mPadding + i + pxFromDp(super.getContext(), 9f), whiteTextPaint);
        }

        int[] gatesNumber = new int[6];
        for (int i = 0; i < gos.size(); i++) {
            gatesNumber[qid.get(i)]++;
            otherPaint.setColor(gos.get(i).getColor());

            Rect areaRect = new Rect((int) (mPadding + pxFromDp(super.getContext(), 2) + gatesNumber[qid.get(i)] * pxFromDp(super.getContext(), GATE_SIZE * 3)),
                    ((int)(mPadding + pxFromDp(super.getContext(), 2) + (pxFromDp(super.getContext(), STEP) * (qid.get(i))))),
                    ((int)(mPadding + pxFromDp(super.getContext(), 2) + pxFromDp(super.getContext(), GATE_SIZE * 2) + gatesNumber[qid.get(i)] * pxFromDp(super.getContext(), GATE_SIZE * 3))),
                    ((int)(mPadding + (int) pxFromDp(super.getContext(), 20) + (pxFromDp(super.getContext(), STEP) * qid.get(i)) + pxFromDp(super.getContext(), GATE_SIZE))));

            String symbol = ((LinearOperator) gos.get(i)).getSymbol();
            RectF bounds = new RectF(areaRect);
            bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
            bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
            bounds.left += (areaRect.width() - bounds.right) / 2.0f;
            bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

            canvas.drawRect(areaRect, otherPaint);

            canvas.drawText(symbol, bounds.left, bounds.top - whiteTextPaint.ascent(), whiteTextPaint);
        }
        //canvas.drawRect(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding, mPaint);
        //canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 75, 45, true, mPaint);


        //canvas.drawPath(mPath, mPaint);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2, arcLeft, otherPaint);

        //canvas.drawText("Canvas basics", (float) (getWidth() * 0.3), (float) (getHeight() * 0.8), mTextPaint);

    }

    public void addGate(int qbit, LinearOperator l) {
        qid.add(qbit);
        gos.add(l);
        invalidate();
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}