package edu.birzeit.a1220775_1221026_courseproject.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimplePieChart extends View {

    private List<Float> data = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Paint paint;
    private Paint textPaint;
    private Paint legendPaint;
    private RectF rectF;

    public SimplePieChart(Context context) {
        super(context);
        init();
    }

    public SimplePieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimplePieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setColor(Color.BLACK);
        legendPaint.setTextSize(30f);
        legendPaint.setTextAlign(Paint.Align.LEFT);

        rectF = new RectF();
    }

    public void setData(List<Float> values, List<Integer> colors, List<String> labels) {
        this.data = values;
        this.colors = colors;
        this.labels = labels;
        invalidate(); // Redraw
    }

    public void clear() {
        this.data.clear();
        this.colors.clear();
        this.labels.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) {
            return;
        }

        float total = 0;
        for (float f : data)
            total += f;

        if (total == 0)
            return;

        float width = getWidth();
        float height = getHeight();

        // Reserve space for bottom legend
        float legendHeight = 0;
        if (labels != null && !labels.isEmpty()) {
            legendHeight = (labels.size() / 2 + 1) * 50f; // Approx height for 2 columns
            if (legendHeight > height * 0.3f)
                legendHeight = height * 0.3f; // Cap legend height
        }

        float chartHeight = height - legendHeight - 20; // 20 padding
        float minDim = Math.min(width, chartHeight);
        float radius = minDim / 2f * 0.8f; // 80% of half width

        float cx = width / 2f;
        float cy = chartHeight / 2f;

        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);

        float currentAngle = 0;
        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            float sweepAngle = (value / total) * 360f;

            // Draw Slice
            paint.setColor(colors.get(i % colors.size()));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(rectF, currentAngle, sweepAngle, true, paint);

            // Draw Percentage inside slice if large enough
            if (sweepAngle > 15) {
                float angleRad = (float) Math.toRadians(currentAngle + sweepAngle / 2);
                float labelRadius = radius * 0.6f;
                float labelX = cx + labelRadius * (float) Math.cos(angleRad);
                float labelY = cy + labelRadius * (float) Math.sin(angleRad) + 10; // +10 for vertical center approx

                int percentage = (int) ((value / total) * 100);
                canvas.drawText(percentage + "%", labelX, labelY, textPaint);
            }

            currentAngle += sweepAngle;
        }

        // Draw "Hole" for Donut effect
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius * 0.4f, paint);

        // Draw Legend
        drawLegend(canvas, width, height, legendHeight);
    }

    private void drawLegend(Canvas canvas, float width, float height, float legendHeight) {
        if (labels == null || labels.isEmpty())
            return;

        float startX = 50f;
        float startY = height - legendHeight + 20;
        float rowHeight = 40f;
        float colWidth = width / 2f - 20;

        for (int i = 0; i < labels.size(); i++) {
            int col = i % 2;
            int row = i / 2;

            float x = startX + (col * colWidth);
            float y = startY + (row * rowHeight);

            // Draw Color Box
            paint.setColor(colors.get(i % colors.size()));
            canvas.drawRect(x, y - 20, x + 20, y, paint);

            // Draw Label
            // Truncate if too long
            String originalLabel = labels.get(i);
            String label = originalLabel.length() > 15 ? originalLabel.substring(0, 15) + "..." : originalLabel;

            canvas.drawText(label, x + 30, y, legendPaint);
        }
    }
}
