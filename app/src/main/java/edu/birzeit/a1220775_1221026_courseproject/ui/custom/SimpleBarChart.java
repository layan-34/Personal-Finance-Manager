package edu.birzeit.a1220775_1221026_courseproject.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimpleBarChart extends View {

    private List<Pair<String, Double>> data = new ArrayList<>();
    private Paint barPaint;
    private Paint textPaint;
    private Paint axisPaint;
    private Paint gridPaint;

    public SimpleBarChart(Context context) {
        super(context);
        init();
    }

    public SimpleBarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleBarChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(Color.parseColor("#42A5F5")); // Softer Blue

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStrokeWidth(3f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 0));
    }

    public void setData(List<Pair<String, Double>> data) {
        this.data = data;
        invalidate();
    }

    public void clear() {
        this.data.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty())
            return;

        float width = getWidth();
        float height = getHeight();
        float paddingLeft = 100f; // More space for Y-axis labels
        float paddingRight = 40f;
        float paddingBottom = 60f;
        float paddingTop = 40f;

        float chartHeight = height - paddingBottom - paddingTop;
        float chartWidth = width - paddingLeft - paddingRight;

        // Find max value
        double max = 0;
        for (Pair<String, Double> item : data) {
            if (item.second > max)
                max = item.second;
        }
        if (max == 0)
            max = 1;

        // Round up max for nicer grid
        long maxCeil = (long) Math.ceil(max);
        if (maxCeil % 10 != 0)
            maxCeil = ((maxCeil / 10) + 1) * 10;
        double displayMax = maxCeil;

        // Draw Grid and Y-Axis Labels (0, 50%, 100%)
        textPaint.setTextAlign(Paint.Align.RIGHT);

        // Top line (Max)
        float yMax = paddingTop;
        canvas.drawLine(paddingLeft, yMax, width - paddingRight, yMax, gridPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.0f", displayMax), paddingLeft - 10, yMax + 10, textPaint);

        // Middle line (Half)
        float yMid = paddingTop + chartHeight / 2;
        canvas.drawLine(paddingLeft, yMid, width - paddingRight, yMid, gridPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.0f", displayMax / 2), paddingLeft - 10, yMid + 10,
                textPaint);

        // Bottom line (0)
        float yZero = height - paddingBottom;
        canvas.drawLine(paddingLeft, yZero, width - paddingRight, yZero, axisPaint); // Solid axis
        canvas.drawText("0", paddingLeft - 10, yZero + 10, textPaint);

        // Draw Bars
        float barWidth = (chartWidth / data.size()) * 0.5f; // 50% width
        float spacing = chartWidth / data.size();

        textPaint.setTextAlign(Paint.Align.CENTER); // Reset for X-axis labels

        for (int i = 0; i < data.size(); i++) {
            Pair<String, Double> item = data.get(i);
            float xCenter = paddingLeft + (i * spacing) + (spacing / 2);

            float barHeight = (float) ((item.second / displayMax) * chartHeight);
            float top = yZero - barHeight;
            float bottom = yZero;

            // Draw Rounded Bar
            RectF rect = new RectF(xCenter - barWidth / 2, top, xCenter + barWidth / 2, bottom);
            canvas.drawRoundRect(rect, 10, 10, barPaint);

            // X-Axis Label
            String label = item.first;
            if (data.size() > 8 && label.length() > 3)
                label = label.substring(0, 3);
            canvas.drawText(label, xCenter, height - 15, textPaint);

            // Draw Value on top of bar if space exists
            if (barHeight > 40) {
                Paint valuePaint = new Paint(textPaint);
                valuePaint.setColor(Color.WHITE);
                valuePaint.setTextSize(24f);
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", item.second), xCenter, top + 30, valuePaint);
            }
        }
    }
}
