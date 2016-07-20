package uk.co.deanwild.materialshowcaseview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.shape.CircleShape;
import uk.co.deanwild.materialshowcaseview.shape.NoShape;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;
import uk.co.deanwild.materialshowcaseview.shape.Shape;
import uk.co.deanwild.materialshowcaseview.shape.ShapeType;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;


/**
 * Helper class to show a sequence of showcase views.
 */
public class MaterialShowcaseView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {
    private static final int DEFAULT_CONTENT_LAYOUT_ID = R.layout.showcase_content;

    private int mOldHeight;
    private int mOldWidth;
    private Bitmap mBitmap;// = new WeakReference<>(null);
    private Canvas mCanvas;
    private Paint mEraser;
    protected List<ViewTarget> mTargets;

    private boolean mWasDismissed = false;
    private int mShapePadding = ShowcaseConfig.DEFAULT_SHAPE_PADDING;

    private View mContentBox;
    private boolean mDismissOnTouch = false;
    private boolean mShouldRender = false; // flag to decide when we should actually render
    private boolean mRenderOverNav = false;
    private int mMaskColour;
    private AnimationFactory mAnimationFactory;
    private boolean mShouldAnimate = true;
    private long mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME;
    private Handler mHandler;
    private long mDelayInMillis = ShowcaseConfig.DEFAULT_DELAY;
    private boolean mSingleUse = false; // should display only once
    private PrefsManager mPrefsManager; // used to store state doe single use mode
    List<IShowcaseListener> mListeners; // external listeners who want to observe when we show and dismiss
    private UpdateOnGlobalLayout mLayoutListener;
    private IDetachedListener mDetachedListener;
    private boolean mTargetTouchable = false;
    private boolean mDismissOnTargetTouch = true;
    private ShapeType mShapeType = ShapeType.RECTANGLE_SHAPE;

    public MaterialShowcaseView(Context context) {
        this(context, null);
    }

    public MaterialShowcaseView(Context context, int contentResId) {
        this(context, null, 0, contentResId);
    }

    public MaterialShowcaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, DEFAULT_CONTENT_LAYOUT_ID);
    }

    public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr, int contentResId) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        // create our animation factory
        mAnimationFactory = new AnimationFactory();

        mListeners = new ArrayList<>();

        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = new UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        // consume touch events
        setOnTouchListener(this);

        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
        setVisibility(INVISIBLE);


        mContentBox = LayoutInflater.from(getContext()).inflate(contentResId, this, true);
    }


    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // don't bother drawing if we're not ready
        if (!mShouldRender) return;

        // get current dimensions
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        // don't bother drawing if there is nothing to draw on
        if (width <= 0 || height <= 0) return;

        // build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {

            if (mBitmap != null) mBitmap.recycle();

            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            mCanvas = new Canvas(mBitmap);
        }

        // save our 'old' dimensions
        mOldWidth = width;
        mOldHeight = height;

        // clear canvas
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // draw solid background
        mCanvas.drawColor(mMaskColour);

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setColor(0xFFFFFFFF);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        // draw (erase) shape
        for (ViewTarget target : mTargets) {
            if (target.getShape() == null) {
                target.setShape(createShape(target));
            }

            Point point = target.getPoint();
            target.getShape().draw(mCanvas, mEraser, point.x, point.y, mShapePadding);
        }

        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /**
         * If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
         * Probably due to an orientation change or user backed out of activity.
         * Ensure we reset the flag so the showcase display again.
         */
        if (!mWasDismissed && mSingleUse && mPrefsManager != null) {
            mPrefsManager.resetShowcase();
        }


        notifyOnDismissed();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mDismissOnTouch) {
            hide();
        }
        for (Target target : mTargets) {
            if (mTargetTouchable && target.getBounds().contains((int) event.getX(), (int) event.getY())) {
                if (mDismissOnTargetTouch) {
                    hide();
                }
                return false;
            }

        }
        return true;
    }


    private void notifyOnDisplayed() {

        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDisplayed(this);
            }
        }
    }

    private void notifyOnDismissed() {
        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDismissed(this);
            }

            mListeners.clear();
            mListeners = null;
        }

        /**
         * internal listener used by sequence for storing progress within the sequence
         */
        if (mDetachedListener != null) {
            mDetachedListener.onShowcaseDetached(this, mWasDismissed);
        }
    }

    /**
     * Dismiss button clicked
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        hide();
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target
     */
    public void addTarget(ViewTarget target) {
        if (mTargets == null) {
            mTargets = new ArrayList<>();
        }
        mTargets.add(target);
    }

    /**
     * SETTERS
     */

    public void setShapePadding(int padding) {
        mShapePadding = padding;
    }

    private void setDismissOnTouch(boolean dismissOnTouch) {
        mDismissOnTouch = dismissOnTouch;
    }

    private void setShouldRender(boolean shouldRender) {
        mShouldRender = shouldRender;
    }

    protected void setMaskColour(int maskColour) {
        mMaskColour = maskColour;
    }

    private void setDelay(long delayInMillis) {
        mDelayInMillis = delayInMillis;
    }

    private void setFadeDuration(long fadeDurationInMillis) {
        mFadeDurationInMillis = fadeDurationInMillis;
    }

    private void setTargetTouchable(boolean targetTouchable) {
        mTargetTouchable = targetTouchable;
    }

    private void setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
        mDismissOnTargetTouch = dismissOnTargetTouch;
    }

    public void addShowcaseListener(IShowcaseListener showcaseListener) {

        if (mListeners != null)
            mListeners.add(showcaseListener);
    }

    public void removeShowcaseListener(MaterialShowcaseSequence showcaseListener) {

        if ((mListeners != null) && mListeners.contains(showcaseListener)) {
            mListeners.remove(showcaseListener);
        }
    }

    void setDetachedListener(IDetachedListener detachedListener) {
        mDetachedListener = detachedListener;
    }

    /**
     * Set properties based on a config object
     *
     * @param config
     */
    public void setConfig(ShowcaseConfig config) {
        setDelay(config.getDelay());
        setFadeDuration(config.getFadeDuration());
        setMaskColour(config.getMaskColor());
        setShapePadding(config.getShapePadding());
        setRenderOverNavigationBar(config.getRenderOverNavigationBar());
    }

    public boolean hasFired() {
        return mPrefsManager.hasFired();
    }

    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {

        }
    }


    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for eaily configuring showcase views
     */
    public static class Builder {
        private boolean fullWidth = false;

        final MaterialShowcaseView showcaseView;

        private final Activity activity;

        public Builder(Activity activity, int contentResId) {
            this.activity = activity;

            showcaseView = new MaterialShowcaseView(activity, contentResId);
        }

        public Builder(Activity activity) {
            this.activity = activity;
            showcaseView = new MaterialShowcaseView(activity);
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder addTarget(View target) {
            showcaseView.addTarget(new ViewTarget(target));
            return this;
        }


        /**
         * Set whether or not the target view can be touched while the showcase is visible.
         * <p/>
         * False by default.
         */
        public Builder setTargetTouchable(boolean targetTouchable) {
            showcaseView.setTargetTouchable(targetTouchable);
            return this;
        }

        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         * <p/>
         * True by default.
         */
        public Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
            showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch);
            return this;
        }

        public Builder setDismissOnTouch(boolean dismissOnTouch) {
            showcaseView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public Builder setMaskColour(int maskColour) {
            showcaseView.setMaskColour(maskColour);
            return this;
        }


        public Builder setDelay(int delayInMillis) {
            showcaseView.setDelay(delayInMillis);
            return this;
        }

        public Builder setFadeDuration(int fadeDurationInMillis) {
            showcaseView.setFadeDuration(fadeDurationInMillis);
            return this;
        }

        public Builder setListener(IShowcaseListener listener) {
            showcaseView.addShowcaseListener(listener);
            return this;
        }

        public Builder singleUse(String showcaseID) {
            showcaseView.singleUse(showcaseID);
            return this;
        }

        public Builder withCircleShape() {
            showcaseView.mShapeType = ShapeType.CIRCLE_SHAPE;
            return this;
        }

        public Builder withoutShape() {
            showcaseView.mShapeType = ShapeType.NO_SHAPE;
            return this;
        }

        public Builder setShapePadding(int padding) {
            showcaseView.setShapePadding(padding);
            return this;
        }

        public Builder withRectangleShape() {
            return withRectangleShape(false);
        }

        public Builder withRectangleShape(boolean fullWidth) {
            showcaseView.mShapeType = ShapeType.RECTANGLE_SHAPE;
            this.fullWidth = fullWidth;
            return this;
        }

        public Builder renderOverNavigationBar() {
            // Note: This only has an effect in Lollipop or above.
            showcaseView.setRenderOverNavigationBar(true);
            return this;
        }

        public MaterialShowcaseView build() {
            return showcaseView;
        }

        public MaterialShowcaseView show() {
            build().show(activity);
            return showcaseView;
        }

    }

    private Shape createShape(Target target) {
        switch (mShapeType) {
            case RECTANGLE_SHAPE:
                return new RectangleShape(target.getBounds());
            case CIRCLE_SHAPE:
                return new CircleShape(target);
            default:
                return new NoShape();
        }
    }

    private void singleUse(String showcaseID) {
        mSingleUse = true;
        mPrefsManager = new PrefsManager(getContext(), showcaseID);
    }

    public void removeFromWindow() {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mEraser = null;
        mAnimationFactory = null;
        mCanvas = null;
        mHandler = null;

        getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        mLayoutListener = null;

        if (mPrefsManager != null)
            mPrefsManager.close();

        mPrefsManager = null;
    }

    /**
     * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
     *
     * @param activity
     * @return
     */
    public boolean show(final Activity activity) {

        /**
         * if we're in single use mode and have already shot our bolt then do nothing
         */
        if (mSingleUse) {
            if (mPrefsManager.hasFired()) {
                return false;
            } else {
                mPrefsManager.setFired();
            }
        }

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setShouldRender(true);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mShouldAnimate) {
                    fadeIn();
                } else {
                    setVisibility(VISIBLE);
                    notifyOnDisplayed();
                }
            }
        }, mDelayInMillis);

        return true;
    }


    public void hide() {

        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
         */
        mWasDismissed = true;

        if (mShouldAnimate) {
            fadeOut();
        } else {
            removeFromWindow();
        }
    }

    public void fadeIn() {
        setVisibility(INVISIBLE);

        mAnimationFactory.fadeInView(this, mFadeDurationInMillis,
                new IAnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                        notifyOnDisplayed();
                    }
                }
        );
    }

    public void fadeOut() {

        mAnimationFactory.fadeOutView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(INVISIBLE);
                removeFromWindow();
            }
        });
    }

    public void resetSingleUse() {
        if (mSingleUse && mPrefsManager != null) mPrefsManager.resetShowcase();
    }

    /**
     * Static helper method for resetting single use flag
     *
     * @param context
     * @param showcaseID
     */
    public static void resetSingleUse(Context context, String showcaseID) {
        PrefsManager.resetShowcase(context, showcaseID);
    }

    /**
     * Static helper method for resetting all single use flags
     *
     * @param context
     */
    public static void resetAll(Context context) {
        PrefsManager.resetAll(context);
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    private void setRenderOverNavigationBar(boolean mRenderOverNav) {
        this.mRenderOverNav = mRenderOverNav;
    }
}
