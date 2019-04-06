package com.itcom202.weroom.cameraGallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Camera {
    private static final int REQUEST_IMAGE_CAPTURE = 0 ;
    private static String TAG  = "Camera";
    public static String currentPhotoPath;

    private static FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public static File dispatchTakePictureIntent(Activity activity, Fragment fragment) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = null;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                file = Camera.createImageFile(activity);
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (file  != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.itcom202.weroom.fileprovider",
                        file );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                fragment.startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
        return file;
    }



    public static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",       /* suffix */
                storageDir          /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void galleryAddPic(Activity activity) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public static void uploadFile(Bitmap[] bitmap, String[] pictureName ) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://weroom-fa6fe.appspot.com");
        for (int i = 0 ; i < bitmap.length; i++){
            if (bitmap[i] != null && pictureName[i] != null){
                StorageReference mountainImagesRef = storageRef.child("images/" + mUser.getUid()+ "/" +pictureName[i]+ ".jpg");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap[i].compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = mountainImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "Exception: " + exception);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });
            }
        }


    } public static void uploadFile(List<PictureUploader> pictures) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://weroom-fa6fe.appspot.com");
        for (PictureUploader p : pictures) {
            if (p.getPicture() != null && p.getPicturName() != null) {
                StorageReference mountainImagesRef = storageRef.child("images/" + mUser.getUid() + "/" + p.getPicturName() + ".jpg");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                p.getPicture().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = mountainImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "Exception: " + exception);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });
            }
        }
    }
}