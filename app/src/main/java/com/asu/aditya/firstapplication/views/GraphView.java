package com.asu.aditya.firstapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

/**
 * GraphView creates a scaled line or bar graph with x and y axis labels.
 * <p>
 * Using code provide by professor with some modifications.
 */
public class GraphView extends View {

    public static boolean BAR = false;
    public static boolean LINE = true;

    private Paint paint;
    private float[] values;
    private String[] horlabels;
    private String[] verlabels;
    private String title;
    private boolean type;
    private static final int MAX_Y_VALUE = 20;
    private static final int MIN_Y_VALUE = 0;


    public GraphView(Context context, String[] horlabels, String[] verlabels, boolean type) {
        super(context);

        if (horlabels == null)
            this.horlabels = new String[0];
        else
            this.horlabels = horlabels;
        if (verlabels == null)
            this.verlabels = new String[0];
        else
            this.verlabels = verlabels;
        this.type = type;
        paint = new Paint();
    }

    /*
    setValues method is used my PatientGraphActivity
    to send updated values to the graph view.
     */
    public void setValues(float[] newValues) {
        this.values = newValues;
    }

    public void setTitle(String patientName) {
        this.title = (patientName != null) ? "Showing Grapth for : " + patientName : "Welcome";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float border = 20;
        float horstart = border * 2;
        float height = getHeight();
        float width = getWidth();
        float max = getMax();
        float min = getMin();
        float diff = max - min;
        float graphHeight = height - (2 * border);
        float graphWidth = width - (2 * border);


        paint.setTextAlign(Align.LEFT);
        int vers = verlabels.length - 1;
        for (int i = 0; i < verlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float y = ((graphHeight / vers) * i) + border;
            canvas.drawLine(horstart, y, width, y, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(verlabels[i], 0, y, paint);
        }
        int hors = horlabels.length - 1;
        for (int i = 0; i < horlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float x = ((graphWidth / hors) * i) + horstart;
            canvas.drawLine(x, height - border, x, border, paint);
            paint.setTextAlign(Align.CENTER);
            if (i == horlabels.length - 1)
                paint.setTextAlign(Align.RIGHT);
            if (i == 0)
                paint.setTextAlign(Align.LEFT);
            paint.setColor(Color.WHITE);
            canvas.drawText(horlabels[i], x, height - 4, paint);
        }

        paint.setTextAlign(Align.CENTER);
        Paint titlePaint = paint;
        titlePaint.setTextSize(20);
        canvas.drawText(title, (graphWidth / 2) + horstart, border - 4, titlePaint);

        if (max != min) {
            paint.setColor(Color.LTGRAY);
            if (type == BAR) {
                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                for (int i = 0; i < values.length; i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float h = graphHeight * rat;
                    canvas.drawRect((i * colwidth) + horstart, (border - h) + graphHeight, ((i * colwidth) + horstart) + (colwidth - 1), height - (border - 1), paint);
                }
            } else {
                for (int i = 0; i < (values.length - 1); i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float current_height = graphHeight * rat;
                    val = values[i + 1] - min;
                    rat = val / diff;
                    float next_height = graphHeight * rat;
                    paint.setColor(Color.GREEN);
                    paint.setStrokeWidth(4.0f);
                    float startX = ((graphWidth / hors) * i) + horstart;
                    float stopX = ((graphWidth / hors) * (i + 1)) + horstart;
                    float startY = (border - current_height) + graphHeight;
                    float stopY = (border - next_height) + graphHeight;
                    canvas.drawLine(startX, startY, stopX, stopY, paint);
                }
            }
        }
    }

    private float getMax() {
        return MAX_Y_VALUE;
    }

    private float getMin() {
        return MIN_Y_VALUE;
    }

}