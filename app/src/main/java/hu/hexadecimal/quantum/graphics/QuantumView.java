package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import hu.hexadecimal.quantum.VisualOperator;
import hu.hexadecimal.quantum.Qubit;

public class QuantumView extends View {

    Paint mPaint, otherPaint, outerPaint, mTextPaint, whiteTextPaint;
    int mPadding;
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

        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);

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

        mPaint.setColor(0xff888888);

        otherPaint.setStyle(Paint.Style.FILL);
        otherPaint.setColor(0xffBA2121);
        for (int i = (int) START_Y; i < getHeight() - 2 * mPadding - START_Y && i <= pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) pxFromDp(super.getContext(), STEP)) {
            canvas.drawLine(mPadding, mPadding + i, getWidth() - mPadding, mPadding + i, mPaint);

            mPath.reset();
            mPath.moveTo(getWidth() - mPadding - pxFromDp(super.getContext(), 5), mPadding + i - pxFromDp(super.getContext(), 5));
            mPath.lineTo(getWidth() - mPadding + UNIT / 2, mPadding + i);
            mPath.lineTo(getWidth() - mPadding - pxFromDp(super.getContext(), 5), mPadding + i + pxFromDp(super.getContext(), 5));
            mPath.close();
            canvas.drawPath(mPath, mPaint);

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
                symbol = v.getSymbols()[j];
                v.addRect(areaRect);
                RectF bounds = new RectF(areaRect);
                bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
                bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
                bounds.left += (areaRect.width() - bounds.right) / 2.0f;
                bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

                canvas.drawRect(areaRect, otherPaint);
                if (j != 0) {
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

    public boolean deleteGateAt(float posx, float posy) {
        for (int i = 0; i < gos.size(); i++) {
            List<Rect> rects = gos.get(i).getRect();
            for (int j = 0; j < rects.size(); j++) {
                if (posx <= rects.get(j).right && posx >= rects.get(j).left
                        && posy <= rects.get(j).bottom && posy >= rects.get(j).top) {
                    gos.remove(i);
                    invalidate();
                    return true;
                }
            }
        }
        return false;
    }

    public void addGate(int[] qubits, VisualOperator m) {
        VisualOperator mm = m.copy();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (IntStream.of(gos.get(i).getQubitIDs()).noneMatch(x -> x == qbit)) continue;
                } else {
                    for (int k = 0; k < gos.size(); k++) {
                        if (gos.get(i).getQubitIDs()[k] != qbit) continue;
                    }
                }
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

    public boolean removeLastGate() {
        if (gos.size() > 0) {
            gos.removeLast();
            invalidate();
            return true;
        }
        return false;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}