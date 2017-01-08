package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import uk.co.deanwild.materialshowcaseview.target.Target;

/**
 * Created by luka on 7.1.17..
 */

public class OvalShape implements Shape {
    private RectF bounds;
    private boolean adjustToTarget = true;


    public OvalShape(Rect bounds) {
        this.bounds = new RectF(bounds);
    }

    public OvalShape(Target target) {
        this(target.getBounds());
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    public boolean isAdjustToTarget() {
        return adjustToTarget;
    }

    public RectF getBounds() {
        return bounds;
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        float w = bounds.width()/1.4140f;
        float h = bounds.height()/1.4140f;//(w*2)/bounds.width() * bounds.height();
        float left = x - w, right = x + w, top = y - h, bot = y + h;
        if(bounds != null)
            canvas.drawOval(new RectF(left-padding, top-padding, right+padding, bot+padding), paint);
    }

    @Override
    public int getWidth() {
        return (int)bounds.width();
    }

    @Override
    public int getHeight() {
        return (int)bounds.height();
    }

    @Override
    public void updateTarget(Target target) {
        if (adjustToTarget)
            bounds = new RectF(target.getBounds());
    }
}
