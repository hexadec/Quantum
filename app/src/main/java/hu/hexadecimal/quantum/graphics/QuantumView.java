package hu.hexadecimal.quantum.graphics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.DisplayMetrics;
import android.view.View;

import hu.hexadecimal.quantum.LinearOperator;

public class QuantumView extends View {

    Paint mPaint, otherPaint, outerPaint, mTextPaint, whiteTextPaint;
    RectF mRectF;
    int mPadding;

    float arcLeft;

    Path mPath;

    private Canvas canvas;




    public QuantumView(Context context) {
        super(context);

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

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        //canvas.drawRoundRect(mRectF, 10, 10, otherPaint);
        //canvas.clipRect(mRectF, Region.Op.DIFFERENCE);
        //canvas.drawPaint(outerPaint);
        mPaint.setColor(0xff888888);

        otherPaint.setStyle(Paint.Style.FILL);
        otherPaint.setColor(0xff85FF00);
        for (int i = 100; i < getHeight() - 2*mPadding - 100 && i <= 1100; i += 200) {
            canvas.drawLine(mPadding, mPadding + i, getWidth() - mPadding, mPadding + i, mPaint);
            canvas.drawRect(mPadding, mPadding + i - 50, mPadding + 100, mPadding + i + 50, otherPaint);
            canvas.drawText("" + (i + 100) / 200, mPadding - 50, mPadding + i + 20, mTextPaint);
            canvas.drawText("\uD835\uDFD8", mPadding + 30, mPadding + i + 22, whiteTextPaint);
        }
        //canvas.drawRect(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding, mPaint);
        //canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 75, 45, true, mPaint);





        //canvas.drawPath(mPath, mPaint);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2, arcLeft, otherPaint);

        //canvas.drawText("Canvas basics", (float) (getWidth() * 0.3), (float) (getHeight() * 0.8), mTextPaint);

    }

    public void addGate(int qbit, LinearOperator l) {

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }



}