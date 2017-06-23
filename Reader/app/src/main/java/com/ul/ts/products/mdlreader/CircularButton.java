package com.ul.ts.products.mdlreader;

import android.content.Context;
import android.util.AttributeSet;

import static java.lang.Math.max;

// adapted from https://stackoverflow.com/a/7979110
public class CircularButton extends android.support.v7.widget.AppCompatButton {
    public CircularButton(Context context) {
        super(context);
    }

    public CircularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measure = max(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(measure, measure);
    }
}
