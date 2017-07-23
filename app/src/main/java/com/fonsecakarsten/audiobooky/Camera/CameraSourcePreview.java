package com.fonsecakarsten.audiobooky.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Size;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.fonsecakarsten.audiobooky.CameraView;

/**
 * Created by Karsten on 6/16/2017.
 * CameraSourcePreview from Android Open Source Project
 * Credits to Ezequiel Adrian Minniti for the migration to Camera2 API
 */

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";

    //PREVIEW VISUALIZERS FOR BOTH CAMERA1 AND CAMERA2 API.
    private SurfaceView mSurfaceView;
    private CameraView mAutoFitTextureView;

    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private boolean viewAdded = false;

    private CameraSource mCamera2Source;

    private int screenWidth;
    private int screenHeight;
    private int screenRotation;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mSurfaceAvailable = true;
            startIfReady();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            mSurfaceAvailable = false;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    public CameraSourcePreview(Context context) {
        super(context);
        screenHeight = Utils.getScreenHeight(context);
        screenWidth = Utils.getScreenWidth(context);
        screenRotation = Utils.getScreenRotation(context);
        mStartRequested = false;
        mSurfaceAvailable = false;
        mAutoFitTextureView = new CameraView(context);
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenHeight = Utils.getScreenHeight(context);
        screenWidth = Utils.getScreenWidth(context);
        screenRotation = Utils.getScreenRotation(context);
        mStartRequested = false;
        mSurfaceAvailable = false;
        mAutoFitTextureView = new CameraView(context);
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void start(CameraSource camera2Source) throws SecurityException {
        if (camera2Source == null) {
            stop();
        }
        mCamera2Source = camera2Source;

        if (mCamera2Source != null) {
            mStartRequested = true;
            if (!viewAdded) {
                addView(mAutoFitTextureView);
                viewAdded = true;
            }
            startIfReady();
        }
    }

    public void stop() {
        mStartRequested = false;
        if (mCamera2Source != null) {
            mCamera2Source.stop();
        }
    }

    public void release() {
        if (mCamera2Source != null) {
            mCamera2Source.release();
            mCamera2Source = null;
        }
    }

    private void startIfReady() throws SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCamera2Source.start(mAutoFitTextureView, screenRotation);
            mStartRequested = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 480;
        int height = 720;
        if (mCamera2Source != null) {
            Size size = mCamera2Source.getPreviewSize();
            if (size != null) {
                // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
                height = size.getWidth();
                width = size.getHeight();
            }
        }

        //RESIZE PREVIEW IGNORING ASPECT RATIO. THIS IS ESSENTIAL.
        int newWidth = (height * screenWidth) / screenHeight;

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;
        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int) (((float) layoutWidth / (float) newWidth) * height);
        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) height) * newWidth);
        }
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }
        startIfReady();
    }
}