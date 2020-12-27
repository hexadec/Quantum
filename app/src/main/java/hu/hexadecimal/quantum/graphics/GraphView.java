package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A view used to display the probabilities/results as a graph
 */
public class GraphView extends View {

    protected float min, max, size;
    protected float[] positions, values, complexArguments;
    protected Paint axisPaint, gridPaint, linePaint, labelPaint, rectPaint, naPaint;
    protected float START_X, START_Y, END_X, END_Y;

    private final int OVERSAMPLING_MULTIPLIER = 3;
    private RenderScript rs;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public GraphView(Context context) {
        super(context);
        setupView();
    }

    public void setData(@NonNull float[] data, int size, @Nullable float[] arguments) {
        if (data.length < size || (arguments != null && arguments.length < size)) {
            throw new RuntimeException("Data size error: " + data.length + " size: " + size);
        }
        this.complexArguments = arguments;
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
        axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setAntiAlias(true);
        axisPaint.setStrokeWidth(pxFromDp(getContext(), 2.5f) * OVERSAMPLING_MULTIPLIER);

        gridPaint = new Paint();
        gridPaint.setColor(0xffbbbbbb);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(pxFromDp(getContext(), 0.7f) * OVERSAMPLING_MULTIPLIER);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.RED);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(pxFromDp(getContext(), 1f) * OVERSAMPLING_MULTIPLIER);

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(Color.BLUE);
        rectPaint.setDither(true);
        rectPaint.setAntiAlias(true);

        labelPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTypeface(Typeface.MONOSPACE);
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextSize(pxFromDp(getContext(), 10) * OVERSAMPLING_MULTIPLIER);

        naPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        naPaint.setColor(Color.DKGRAY);
        naPaint.setTextSize(pxFromDp(getContext(), 32));

        rs = RenderScript.create(getContext());
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setupPadding();
    }

    protected void setupPadding() {
        float niceNumber = new BigDecimal((max - min) / 10).round(new MathContext(1, RoundingMode.HALF_UP)).floatValue();
        String text = String.format("%" + Math.round(Math.log(size == 0 ? 8 : size) / Math.log(2)) + "s", Integer.toBinaryString(5)).replace(' ', '0');
        START_X = labelPaint.measureText(formatNumber(niceNumber)) + pxFromDp(getContext(), 4) * OVERSAMPLING_MULTIPLIER;
        START_Y = pxFromDp(getContext(), 10) * OVERSAMPLING_MULTIPLIER;
        END_X = getWidth() * OVERSAMPLING_MULTIPLIER - pxFromDp(getContext(), 10) * OVERSAMPLING_MULTIPLIER;
        END_Y = getHeight() * OVERSAMPLING_MULTIPLIER - labelPaint.measureText(text) - pxFromDp(getContext(), 5) * OVERSAMPLING_MULTIPLIER;
    }

    @Override
    protected void onDraw(Canvas c) {
        Log.i("DisplayView", "onDraw was called");
        super.onDraw(c);
        Bitmap bitmap = Bitmap.createBitmap(c.getWidth() * 3, c.getHeight() * 3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        setupPadding();

        if (values == null || values.length == 0 || size == 0) {
            c.drawText("N/A", (getWidth() - naPaint.measureText("N/A")) / 2, (getHeight() - naPaint.ascent()) / 2, naPaint);
            return;
        }

        drawAxes(canvas);

        float astroke2 = axisPaint.getStrokeWidth() / 2f;
        float width = END_X - START_X - astroke2;
        float height = END_Y - START_Y - astroke2;

        drawStateGuidingLines(canvas, width);
        drawDataGuidingLines(canvas, height);

        //Step size
        final int step = 1;
        if (size >= 512 /*2^9*/) {
            for (int i = 0; i < size - step; i += step) {
                final float current = values[i];
                final float next = values[i + step];
                positions[4 * i + 1] = height + START_Y - ((current - min) / (max - min)) * (height * 1f);
                positions[4 * i] = size - 1 == 0 ? 0 : (START_X + astroke2 + (i * 1f) / (size - 1) * (width * 1f));
                positions[4 * i + 3] = height + START_Y - ((next - min) / (max - min)) * (height * 1f);
                positions[4 * i + 2] = (START_X + astroke2 + (i + step * 1f) / (size - 1) * (width * 1f));
            }
            canvas.drawLines(positions, linePaint);
        } else {
            float avWidth = width / size;
            for (int j = 0; j < size; j++) {
                String text = String.format("%" + Math.round(Math.log(size) / Math.log(2)) + "s", Integer.toBinaryString(j)).replace(' ', '0');
                final float current = values[j];
                float t = height + START_Y - ((current - min) / (max - min)) * (height * 1f);
                float w = avWidth / 1.2f;
                float l = START_X + astroke2 + avWidth * j + (avWidth - w) / 2;
                float r = l + w;
                if (complexArguments != null) {
                    colorRectPaint(complexArguments[j]);
                }
                canvas.drawRect(l, t, r, height + START_Y, rectPaint);
                if (size < 32) {
                    canvas.save();
                    canvas.rotate(-90, (l + r) / 2, canvas.getHeight());
                    canvas.drawText(text, (l + r) / 2, canvas.getHeight() + pxFromDp(getContext(), 2) * OVERSAMPLING_MULTIPLIER, labelPaint);
                    canvas.restore();
                }
            }
        }
        blurBitmap(bitmap, 1.2f);
        c.drawBitmap(bitmap, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()), rectPaint);
    }

    private void colorRectPaint(float argument) {
        int baseColor = 0xff220022;
        float blue = (float) ((argument + Math.PI) / 2f / Math.PI) * 216;
        float red = 221 - (float) ((argument + Math.PI) / 2f / Math.PI) * 216;
        rectPaint.setColor((int)(baseColor + blue + (int) red * 0x10000));
        //Log.v("GraphView", "Paint color: " + Integer.toHexString(rectPaint.getColor()));
    }

    private void blurBitmap(Bitmap bitmap, float radius) {
        final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
    }

    public void drawAxes(Canvas canvas) {
        canvas.drawLine(START_X, END_Y, END_X, END_Y, axisPaint);
        canvas.drawLine(START_X, START_Y, START_X, END_Y, axisPaint);
    }

    public void drawStateGuidingLines(Canvas canvas, float width) {
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

    public void resetStyle() {
        setupView();
    }

    public String formatNumber(float number) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(" #.####");
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
