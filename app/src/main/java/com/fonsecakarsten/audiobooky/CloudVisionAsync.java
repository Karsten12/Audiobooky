package com.fonsecakarsten.audiobooky;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Karsten on 6/15/2017.
 */

public class CloudVisionAsync extends AsyncTask<String, Void, String> {

    private String accessToken;
    private Bitmap bitmap;

    CloudVisionAsync(String idk, Bitmap bmap) {
        this.accessToken = idk;
        this.bitmap = bmap;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, credential);
            Vision vision = builder.build();

            List<Feature> featureList = new ArrayList<>();
            Feature labelDetection = new Feature();
            labelDetection.setType("LABEL_DETECTION");
            labelDetection.setMaxResults(10);
            featureList.add(labelDetection);

            Feature textDetection = new Feature();
            textDetection.setType("TEXT_DETECTION");
            textDetection.setMaxResults(10);
            featureList.add(textDetection);

            Feature landmarkDetection = new Feature();
            landmarkDetection.setType("LANDMARK_DETECTION");
            landmarkDetection.setMaxResults(10);
            featureList.add(landmarkDetection);

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

        } catch (GoogleJsonResponseException e) {
            //Log.e(LOG_TAG, "Request failed: " + e.getContent());
        } catch (IOException e) {
            //Log.d(LOG_TAG, "Request failed: " + e.getMessage());
        }
        return "Cloud Vision API request failed.";
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("Results:\n\n");
        message.append("Labels:\n");
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.getDefault(), "%.3f: %s",
                        label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing\n");
        }

        message.append("Texts:\n");
        List<EntityAnnotation> texts = response.getResponses().get(0)
                .getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                message.append(String.format(Locale.getDefault(), "%s: %s",
                        text.getLocale(), text.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing\n");
        }

        message.append("Landmarks:\n");
        List<EntityAnnotation> landmarks = response.getResponses().get(0)
                .getLandmarkAnnotations();
        if (landmarks != null) {
            for (EntityAnnotation landmark : landmarks) {
                message.append(String.format(Locale.getDefault(), "%.3f: %s",
                        landmark.getScore(), landmark.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing\n");
        }

        return message.toString();
    }

    public Image getBase64EncodedJpeg(Bitmap bitmap) {
        Image image = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        image.encodeContent(imageBytes);
        return image;
    }
}
