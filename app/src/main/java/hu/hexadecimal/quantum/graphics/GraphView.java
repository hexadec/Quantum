package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;

public class GraphView extends View {

    protected float min, max, size;
    protected float[] positions, values;
    protected Paint axisPaint, gridPaint, gridMarkerPaint, gridMarker2Paint, linePaint, labelPaint, labelMarkPaint, naPaint;
    protected boolean guidingLinesEnabled = false;
    protected float START_X, START_Y, END_X, END_Y;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public GraphView(Context context) {
        super(context);
        setupView();
    }

    public GraphView(Context context, @NonNull float[] values, int size) {
        super(context);
        setupView();
        this.values = values;
        this.size = size;
        positions = new float[values.length * 4];
        calculateMinMax();
        invalidate();
    }

    public void setData(@NonNull float[] data, int size) {
        this.values = data;
        this.size = size;
        positions = new float[values.length * 4];
        calculateMinMax();
        invalidate();
    }

    private void calculateMinMax() {
        if (values.length == 0) {
            min = 0;
            max = 0;
            return;
        }
        min = 0;
        max = 0;
        for (int i = 0; i < size; i++) {
            float current = values[i];
            if (current > max) {
                max = current;
            }
            if (current < min) {
                min = current;
            }
        }
        max *= 1.05;
    }

    protected void setupView() {
        guidingLinesEnabled = false;

        axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setAntiAlias(true);
        axisPaint.setStrokeWidth(pxFromDp(getContext(), 2.5f));

        gridPaint = new Paint();
        gridPaint.setColor(0xffbbbbbb);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(pxFromDp(getContext(), 0.7f));

        gridMarkerPaint = new Paint();
        gridMarkerPaint.setColor(0xffbbbbbb);
        gridMarkerPaint.setStyle(Paint.Style.STROKE);
        gridMarkerPaint.setAntiAlias(true);
        gridMarkerPaint.setStrokeWidth(pxFromDp(getContext(), 1f));

        gridMarker2Paint = new Paint();
        gridMarker2Paint.setColor(0xffbbbbbb);
        gridMarker2Paint.setStyle(Paint.Style.STROKE);
        gridMarker2Paint.setAntiAlias(true);
        gridMarker2Paint.setStrokeWidth(pxFromDp(getContext(), 1.3f));

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.RED);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(pxFromDp(getContext(), 1f));

        labelPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTypeface(Typeface.MONOSPACE);
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextSize(pxFromDp(getContext(), 10));

        labelMarkPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        labelMarkPaint.setTypeface((Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)));
        labelMarkPaint.setColor(Color.DKGRAY);
        labelMarkPaint.setTextSize(pxFromDp(getContext(), 10));

        naPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        naPaint.setColor(Color.DKGRAY);
        naPaint.setTextSize(pxFromDp(getContext(), 32));

        setupPadding();
    }

    protected void setupPadding() {
        float niceNumber = new BigDecimal((max - min) / 10).round(new MathContext(1, RoundingMode.HALF_UP)).floatValue();
        START_X = labelPaint.measureText(formatNumber(niceNumber)) + pxFromDp(getContext(), 2);
        START_Y = pxFromDp(getContext(), 10);
        END_X = getWidth() - pxFromDp(getContext(), 10);
        END_Y = getHeight() - pxFromDp(getContext(), 15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("DisplayView", "onDraw was called");
        super.onDraw(canvas);

        setupPadding();

        if (values == null || values.length == 0) {
            drawAxes(canvas);
            canvas.drawText("N/A", (getWidth() - naPaint.measureText("N/A")) / 2, (getHeight() - naPaint.ascent()) / 2, naPaint);
            return;
        }

        drawAxes(canvas);

        float astroke2 = axisPaint.getStrokeWidth() / 2f;
        float width = END_X - START_X - astroke2;
        float height = END_Y - START_Y - astroke2;
        //Step size for larger data sets
        final int step = 1;
        for (int i = 0; i < size - step; i += step) {
            final float current = values[i];
            final float next = values[i + step];
            positions[4 * i + 1] = height + START_Y - ((current - min) / (max - min)) * (height * 1f);
            positions[4 * i] = size - 1 == 0 ? 0 : (START_X + astroke2 + (i * 1f) / (size - 1) * (width * 1f));
            positions[4 * i + 3] = height + START_Y - ((next - min) / (max - min)) * (height * 1f);
            positions[4 * i + 2] = (START_X + astroke2 + (i + step * 1f) / (size - 1) * (width * 1f));
        }

        drawTimeGuidingLines(canvas, width);
        drawDataGuidingLines(canvas, height);
        canvas.drawLines(positions, linePaint);
    }

    public void drawAxes(Canvas canvas) {
        float step = pxFromDp(getContext(), 15);
        if (guidingLinesEnabled) {
            for (float i = START_X + step; i <= END_X; i += step)
                canvas.drawLine(i, START_Y, i, END_Y, gridPaint);
            for (float i = END_Y - step; i >= START_Y; i -= step)
                canvas.drawLine(START_X, i, END_X, i, gridPaint);
        }
        canvas.drawLine(START_X, END_Y, END_X, END_Y, axisPaint);
        canvas.drawLine(START_X, START_Y, START_X, END_Y, axisPaint);
    }

    public void drawTimeGuidingLines(Canvas canvas, float width) {
        //This method is responsible for printing out vertical lines marking some values

    }

    public void drawDataGuidingLines(Canvas canvas, float height) {
        final int lines = 10;
        int counter = 1;

        float difference = max - min;
        float niceNumber = new BigDecimal(difference / lines).round(new MathContext(1, RoundingMode.HALF_UP)).floatValue();
        float start = min != 0 ? min - (min % niceNumber) : niceNumber;
        Log.d("DataLines", "Diff: " + difference + ", Nice: " + niceNumber + ", Start: " + start);
        if (min == 0) {
            String minText = formatNumber(min);
            canvas.drawText(minText, 0, height - labelPaint.ascent(), labelPaint);
        }

        while (start <= max) {
            if (start < min || start > max) {
                start += niceNumber;
                continue;
            }
            float ypos = height + START_Y - ((start - min) / (max - min)) * (height);
            float astroke2 = axisPaint.getStrokeWidth() / 2;
            canvas.drawLine(START_X + astroke2, ypos, END_X, ypos, gridPaint);
            canvas.drawText(formatNumber(start), 0, ypos - labelPaint.ascent() / 2, labelPaint);
            start += niceNumber;
            counter++;
        }

    }

    public void setAxisColor(int axisColor) {
        axisPaint.setColor(axisColor);
        invalidate();
    }

    public void setLineColor(int lineColor) {
        linePaint.setColor(lineColor);
        invalidate();
    }

    public void setLabelColor(int labelColor) {
        labelPaint.setColor(labelColor);
        invalidate();
    }

    public void setGuidingLineColor(int guidingLineColor) {
        gridPaint.setColor(guidingLineColor);
        invalidate();
    }

    public void setColors(int axisColor, int lineColor, int labelColor, int guidingLineColor) {
        axisPaint.setColor(axisColor);
        linePaint.setColor(lineColor);
        labelPaint.setColor(labelColor);
        gridPaint.setColor(guidingLineColor);
        invalidate();
    }

    public void setGuidingLinesEnabled(boolean guidingLinesEnabled) {
        this.guidingLinesEnabled = guidingLinesEnabled;
        invalidate();
    }

    public void resetStyle() {
        setupView();
    }

    public String formatNumber(float number) {
        DecimalFormat df = new DecimalFormat(" #.####");
        BigDecimal bd = new BigDecimal(number);
        bd = bd.round(new MathContext(5));
        number = bd.floatValue();
        return df.format(number);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float dpFromPx(final Context context, final int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
