package ua.meugen.android.levelup.homework3.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import ua.meugen.android.levelup.homework3.R;

/**
 * Created by meugen on 06.12.16.
 */

public class TransformationContentView extends View {

    private static final String TAG = TransformationContentView.class.getSimpleName();

    public static final int NONE = 0;
    public static final int INVERT_GREEN_AND_BLUE = 1;
    public static final int CUT_IMAGE = 2;
    public static final int PUT_GRADIENT = 3;

    private final Paint paint = new Paint();
    private final RectF bitmapRect = new RectF();

    private Drawable image;
    private int transformation = NONE;
    private DrawSetupHelper drawSetupHelper;

    public TransformationContentView(final Context context) {
        super(context);
    }

    public TransformationContentView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformationContentView(final Context context, final AttributeSet attrs,
                                     final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.TransformationContentView,
                defStyleAttr, 0);
        this.image = array.getDrawable(R.styleable.TransformationContentView_image);
        this.transformation = array.getInt(R.styleable.TransformationContentView_transformation, NONE);
        array.recycle();

        this.initPaintSetupHelper();
    }

    private void initPaintSetupHelper() {
        if (this.transformation == INVERT_GREEN_AND_BLUE) {
            this.drawSetupHelper = new InvertGreenAndBlueDrawSetupHelper();
        } else if (this.transformation == CUT_IMAGE) {
            this.drawSetupHelper = new CutImageDrawSetupHelper();
        } else {
            this.drawSetupHelper = new NoneDrawSetupHelper();
        }
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(final Drawable image) {
        this.image = image;
        invalidate();
    }

    public int getTransformation() {
        return transformation;
    }

    public void setTransformation(final int transformation) {
        this.transformation = transformation;
        this.initPaintSetupHelper();
        invalidate();
    }

    @Override
    protected void onLayout(final boolean changed, final int left,
                            final int top, final int right,
                            final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (this.image == null) {
            this.bitmapRect.set(0, 0, 0, 0);
        } else {
            final int width = right - left;
            final int height = bottom - top;

            final Bitmap bitmap = ((BitmapDrawable) this.image).getBitmap();
            final float scale = Math.min((float) width / bitmap.getWidth(),
                    (float) height / bitmap.getHeight());
            final float scaledWidth = bitmap.getWidth() * scale;
            final float scaledHeight = bitmap.getHeight() * scale;

            final float verticalPadding = (height - scaledHeight) / 2;
            final float horizontalPadding = (width - scaledWidth) / 2;
            this.bitmapRect.set(horizontalPadding, verticalPadding,
                    width - horizontalPadding, height - verticalPadding);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (this.image == null) {
            return;
        }
        this.drawSetupHelper.setupPaint(this.paint);

        canvas.save();
        this.drawSetupHelper.setupCanvas(canvas, this.bitmapRect);

        final Bitmap bitmap = ((BitmapDrawable) this.image).getBitmap();
        canvas.drawBitmap(bitmap, null, this.bitmapRect, this.paint);
        canvas.restore();
    }
}

interface DrawSetupHelper {

    void setupPaint(Paint paint);

    void setupCanvas(Canvas canvas, RectF rect);
}

class NoneDrawSetupHelper implements DrawSetupHelper {

    @Override
    public void setupPaint(final Paint paint) {
        paint.setColorFilter(null);
    }

    @Override
    public void setupCanvas(final Canvas canvas, final RectF rect) {}
}

class InvertGreenAndBlueDrawSetupHelper implements DrawSetupHelper {

    private static final float[] COLOR_MATRIX_ARRAY = new float[] {
            1,  0,  0, 0,   0, // the same red channel
            0, -1,  0, 0, 255, // invert green channel
            0,  0, -1, 0, 255, // invert blue channel
            0,  0,  0, 1,   0  // the same alpha channel
    };

    private final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(COLOR_MATRIX_ARRAY);

    @Override
    public void setupPaint(final Paint paint) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public void setupCanvas(final Canvas canvas, final RectF rect) {}
}

class CutImageDrawSetupHelper implements DrawSetupHelper {

    private final Matrix matrix = new Matrix();
    private final Path path = new Path();
    //private final RectF pathRect = new RectF();

    @Override
    public void setupPaint(final Paint paint) {
        paint.setColorFilter(null);
    }

    @Override
    public void setupCanvas(final Canvas canvas, final RectF rect) {
        this.matrix.setRotate(40f, rect.centerX(), rect.centerY());

        this.path.reset();
        this.path.moveTo(rect.centerX(), rect.top);
        this.path.rLineTo(rect.width() / 2, rect.height() * 2 / 3);
        this.path.rLineTo(-rect.width(), 0);
        this.path.lineTo(rect.centerX(), rect.top);
        this.path.transform(this.matrix);

        canvas.clipPath(this.path);
    }
}