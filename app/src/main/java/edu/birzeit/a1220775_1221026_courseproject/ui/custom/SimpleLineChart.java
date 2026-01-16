package edu.birzeit.a1220775_1221026_courseproject.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimpleLineChart extends View {

    private List<Pair<String, Double>> data = new ArrayList<>();
    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Path path;

    public SimpleLineChart(Context context) {
        super(context);
        init();
    }

    public SimpleLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleLineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#FF7043")); // Orange Red
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 0));

        path = new Path();
    }

    public void setData(List<Pair<String, Double>> data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.size() < 2)
            return;

        float width = getWidth();
        float height = getHeight();
        float paddingLeft = 100f;
        float paddingRight = 40f;
        float paddingBottom = 60f;
        float paddingTop = 40f;

        float chartHeight = height - paddingBottom - paddingTop;
        float chartWidth = width - paddingLeft - paddingRight;

        // Find Max
        double max = 0;
        for (Pair<String, Double> item : data) {
            if (item.second > max)
                max = item.second;
        }
        if (max == 0)
            max = 1;

        long maxCeil = (long) Math.ceil(max);
        if (maxCeil % 10 != 0)
            maxCeil = ((maxCeil / 10) + 1) * 10;
        double displayMax = maxCeil;

        // Draw Grids (Same as BarChart)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float yMax = paddingTop;
        canvas.drawLine(paddingLeft, yMax, width - paddingRight, yMax, gridPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.0f", displayMax), paddingLeft - 10, yMax + 10, textPaint);

        float yZero = height - paddingBottom;
        canvas.drawLine(paddingLeft, yZero, width - paddingRight, yZero, gridPaint); // Bottom Axis
        canvas.drawText("0", paddingLeft - 10, yZero + 10, textPaint);

        float spacing = chartWidth / (data.size() - 1); // Spacing between points

        path.reset();
        List<Float> pointsX = new ArrayList<>();
        List<Float> pointsY = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            float x = paddingLeft + (i * spacing);
            float y = (float) (yZero - ((data.get(i).second / displayMax) * chartHeight));

            pointsX.add(x);
            pointsY.add(y);

            if (i == 0)
                path.moveTo(x, y);
            else
                path.lineTo(x, y);

            // Draw Label
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(data.get(i).first, x, height - 15, textPaint);
        }

        // Draw Filled Area
        Path fillPath = new Path(path);
        fillPath.lineTo(width - paddingRight, yZero);
        fillPath.lineTo(paddingLeft, yZero);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(0, paddingTop, 0, yZero,
                Color.parseColor("#55FF7043"), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);

        // Draw Line
        canvas.drawPath(path, linePaint);

        // Draw Dots
        for (int i = 0; i < pointsX.size(); i++) {
            // Outline
            Paint ringPaint = new Paint(linePaint);
            ringPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(pointsX.get(i), pointsY.get(i), 8f, ringPaint);
            // Center
            canvas.drawCircle(pointsX.get(i), pointsY.get(i), 4f, dotPaint);
        }
    }
}
