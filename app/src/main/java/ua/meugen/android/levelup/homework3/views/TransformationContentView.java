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
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import ua.meugen.android.levelup.homework3.R;

/**
 * Created by meugen on 06.12.16.
 */

public class TransformationContentView extends View {

    public static final int NONE = 0;
    public static final int INVERT_GREEN_AND_BLUE = 1;
    public static final int CUT_IMAGE = 2;
    public static final int PUT_GRADIENT = 3;

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
        this.drawHelper.sizeChanged(bitmapRect);
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
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (this.image == null) {
            this.bitmapRect.set(0, 0, 0, 0);
        } else {
            final Bitmap bitmap = ((BitmapDrawable) this.image).getBitmap();
            final float scale = Math.min((float) w / bitmap.getWidth(),
                    (float) h / bitmap.getHeight());
            final float scaledWidth = bitmap.getWidth() * scale;
            final float scaledHeight = bitmap.getHeight() * scale;

            final float verticalPadding = (h - scaledHeight) / 2;
            final float horizontalPadding = (w - scaledWidth) / 2;
            this.bitmapRect.set(horizontalPadding, verticalPadding,
                    w - horizontalPadding, h - verticalPadding);
        }
        this.drawHelper.sizeChanged(this.bitmapRect);
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

    @Override
    protected Parcelable onSaveInstanceState() {
        return new Parcelable_(super.onSaveInstanceState(), this.transformation);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final Parcelable_ parcelable = (Parcelable_) state;
        super.onRestoreInstanceState(parcelable.getBase());
        setTransformation(parcelable.getTransformation());
    }

    public static final class Parcelable_ implements Parcelable {

        public static final Creator<Parcelable_> CREATOR = new ClassLoaderCreator<Parcelable_>() {
            @Override
            public Parcelable_ createFromParcel(final Parcel parcel, final ClassLoader classLoader) {
                return new Parcelable_(parcel, classLoader);
            }

            @Override
            public Parcelable_ createFromParcel(final Parcel parcel) {
                return createFromParcel(parcel, TransformationContentView.class.getClassLoader());
            }

            @Override
            public Parcelable_[] newArray(final int size) {
                return new Parcelable_[size];
            }
        };

        private final Parcelable base;
        private final int transformation;

        public Parcelable_(final Parcel parcel, final ClassLoader classLoader) {
            this.base = parcel.readParcelable(classLoader);
            this.transformation = parcel.readInt();
        }

        public Parcelable_(final Parcelable base, final int transformation) {
            this.base = base;
            this.transformation = transformation;
        }

        public Parcelable getBase() {
            return base;
        }

        public int getTransformation() {
            return transformation;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel parcel, final int flags) {
            parcel.writeParcelable(this.base, flags);
            parcel.writeInt(this.transformation);
        }
    }
}

class DrawHelper {

    final Paint paint = new Paint();
    RectF rect;

    public void sizeChanged(final RectF rect) {
        this.rect = rect;
    }

    public void draw(final Canvas canvas, final Bitmap bitmap) {
        canvas.drawBitmap(bitmap, null, rect, paint);
    }
}

class InvertGreenAndBlueDrawHelper extends DrawHelper {

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

class CutImageDrawHelper extends DrawHelper {

    private final Matrix matrix = new Matrix();
    private final Path path = new Path();

    @Override
    public void draw(final Canvas canvas, final Bitmap bitmap) {
        this.matrix.setRotate(40f, rect.centerX(), rect.centerY());

        this.path.reset();
        this.path.moveTo(rect.centerX(), rect.top);
        this.path.rLineTo(rect.width() / 2, rect.height() * 2 / 3);
        this.path.rLineTo(-rect.width(), 0);
        this.path.lineTo(rect.centerX(), rect.top);
        this.path.transform(this.matrix);

        canvas.save();
        canvas.clipPath(this.path);
        super.draw(canvas, bitmap);
        canvas.restore();
    }
}

class PutGradientDrawHelper extends DrawHelper {

    private RadialGradient gradient;

    @Override
    public void sizeChanged(final RectF rect) {
        super.sizeChanged(rect);
        if (!rect.isEmpty()) {
            this.gradient = new RadialGradient(rect.centerX(), rect.centerY(),
                    rect.width() / 2,
                    new int[] {Color.CYAN, Color.MAGENTA, Color.YELLOW },
                    null, Shader.TileMode.REPEAT);
        }
    }

    @Override
    public void draw(final Canvas canvas, final Bitmap bitmap) {
        super.draw(canvas, bitmap);

        paint.setFakeBoldText(true);
        paint.setTextSize(300);
        paint.setShader(gradient);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Android", rect.centerX(), rect.centerY(), paint);
        paint.setShader(null);
    }
}