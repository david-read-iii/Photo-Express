package com.davidread.photoexpress;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.content.ContentResolver;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * {@link ImageSaver} provides the method
 * {@link #saveAlteredPhotoAsync(File, int, int, SaveImageCallback)} which overwrites a photo with
 * an altered version of the photo on a background thread. The interface {@link SaveImageCallback}
 * is provided so that the calling class can specify what to do after this overwrite is complete.
 */
public class ImageSaver {

    /**
     * {@link SaveImageCallback} is an interface that allows a calling class to specify what to do
     * after {@link #saveAlteredPhotoAsync(File, int, int, SaveImageCallback)} completes its
     * operations.
     */
    interface SaveImageCallback {
        void onComplete(boolean result);
    }

    /**
     * {@link Executor} for executing operations on a background thread.
     */
    private final Executor mExecutor;

    /**
     * {@link Handler} for invoking the {@link SaveImageCallback#onComplete(boolean)} method on the
     * main thread.
     */
    private final Handler mHandler;

    /**
     * {@link Context} for getting a {@link ContentResolver}.
     */
    private final Context mActivityContext;

    /**
     * Constructs a new {@link ImageSaver}.
     *
     * @param context {@link Context} for getting a {@link ContentResolver}.
     */
    public ImageSaver(Context context) {
        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(Looper.getMainLooper());
        mActivityContext = context;
    }

    /**
     * Overwrites the image at the passed {@link File} with a brightness-altered version of itself
     * given some brightness alteration values to apply. This operation is done on a background
     * thread asynchronously. When this operation is complete, the
     * {@link SaveImageCallback#onComplete(boolean)} method is invoked indicating the result status
     * of the overwrite.
     *
     * @param photoFile       {@link File} referencing an image in the device's external storage to
     *                        be altered.
     * @param filterMultColor Int color value to multiply against RGB values when changing the
     *                        image's brightness.
     * @param filterAddColor  Int color value to add to RGB values when changing the image's
     *                        brightness.
     * @param callback        {@link SaveImageCallback} whose methods are invoked when this
     *                        operation completes.
     */
    public void saveAlteredPhotoAsync(final File photoFile, final int filterMultColor,
                                      final int filterAddColor, final SaveImageCallback callback) {
        // Call saveAlteredPhoto() on a background thread.
        mExecutor.execute(() -> {
            try {
                saveAlteredPhoto(photoFile, filterMultColor, filterAddColor);
                notifyResult(callback, true);
            } catch (IOException e) {
                e.printStackTrace();
                notifyResult(callback, false);
            }
        });
    }

    /**
     * Overwrites the image at the passed {@link File} with a brightness-altered version of itself
     * given some brightness alteration values to apply. Then, it inserts the altered image's
     * metadata into {@link MediaStore} so that the image may be accessed in shared storage.
     *
     * @param photoFile       {@link File} referencing an image in the device's external storage to
     *                        be altered.
     * @param filterMultColor Int color value to multiply against RGB values when changing the
     *                        image's brightness.
     * @param filterAddColor  Int color value to add to RGB values when changing the image's
     *                        brightness.
     * @throws IOException Thrown when the image's metadata cannot be inserted into
     *                     {@link MediaStore}.
     */
    private void saveAlteredPhoto(File photoFile, int filterMultColor, int filterAddColor)
            throws IOException {

        // Read original image.
        Bitmap origBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), null);

        // Create a new origBitmap with the same dimensions as the original.
        Bitmap alteredBitmap = Bitmap.createBitmap(origBitmap.getWidth(), origBitmap.getHeight(),
                origBitmap.getConfig());

        // Draw original origBitmap on canvas and apply the color filter.
        Canvas canvas = new Canvas(alteredBitmap);
        Paint paint = new Paint();
        LightingColorFilter colorFilter = new LightingColorFilter(filterMultColor, filterAddColor);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(origBitmap, 0, 0, paint);

        // Create an entry for the MediaStore.
        ContentValues imageValues = new ContentValues();
        imageValues.put(MediaStore.MediaColumns.DISPLAY_NAME, photoFile.getName());
        imageValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        imageValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        // Insert a new row into the MediaStore.
        ContentResolver resolver = mActivityContext.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues);
        OutputStream outStream = null;

        try {
            if (uri == null) {
                throw new IOException("Failed to insert MediaStore row");
            }

            // Save the image using the URI.
            outStream = resolver.openOutputStream(uri);
            alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    /**
     * Called to invoke a {@link SaveImageCallback#onComplete(boolean)} method with some result.
     *
     * @param callback {@link SaveImageCallback} to invoke.
     * @param result   The result to pass.
     */
    private void notifyResult(SaveImageCallback callback, boolean result) {
        // Call onComplete() on the main thread.
        mHandler.post(() -> callback.onComplete(result));
    }
}