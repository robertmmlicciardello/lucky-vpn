package app.lovable.luckyvpnmaster.rewards;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.Random;

/**
 * A spinning prize wheel drawn on canvas: 8 alternating blue segments with
 * point values, a pointer at the top, and a decelerating spin animation.
 * The server decides the real prize; the wheel is purely visual.
 */
public class LuckyWheelView extends View {

    /** Visible prize labels (server awards a random 10–1000). */
    private static final int[] SEGMENTS = {10, 20, 30, 50, 100, 200, 500, 1000};

    private static final int[] COLORS = {
            0xFF2196F3, 0xFF0D47A1, 0xFF64B5F6, 0xFF1565C0,
            0xFF90CAF9, 0xFF1976D2, 0xFF42A5F5, 0xFF0D47A1
    };

    private float rotation = 0f;
    private ValueAnimator spinAnimator;
    private OnSpinFinishedListener spinListener;
    private final Random random = new Random();

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnSpinFinishedListener {
        void onSpinFinished();
    }

    public LuckyWheelView(Context context) {
        super(context);
        init();
    }

    public LuckyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LuckyWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        centerPaint.setColor(Color.WHITE);
        pointerPaint.setColor(0xFFFFC107); // amber pointer
        pointerPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(cx, cy) - 24f;
        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        int n = SEGMENTS.length;
        float sweep = 360f / n;

        // Segments.
        for (int i = 0; i < n; i++) {
            arcPaint.setColor(COLORS[i % COLORS.length]);
            float start = i * sweep + rotation - 90f;
            canvas.drawArc(oval, start, sweep, true, arcPaint);

            // Label: centered along the segment's radial mid-line.
            float midAngle = (start + sweep / 2f) * (float) Math.PI / 180f;
            float lx = cx + (float) Math.cos(midAngle) * radius * 0.62f;
            float ly = cy + (float) Math.sin(midAngle) * radius * 0.62f;
            canvas.save();
            canvas.rotate(start + sweep / 2f, lx, ly);
            canvas.drawText(String.valueOf(SEGMENTS[i]), lx, ly + 14f, textPaint);
            canvas.restore();
        }

        // Outer ring.
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(10f);
        arcPaint.setColor(0xFF0D47A1);
        canvas.drawCircle(cx, cy, radius + 5f, arcPaint);
        arcPaint.setStyle(Paint.Style.FILL);

        // Hub.
        canvas.drawCircle(cx, cy, radius * 0.14f, centerPaint);
        arcPaint.setColor(0xFF0D47A1);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(6f);
        canvas.drawCircle(cx, cy, radius * 0.14f, arcPaint);
        arcPaint.setStyle(Paint.Style.FILL);

        // Pointer at the top.
        Path pointer = new Path();
        pointer.moveTo(cx - 28f, 6f);
        pointer.lineTo(cx + 28f, 6f);
        pointer.lineTo(cx, 64f);
        pointer.close();
        canvas.drawPath(pointer, pointerPaint);
    }

    public boolean isSpinning() {
        return spinAnimator != null && spinAnimator.isRunning();
    }

    /**
     * Spin the wheel with a decelerating animation: 5–7 full turns ending at
     * a random angle. Calls back when the wheel stops.
     */
    public void spin(OnSpinFinishedListener listener) {
        if (isSpinning()) return;
        this.spinListener = listener;
        float start = rotation;
        float target = start + 360f * (5 + random.nextInt(3)) + random.nextFloat() * 360f;

        spinAnimator = ValueAnimator.ofFloat(start, target);
        spinAnimator.setDuration(4200);
        spinAnimator.setInterpolator(new DecelerateInterpolator(1.6f));
        spinAnimator.addUpdateListener(anim -> {
            rotation = (float) anim.getAnimatedValue();
            invalidate();
        });
        spinAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                rotation = rotation % 360f;
                if (spinListener != null) spinListener.onSpinFinished();
            }
        });
        spinAnimator.start();
    }
}
