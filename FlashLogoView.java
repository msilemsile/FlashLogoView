import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import me.msile.train.videoparse.utils.DensityUtil;

public class FlashLogoView extends View implements Choreographer.FrameCallback {

    private int clipColor = 0xff000000;
    private int textColor = 0xffffffff;

    private static final String LOGO = "MCAN";

    private Paint textPaint;
    private Paint clipPaint;

    private Choreographer choreographer;
    private int clipStart;
    private int clipWidth;
    private int measureWidth;
    private int measureHeight;
    private ValueAnimator clipAnimator;
    private int textDPSize = 25;
    private Path tempPath;

    public FlashLogoView(Context context) {
        this(context, null);
    }

    public FlashLogoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlashLogoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        tempPath = new Path();

        textDPSize = DensityUtil.dip2px(textDPSize);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textDPSize);

        clipWidth = textDPSize;
        measureWidth = (int) textPaint.measureText(LOGO);
        measureHeight = textDPSize;

        clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clipPaint.setColor(clipColor);
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(this);

        startAnim();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void startAnim() {
        clipAnimator = ValueAnimator.ofInt(-clipWidth, measureWidth + clipWidth);
        clipAnimator.setDuration(1250);
        clipAnimator.setRepeatCount(1);
        clipAnimator.setInterpolator(new LinearInterpolator());
        clipAnimator.start();
    }

    public void stopAnim() {
        choreographer.removeFrameCallback(this);
        clipAnimator.end();
        clipAnimator = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //背景
        int baseY = (int) (measureHeight / 2 - (textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(LOGO, 0, baseY, textPaint);
        //裁剪
        tempPath.reset();
        tempPath.moveTo(clipStart, 0);
        tempPath.lineTo(clipStart + clipWidth, 0);
        tempPath.lineTo(clipStart, measureHeight);
        tempPath.lineTo(clipStart - clipWidth, measureHeight);
        tempPath.close();
        canvas.drawPath(tempPath, clipPaint);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (clipAnimator != null) {
            Integer value = (Integer) clipAnimator.getAnimatedValue();
            clipStart = value;
            invalidate();
            Log.i("MCanLogoView", "doFrame " + value);
        }
        choreographer.postFrameCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnim();
    }
}
