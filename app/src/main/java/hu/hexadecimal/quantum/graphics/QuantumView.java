package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import androidx.core.graphics.PaintCompat;
import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.UIHelper;
import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.tools.Doable;
import hu.hexadecimal.quantum.tools.DoableType;
import hu.hexadecimal.quantum.tools.GateSequence;
import hu.hexadecimal.quantum.math.VisualOperator;
import hu.hexadecimal.quantum.tools.QuantumViewData;

/**
 * Main view of the application
 * The user can place/edit gates in this view and execute the circuit displayed
 */
public class QuantumView extends View {

    final Paint linePaint, otherPaint, mTextPaint, whiteTextPaint, hlPaint, strokePaint;
    final int mPadding;
    final Path mPath;

    private LinkedList<VisualOperator> visualOperators;
    private short[] measuredQubits;
    private Complex[] statevector;
    private boolean[] ignoredQubits;
    private int[] highlight = new int[]{-1, -1};
    private RectF highlightRect;
    public boolean saved;

    public volatile boolean shouldStop;

    /**
     * VIsual QUantum-gate Sequence
     */
    public static final String FILE_EXTENSION_LEGACY = ".viqus";
    /**
     * Quantum Sequence File
     */
    public static final String FILE_EXTENSION = ".qsf";
    public static final String OPENQASM_FILE_EXTENSION = ".qasm";

    public static final int STEP = 70;
    public static final int MAX_QUBITS = 10;
    public static final int GATE_SIZE = 18;
    public final float UNIT;
    public final float START_Y;
    public final float START_X;

    public String name = "";

    public LinkedList<Doable> undoList, redoList;

    public QuantumView(Context context) {
        super(context);
        UNIT = UIHelper.pxFromDp(super.getContext(), 1);
        START_Y = UIHelper.pxFromDp(super.getContext(), 20);
        START_X = UIHelper.pxFromDp(super.getContext(), 40);
        visualOperators = new LinkedList<>();

        undoList = new LinkedList<>();
        redoList = new LinkedList<>();
        this.setLongClickable(true);
        this.setOnCreateContextMenuListener(contextMenuListener);


        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(UIHelper.pxFromDp(context, 2.5f));

        strokePaint = new Paint(linePaint);
        strokePaint.setStrokeWidth(UIHelper.pxFromDp(context, 7.5f));

        hlPaint = new Paint();
        hlPaint.setAntiAlias(true);
        hlPaint.setStyle(Paint.Style.STROKE);
        hlPaint.setColor(Color.BLUE);
        hlPaint.setStrokeWidth(UIHelper.pxFromDp(context, 1.5f));

        measuredQubits = new short[MAX_QUBITS];
        ignoredQubits = new boolean[MAX_QUBITS];

        mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.DKGRAY);
        mTextPaint.setTextSize(UIHelper.pxFromDp(context, 24));

        whiteTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        whiteTextPaint.setColor(0xffffffff);
        whiteTextPaint.setTextSize(UIHelper.pxFromDp(context, 24));
        whiteTextPaint.setTypeface(Typeface.MONOSPACE);

        otherPaint = new Paint();
        otherPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mPadding = (int) UIHelper.pxFromDp(context, 32);
        saved = true;
    }

    public double getLimit() {
        return getHeight() - mPadding - START_Y;
    }

    public void setLParams() {
        setLayoutParams(new LinearLayout.LayoutParams((int) (getWidth() + UIHelper.pxFromDp(getContext(), 150)), getRecommendedHeight()));
    }

    public int getRecommendedHeight() {
        return (int) (UIHelper.pxFromDp(getContext(), STEP * MAX_QUBITS) + START_Y + mPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        linePaint.setColor(0xff888888);

        otherPaint.setStyle(Paint.Style.FILL);
        int qubitPos = 0;

        float roundRectRadius = UIHelper.pxFromDp(getContext(), 1.6f);
        float hlPaintStrokeWidth = hlPaint.getStrokeWidth();

        char verticalBar = PaintCompat.hasGlyph(whiteTextPaint, "⎥") ? '⎥' : '|';
        for (float i = START_Y; i < getLimit() && i <= UIHelper.pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += UIHelper.pxFromDp(super.getContext(), STEP)) {
            canvas.drawLine(START_X, mPadding + i, getWidth() - mPadding, mPadding + i, linePaint);

            mPath.reset();
            mPath.moveTo(getWidth() - mPadding - UIHelper.pxFromDp(super.getContext(), 5), mPadding + i - UIHelper.pxFromDp(super.getContext(), 5));
            mPath.lineTo(getWidth() - mPadding + UNIT / 2, mPadding + i);
            mPath.lineTo(getWidth() - mPadding - UIHelper.pxFromDp(super.getContext(), 5), mPadding + i + UIHelper.pxFromDp(super.getContext(), 5));
            mPath.close();
            canvas.drawPath(mPath, linePaint);

            otherPaint.setColor(ignoredQubits[qubitPos] ? 0xffaaaaaa : measuredQubits[qubitPos] > 0 ? 0xffba2121 : 0xff555555);
            canvas.drawRoundRect(START_X,
                    mPadding + i - UIHelper.pxFromDp(super.getContext(), GATE_SIZE),
                    START_X + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2),
                    mPadding + i + UIHelper.pxFromDp(super.getContext(), GATE_SIZE),
                    roundRectRadius, roundRectRadius, otherPaint);
            String qText = "q" + Math.round((i + START_Y) / UIHelper.pxFromDp(super.getContext(), STEP));
            canvas.drawText(qText, (START_X - mTextPaint.measureText(qText)) / 2, mPadding + i + UIHelper.pxFromDp(super.getContext(), 6), mTextPaint);
            canvas.drawText(verticalBar + "0⟩", START_X + (verticalBar == '|' ? -UIHelper.pxFromDp(super.getContext(), 2.8f) : UIHelper.pxFromDp(super.getContext(), 2f)), mPadding + i + UIHelper.pxFromDp(super.getContext(), 8f), whiteTextPaint);
            qubitPos++;
        }
        int[] gatesNumber = new int[MAX_QUBITS];
        RectF bounds = new RectF();
        for (int i = 0; i < visualOperators.size(); i++) {
            VisualOperator v = visualOperators.get(i);
            v.resetRect();
            final int[] qubitIDs = v.getQubitIDs();
            boolean controlled = false;
            int maxGatePosition = getMaxGatesNumber(gatesNumber, qubitIDs);
            for (int j = 0; j < qubitIDs.length; j++) {
                otherPaint.setColor(v.getColor());
                bounds.left = (START_X + UIHelper.pxFromDp(super.getContext(), 2) + maxGatePosition * UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 3));
                bounds.top = (mPadding + UIHelper.pxFromDp(super.getContext(), 2) + (UIHelper.pxFromDp(super.getContext(), STEP) * (qubitIDs[j])));
                RectF areaRect = new RectF(bounds.left,
                        bounds.top,
                        bounds.left + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2),
                        bounds.top + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2));
                String symbol = v.getSymbols()[j];
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
                v.addRect(areaRect);
                bounds.right = whiteTextPaint.measureText(symbol, 0, symbol.length());
                bounds.bottom = whiteTextPaint.descent() - whiteTextPaint.ascent();
                bounds.left += (areaRect.width() - bounds.right) / 2.0f;
                bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

                int hlColor = ((otherPaint.getColor() & 0xff000000) | ((int) (((otherPaint.getColor() & 0xff0000) >> 16) * 0.8) << 16) | ((int) (((otherPaint.getColor() & 0xff00) >> 8) * 0.8) << 8) | ((int) ((otherPaint.getColor() & 0xff) * 0.8)));
                hlPaint.setColor(hlColor);
                boolean isHighlighted = highlightRect != null && highlightRect.contains(areaRect);

                if (symbol.equals(VisualOperator.CNOT.getSymbols()[0])) {
                    float minus = UIHelper.pxFromDp(getContext(), 6f);
                    canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus, otherPaint);
                    if (isHighlighted) {
                        canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus + hlPaintStrokeWidth, hlPaint);
                    }
                } else if (symbol.equals(VisualOperator.CNOT.getSymbols()[1]) || symbol.equals("⊕")) {
                    float minus = UIHelper.pxFromDp(getContext(), -1.5f);
                    canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus, otherPaint);
                    if (isHighlighted) {
                        canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus + hlPaintStrokeWidth, hlPaint);
                    }
                } else if (symbol.equals(VisualOperator.SWAP.getSymbols()[0])) {
                    strokePaint.setColor(v.getColor());
                    float padding = strokePaint.getStrokeWidth() / 2;
                    canvas.drawLine(areaRect.left + padding, areaRect.top + padding, areaRect.right - padding, areaRect.bottom - padding, strokePaint);
                    canvas.drawLine(areaRect.right - padding, areaRect.top + padding, areaRect.left + padding, areaRect.bottom - padding, strokePaint);
                    if (isHighlighted) {
                        canvas.drawRoundRect(areaRect.left - hlPaintStrokeWidth,
                                areaRect.top - hlPaintStrokeWidth,
                                areaRect.right + hlPaintStrokeWidth,
                                areaRect.bottom + hlPaintStrokeWidth,
                                roundRectRadius + hlPaintStrokeWidth, roundRectRadius + hlPaintStrokeWidth, hlPaint);
                    }
                } else {
                    canvas.drawRoundRect(areaRect, roundRectRadius, roundRectRadius, otherPaint);
                    if (isHighlighted) {
                        canvas.drawRoundRect(areaRect.left - hlPaintStrokeWidth,
                                areaRect.top - hlPaintStrokeWidth,
                                areaRect.right + hlPaintStrokeWidth,
                                areaRect.bottom + hlPaintStrokeWidth,
                                roundRectRadius + hlPaintStrokeWidth, roundRectRadius + hlPaintStrokeWidth, hlPaint);
                    }
                }
                if (j != 0) {
                    linePaint.setColor(v.getColor());
                    float center1x = areaRect.centerX();
                    float center1y = areaRect.centerY();
                    RectF a = v.getRect().get(j - 1);
                    float center2x = a.centerX();
                    float center2y = a.centerY();
                    if (!v.getSymbols()[j - 1].equals(VisualOperator.SWAP.getSymbols()[0])) {
                        center2x += (UIHelper.pxFromDp(super.getContext(), GATE_SIZE / (controlled ? 1.55f : 1.15f)) * (center1x - center2x) / Math.sqrt(Math.pow((center2x - center1x), 2) + Math.pow((center2y - center1y), 2)));
                        center2y += UIHelper.pxFromDp(super.getContext(), GATE_SIZE / (controlled ? 1.55f : 1.15f)) * (center1y - center2y) / Math.sqrt(Math.pow((center2x - center1x), 2) + Math.pow((center2y - center1y), 2));
                    }
                    canvas.drawLine(center1x, center1y, center2x, center2y, linePaint);
                }

                if (symbol.equals("●")) {
                    controlled = true;
                } else if (symbol.equals(VisualOperator.SWAP.getSymbols()[0])) {
                    //Do nothing
                    continue;
                } else {
                    canvas.drawText(symbol, bounds.left, bounds.top - whiteTextPaint.ascent(), whiteTextPaint);
                    if (controlled && !(symbol.equals(VisualOperator.CNOT.getSymbols()[1]) || symbol.equals("⊕"))) {
                        whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 11));
                        canvas.drawText("T", areaRect.right - (whiteTextPaint.measureText("T") * 1.3f), bounds.top - (whiteTextPaint.ascent() / 1.3f), whiteTextPaint);
                    }
                }
            }
        }
        whiteTextPaint.setTextSize(UIHelper.pxFromDp(super.getContext(), 24));

    }

    private int getMaxGatesNumber(int[] gatesNumber, int[] qubitIDs) {
        int max = 0;
        for (int qubitID : qubitIDs) {
            gatesNumber[qubitID]++;
            if (gatesNumber[qubitID] > max)
                max = gatesNumber[qubitID];
        }
        for (int qubitID : qubitIDs) gatesNumber[qubitID] = max;
        return max;
    }

    public boolean isStartRow(float posx) {
        return (START_X + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2)) >= posx;
    }

    public void toggleIgnoredState(int qubit) {
        ignoredQubits[qubit] = !ignoredQubits[qubit];
        super.invalidate();
    }

    public boolean[] getIgnoredQubits() {
        return ignoredQubits;
    }

    public VisualOperator whichGate(float posx, float posy) {
        for (int i = 0; i < visualOperators.size(); i++) {
            List<RectF> rectList = visualOperators.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    return visualOperators.get(i);
                }
            }
        }
        return null;
    }

    public int[] getHighlight() {
        return highlight;
    }

    public void highlightOperator(int[] gridPos) {
        this.highlight = gridPos;
        this.highlightRect = getRectInGrid(gridPos);
        super.invalidate();
    }

    public void moveHighlight(int where) {
        if (highlight[0] < 0 || highlight[1] < 0) {
            highlightOperator(new int[]{0, 0});
            return;
        }
        switch (where) {
            case 0:
                //UP
                if (highlight[0] == 0)
                    return;
                else if (measuredQubits[highlight[0] - 1] == 0) {
                    highlight[0]--;
                    moveHighlight(0);
                    return;
                }
                highlightOperator(new int[]{--highlight[0], highlight[1] > measuredQubits[highlight[0]] - 1 ? measuredQubits[highlight[0]] - 1 : highlight[1]});
                break;
            case 2:
                //DOWN
                if (highlight[0] == getLastUsedQubit())
                    return;
                else if (measuredQubits[highlight[0] + 1] == 0) {
                    highlight[0]++;
                    moveHighlight(2);
                    return;
                }
                highlightOperator(new int[]{++highlight[0], highlight[1] > measuredQubits[highlight[0]] - 1 ? measuredQubits[highlight[0]] - 1 : highlight[1]});
                break;
            case 1:
                //LEFT
                if (highlight[1] == 0)
                    return;
                highlightOperator(new int[]{highlight[0], --highlight[1]});
                break;
            case 3:
                //RIGHT
                if (highlight[1] == measuredQubits[highlight[0]] - 1)
                    return;
                highlightOperator(new int[]{highlight[0], ++highlight[1]});
                break;
        }
    }

    public VisualOperator getGateInGrid(int[] gridPos) {
        int qubit = gridPos[0];
        int pos = gridPos[1];
        int currentPos = 0;
        if (qubit >= MAX_QUBITS || pos >= measuredQubits[qubit]) {
            return null;
        }
        for (int i = 0; i < visualOperators.size(); i++) {
            VisualOperator op = visualOperators.get(i);
            int[] qubits = op.getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                if (qubits[j] == qubit) {
                    if (currentPos++ == pos) {
                        return op;
                    }
                }
            }
        }
        return null;
    }

    public RectF getRectInGrid(int[] gridPos) {
        int qubit = gridPos[0];
        int pos = gridPos[1];
        int currentPos = 0;
        if (qubit > MAX_QUBITS || qubit < 0 || pos >= measuredQubits[qubit]) {
            return null;
        }
        for (int i = 0; i < visualOperators.size(); i++) {
            VisualOperator op = visualOperators.get(i);
            int[] qubits = op.getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                if (qubits[j] == qubit) {
                    if (currentPos++ == pos) {
                        try {
                            return op.getRect().get(j);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean replaceGateAt(int[] qubits, VisualOperator visualOperator, float posx, float posy) {
        if (posx < 0 || posy < 0) {
            addGate(qubits, visualOperator);
            return true;
        } else {
            for (int i = 0; i < visualOperators.size(); i++) {
                List<RectF> rectList = visualOperators.get(i).getRect();
                for (int j = 0; j < rectList.size(); j++) {
                    if (rectList.get(j).contains(posx, posy)) {
                        for (int qubit : visualOperators.get(i).getQubitIDs()) {
                            measuredQubits[qubit]--;
                        }
                        VisualOperator old = visualOperators.remove(i);
                        for (int qubit : qubits) {
                            if (qubit >= getDisplayedQubits()) {
                                invalidate();
                                return false;
                            }
                            if (!canAddGate(qubit))
                                setLParams();
                            measuredQubits[qubit]++;
                        }
                        visualOperator.setQubitIDs(qubits);
                        visualOperators.add(i, visualOperator);
                        undoList.addLast(new Doable(visualOperator, DoableType.EDIT, getContext(), i, old));
                        redoList.clear();
                        if (getRectInGrid(highlight) == null && highlight[0] != -1 && highlight[1] != -1) {
                            moveHighlight(0);
                        }
                        invalidate();
                        saved = false;
                        return true;
                    }
                }
            }
            visualOperators.addLast(visualOperator);
            undoList.addLast(new Doable(visualOperator, DoableType.ADD, getContext()));
            if (getRectInGrid(highlight) == null && highlight[0] != -1 && highlight[1] != -1) {
                moveHighlight(0);
            }
            invalidate();
            return false;
        }
    }

    public boolean deleteGateAt(float posx, float posy) {
        for (int i = 0; i < visualOperators.size(); i++) {
            List<RectF> rectList = visualOperators.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    for (int qubit : visualOperators.get(i).getQubitIDs()) {
                        measuredQubits[qubit]--;
                    }
                    undoList.addLast(new Doable(visualOperators.remove(i), DoableType.DELETE, getContext(), i, null));
                    redoList.clear();
                    if (getRectInGrid(highlight) == null && highlight[0] != -1 && highlight[1] != -1) {
                        moveHighlight(0);
                    }
                    invalidate();
                    saved = false;
                    return true;
                }
            }
        }
        return false;
    }

    public void addGate(int[] qubits, VisualOperator m) {
        for (int qubit : qubits) {
            if (qubit >= getDisplayedQubits()) return;
            if (!canAddGate(qubit))
                setLParams();
            measuredQubits[qubit]++;
        }
        VisualOperator mm = m.copy();
        mm.setQubitIDs(qubits);
        visualOperators.addLast(mm);
        invalidate();
        undoList.addLast(new Doable(mm, DoableType.ADD, getContext()));
        redoList.clear();
        saved = false;
    }

    public boolean canAddGate(int qubit) {
        if (qubit < 0 || qubit > getDisplayedQubits()) {
            return false;
        }
        int gateNumber = 0;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) (getContext())).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = getWidth() < 1 ? displayMetrics.widthPixels : getWidth();
        outerLoop:
        for (int i = 0; i <= visualOperators.size() + 1; i++) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (IntStream.of(visualOperators.get(i).getQubitIDs()).noneMatch(x -> x == qubit)) continue;
                } else {
                    for (int k = 0; k < visualOperators.size(); k++) {
                        if (visualOperators.get(i).getQubitIDs()[k] == qubit) break;
                        if (k == visualOperators.size() - 1) continue outerLoop;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Log.v("QuantumView", "Controlled error");
            }
            gateNumber++;
            if (START_X + UIHelper.pxFromDp(super.getContext(), 2) + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 3) + gateNumber * UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 3) > width)
                return false;
        }
        return true;
    }

    public int getDisplayedQubits() {
        int count = 0;
        for (int i = (int) START_Y; i < getLimit() && i <= UIHelper.pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) UIHelper.pxFromDp(super.getContext(), STEP)) {
            count++;
        }
        return count;
    }

    public int whichQubit(float posy) {
        int count = 0;
        for (int i = (int) START_Y; i < getLimit() && i <= UIHelper.pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) UIHelper.pxFromDp(super.getContext(), STEP)) {
            if (posy > i && posy < i + (int) UIHelper.pxFromDp(super.getContext(), STEP))
                return count;
            count++;
        }
        return -1;
    }

    public short[] getMeasuredQubits() {
        return measuredQubits;
    }

    public LinkedList<VisualOperator> getOperators() {
        return visualOperators;
    }

    public QuantumViewData getData() {
        return new QuantumViewData(undoList, redoList, new GateSequence<>(visualOperators, name), new LinkedList(Arrays.asList(statevector)));
    }

    public boolean setData(QuantumViewData data) {
        try {
            this.name = data.operators.getName();
            visualOperators = new LinkedList<>();
            measuredQubits = new short[MAX_QUBITS];
            boolean hadError = false;
            for (VisualOperator vo : data.operators) {
                if (vo == null) {
                    hadError = true;
                    continue;
                }
                addGate(vo.getQubitIDs(), vo);
            }
            undoList = data.undoList;
            redoList = data.redoList;
            statevector = (Complex[]) data.statevector.toArray();
            super.invalidate();
            return !hadError;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Call this.invalidate() if the gates have changed
     * call super.invalidate() if the gates haven't changed
     */
    public void invalidate() {
        statevector = null;
        super.invalidate();
    }

    public void setStatevector(Complex[] statevector) {
        this.statevector = statevector;
    }

    public Complex[] getStatevector() {
        return statevector;
    }

    public boolean removeLastGate() {
        if (visualOperators.size() > 0) {
            VisualOperator v = visualOperators.removeLast();
            for (int i = 0; i < v.getQubitIDs().length; i++) {
                measuredQubits[v.getQubitIDs()[i]]--;
            }
            invalidate();
            undoList.addLast(new Doable(v, DoableType.DELETE, getContext(), visualOperators.size(), null));
            redoList.clear();
            saved = false;
            return true;
        }
        return false;
    }

    public void clearScreen() {
        visualOperators = new LinkedList<>();
        measuredQubits = null;
        measuredQubits = new short[MAX_QUBITS];
        ignoredQubits = new boolean[MAX_QUBITS];
        undoList.clear();
        redoList.clear();
        invalidate();
        saved = false;
    }

    public int getLastUsedQubit() {
        int last = 0;
        for (int i = 0; i < measuredQubits.length; i++) {
            if (measuredQubits[i] > 0) {
                last = i;
            }
        }
        return last;
    }

    public int getUsedQubitsCount() {
        int count = 0;
        for (int i = 0; i < measuredQubits.length; i++) {
            if (measuredQubits[i] > 0) {
                count++;
            }
        }
        return count;
    }

    public void moveGate(float posx, float posy, boolean toRight) {
        int index = 0;
        int qubit = 0;
        VisualOperator operator = null;
        outer:
        for (int i = 0; i < visualOperators.size(); i++) {
            List<RectF> rectList = visualOperators.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    operator = visualOperators.get(i);
                    qubit = operator.getQubitIDs()[j];
                    //Log.e("X", "" + qubit);
                    index = i;
                    break outer;
                }
            }
        }
        if (operator == null)
            return;
        int direction = toRight ? 1 : -1;
        outer:
        for (int i = index + direction; i < visualOperators.size() && i >= 0; i += direction) {
            int[] qubits = visualOperators.get(i).getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                if (qubits[j] == qubit) {
                    visualOperators.remove(index);
                    visualOperators.add(i, operator);
                    invalidate();
                    undoList.addLast(new Doable(operator, DoableType.MOVE, getContext(), index, i));
                    redoList.clear();
                    saved = false;
                    return;
                }
            }
        }
    }

    public JSONObject exportGates(String name) {
        try {
            return new GateSequence<VisualOperator>(visualOperators, name).toJSON();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean importGates(JSONObject input) {
        try {
            GateSequence<VisualOperator> visualOperators = GateSequence.fromJSON(input);
            this.name = visualOperators.getName();
            this.visualOperators = new LinkedList<>();
            measuredQubits = new short[MAX_QUBITS];
            boolean hadError = false;
            for (VisualOperator vo : visualOperators) {
                if (vo == null) {
                    hadError = true;
                    continue;
                }
                addGate(vo.getQubitIDs(), vo);
            }
            undoList.clear();
            redoList.clear();
            invalidate();
            return !hadError;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int optimizeCircuit() {
        if (visualOperators.size() == 0) return 0;
        int counter = MAX_QUBITS;
        boolean hasChanged = false;
        for (int i = MAX_QUBITS - 1; i > -1; i--) {
            if (measuredQubits[i] == 0) {
                counter--;
                for (int j = 0; j < visualOperators.size(); j++) {
                    VisualOperator operator = visualOperators.get(j);
                    int[] quids = operator.getQubitIDs();
                    for (int k = 0; k < quids.length; k++) {
                        if (quids[k] <= i) continue;
                        measuredQubits[quids[k] - 1]++;
                        measuredQubits[quids[k]]--;
                        quids[k]--;
                        hasChanged = true;
                    }
                }
            }
        }
        if (hasChanged)
            invalidate();
        return counter;
    }

    public String openQASMExport() {
        StringBuilder builder = new StringBuilder();
        builder.append("OPENQASM 2.0;\n" +
                "include \"qelib1.inc\";\n\n");
        builder.append("qreg qubit[");
        builder.append(getLastUsedQubit() + 1);
        builder.append("];\n");
        builder.append("creg c[");
        builder.append(getLastUsedQubit() + 1);
        builder.append("];\n\n");
        for (VisualOperator visualOperator : visualOperators) {
            try {
                builder.append(visualOperator.getOpenQASMSymbol());
                builder.append("\n");
            } catch (Exception e) {
                e.printStackTrace();
                builder.append("//Error while exporting the following: ");
                builder.append(visualOperator.getName());
                builder.append("\n");
            }
        }
        builder.append("measure qubit -> c;\n");
        return builder.toString();
    }

    public void undo() {
        try {
            Doable d = undoList.getLast();
            VisualOperator visualOperator = d.getVisualOperator();
            switch (d.getType()) {
                case ADD:
                    if (visualOperators.size() > 0) {
                        VisualOperator v = visualOperators.removeLast();
                        for (int i = 0; i < v.getQubitIDs().length; i++) {
                            measuredQubits[v.getQubitIDs()[i]]--;
                        }
                        break;
                    }
                case DELETE:
                    for (int qubit : visualOperator.getQubitIDs()) {
                        if (qubit >= getDisplayedQubits()) return;
                        if (!canAddGate(qubit))
                            setLParams();
                        measuredQubits[qubit]++;
                    }
                    visualOperators.add(d.index, visualOperator);
                    break;
                case EDIT:
                    VisualOperator old = visualOperators.remove(d.index);
                    for (int qubit : old.getQubitIDs()) {
                        measuredQubits[qubit]--;
                    }
                    for (int qubit : d.oldOperator().getQubitIDs()) {
                        if (qubit >= getDisplayedQubits()) {
                            invalidate();
                            return;
                        }
                        if (!canAddGate(qubit))
                            setLParams();
                        measuredQubits[qubit]++;
                    }
                    visualOperators.add(d.index, d.oldOperator());
                    break;
                case MOVE:
                    visualOperators.remove(d.index);
                    visualOperators.add(d.oldIndex, visualOperator);
                    break;
            }
            invalidate();
            redoList.addLast(undoList.removeLast());
            saved = false;
        } catch (NoSuchElementException e) {
        }
    }

    public void redo() {
        try {
            Doable d = redoList.getLast();
            VisualOperator visualOperator = d.getVisualOperator();
            switch (d.getType()) {
                case DELETE:
                    if (visualOperators.size() > 0) {
                        VisualOperator v = visualOperators.remove(d.index);
                        for (int i = 0; i < v.getQubitIDs().length; i++) {
                            measuredQubits[v.getQubitIDs()[i]]--;
                        }
                        break;
                    }
                case ADD:
                    for (int qubit : visualOperator.getQubitIDs()) {
                        if (qubit >= getDisplayedQubits()) return;
                        if (!canAddGate(qubit))
                            setLParams();
                        measuredQubits[qubit]++;
                    }
                    visualOperators.addLast(visualOperator);
                    break;
                case EDIT:
                    VisualOperator old = visualOperators.remove(d.index);
                    for (int qubit : old.getQubitIDs()) {
                        measuredQubits[qubit]--;
                    }
                    for (int qubit : visualOperator.getQubitIDs()) {
                        if (qubit >= getDisplayedQubits()) {
                            invalidate();
                            return;
                        }
                        if (!canAddGate(qubit))
                            setLParams();
                        measuredQubits[qubit]++;
                    }
                    visualOperators.add(d.index, visualOperator);
                    break;
                case MOVE:
                    visualOperators.remove(d.oldIndex);
                    visualOperators.add(d.index, visualOperator);
                    break;
            }
            invalidate();
            undoList.addLast(redoList.removeLast());
            saved = false;
        } catch (NoSuchElementException e) {

        }
    }

    View.OnCreateContextMenuListener contextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(getContext().getString(R.string.doable_edit_menu));
            try {
                contextMenu.add(0, 0, 0,
                        Html.fromHtml("<b>" + QuantumView.this.getContext().getString(R.string.undo) + "</b> " + undoList.getLast().name + ""))
                        .setOnMenuItemClickListener(menuItemClickListener);
            } catch (NoSuchElementException e) {
                contextMenu.add(0, 0, 0, QuantumView.this.getContext().getString(R.string.undo));
                contextMenu.setGroupEnabled(0, false);
            }

            try {
                contextMenu.add(1, 1, 1,
                        Html.fromHtml("<b>" + QuantumView.this.getContext().getString(R.string.redo) + "</b> " + redoList.getLast().name + ""))
                        .setOnMenuItemClickListener(menuItemClickListener);
            } catch (NoSuchElementException e) {
                contextMenu.add(1, 1, 1, QuantumView.this.getContext().getString(R.string.redo));
                contextMenu.setGroupEnabled(1, false);
            }
        }
    };

    MenuItem.OnMenuItemClickListener menuItemClickListener = (MenuItem menuItem) -> {
        switch (menuItem.getItemId()) {
            case 0:
                undo();
                return true;
            case 1:
                redo();
                return true;
        }
        return false;
    };
}