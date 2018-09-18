/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.drive.sample.quickstart;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it in Google Drive. The
 * user is prompted with a pre-made dialog which allows them to choose the file location.
 */
public class MainActivity extends Activity {

  private static final String TAG = "drive-quickstart";
  private static final int REQUEST_CODE_SIGN_IN = 0;
  private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
  private static final int REQUEST_CODE_CREATOR = 2;
  private static  boolean PAUSE =true;
  private static  boolean PLAYING =false;


  private DriveClient mDriveClient;
  private DriveResourceClient mDriveResourceClient;
  private Bitmap mBitmapToSave;

  ImageView button;
  ImageView downlaod;
  TextView filename;

  ImageView button1;
  ImageView downlaod1;
  TextView filename1;
  MediaPlayer mediaPlayer;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      button = (ImageView) findViewById(R.id.playbutton);
      filename = (TextView) findViewById(R.id.filename);
      downlaod =(ImageView)findViewById(R.id.upload);
    button1 = (ImageView) findViewById(R.id.playbutton1);
    filename1 = (TextView) findViewById(R.id.filename1);
    downlaod1 =(ImageView)findViewById(R.id.upload1);
    filename.setText("tone.mp3");
    filename1.setText("done.mp3");

    PackageManager pm = this.getPackageManager();
    int hasPerm = pm.checkPermission(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            this.getPackageName());
    int hasPerm1 = pm.checkPermission(
            Manifest.permission.INTERNET,
            this.getPackageName());
    if (hasPerm != PackageManager.PERMISSION_GRANTED && hasPerm1 != PackageManager.PERMISSION_GRANTED ) {
      // do stuff
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET},
              1);
    }

    signIn();

  }

  /** Start sign in activity. */
  private void signIn() {
    Log.i(TAG, "Start sign in");
    GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
    startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
  }

  /** Build a Google SignIn client. */
  private GoogleSignInClient buildGoogleSignInClient() {
    GoogleSignInOptions signInOptions =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE)
            .build();
    return GoogleSignIn.getClient(this, signInOptions);
  }

  /** Create a new file and save it to Drive. */
  private void saveFileToDrive(int id) {
    // Start by creating a new contents, and setting a callback.
    Log.i(TAG, "Creating new contents.");
    final Bitmap image =  mBitmapToSave;

    mDriveResourceClient
        .createContents()
        .continueWithTask(
                task -> createFileIntentSender(task.getResult(),id))
        .addOnFailureListener(
                e -> Log.w(TAG, "Failed to create new contents.", e));
  }

  /**
   * Creates an {@link IntentSender} to start a dialog activity with configured {@link
   * CreateFileActivityOptions} for user to create a new photo in Drive.
   */
  private Task<Void> createFileIntentSender(DriveContents driveContents,int id) {
    Log.i(TAG, "New contents created.");
    // Get an output stream for the contents.
    OutputStream outputStream = driveContents.getOutputStream();
    // Write the bitmap data from it.
    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//    image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
    InputStream inStream;
   if(id == R.id.playbutton){
      inStream = getResources().openRawResource(R.raw.bird);
   }else {
      inStream = getResources().openRawResource(R.raw.don);
   }
    try {
      byte[] music = IOUtils.toByteArray(inStream);

      outputStream.write(music);
    } catch (IOException e) {
      e.printStackTrace();
      Log.w(TAG, "Unable to write file contents.", e);
    }


//    try {
//      outputStream.write(bitmapStream.toByteArray());
//    } catch (IOException e) {
//      Log.w(TAG, "Unable to write file contents.", e);
//    }

    // Create the initial metadata - MIME type and title.
    // Note that the user will be able to change the title later.
    MetadataChangeSet metadataChangeSet =
        new MetadataChangeSet.Builder()
            .setMimeType("audio/mpeg")
            .setTitle("Audio file")
            .build();
    // Set up options to configure and display the create file activity.
    CreateFileActivityOptions createFileActivityOptions =
        new CreateFileActivityOptions.Builder()
            .setInitialMetadata(metadataChangeSet)
            .setInitialDriveContents(driveContents)
            .build();

    return mDriveClient
        .newCreateFileActivityIntentSender(createFileActivityOptions)
        .continueWith(
                task -> {
                  startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                  return null;
                });
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CODE_SIGN_IN:
        Log.i(TAG, "Sign in request code");
        // Called after user is signed in.
        if (resultCode == RESULT_OK) {
          Log.i(TAG, "Signed in successfully.");
          // Use the last signed in account here since it already have a Drive scope.
          mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
          // Build a drive resource client.
          mDriveResourceClient =
              Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
          // Start camera.
//          startActivityForResult(
//              new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);

        }
        break;
      case REQUEST_CODE_CAPTURE_IMAGE:
        Log.i(TAG, "capture image request code");
        // Called after a photo has been taken.
        if (resultCode == Activity.RESULT_OK) {
          Log.i(TAG, "Image captured successfully.");
          // Store the image data as a bitmap for writing later.
          mBitmapToSave = (Bitmap) data.getExtras().get("data");

        }
        break;
      case REQUEST_CODE_CREATOR:
        Log.i(TAG, "creator request code");
        // Called after a file is saved to Drive.
        if (resultCode == RESULT_OK) {
          Log.i(TAG, "Image successfully saved.");
          Toast.makeText(this,"Audio successfully uploaded.",Toast.LENGTH_SHORT).show();
          mBitmapToSave = null;
          // Just start the camera again for another photo.
//          startActivityForResult(
//              new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
        }
        break;
    }
  }

    public void playAudio(View view) {

      if(PAUSE){
        PAUSE=false;

        if(view.getId() == R.id.playbutton && !PLAYING){
          button.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
          mediaPlayer= MediaPlayer.create(this,R.raw.bird);
          mediaPlayer.start();
          PLAYING =true;
        }else {
          button1.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
          mediaPlayer= MediaPlayer.create(this,R.raw.don);
          mediaPlayer.start();
          PLAYING =true;
        }
      }
      else {
        PAUSE = true;
        PLAYING= false;
        if(view.getId() == R.id.playbutton){
          button.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        }else {
          button1.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        }
        mediaPlayer.stop();
        mediaPlayer.release();

      }


    }

    public void upload(View view) {
        saveFileToDrive(view.getId());
    }
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 1: {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          // permission was granted, yay! Do the
          // contacts-related task you need to do.
          Toast.makeText(MainActivity.this, "Permission granted ", Toast.LENGTH_SHORT).show();

        } else {

          // permission denied, boo! Disable the
          // functionality that depends on this permission.
          Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
        }

      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }
}
