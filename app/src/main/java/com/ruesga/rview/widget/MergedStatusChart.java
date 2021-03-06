/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.ruesga.rview.R;
import com.ruesga.rview.gerrit.model.ChangeStatus;
import com.ruesga.rview.model.Stats;

import java.util.List;

public class MergedStatusChart extends View {

    private final Object mLock = new Object();

    private final RectF mViewArea = new RectF();
    private final RectF mOpenRect = new RectF();
    private final RectF mMergedRect = new RectF();
    private final RectF mAbandonedRect = new RectF();

    private final Paint mOpenPaint;
    private final Paint mMergedPaint;
    private final Paint mAbandonedPaint;
    private final TextPaint mLabelPaint;

    private float mHeightBarPadding;
    private float mMinBarWidth;

    private int mTotal = 0;
    private int mOpen = 0;
    private int mMerged = 0;
    private int mAbandoned = 0;

    private float mLabelPadding;
    private float mLabelHeight;

    private ValueAnimator mAnimator;
    private float mAnimationDelta = 0f;

    public MergedStatusChart(Context context) {
        this(context, null);
    }

    public MergedStatusChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MergedStatusChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final Resources res = getResources();
        mHeightBarPadding = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, res.getDisplayMetrics());
        mMinBarWidth = 0f;
        int openColor = Color.DKGRAY;
        int mergedColor = Color.DKGRAY;
        int abandonedColor = Color.DKGRAY;
        int textColor = Color.WHITE;

        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(
                attrs, R.styleable.MergedStatusChart, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.MergedStatusChart_heightBarPadding:
                    mHeightBarPadding = a.getDimension(attr, mHeightBarPadding);
                    break;

                case R.styleable.MergedStatusChart_minBarWidth:
                    mMinBarWidth = a.getDimension(attr, mMinBarWidth);
                    break;

                case R.styleable.MergedStatusChart_openColor:
                    openColor = a.getColor(attr, openColor);
                    break;

                case R.styleable.MergedStatusChart_mergedColor:
                    mergedColor = a.getColor(attr, mergedColor);
                    break;

                case R.styleable.MergedStatusChart_abandonedColor:
                    abandonedColor = a.getColor(attr, abandonedColor);
                    break;

                case R.styleable.MergedStatusChart_statusLabelTextColor:
                    textColor = a.getColor(attr, textColor);
                    break;
            }
        }
        a.recycle();

        mOpenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOpenPaint.setStyle(Paint.Style.FILL);
        mMergedPaint = new Paint(mOpenPaint);
        mAbandonedPaint = new Paint(mOpenPaint);
        mOpenPaint.setColor(openColor);
        mMergedPaint.setColor(mergedColor);
        mAbandonedPaint.setColor(abandonedColor);

        mLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mLabelPaint.setTextAlign(Paint.Align.LEFT);
        mLabelPaint.setFakeBoldText(true);
        mLabelPaint.setColor(textColor);
        mLabelPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10f, res.getDisplayMetrics()));
        mLabelPadding = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 2f, res.getDisplayMetrics());
        Rect bounds = new Rect();
        mLabelPaint.getTextBounds("0", 0, 1, bounds);
        mLabelHeight = bounds.height();

        if (getBackground() == null) {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Bars
        canvas.drawRect(
                mOpenRect.left,
                mOpenRect.top,
                Math.min(mViewArea.width() * mAnimationDelta, mOpenRect.right),
                mOpenRect.bottom,
                mOpenPaint);
        canvas.drawRect(
                mMergedRect.left,
                mMergedRect.top,
                Math.min(mViewArea.width() * mAnimationDelta, mMergedRect.right),
                mMergedRect.bottom,
                mMergedPaint);
        canvas.drawRect(
                mAbandonedRect.left,
                mAbandonedRect.top,
                Math.min(mViewArea.width() * mAnimationDelta, mAbandonedRect.right),
                mAbandonedRect.bottom,
                mAbandonedPaint);

        // Number of items as text
        if (mAnimationDelta > .9f) {
            canvas.drawText(
                    String.valueOf(mOpen),
                    mOpenRect.left + mLabelPadding,
                    mOpenRect.top + (mOpenRect.height() / 2) + (mLabelHeight / 2),
                    mLabelPaint);
            canvas.drawText(
                    String.valueOf(mMerged),
                    mOpenRect.left + mLabelPadding,
                    mMergedRect.top + (mMergedRect.height() / 2) + (mLabelHeight / 2),
                    mLabelPaint);
            canvas.drawText(
                    String.valueOf(mAbandoned),
                    mOpenRect.left + mLabelPadding,
                    mAbandonedRect.top + (mAbandonedRect.height() / 2) + (mLabelHeight / 2),
                    mLabelPaint);
        }
    }

    public void update(List<Stats> stats) {
        int total = 0, open = 0, merged = 0, abandoned = 0;
        for (Stats s : stats) {
            if (s.mStatus.equals(ChangeStatus.NEW)) {
                open++;
            } else if (s.mStatus.equals(ChangeStatus.MERGED)) {
                merged++;
            } else if (s.mStatus.equals(ChangeStatus.ABANDONED)) {
                abandoned++;
            }
            total++;
        }
        synchronized (mLock) {
            mOpen = open;
            mMerged = merged;
            mAbandoned = abandoned;
            mTotal = total;
            computeDrawObjects();
        }

        // Animate the chart
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimationDelta = 0f;
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.setDuration(350L);
        mAnimator.addUpdateListener(animation -> {
            mAnimationDelta = animation.getAnimatedFraction();
            ViewCompat.postInvalidateOnAnimation(this);
        });
        mAnimator.start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // View area
        mViewArea.set(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());

        // Create the drawing objects
        synchronized (mLock) {
            computeDrawObjects();
        }
    }

    private void computeDrawObjects() {
        float barHeight = (mViewArea.height() - (mHeightBarPadding * 4)) / 3;
        float totalWidth = mViewArea.width();
        mOpenRect.set(
                0,
                mHeightBarPadding,
                mTotal == 0 ? 0 : Math.max((totalWidth * mOpen / mTotal), mMinBarWidth),
                mHeightBarPadding + barHeight);
        mMergedRect.set(
                0,
                (mHeightBarPadding * 2) + barHeight,
                mTotal == 0 ? 0 : Math.max((totalWidth * mMerged / mTotal), mMinBarWidth),
                (mHeightBarPadding * 2) + (barHeight * 2));
        mAbandonedRect.set(
                0,
                (mHeightBarPadding * 3) + (barHeight * 2),
                mTotal == 0 ? 0 : Math.max((totalWidth * mAbandoned / mTotal), mMinBarWidth),
                (mHeightBarPadding * 3) + (barHeight * 3));
    }
}
