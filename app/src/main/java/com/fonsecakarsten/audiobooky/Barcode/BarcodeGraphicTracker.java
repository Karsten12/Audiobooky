package com.fonsecakarsten.audiobooky.Barcode;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Generic tracker which is used for tracking or reading a barcode
 */
class BarcodeGraphicTracker extends Tracker<Barcode> {
    private Callback mCallback;

    BarcodeGraphicTracker(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onFound(String barcodeValue);
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        mCallback.onFound(item.displayValue);
    }
}
