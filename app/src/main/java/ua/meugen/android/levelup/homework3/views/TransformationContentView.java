package ua.meugen.android.levelup.homework3.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
    private DrawHelper drawHelper;

    public TransformationContentView(final Context context) {
        super(context);
        initPaintSetupHelper();
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
            this.drawHelper = new InvertGreenAndBlueDrawHelper();
        } else if (this.transformation == CUT_IMAGE) {
            this.drawHelper = new CutImageDrawHelper();
        } else if (this.transformation == PUT_GRADIENT) {
            this.drawHelper = new PutGradientDrawHelper();
        } else {
            this.drawHelper = new DrawHelper();
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
        final Bitmap bitmap = ((BitmapDrawable) this.image).getBitmap();
        this.drawHelper.draw(canvas, bitmap);
    }

    private class DrawHelper {

        public void draw(final Canvas canvas, final Bitmap bitmap) {
            canvas.drawBitmap(bitmap, null, bitmapRect, paint);
        }
    }

    private class InvertGreenAndBlueDrawHelper extends DrawHelper {

        private final float[] colorMatrixArray = new float[] {
                1,  0,  0, 0,   0, // the same red channel
                0, -1,  0, 0, 255, // invert green channel
                0,  0, -1, 0, 255, // invert blue channel
                0,  0,  0, 1,   0  // the same alpha channel
        };
        private final ColorMatrixColorFilter colorFilter
                = new ColorMatrixColorFilter(colorMatrixArray);

        @Override
        public void draw(final Canvas canvas, final Bitmap bitmap) {
            paint.setColorFilter(colorFilter);
            super.draw(canvas, bitmap);
            paint.setColorFilter(null);
        }
    }

    private class CutImageDrawHelper extends DrawHelper {

        private final Matrix matrix = new Matrix();
        private final Path path = new Path();

        @Override
        public void draw(final Canvas canvas, final Bitmap bitmap) {
            this.matrix.setRotate(40f, bitmapRect.centerX(), bitmapRect.centerY());

            this.path.reset();
            this.path.moveTo(bitmapRect.centerX(), bitmapRect.top);
            this.path.rLineTo(bitmapRect.width() / 2, bitmapRect.height() * 2 / 3);
            this.path.rLineTo(-bitmapRect.width(), 0);
            this.path.lineTo(bitmapRect.centerX(), bitmapRect.top);
            this.path.transform(this.matrix);

            canvas.save();
            canvas.clipPath(this.path);
            super.draw(canvas, bitmap);
            canvas.restore();
        }
    }

    private class PutGradientDrawHelper extends DrawHelper {

        private final GradientDrawable gradient;
        private final PorterDuffXfermode xfermode
                = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
        private final Rect gradientRect = new Rect();

        public PutGradientDrawHelper() {
            this.gradient = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
                    new int[] {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.TRANSPARENT });
            this.gradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        }

        @Override
        public void draw(final Canvas canvas, final Bitmap bitmap) {
            this.gradient.setGradientRadius(Math.max(bitmapRect.width(), bitmapRect.height()) / 2);
            this.gradientRect.set(Math.round(bitmapRect.left), Math.round(bitmapRect.top),
                    Math.round(bitmapRect.right), Math.round(bitmapRect.bottom));
            this.gradient.setBounds(this.gradientRect);

            paint.setXfermode(this.xfermode);
            super.draw(canvas, bitmap);
            this.gradient.draw(canvas);
            paint.setXfermode(null);
        }
    }
}