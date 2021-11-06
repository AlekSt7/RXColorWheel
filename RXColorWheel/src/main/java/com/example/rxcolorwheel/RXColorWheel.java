package com.example.rxcolorwheel;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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

public final class RXColorWheel extends View {


    /** Interface definition for a callback which to be invoked when the color changes. */
    public interface ColorChagneListener{

        /**
         * Called when the color changes.
         *
         * @param color The current color.
         */
        void onColorChanged(int color);

        /**
         * Called when the view was rendered for the first time.
         *
         * @param color The current color.
         */
        void firstDraw(int color);
    }

    /** Interface definition for a callback which to be invoked when the buttons are pressed. */
    public interface ButtonTouchListener{

        void on_cPointerTouch();

        void on_excPointerTouch();

    }

    /** Interface definition for a callback which to be invoked when switching a step of stepper mode to the next value. */
    public interface StepperListener{

        void onStep();

    }

    private ColorChagneListener colorChagneListener;
    private ButtonTouchListener buttonTouchListener;
    private StepperListener     stepperListener;

    private final Paint     p_color = new Paint(Paint.ANTI_ALIAS_FLAG); //Color ring
    private final Paint     p_pointer = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer
    private final Paint     p_pStroke = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer outline
    private final Paint     p_background = new Paint();//Background
    private final Paint     p_pLine = new Paint(Paint.ANTI_ALIAS_FLAG); //Pointer line
    private final Paint     p_cPointer = new Paint(); //Color pointer
    private final Paint     p_excPointer = new Paint(); //External color pointer
    private final Paint     p_placemarks = new Paint(); //Placemarks

    private double          py, px; //Pointer coordinates

    private float           angle; //The angle of the touch point relative to the center
    private float           cx, cy; //Center coordinates of view
    private float           color_rad; //Color ring radius
    private float           color_rTh; //Color ring thickness
    private float           placemarks_rad; //Placemarks radius
    private float           cPointer_rad; //Color pointer radius
    private float           excPointer_rad; //External color pointer radius
    private float           pointer_rad; //Pointer radius
    private float           background_rad; //Background radius
    private float           badge_size; //Badge image size
    private float           degrees[];

    private int             color_palette[];
    private int             color; //The currently selected color
    private int             minVsize; //Minimal view size (by width or height)
    private int             pCount; //Number of decorative placemarks

    /** Boolean preference variables. */
    private boolean         isBackground;
    private boolean         isExColorPointer;
    private boolean         isColorPointerCustomColor;
    private boolean         isPointerLine;
    private boolean         isPlacemarks;
    private boolean         isPlacemarksRound;
    private boolean         isColorPointer;
    private boolean         isBadge;
    private boolean         isRoundBadge;
    private boolean         isPointerOutline;
    private boolean         isColorPointerShadow;
    private boolean         isPointerCustomColor;
    private boolean         isPointerShadow;
    private boolean         isShadow;
    private boolean         stepperMode;

    private boolean firstDraw = true;

    private Bitmap          mainImageBitmap;
    private TypedArray      typedArray;

    public RXColorWheel(Activity context) { super(context); }

    public RXColorWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        this.setDrawingCacheEnabled(true);

        setColorPalette(getResources().getIntArray(R.array.default_color_palette));

        typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PreferencesColorRing);

        isBackground = typedArray.getBoolean(R.styleable.PreferencesColorRing_isBackground,true);

        isExColorPointer = typedArray.getBoolean(R.styleable.PreferencesColorRing_isExColorPointer,true);

        isPointerLine = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerLine,true);

        isPlacemarks = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPlacemarks,true);

        isPlacemarksRound = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPlacemarksRound,true);

        isColorPointer = typedArray.getBoolean(R.styleable.PreferencesColorRing_isColorPointer,true);

        isColorPointerShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isColorPointerShadow, true);

        isBadge = typedArray.getBoolean(R.styleable.PreferencesColorRing_isBadge, true);

        isRoundBadge = typedArray.getBoolean(R.styleable.PreferencesColorRing_isRoundBadge, false);

        isPointerOutline = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerOutline, true);

        isPointerShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerShadow, false);

        pCount = even(typedArray.getInt(R.styleable.PreferencesColorRing_placemarksCount,20));
        if(stepperMode) calculate_step_angle(pCount);

        p_background.setColor(typedArray.getColor(R.styleable.PreferencesColorRing_bgColor,
                getResources().getColor(R.color.background)));

        isShadow = typedArray.getBoolean(R.styleable.PreferencesColorRing_isShadow, true);

        setIsPointerCustomColor(typedArray.getBoolean(R.styleable.PreferencesColorRing_isPointerCustomColor, false));

        setIsColorPointerCustomColor(typedArray.getBoolean(R.styleable.PreferencesColorRing_isColorPointerCustomColor, false));

        if (isPlacemarks) {stepperMode = typedArray.getBoolean(R.styleable.PreferencesColorRing_stepperMode, false);}
        else {stepperMode = false;}

        int cp_color = typedArray.getColor(R.styleable.PreferencesColorRing_colorPointerCustomColor, 0);
        if(cp_color != 0) setColorPointerCustomColor(cp_color);

        int pColor = typedArray.getColor(R.styleable.PreferencesColorRing_pointerCustomColor, 0);
        if(pColor != 0) setPointerCustomColor(pColor);

        mainImageBitmap = getBitmapFromVectorDrawable(context, typedArray.getResourceId(R.styleable.PreferencesColorRing_badge, R.drawable.ic_baseline_add_24));

    }

    public RXColorWheel(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth = decodeMeasureSpec(widthMeasureSpec);
        int mHeight = decodeMeasureSpec(heightMeasureSpec);

        //int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        //int mHeight = MeasureSpec.getSize(heightMeasureSpec);

        minVsize = Math.min(mWidth, mHeight);
        setMeasuredDimension(mWidth, mHeight);

        cx = mWidth * 0.5f;
        cy = mHeight * 0.5f;

        calculateSizes();
        init();

    }

    private int decodeMeasureSpec(int measureSpec) {
        int result;
        int specMoge = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMoge == MeasureSpec.UNSPECIFIED) result = 350;
        else result = specSize;
        return result;
    }

    /** Calculates the dimensions of the elements inside the View */
    private void calculateSizes() {

        float color_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorRingRad, 0.41f);
        float color_rWidth_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorRingThickness, 0.04f);
        float pointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_pointerRad, 0.12f);
        float cPointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_colorPointerRad, 0.17f);
        float badge_size_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_badgeSize, 1);
        float excPointer_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_excPointerRad, 0.6f);
        float placemarks_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_placemarksRad, 0.96f);
        float background_rad_coef = typedArray.getFloat(R.styleable.PreferencesColorRing_backgroundRad, 1);

        color_rad = minVsize * color_rad_coef;
        color_rTh = color_rad * color_rWidth_coef;
        pointer_rad = color_rad * pointer_rad_coef;
        cPointer_rad = color_rad * cPointer_rad_coef;
        badge_size = cPointer_rad * badge_size_coef;
        excPointer_rad = color_rad * excPointer_rad_coef;
        placemarks_rad = color_rad * placemarks_rad_coef - color_rTh * 0.5f;
        background_rad = color_rad * background_rad_coef;
        px = cx + color_rad;
        py = cy;

    }

    /** Sets shaders and sizes of the elements inside the View */
    private void init(){

        Shader s_color = new SweepGradient(cx, cy, color_palette, null); //Color ring shader

        p_color.setStyle(Paint.Style.STROKE); //Color ring
        p_color.setStrokeWidth(color_rTh);
        p_color.setShader(s_color);

        p_pointer.setStyle(Paint.Style.FILL); //Pointer
        if(isPointerShadow) {
            p_pointer.setShadowLayer(15.0f, 0.0f, 0.0f, Color.argb(110, 0, 0, 0));
        }

        p_pStroke.setStyle(Paint.Style.STROKE); //Pointer outline
        p_pStroke.setColor(getResources().getColor(R.color.pointer_outline));
        p_pStroke.setStrokeWidth(pointer_rad * 0.08f);

        if(isShadow) {
            p_background.setShadowLayer(50.0f, 0.0f, 0.0f, 0xFF000000);
        }

        p_pLine.setStyle(Paint.Style.STROKE); //Pointer line
        p_pLine.setColor(getResources().getColor(R.color.pointer_line));

        p_cPointer.setStyle(Paint.Style.FILL); //Color pointer
        if(isColorPointerShadow) {
            p_cPointer.setShadowLayer(90.0f, 0.0f, 0.0f, Color.argb(130, 0, 0, 0));
        }

        p_excPointer.setStyle(Paint.Style.FILL); //External color pointer

        p_placemarks.setStyle(Paint.Style.STROKE); //Placemarks
        p_placemarks.setARGB(255, 124,122,129);

        if(mainImageBitmap != null) {
            mainImageBitmap = Bitmap.createScaledBitmap(mainImageBitmap, (int) badge_size,
                    (int) badge_size, false); //Set BitMap Size
        }

    }

    /** Calculates the angle to which the pointer should move in stepper mode */
    private void calculate_step_angle(int line_count){

        float angle = 0;
        float degree = (float) Math.toRadians(360 / line_count);

        degrees = new float[line_count + 1];

        int half = line_count/2;
        degrees[0] = 0;

        float array[] = new float[half];

        for(int i = 1; i < half+1; i++) {
                angle = angle + degree;
                degrees[i] = angle;
                array[i-1] = degrees[i];
        }

        for(int i = half+1; i < line_count+1; i++){
            degrees[i] = array[i-half-1] * -1;
        }

    }

    /**
     * Draws placemark lines around a circle
     *
     * @param r The outer radius of the lines, the beginning of the placemark line
     * @param r2 The inner radius of the lines, the end of the placemark line
     * @param line_count The number of lines to be drawn
     */
    private void drawRadialLines(Canvas c, float r, float r2, int line_count){

        double r1x; //Internal diameter coordinates
        double r1y;
        double r2x; //Outer diameter coordinates
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

    /** Returns Bitmap from vector drawable */
    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
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

    /** Returns round Bitmap from Bitmap */
    private static Bitmap getCircledBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if(isBackground) {c.drawCircle(cx, cy, background_rad, p_background);} //Background
        c.drawCircle(cx, cy, color_rad, p_color); //Color ring

        int pixel = getDrawingCache().getPixel((int) px,(int) py);

        color = pixel;

        if(!isColorPointerCustomColor) {p_cPointer.setColor(pixel);}
        if(!isPointerCustomColor) {p_pointer.setColor(pixel);}

        float[] hsv = new float[] {0, 1f, 1f};

        Color.colorToHSV(pixel, hsv);

        hsv[2] = hsv[2] * 0.90f;

        p_excPointer.setColor(Color.HSVToColor(hsv));

        if(isExColorPointer) {c.drawCircle(cx, cy, excPointer_rad, p_excPointer);} //External color pointer

        if(firstDraw) {
            firstDraw = false;
            if(stepperMode) calculate_step_angle(pCount);
            colorChagneListener.firstDraw(color);
        }
        else if(!firstDraw){
            if(isPointerLine) {c.drawLine(cx,cy,(float) px,(float) py, p_pLine);} //Pointer line
            if(isColorPointer) { //Color pointer
                c.drawCircle(cx, cy, cPointer_rad, p_cPointer);
                if(isBadge){
                    c.drawBitmap(
                            isRoundBadge ? getCircledBitmap(mainImageBitmap) : mainImageBitmap, // Bitmap
                            cx - mainImageBitmap.getWidth() * 0.5f,
                            cy - mainImageBitmap.getHeight() * 0.5f,
                            p_cPointer // Paint
                    );
                }
            }

            if(isPlacemarks){
                drawRadialLines(c, placemarks_rad - 20, 20, pCount); //Placemarks
                if(isPlacemarksRound) c.drawCircle(cx, cy, placemarks_rad, p_placemarks);
            }

            c.drawCircle((float) px, (float) py, pointer_rad, p_pointer); //Pointer
            if (isPointerOutline) { //Pointer outline
                c.drawCircle((float) px, (float) py, pointer_rad, p_pStroke);
            }
        }

    }

    /**
     * Finds the nearest value from an array of values
     *
     * @param n Value for comparison
     * @param args Array of values to compare
     */
    static float nearest(float n, float...args) {
        float nearest = 0;
        float value = 2*Float.MAX_VALUE;
        if(args != null){
            for(float arg : args){
                if (value > Math.abs(n - arg)){
                    value = Math.abs(n-arg);
                    nearest = arg;}}
        }
        return nearest;
    }

    float nearest_old = 0;
    boolean exPBL = false, pBL = false, cL = false;
    boolean move_pointer = false;
    float angle_old = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX() - cx;
        float y = event.getY() - cy;

        float s = cx - color_rad;

        float nearest;

        angle = (float) Math.atan2(y,x); //Find the angle relative to the center and the touch point
        double d = Math.sqrt(x*x + y*y); //Calculating the distance from the touch point to the center

        if(stepperMode) nearest = nearest(angle, degrees); else nearest = 0;

            switch (event.getAction()) {

                case MotionEvent.ACTION_UP:

                    move_pointer = false;

                    if(exPBL){
                        if(buttonTouchListener != null) buttonTouchListener.on_excPointerTouch();
                        exPBL = false;
                    }
                    else if(pBL){
                        if(buttonTouchListener != null) buttonTouchListener.on_excPointerTouch();
                        pBL = false;
                    }
                    else if(cL){
                        if(buttonTouchListener != null) buttonTouchListener.on_cPointerTouch();
                        cL = false;
                    }

                break;

                case MotionEvent.ACTION_DOWN:

                    if(d < excPointer_rad && d > cPointer_rad && isExColorPointer){ exPBL = true; }
                    else if(d < excPointer_rad && !isColorPointer && isExColorPointer){ pBL = true; }
                    else if(d < cPointer_rad && isColorPointer){ cL = true; }

                    float t = color_rTh * 0.5f + 48;

                    if(d < color_rad + t  && d > color_rad - t) {
                        move_pointer = true;
                        if(stepperMode) {
                            angle = nearest;
                            if(Math.abs(nearest_old) != Math.abs(nearest)) {
                                nearest_old = nearest;
                                if (stepperListener != null) stepperListener.onStep();
                            }
                        }
                            px = (cx - s) * Math.cos(angle) + cx;
                            py = (cx - s) * Math.sin(angle) + cy;

                        if(colorChagneListener != null) colorChagneListener.onColorChanged(color);

                    }

                break;

                case MotionEvent.ACTION_MOVE:

                    if (move_pointer) {
                        if(stepperMode){
                                angle = nearest;
                            if(Math.abs(nearest_old) != Math.abs(nearest)) {
                                nearest_old = nearest;
                                if (stepperListener != null) stepperListener.onStep();
                            }
                        }
                            px = (cx - s) * Math.cos(angle) + cx;
                            py = (cx - s) * Math.sin(angle) + cy;

                            if(colorChagneListener != null) colorChagneListener.onColorChanged(color);
                    }

                break;

            }

        if(angle_old != angle) {
            angle_old = angle;
            invalidate();
        }

        return true;
    }

    /** Returns always an even number */
    private int even(int c){
        int cc;
        if(c % 2 == 0) { cc = c; }
        else{ cc = c + 1; }
        return cc;
    }

    /** ---------Public user set methods--------- */

    /** Sets user color on color wheel */
    public boolean setColor(int color){

        float s = cx - color_rad;
        double x = cx + color_rad;
        double y = cy;

        for(float p = 0; p <= 6.2831f; p=p+0.0001f){

            x = (cx - s) * Math.cos(p) + cx;
            y = (cx - s) * Math.sin(p) + cy;

            if(color == getDrawingCache().getPixel((int) x,(int) y)){
                px = x;
                py = y;
                invalidate();
                return true;
            }

        }
        return false;
    }

    public void setButtonTouchListener(@NonNull ButtonTouchListener listener){ buttonTouchListener = listener;}

    public void setColorChangeListener(@NonNull ColorChagneListener listener){colorChagneListener = listener;}

    public void setStepperListener(@NonNull StepperListener listener){ stepperListener = listener;}


    /**
     * Sets the color palette of color wheel
     *
     * @param colors Accepts an array of color indexes
     */
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

    public void setIsColorPointer(boolean isColorPointer){this.isColorPointer = isColorPointer;}

    public void setColorPointerCustomColor(int color){this.isColorPointerCustomColor = true; p_cPointer.setColor(color);}

    public void setColorPointerCustomColor(String color){this.isColorPointerCustomColor = true; p_cPointer.setColor(Color.parseColor(color));}

    public void setIsColorPointerCustomColor(boolean isColorPointerCustomColor){
        this.isColorPointerCustomColor = isColorPointerCustomColor;
        if(isColorPointerCustomColor){p_cPointer.setColor(getResources().getColor(R.color.color_pointer));}
    }

    public void setPointerCustomColor(int color){this.isPointerCustomColor = true; p_pointer.setColor(color);}

    public void setPointerCustomColor(String color){this.isPointerCustomColor = true; p_pointer.setColor(Color.parseColor(color));}

    public void setIsPointerCustomColor(boolean isPointerCustomColor){
        this.isPointerCustomColor = isPointerCustomColor;
        if(isPointerCustomColor) p_pointer.setColor(getResources().getColor(R.color.pointer));
    }

    public void setColorPointerRadius(float colorPointerRadius){cPointer_rad = color_rad * colorPointerRadius;}

    public void setIsBadge(boolean isBadge){this.isBadge = isBadge;}

    public void setIsRoundBadge(boolean isRoundBadge){this.isRoundBadge = isRoundBadge;}

    public void setBadgeSize(float badge_size){this.badge_size = cPointer_rad * badge_size;}

    public void setImageBitmap(Bitmap bitmap){
        mainImageBitmap = bitmap;
        mainImageBitmap = Bitmap.createScaledBitmap(mainImageBitmap, (int)cPointer_rad,
                (int)cPointer_rad, false); //Set BitMap Size
    }

    public void setImageById(Context context, int drawableId){
        mainImageBitmap = getBitmapFromVectorDrawable(context, drawableId);
        mainImageBitmap = Bitmap.createScaledBitmap(mainImageBitmap, (int)cPointer_rad,
                (int)cPointer_rad, false); //Set BitMap Size
    }

    public void setIsExColorPointer(boolean isExColorPointer){this.isExColorPointer = isExColorPointer;}

    public void setExColorPointerRadius(float ExColorPointerRadius){this.excPointer_rad = color_rad * ExColorPointerRadius;}

    public void setBackgroundColor(int color){this.p_background.setColor(color);}

    public void setIsBackground(boolean background){this.isBackground = background;}

    public void setIsPointerLine(boolean isPointerLine){this.isPointerLine = isPointerLine;}

    public void setIsPointerShadow(boolean isPointerShadow){this.isPointerShadow = isPointerShadow;}

    public void setIsPlacemarks(boolean isPlacemarks){this.isPlacemarks = isPlacemarks;}

    public void setIsPlacemarksRound(boolean isPlacemarksRound){this.isPlacemarksRound = isPlacemarksRound;}

    public void setPlacemarksCount(int count){this.pCount = even(count); calculate_step_angle(pCount);}

    public void setColorRingRadius(float colorRingRadius){this.color_rad = minVsize * colorRingRadius;}

    public void setColorRingThickness(float colorRingThickness){this.color_rTh = color_rad * colorRingThickness;}

    public void setIsColorPointerShadow(boolean isColorPointerShadow){this.isColorPointerShadow = isColorPointerShadow;}

    public void setPointerRadius(float pointerRadius){this.pointer_rad = color_rad * pointerRadius;}

    public void setIsPointerOutline(boolean isPointerOutline){this.isPointerOutline = isPointerOutline;}

    public void setStepperMode(boolean stepperMode){if(isPlacemarks) this.stepperMode = stepperMode; if(this.stepperMode) calculate_step_angle(pCount);}

    /** ---------Public user get methods--------- */

    public int[] getColor_palette() {return this.color_palette;}

    public boolean getIsColorPointer(){return this.isColorPointer;}

    public boolean getIsColorPointerCustomColor(){return this.isColorPointerCustomColor;}

    public int getColorPointerCustomColor(){return this.p_cPointer.getColor();}

    public boolean getIsPointerCustomColor(){return this.isPointerCustomColor;}

    public int getPointerCustomColor(){return this.p_pointer.getColor();}

    public float getColorPointerRadius(){return this.cPointer_rad;}

    public boolean getIsBadge(){return this.isBadge;}

    public boolean getIsRoundBadge(){return this.isRoundBadge;}

    public float getBadgeSize(){return this.badge_size;}

    public Bitmap getImageBitmap(){ return this.mainImageBitmap;}

    public boolean getIsExColorPointer(){return this.isExColorPointer;}

    public float getExColoPointerRadius(){return this.excPointer_rad;}

    public int getBackgroundColor(){return this.p_background.getColor();}

    public boolean getIsBackground(){return this.isBackground;}

    public boolean getIsPointerLine(){return this.isPointerLine;}

    public boolean getIsPointerShadow(){return this.isPointerShadow;}

    public boolean getIsPlacemarks(){return this.isPlacemarks;}

    public boolean getIsPlacemarksRound(){return this.isPlacemarksRound;}

    public int getPlacemarksCount(){return this.pCount;}

    public float getColorRingRadius(){return this.color_rad;}

    public float getColorRingThickness(){return this.color_rTh;}

    public boolean getIsColorPointerShadow(){return this.isColorPointerShadow;}

    public float getPointerRadius(){return this.pointer_rad;}

    public boolean getIsPointerOutline(){return this.isPointerOutline;}

    public boolean getStepperMode() {return this.stepperMode;}

}

