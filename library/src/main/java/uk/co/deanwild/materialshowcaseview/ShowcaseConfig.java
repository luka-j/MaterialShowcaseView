package uk.co.deanwild.materialshowcaseview;

import android.graphics.Color;

import uk.co.deanwild.materialshowcaseview.shape.CircleShape;
import uk.co.deanwild.materialshowcaseview.shape.Shape;

public class ShowcaseConfig {
    public static final int DISMISS_STYLE_TEXT = 0;
    public static final int DISMISS_STYLE_BUTTON = 1;

    public static final String DEFAULT_MASK_COLOUR = "#dd335075";
    public static final int DEFAULT_DISMISS_STYLE = DISMISS_STYLE_TEXT;
    public static final int DEFAULT_DISMISS_BUTTON_STYLE = R.drawable.gray_button;
    public static final long DEFAULT_FADE_TIME = 300;
    public static final long DEFAULT_DELAY = 0;
    public static final Shape DEFAULT_SHAPE = new CircleShape();
    public static final int DEFAULT_SHAPE_PADDING = 10;

    private long mDelay = DEFAULT_DELAY;
    private int mMaskColor;
    private int mContentTextColor;
    private int mDismissStyle;
    private int mDismissTextColor;
    private int mDismissButtonStyle;
    private int mSkipTextColor;
    private long mFadeDuration = DEFAULT_FADE_TIME;
    private Shape mShape = DEFAULT_SHAPE;
    private int mShapePadding = DEFAULT_SHAPE_PADDING;
    private boolean renderOverNav = false;

    public ShowcaseConfig() {
        mMaskColor = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
        mContentTextColor = Color.parseColor("#ffffff");
        mDismissStyle = ShowcaseConfig.DEFAULT_DISMISS_STYLE;
        mDismissTextColor = Color.parseColor("#ffffff");
        mDismissButtonStyle = DEFAULT_DISMISS_BUTTON_STYLE;
        mSkipTextColor = Color.parseColor("#ffffff");
    }

    public long getDelay() {
        return mDelay;
    }

    public void setDelay(long delay) {
        this.mDelay = delay;
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
    }

    public int getContentTextColor() {
        return mContentTextColor;
    }

    public void setContentTextColor(int mContentTextColor) {
        this.mContentTextColor = mContentTextColor;
    }

    public int getDismissStyle() {
        return mDismissStyle;
    }

    public void setDismissStyle(int dismissStyle) {
        this.mDismissButtonStyle = dismissStyle;
    }

    public int getDismissTextColor() {
        return mDismissTextColor;
    }

    public void setDismissTextColor(int dismissTextColor) {
        this.mDismissTextColor = dismissTextColor;
    }

    public int getDismissButtonStyle() {
        return mDismissButtonStyle;
    }

    public void setDismissButtonStyle(int dismissButtonStyle) {
        this.mDismissButtonStyle = dismissButtonStyle;
    }

    public int getSkipTextColor() {
        return mSkipTextColor;
    }

    public void setSkipTextColor(int skipTextColor) {
        this.mSkipTextColor = skipTextColor;
    }

    public long getFadeDuration() {
        return mFadeDuration;
    }

    public void setFadeDuration(long fadeDuration) {
        this.mFadeDuration = fadeDuration;
    }

    public Shape getShape() {
        return mShape;
    }

    public void setShape(Shape shape) {
        this.mShape = shape;
    }

    public int getShapePadding() {
        return mShapePadding;
    }

    public void setShapePadding(int padding) {
        this.mShapePadding = padding;
    }

    public boolean getRenderOverNavigationBar() {
        return renderOverNav;
    }

    public void setRenderOverNavigationBar(boolean renderOverNav) {
        this.renderOverNav = renderOverNav;
    }
}
