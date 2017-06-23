package com.fonsecakarsten.audiobooky;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karsten on 6/15/2017.
 */

class CloudVisionAsync extends AsyncTask<String, Void, ArrayList<String>> {

    private String accessToken;
    private String URI;

    CloudVisionAsync(String token, String imageURI) {
        this.accessToken = token;
        this.URI = imageURI;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        Bitmap bitmap = resizeBitmap(URI);
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, credential);
            Vision vision = builder.build();

            List<Feature> featureList = new ArrayList<>();

            Feature textDetection = new Feature();
            textDetection.setType("TEXT_DETECTION");
            textDetection.setMaxResults(10);
            featureList.add(textDetection);

            List<AnnotateImageRequest> imageList = new ArrayList<>();
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
            Image base64EncodedImage = getBase64EncodedJpeg(bitmap);
            annotateImageRequest.setImage(base64EncodedImage);
            annotateImageRequest.setFeatures(featureList);
            imageList.add(annotateImageRequest);

            BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(imageList);

            Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
            // Due to a bug: requests to Vision API containing large images fail when GZipped.
            annotateRequest.setDisableGZipContent(true);
            //Log.d(LOG_TAG, "sending request");

            BatchAnnotateImagesResponse response = annotateRequest.execute();
            return convertResponseToString(response);

        } catch (IOException e) {
            //Log.d(LOG_TAG, "Request failed: " + e.getMessage());
        }
        return null;
    }

    private ArrayList<String> convertResponseToString(BatchAnnotateImagesResponse response) {
        //StringBuilder message = new StringBuilder("Results:\n\n");

        int pages = response.getResponses().size();
        ArrayList<String> pageTexts = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            String pageText = response.getResponses().get(i).getTextAnnotations().get(0).getDescription(); // Get the text on the ith page
            pageTexts.add(pageText);
        }
        return pageTexts;
    }

    private Image getBase64EncodedJpeg(Bitmap bitmap) {
        Image image = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        image.encodeContent(imageBytes);
        return image;
    }

    private Bitmap resizeBitmap(String uri) {
        Bitmap bitmap = getAndRotateImage(uri);

        int maxDimension = 1024;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private Bitmap getAndRotateImage(String path) {
        try {
            File f = new File(path);
            long length = f.length();
            length = length / 1024;
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    break;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

        } catch (IOException e) {
            System.out.println("TAG-- Error in setting image");
        } catch (OutOfMemoryError oom) {
            System.out.println("TAG-- OOM Error in setting image");
        }
        return null;
    }

}
