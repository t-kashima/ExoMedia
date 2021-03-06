/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.devbrackets.android.exomedia.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.TextureView;

import com.devbrackets.android.exomedia.listener.ExoPlayerListener;

/**
 * A SurfaceView that resizes itself to match a specified aspect ratio.
 */
public class VideoSurfaceView extends TextureView implements ExoPlayerListener {
    /**
     * The surface view will not resize itself if the fractional difference between its default
     * aspect ratio and the aspect ratio of the video falls below this threshold.
     * <p>
     * This tolerance is useful for fullscreen playbacks, since it ensures that the surface will
     * occupy the whole of the screen when playing content that has the same (or virtually the same)
     * aspect ratio as the device. This typically reduces the number of view layers that need to be
     * composited by the underlying system, which can help to reduce power consumption.
     */
    private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION  = 0.01f;

    private float videoAspectRatio;

    private int viewWidth;
    private int viewHeight;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set the aspect ratio that this {@link VideoSurfaceView} should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    public void setAspectRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoAspectRatio == 0) {
            // Aspect ratio not set.
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
        if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            // We're within the allowed tolerance.
            return;
        }

        if (1 > videoAspectRatio) {
            height = (int) (width / videoAspectRatio);
        } else {
            width = (int) (height * videoAspectRatio);
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (1 > videoAspectRatio) {
            viewHeight = (int) (viewWidth / videoAspectRatio);
        } else {
            viewWidth = (int) (viewHeight * videoAspectRatio);
        }

        float pivotX = viewWidth / 2f;
        float pivotY = viewHeight / 2f;

        Matrix transform = new Matrix();
        transform.postRotate(unappliedRotationDegrees, pivotX, pivotY);
//        if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
//            float viewAspectRatio = (float) viewHeight / viewWidth;
//            transform.postScale(1 / viewAspectRatio, viewAspectRatio, pivotX, pivotY);
//        }
        setTransform(transform);
    }
}
