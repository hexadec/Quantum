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
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import hu.hexadecimal.quantum.MultiQubitOperator;
import hu.hexadecimal.quantum.Qubit;
import hu.hexadecimal.quantum.VisualOperator;
import hu.hexadecimal.quantum.LinearOperator;

public class QuantumView extends View {

    Paint mPaint, otherPaint, outerPaint, mTextPaint, whiteTextPaint;
    RectF mRectF;
    int mPadding;

    float arcLeft;

    Path mPath;

    public LinkedList<Qubit> qbits;
    private LinkedList<VisualOperator> gos;

    public static final int STEP = 60;
    public static final int MAX_QUBITS = 6;
    public static final int GATE_SIZE = 18;
    public final float UNIT = pxFromDp(super.getContext(), 1);
    public final float START_Y = pxFromDp(super.getContext(), 20);


    public QuantumView(Context context) {
        super(context);
        qbits = new LinkedList<>();
        for (int i = 0; i < MAX_QUBITS; i++) {
            qbits.add(new Qubit());
        }
        gos = new LinkedList<>();

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

        mPadding = (int) pxFromDp(context, 32);


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

    }

    public boolean isAnOperator(int x, int y) {
        if (x < mPadding || x > getWidth() - mPadding) return false;
        if (y < mPadding || y > getHeight() - mPadding || y > START_Y + pxFromDp(super.getContext(), STEP * MAX_QUBITS))
            return false;
        return true;
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
        for (int i = (int) START_Y; i < getHeight() - 2 * mPadding - START_Y && i <= pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) pxFromDp(super.getContext(), STEP)) {
            canvas.drawLine(mPadding, mPadding + i, getWidth() - mPadding, mPadding + i, mPaint);
            canvas.drawLine(getWidth() - mPadding - pxFromDp(super.getContext(), 5), mPadding + i - pxFromDp(super.getContext(), 5), getWidth() - mPadding + UNIT / 2, mPadding + i + UNIT / 2, mPaint);
            canvas.drawLine(getWidth() - mPadding - pxFromDp(super.getContext(), 5), mPadding + i + pxFromDp(super.getContext(), 5), getWidth() - mPadding + UNIT / 2, mPadding + i - UNIT / 2, mPaint);
            canvas.drawRect(mPadding,
                    mPadding + i - pxFromDp(super.getContext(), GATE_SIZE),
                    mPadding + pxFromDp(super.getContext(), GATE_SIZE * 2),
                    mPadding + i + pxFromDp(super.getContext(), GATE_SIZE),
                    otherPaint);
            canvas.drawText("q" + Math.round((i + START_Y) / pxFromDp(super.getContext(), STEP)), mPadding - pxFromDp(super.getContext(), 30), mPadding + i + pxFromDp(super.getContext(), 6), mTextPaint);
            canvas.drawText("⎥0⟩", mPadding + pxFromDp(super.getContext(), 2f), mPadding + i + pxFromDp(super.getContext(), 8f), whiteTextPaint);
        }
        int[] gatesNumber = new int[MAX_QUBITS];
        for (int i = 0; i < gos.size(); i++) {
            VisualOperator v = gos.get(i);
            v.resetRect();
            final int[] qubitids = v.getQubitIDs();
            for (int j = 0; j < qubitids.length; j++) {
                gatesNumber[qubitids[j]]++;
                otherPaint.setColor(v.getColor());

                Rect areaRect = new Rect((int) (mPadding + pxFromDp(super.getContext(), 2) + gatesNumber[qubitids[j]] * pxFromDp(super.getContext(), GATE_SIZE * 3)),
                        ((int) (mPadding + pxFromDp(super.getContext(), 2) + (pxFromDp(super.getContext(), STEP) * (qubitids[j])))),
                        ((int) (mPadding + pxFromDp(super.getContext(), 2) + pxFromDp(super.getContext(), GATE_SIZE * 2) + gatesNumber[qubitids[j]] * pxFromDp(super.getContext(), GATE_SIZE * 3))),
                        ((int) (mPadding + (int) pxFromDp(super.getContext(), 20) + (pxFromDp(super.getContext(), STEP) * qubitids[j]) + pxFromDp(super.getContext(), GATE_SIZE))));
                String symbol = "C";
                if (gos.get(i) instanceof LinearOperator) {
                    symbol = ((LinearOperator) v).getSymbol();
                } else {
                    symbol = ((MultiQubitOperator) v).getSymbols()[j];
                }
                v.addRect(areaRect);
                RectF bounds = new RectF(areaRect);
                bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
                bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
                bounds.left += (areaRect.width() - bounds.right) / 2.0f;
                bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

                canvas.drawRect(areaRect, otherPaint);
                if (v instanceof MultiQubitOperator && j != 0) {
                    mPaint.setColor(v.getColor());
                    float center1x = (areaRect.left + areaRect.right) / 2;
                    float center1y = (areaRect.top + areaRect.bottom) / 2;
                    Rect a = v.getRect().get(j - 1);
                    float center2x = (a.left + a.right) / 2;
                    float center2y = (a.top + a.bottom) / 2;
                    center2x += (pxFromDp(super.getContext(), GATE_SIZE / 2) * (center2x - center1x) / (center2y - center1y)) / 2;
                    center2y += pxFromDp(super.getContext(), GATE_SIZE / 1.7f) * (center2y > center1y ? -1 : 1);
                    canvas.drawLine(center1x, center1y, center2x, center2y, mPaint);
                }

                canvas.drawText(symbol, bounds.left, bounds.top - whiteTextPaint.ascent(), whiteTextPaint);
            }
        }
        //canvas.drawRect(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding, mPaint);
        //canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 75, 45, true, mPaint);


        //canvas.drawPath(mPath, mPaint);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2, arcLeft, otherPaint);

        //canvas.drawText("Canvas basics", (float) (getWidth() * 0.3), (float) (getHeight() * 0.8), mTextPaint);

    }

    public VisualOperator whichGate(float posx, float posy) {
        for (int i = 0; i < gos.size(); i++) {
            List<Rect> rects = gos.get(i).getRect();
            for (int j = 0; j < rects.size(); j++) {
                if (posx <= rects.get(j).right && posx >= rects.get(j).left
                        && posy <= rects.get(j).bottom && posy >= rects.get(j).top) {
                    return gos.get(i);
                }
            }
        }
        return null;
    }

    public void addGate(int qbit, LinearOperator l) {
        LinearOperator ml = l.copy();
        ml.setQubitIDs(new int[]{qbit});
        gos.addLast(ml);
        invalidate();
    }

    public void addMultiQubitGate(int[] qubits, MultiQubitOperator m) {
        MultiQubitOperator mm = m.copy();
        mm.setQubitIDs(qubits);
        gos.addLast(mm);
        invalidate();
    }

    public boolean canAddGate(int qbit) {
        if (qbit < 0 || qbit > getDisplayedQubits()) {
            return false;
        }
        int gateNumber = 0;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) (getContext())).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        for (int i = 0; i <= gos.size(); i++) {
            try {
                if (IntStream.of(gos.get(i).getQubitIDs()).noneMatch(x -> x == qbit)) continue;
            } catch (IndexOutOfBoundsException e) {
            }
            gateNumber++;
            if (mPadding + pxFromDp(super.getContext(), 2) + pxFromDp(super.getContext(), GATE_SIZE * 2) + gateNumber * pxFromDp(super.getContext(), GATE_SIZE * 3) > width)
                return false;
        }
        return true;
    }

    public boolean canAddMultiQBitGate(int[] qbits) {
        for (int q : qbits) {
            if (!canAddGate(q)) return false;
        }
        return true;
    }

    public int getDisplayedQubits() {
        int count = 0;
        for (int i = (int) START_Y; i < getHeight() - 2 * mPadding - START_Y && i <= pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) pxFromDp(super.getContext(), STEP)) {
            count++;
        }
        return count;
    }

    public LinkedList<VisualOperator> getOperators() {
        return gos;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}