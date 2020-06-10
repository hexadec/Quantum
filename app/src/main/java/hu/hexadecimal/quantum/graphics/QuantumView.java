package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import androidx.core.graphics.PaintCompat;
import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.UIHelper;
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

    final Paint mPaint, otherPaint, mTextPaint, whiteTextPaint, hlPaint;
    final int mPadding;
    final Path mPath;

    private LinkedList<VisualOperator> gos;
    private short[] measuredQubits;
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
        gos = new LinkedList<>();

        undoList = new LinkedList<>();
        redoList = new LinkedList<>();
        this.setLongClickable(true);
        this.setOnCreateContextMenuListener(contextMenuListener);


        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(UIHelper.pxFromDp(context, 2.5f));

        hlPaint = new Paint();
        hlPaint.setAntiAlias(true);
        hlPaint.setStyle(Paint.Style.STROKE);
        hlPaint.setColor(Color.BLUE);
        hlPaint.setStrokeWidth(UIHelper.pxFromDp(context, 1.5f));

        measuredQubits = new short[MAX_QUBITS];

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

        mPaint.setColor(0xff888888);

        otherPaint.setStyle(Paint.Style.FILL);
        int qubitPos = 0;
        char verticalBar = PaintCompat.hasGlyph(whiteTextPaint, "⎥") ? '⎥' : '|';
        for (int i = (int) START_Y; i < getLimit() && i <= UIHelper.pxFromDp(super.getContext(), STEP * MAX_QUBITS); i += (int) UIHelper.pxFromDp(super.getContext(), STEP)) {
            canvas.drawLine(START_X, mPadding + i, getWidth() - mPadding, mPadding + i, mPaint);

            mPath.reset();
            mPath.moveTo(getWidth() - mPadding - UIHelper.pxFromDp(super.getContext(), 5), mPadding + i - UIHelper.pxFromDp(super.getContext(), 5));
            mPath.lineTo(getWidth() - mPadding + UNIT / 2, mPadding + i);
            mPath.lineTo(getWidth() - mPadding - UIHelper.pxFromDp(super.getContext(), 5), mPadding + i + UIHelper.pxFromDp(super.getContext(), 5));
            mPath.close();
            canvas.drawPath(mPath, mPaint);

            otherPaint.setColor(measuredQubits[qubitPos] > 0 ? 0xffBA2121 : 0xff555555);
            canvas.drawRect(START_X,
                    mPadding + i - UIHelper.pxFromDp(super.getContext(), GATE_SIZE),
                    START_X + UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 2),
                    mPadding + i + UIHelper.pxFromDp(super.getContext(), GATE_SIZE),
                    otherPaint);
            String qText = "q" + Math.round((i + START_Y) / UIHelper.pxFromDp(super.getContext(), STEP));
            canvas.drawText(qText, (START_X - mTextPaint.measureText(qText)) / 2, mPadding + i + UIHelper.pxFromDp(super.getContext(), 6), mTextPaint);
            canvas.drawText(verticalBar + "0⟩", START_X + (verticalBar == '|' ? -UIHelper.pxFromDp(super.getContext(), 2.8f) : UIHelper.pxFromDp(super.getContext(), 2f)), mPadding + i + UIHelper.pxFromDp(super.getContext(), 8f), whiteTextPaint);
            qubitPos++;
        }
        int[] gatesNumber = new int[MAX_QUBITS];
        RectF bounds = new RectF();
        for (int i = 0; i < gos.size(); i++) {
            VisualOperator v = gos.get(i);
            v.resetRect();
            final int[] qubitIDs = v.getQubitIDs();
            boolean controlled = false;
            for (int j = 0; j < qubitIDs.length; j++) {
                gatesNumber[qubitIDs[j]]++;
                otherPaint.setColor(v.getColor());
                bounds.left = (START_X + UIHelper.pxFromDp(super.getContext(), 2) + gatesNumber[qubitIDs[j]] * UIHelper.pxFromDp(super.getContext(), GATE_SIZE * 3));
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
                        canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus + hlPaint.getStrokeWidth(), hlPaint);
                    }
                } else if (symbol.equals(VisualOperator.CNOT.getSymbols()[1]) || symbol.equals("⊕")) {
                    float minus = UIHelper.pxFromDp(getContext(), -1.5f);
                    canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus, otherPaint);
                    if (isHighlighted) {
                        canvas.drawCircle(areaRect.centerX(), areaRect.centerY(), areaRect.width() / 2 - minus + hlPaint.getStrokeWidth(), hlPaint);
                    }
                } else {
                    canvas.drawRect(areaRect, otherPaint);
                    if (isHighlighted) {
                        canvas.drawRect(areaRect.left - hlPaint.getStrokeWidth(),
                                areaRect.top - hlPaint.getStrokeWidth(),
                                areaRect.right + hlPaint.getStrokeWidth(),
                                areaRect.bottom + hlPaint.getStrokeWidth(), hlPaint);
                    }
                }
                if (j != 0) {
                    mPaint.setColor(v.getColor());
                    float center1x = areaRect.centerX();
                    float center1y = areaRect.centerY();
                    RectF a = v.getRect().get(j - 1);
                    float center2x = a.centerX();
                    float center2y = a.centerY();
                    center2x += (UIHelper.pxFromDp(super.getContext(), GATE_SIZE / (controlled ? 1.55f : 1.15f)) * (center1x - center2x) / Math.sqrt(Math.pow((center2x - center1x), 2) + Math.pow((center2y - center1y), 2)));
                    center2y += UIHelper.pxFromDp(super.getContext(), GATE_SIZE / (controlled ? 1.55f : 1.15f)) * (center1y - center2y) / Math.sqrt(Math.pow((center2x - center1x), 2) + Math.pow((center2y - center1y), 2));
                    canvas.drawLine(center1x, center1y, center2x, center2y, mPaint);
                }

                if (symbol.equals("●")) {
                    controlled = true;
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

    public VisualOperator whichGate(float posx, float posy) {
        for (int i = 0; i < gos.size(); i++) {
            List<RectF> rectList = gos.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    return gos.get(i);
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
        invalidate();
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
        for (int i = 0; i < gos.size(); i++) {
            VisualOperator op = gos.get(i);
            int[] qubits = op.getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                if (qubits[j] != qubit) {
                    continue;
                } else {
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
        for (int i = 0; i < gos.size(); i++) {
            VisualOperator op = gos.get(i);
            int[] qubits = op.getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                if (qubits[j] != qubit) {
                    continue;
                } else {
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
            for (int i = 0; i < gos.size(); i++) {
                List<RectF> rectList = gos.get(i).getRect();
                for (int j = 0; j < rectList.size(); j++) {
                    if (rectList.get(j).contains(posx, posy)) {
                        for (int qubit : gos.get(i).getQubitIDs()) {
                            measuredQubits[qubit]--;
                        }
                        VisualOperator old = gos.remove(i);
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
                        gos.add(i, visualOperator);
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
            gos.addLast(visualOperator);
            undoList.addLast(new Doable(visualOperator, DoableType.ADD, getContext()));
            if (getRectInGrid(highlight) == null && highlight[0] != -1 && highlight[1] != -1) {
                moveHighlight(0);
            }
            invalidate();
            return false;
        }
    }

    public boolean deleteGateAt(float posx, float posy) {
        for (int i = 0; i < gos.size(); i++) {
            List<RectF> rectList = gos.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    for (int qubit : gos.get(i).getQubitIDs()) {
                        measuredQubits[qubit]--;
                    }
                    undoList.addLast(new Doable(gos.remove(i), DoableType.DELETE, getContext(), i, null));
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
        gos.addLast(mm);
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
        for (int i = 0; i <= gos.size() + 1; i++) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (IntStream.of(gos.get(i).getQubitIDs()).noneMatch(x -> x == qubit)) continue;
                } else {
                    for (int k = 0; k < gos.size(); k++) {
                        if (gos.get(i).getQubitIDs()[k] == qubit) break;
                        if (k == gos.size() - 1) continue outerLoop;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
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
        return gos;
    }

    public QuantumViewData getData() {
        return new QuantumViewData(undoList, redoList, new GateSequence<>(gos, name));
    }

    public boolean setData(QuantumViewData data) {
        try {
            this.name = data.operators.getName();
            gos = new LinkedList<>();
            measuredQubits = new short[MAX_QUBITS];
            boolean hadError = false;
            for (VisualOperator vo : ((LinkedList<VisualOperator>) data.operators)) {
                if (vo == null) {
                    hadError = true;
                    continue;
                }
                addGate(vo.getQubitIDs(), vo);
            }
            undoList = data.undoList;
            redoList = data.redoList;
            invalidate();
            return !hadError;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeLastGate() {
        if (gos.size() > 0) {
            VisualOperator v = gos.removeLast();
            for (int i = 0; i < v.getQubitIDs().length; i++) {
                measuredQubits[v.getQubitIDs()[i]]--;
            }
            invalidate();
            undoList.addLast(new Doable(v, DoableType.DELETE, getContext(), gos.size(), null));
            redoList.clear();
            saved = false;
            return true;
        }
        return false;
    }

    public void clearScreen() {
        gos = new LinkedList<>();
        measuredQubits = null;
        measuredQubits = new short[MAX_QUBITS];
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
        VisualOperator operator = null;
        outer:
        for (int i = 0; i < gos.size(); i++) {
            List<RectF> rectList = gos.get(i).getRect();
            for (int j = 0; j < rectList.size(); j++) {
                if (rectList.get(j).contains(posx, posy)) {
                    operator = gos.get(i);
                    index = i;
                    break outer;
                }
            }
        }
        if (operator == null)
            return;
        int direction = toRight ? 1 : -1;
        outer:
        for (int i = index + direction; i < gos.size() && i > -1; i += direction) {
            int[] qubits = gos.get(i).getQubitIDs();
            for (int j = 0; j < qubits.length; j++) {
                for (int m = 0; m < operator.getQubitIDs().length; m++) {
                    if (qubits[j] == operator.getQubitIDs()[m]) {
                        gos.remove(index);
                        gos.add(i, operator);
                        invalidate();
                        undoList.addLast(new Doable(operator, DoableType.MOVE, getContext(), index, i));
                        redoList.clear();
                        saved = false;
                        return;
                    }
                }
            }
        }
    }

    public JSONObject exportGates(String name) {
        try {
            return new GateSequence<VisualOperator>(gos, name).toJSON();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean importGates(JSONObject input) {
        try {
            GateSequence<VisualOperator> visualOperators = GateSequence.fromJSON(input);
            this.name = visualOperators.getName();
            gos = new LinkedList<>();
            measuredQubits = new short[MAX_QUBITS];
            boolean hadError = false;
            for (VisualOperator vo : ((LinkedList<VisualOperator>) visualOperators)) {
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
        if (gos.size() == 0) return 0;
        int counter = MAX_QUBITS;
        for (int i = MAX_QUBITS - 1; i > -1; i--) {
            if (measuredQubits[i] == 0) {
                counter--;
                for (int j = 0; j < gos.size(); j++) {
                    VisualOperator operator = gos.get(j);
                    int[] quids = operator.getQubitIDs();
                    for (int k = 0; k < quids.length; k++) {
                        if (quids[k] <= i) continue;
                        measuredQubits[quids[k] - 1]++;
                        measuredQubits[quids[k]]--;
                        quids[k]--;
                    }
                }
            }
        }
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
        for (VisualOperator visualOperator : gos) {
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
                    if (gos.size() > 0) {
                        VisualOperator v = gos.removeLast();
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
                    gos.add(d.index, visualOperator);
                    break;
                case EDIT:
                    VisualOperator old = gos.remove(d.index);
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
                    gos.add(d.index, d.oldOperator());
                    break;
                case MOVE:
                    gos.remove(d.index);
                    gos.add(d.oldIndex, visualOperator);
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
                    if (gos.size() > 0) {
                        VisualOperator v = gos.remove(d.index);
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
                    gos.addLast(visualOperator);
                    break;
                case EDIT:
                    VisualOperator old = gos.remove(d.index);
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
                    gos.add(d.index, visualOperator);
                    break;
                case MOVE:
                    gos.remove(d.oldIndex);
                    gos.add(d.index, visualOperator);
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

    MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case 0:
                    undo();
                    return true;
                case 1:
                    redo();
                    return true;
            }
            return false;
        }
    };
}