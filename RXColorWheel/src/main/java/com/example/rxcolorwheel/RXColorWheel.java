package com.example.rxcolorwheel;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class RXColorWheel extends View {

    /** Interface definition for a callback which to be invoked when the color state changes. */
    public interface ColorChagneListener{
        void onColorChanged(int color);
    }

    /** Interface definition for a callback which to be invoked when the button are pressed. */
    public interface ButtonTouchListener{
        void on_cPointerTouch();
        void on_excPointerTouch();
    }

    static ColorChagneListener colorChagneListener;
    static ButtonTouchListener buttonTouchListener;

    @NonNull private final LinkedHashSet<ColorChagneListener> colorChagneListeners = new LinkedHashSet<>();
    @NonNull private final LinkedHashSet<ButtonTouchListener> buttonTouchListeners = new LinkedHashSet<>();

    private final Paint     p_color = new Paint(Paint.ANTI_ALIAS_FLAG); //Color ring
    private final Paint     p_pointer = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer
    private final Paint     p_pStroke = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer outline
    private final Paint     p_background = new Paint();//Background
    private final Paint     p_pLine = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer line
    private final Paint     p_cPointer = new Paint(); //Color pointer
    private final Paint     p_excPointer = new Paint(); //External color pointer
    private final Paint     p_placemarks = new Paint(); //Placemarks

    private int             color_palette[];

    private int             color; //The currently selected color

    private double          py, px; //Point coordinates

    private float           angle; //The angle of the touch point relative to the center
    private float           cx, cy; //View center coordinates
    private float           color_rad; //Color ring radius
    private float           color_rWidth; //Color ring thickness
    private float           placemarks_rad; //Placemarks radius
    private float           cPointer_rad; //Color pointer radius
    private float           excPointer_rad; //External color pointer radius
    private float           pointer_rad; //Pointer radius
    private float           background_rad; //Background radius

    private int             minVsize; //Minimal view size (by width or height)

    private int             pCount; //Number of decorative placemarks

    /** Boolean preference variables. */
    private boolean     isBackground;
    private boolean     isExColorPointer;
    private boolean     isColorPointerCutomColor;
    private boolean     isPointerLine;
    private boolean     isPlacemarks;
    private boolean     isColorPointer;
    private boolean     isBadge;
    private boolean     isPointerOutline;
    private boolean     isColorPointerShadow;
    private boolean     isPointerCutomColor;
    private boolean     isPointerShadow;
    private boolean     isShadow;

    private Bitmap      mainImageBitmap;

    public RXColorWheel(Activity context) { super(context); }

    public RXColorWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    TypedArray typedArray;

    public RXColorWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.setDrawingCacheEnabled(true);

        setColorPalette(new int[] {Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED});

        typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PreferencesColorRing);

        int badge = typedArray.getResourceId(R.styleable.PreferencesColorRing_badge,-1);

        isBackground = typedArray.getBoolean(R.styleable.PreferencesColorRing_isBackground,true);
        isExColorPointer = typedArray.getBoolean(R.styleable.PreferencesColorRing_isExColorPointer,true);
        String cp_color = typedArray.getString(R.styleable.PreferencesColorRing_colorPointerCustomColor);
        if(cp_color != null) setColorPointerCustomColor(cp_color);
        isPointerLine = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerLine,true);
        isPlacemarks = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPlacemarks,true);
        isColorPointer = typedArray.getBoolean(R.styleable.PreferencesColorRing_isColorPointer,true);
        isColorPointerShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isColorPointerShadow, true);
        isBadge = typedArray.getBoolean(R.styleable.PreferencesColorRing_isBadge, true);
        isPointerOutline = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerOutline, true);
        String p_color = typedArray.getString(R.styleable.PreferencesColorRing_pointerCustomColor);
        if(p_color != null) setPointerCustomColor(p_color);
        isPointerShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerShadow, false);
        pCount = typedArray.getInt(R.styleable.PreferencesColorRing_placemarksCount,20);
        String bg_color = typedArray.getString(R.styleable.PreferencesColorRing_bgColor);
        if(bg_color != null) p_background.setColor(Color.parseColor(bg_color));
        isShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isShadow, true);

        if(badge == -1) {
            mainImageBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_baseline_add_24);
        }else{
            mainImageBitmap = getBitmapFromVectorDrawable(getContext(), badge);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth = measure(widthMeasureSpec);
        int mHeight = measure(heightMeasureSpec);

        //int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        //int mHeight = MeasureSpec.getSize(heightMeasureSpec);

        minVsize = Math.min(mWidth, mHeight);
        setMeasuredDimension(mWidth, mHeight);

        cx = mWidth * 0.5f;
        cy = mHeight * 0.5f;

        calculateSizes();

        Shader s_color = new SweepGradient(cx, cy, color_palette, null); //Color ring shader

        p_color.setStyle(Paint.Style.STROKE); //Color ring
        p_color.setStrokeWidth(color_rWidth);
        p_color.setShader(s_color);

        p_pointer.setStyle(Paint.Style.FILL); //Pointer
        if(isPointerShadow) {
            p_pointer.setShadowLayer(15.0f, 0.0f, 0.0f, Color.argb(110, 0, 0, 0));
        }

        p_pStroke.setStyle(Paint.Style.STROKE); //Pointer outline
        p_pStroke.setColor(Color.WHITE);
        p_pStroke.setStrokeWidth(pointer_rad * 0.08f);
        //p_pointer.setShader(s_color);

        p_background.setARGB(255, 44,42,49); //Background
        if(isShadow) {
            p_background.setShadowLayer(50.0f, 0.0f, 0.0f, 0xFF000000);
        }

        p_pLine.setStyle(Paint.Style.STROKE); //Pointer line
        p_pLine.setColor(Color.WHITE);

        p_cPointer.setStyle(Paint.Style.FILL); //Color pointer
        if(isColorPointerShadow) {
            p_cPointer.setShadowLayer(90.0f, 0.0f, 0.0f, Color.argb(130, 0, 0, 0));
        }

        p_excPointer.setStyle(Paint.Style.FILL); //External color pointer

        p_placemarks.setStyle(Paint.Style.STROKE); //Placemarks
        p_placemarks.setARGB(255, 124,122,129);

        if(mainImageBitmap != null) {
            mainImageBitmap = Bitmap.createScaledBitmap(mainImageBitmap, (int) cPointer_rad,
                    (int) cPointer_rad, false); //Set BitMap Size
        }

    }

    private int measure(int measureSpec) {
        int result;
        int specMoge = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMoge == MeasureSpec.UNSPECIFIED) result = 200;
        else result = specSize;
        return result;
    }

    private void calculateSizes() { //Высчитывает размеры элекментов внутри View

        //  cx = minVsize * 0.5f; //Умножаем на 0.5, просто потому что я так захотел,
        // не буду спорить что быстрее между делением на 2 и умножением на 0.5
        // cy = cx;

        float color_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorRingRad, 0.40f);
        float color_rWidth_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorRingThickness, 0.04f);
        float pointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_pointerRad, 0.12f);
        float cPointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorPointerRad, 0.17f);
        float excPointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_excPointerRad, 0.6f);
        float placemarks_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_placemarksRad, 0.96f);
        float background_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_backgroundRad, 1);

        color_rad = minVsize * color_rad_coef;
        color_rWidth = color_rad * color_rWidth_coef;
        pointer_rad = color_rad * pointer_rad_coef;
        cPointer_rad = color_rad * cPointer_rad_coef;
        excPointer_rad = color_rad * excPointer_rad_coef;
        placemarks_rad = color_rad * placemarks_rad_coef - color_rWidth * 0.5f;
        background_rad = color_rad * background_rad_coef;
        px = cx + color_rad;
        py = cy;

    }

    private void drawRadialLines(Canvas c, float r, float r2, int line_count){ //Рисует линии по кругу (по двум диаметрам)

        double r1x; //Коорды внутреннего диаметра
        double r1y;
        double r2x; //Коорды внешнего диаметра
        double r2y;


        float angle = 0;
        float degree = (float) Math.toRadians(360 / line_count);

        for(int i = 0; i < line_count; i++) {

            angle = angle + degree;

            //r1x = r1 * Math.cos(angle) + cx;
            //r1y = r1 * Math.sin(angle) + cy;

            r1x = (r * Math.cos(angle) - 0 * Math.sin(angle)) + cx;
            r1y = (r * Math.sin(angle) + 0 * Math.cos(angle)) + cy;


            r2x = r1x + Math.cos(angle) * r2;
            r2y = r1y + Math.sin(angle) * r2;

            c.drawLine((float) r1x, (float) r1y, (float) r2x, (float) r2y, p_placemarks);
        }

    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) { //Get Bitmap from vector drawable
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    boolean firstDraw = true;

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        int pixel = getDrawingCache().getPixel((int) px,(int) py);

        color = pixel;

        if(!isColorPointerCutomColor) {p_cPointer.setColor(pixel);}
        if(!isPointerCutomColor) {p_pointer.setColor(pixel);}

        float[] hsv = new float[] {0, 1f, 1f};

        Color.colorToHSV(pixel, hsv);

        hsv[2] = hsv[2] * 0.90f;

        p_excPointer.setColor(Color.HSVToColor(hsv));

        if(isBackground) {c.drawCircle(cx, cy, background_rad, p_background);} //Background
        c.drawCircle(cx, cy, color_rad, p_color); //Color ring
        if(isExColorPointer) {c.drawCircle(cx, cy, excPointer_rad, p_excPointer);} //External color pointer

        if(!firstDraw) {

        if(isPointerLine) {c.drawLine(cx,cy,(float) px,(float) py, p_pLine);} //Pointer line
        if(isColorPointer) { //Color pointer
            c.drawCircle(cx, cy, cPointer_rad, p_cPointer);
            if(isBadge){
                c.drawBitmap(
                        mainImageBitmap, // Bitmap
                        cx - mainImageBitmap.getWidth() * 0.5f,
                        cy - mainImageBitmap.getHeight() *0.5f,
                        p_cPointer // Paint
                );
            }
        }
        if(isPlacemarks){
            drawRadialLines(c, placemarks_rad - 20, 20, pCount); //Placemarks
            c.drawCircle(cx, cy, placemarks_rad, p_placemarks);
        }

            c.drawCircle((float) px, (float) py, pointer_rad, p_pointer); //Pointer
            if (isPointerOutline) {
                c.drawCircle((float) px, (float) py, pointer_rad, p_pStroke);
            } //Pointer outline

        }
        else{ firstDraw = false; }

    }

    boolean move_pointer = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX() - cx;
        float y = event.getY() - cy;

        angle = (float) Math.atan2(y,x); //Находим угол относительно центра и точки касания (костыль с вычитанием числа, нужно исправить)
        double d = Math.sqrt(x*x + y*y); //Вычисление расстояния от точки касания до центра

            switch (event.getAction()) {

                case MotionEvent.ACTION_UP:

                    move_pointer = false;

                    if(d < excPointer_rad && d > cPointer_rad && isExColorPointer){
                        if(buttonTouchListener != null) buttonTouchListener.on_excPointerTouch();

                        for (ButtonTouchListener listener : buttonTouchListeners) {
                            listener.on_excPointerTouch();
                        }

                    }
                    else if(d < excPointer_rad && !isColorPointer && isExColorPointer){
                        if(buttonTouchListener != null) buttonTouchListener.on_excPointerTouch();

                        for (ButtonTouchListener listener : buttonTouchListeners) {
                            listener.on_excPointerTouch();
                        }


                    }
                    else if(d < cPointer_rad && isColorPointer){
                        if(buttonTouchListener != null) buttonTouchListener.on_cPointerTouch();

                        for (ButtonTouchListener listener : buttonTouchListeners) {
                            listener.on_cPointerTouch();
                        }

                    }

                    break;

                    case MotionEvent.ACTION_DOWN:

                        float t = color_rWidth * 0.5f + 48;

                      if(d < color_rad + t  && d > color_rad - t) {

                          move_pointer = true;

                          float s = cx - color_rad;

                          px = (cx - s) * Math.cos(angle) + cx;
                          py = (cx - s) * Math.sin(angle) + cy;

                      }

                    break;

                case MotionEvent.ACTION_MOVE:

                            if (move_pointer) {

                                float s = cx - color_rad;

                                px = (cx - s) * Math.cos(angle) + cx;
                                py = (cx - s) * Math.sin(angle) + cy;

                                if(colorChagneListener != null) colorChagneListener.onColorChanged(color);

                                for (ColorChagneListener listener : colorChagneListeners) {
                                    listener.onColorChanged(this.color);
                                }
                            }

                    break;

            }

            invalidate();

        return true;
    }

    /** Public user methods */

    public void setButtonTouchListener(@NonNull ButtonTouchListener listener){ buttonTouchListener = listener;}

    public void addButtonTouchListener(@NonNull ButtonTouchListener listener){ buttonTouchListeners.add(listener);}

    public void removeButtonTouchListener(@NonNull ButtonTouchListener listener) {buttonTouchListeners.remove(listener);}

    public void clearButtonTouchListener() {buttonTouchListeners.clear();}

    public void setColorChangeListener(@NonNull ColorChagneListener listener){ colorChagneListener = listener;}

    public void addColorChangeListener(@NonNull ColorChagneListener listener){ colorChagneListeners.add(listener);}

    public void removeColorChangeListener(@NonNull ColorChagneListener listener) { colorChagneListeners.remove(listener); }

    /** Remove all previously added {@link ColorChagneListener}s. */
    public void clearColorChangeListeners() { colorChagneListeners.clear(); }

    public void setColorPalette(int colors[]){

        if(colors[0] != colors[colors.length - 1]){
            colors = Arrays.copyOf(colors, colors.length + 1); //Create new array from old array and allocate one more element
            colors[colors.length - 1] = colors[0];
            color_palette = colors;
        }
        else {
            color_palette = colors;
        }

    }

    public void setColorPointer(boolean isColorPointer){this.isColorPointer = isColorPointer;}

    public void setColorPointerCustomColor(int color){this.isColorPointerCutomColor = true; p_cPointer.setColor(color);}

    public void setColorPointerCustomColor(String color){this.isColorPointerCutomColor = true; p_cPointer.setColor(Color.parseColor(color));}

    public void setColorPointerCustomColor(boolean isColorPointerCutomColor){
        this.isColorPointerCutomColor = isColorPointerCutomColor;
        if(isColorPointerCutomColor){p_cPointer.setColor(Color.WHITE);}
    }

    public void setPointerCustomColor(int color){this.isPointerCutomColor = true; p_pointer.setColor(color);}

    public void setPointerCustomColor(String color){this.isPointerCutomColor = true; p_pointer.setColor(Color.parseColor(color));}

    public void setPointerCustomColor(boolean isPointerCutomColor){
        this.isPointerCutomColor = isPointerCutomColor;
        if(isPointerCutomColor) {p_pointer.setColor(Color.WHITE);}
    }

    public void setColorPointerRadius(float colorPointerRadius){cPointer_rad = colorPointerRadius;}

    public void setUseImage(boolean isImage){this.isBadge = isImage;}

    public void setImageBitmap(Bitmap bitmap){
        mainImageBitmap = bitmap;
        mainImageBitmap = Bitmap.createScaledBitmap(mainImageBitmap, (int)cPointer_rad,
                (int)cPointer_rad, false); //Set BitMap Size
    }

    public void setExColorPointer(boolean isExColor){isExColorPointer = isExColor;}

    public void setExColoPointerRadius(float ExColorPointerRadius){this.excPointer_rad = ExColorPointerRadius;}

    public void setBackgroundColor(int color){this.p_background.setColor(color);}

    public void setBackground(boolean background){this.isBackground = background;}

    public void setPointerLine(boolean isPointerLine){this.isPointerLine = isPointerLine;}

    public void setPointerShadow(boolean pointerShadow){this.isPointerShadow = pointerShadow;}

    public void setPlacemarks(boolean isPlacemarks){this.isPlacemarks = isPlacemarks;}

    public void setPlacemarksCount(int count){this.pCount = count;}

    public void setColorRingRadius(float colorRingRadius){this.color_rad = colorRingRadius;}

    public void setColorRingWidth(float colorRingWidth){this.color_rWidth = colorRingWidth;}

    public void setColorPointerShadow(boolean colorPointerShadow){this.isColorPointerShadow = colorPointerShadow;}

    public void setPointerRadius(float pointerRadius){this.pointer_rad = pointerRadius;}

    public void setPointerOutline(boolean pointerOutline){this.isPointerOutline = pointerOutline;}

}

