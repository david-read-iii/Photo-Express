package com.davidread.photoexpress;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * {@link MainActivity} represents a user interface where a snapped image's brightness may be
 * modified and saved to the device's external storage.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * {@link File} referencing the external storage image taken using the camera.
     */
    private File mPhotoFile;

    /**
     * {@link ImageView} to display an image preview to the user.
     */
    private ImageView mPhotoImageView;

    /**
     * {@link SeekBar} for modifying the brightness of the image.
     */
    private SeekBar mSeekBar;

    /**
     * {@link Button} for saving the altered image to the device's external storage.
     */
    private Button mSaveButton;

    /**
     * TODO: Document when {@link #changeBrightness(int)} is implemented
     */
    private int mMultColor = 0xffffffff;

    /**
     * TODO: Document when {@link #changeBrightness(int)} is implemented
     */
    private int mAddColor = 0;

    /**
     * Invoked once when this activity is created. It initializes member variables.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhotoImageView = findViewById(R.id.photo);

        mSaveButton = findViewById(R.id.saveButton);
        mSaveButton.setEnabled(false);

        mSeekBar = findViewById(R.id.brightnessSeekBar);
        mSeekBar.setVisibility(View.INVISIBLE);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Invoked when the "Take Photo" {@link Button} is clicked. It launches the device's camera to
     * take a photo to be altered within this activity.
     */
    public void takePhotoClick(View view) {
        // Create the File for saving the photo.
        mPhotoFile = createImageFile();

        // Create a content URI to grant camera app write permission to mPhotoFile.
        Uri photoUri = FileProvider.getUriForFile(this,
                "com.zybooks.photoexpress.fileprovider", mPhotoFile);

        // Start camera app.
        mTakePicture.launch(photoUri);
    }

    /**
     * {@link ActivityResultLauncher} that specifies what to do when the camera intent started in
     * {@link #takePhotoClick(View)} finishes. For successful finishes, it displays the snapped
     * photo thumbnail in {@link #mPhotoImageView} and displays UI to alter the photo.
     */
    private final ActivityResultLauncher<Uri> mTakePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    displayPhoto();
                    mSeekBar.setProgress(100);
                    mSeekBar.setVisibility(View.VISIBLE);
                    mSaveButton.setEnabled(true);
                }
            });

    /**
     * Returns a {@link File} referring to an empty file in the device's external storage that may
     * be populated with an image.
     *
     * @return A {@link File} referring to an empty file on the device's external storage.
     */
    private File createImageFile() {
        // Create a unique image filename.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFilename = "IMG_" + timeStamp + ".jpg";

        // Get file path where the app can save a private image.
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFilename);
    }

    /**
     * Displays a thumbnail in {@link #mPhotoImageView} of the image snapped by the camera intent
     * started in {@link #takePhotoClick(View)}.
     */
    private void displayPhoto() {
        // Get ImageView dimensions.
        int targetWidth = mPhotoImageView.getWidth();
        int targetHeight = mPhotoImageView.getHeight();

        // Get bitmap dimensions.
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath(), bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        // Determine how much to scale down the image.
        int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

        // Decode the image file into a smaller bitmap that fills the ImageView.
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath(), bmOptions);

        // Display smaller bitmap.
        mPhotoImageView.setImageBitmap(bitmap);
    }

    private void changeBrightness(int brightness) {
        // TODO: Change brightness
    }

    public void savePhotoClick(View view) {
        // TODO: Save the altered photo
    }
}