package com.example.contactapp.activity;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.R;
import com.example.contactapp.databinding.ActivityAddBinding;
import com.example.contactapp.databinding.ActivityMainBinding;
import com.example.contactapp.model.Contact;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";

    IContactAidlInterface dbAidl;
    private static final String TARGET_PACKAGE =
            "com.example.contactapp";
    private static final String TARGET_CLASS =
            "com.example.contactapp.service.ContactService";

    ActivityAddBinding activityAddBinding;
    private Contact insertContact;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    private byte[] imageBytes;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        activityAddBinding = ActivityAddBinding.inflate(getLayoutInflater());
        View viewRoot = activityAddBinding.getRoot();
        setContentView(viewRoot);

        ConnectService();
        insertContact = new Contact();

        activityAddBinding.imageButtonAdd.setOnTouchListener((view, motionEvent) -> {
            if(checkAndRequestPermissions(AddActivity.this)){
                chooseImage(AddActivity.this);
            }
            return true;
        });

        activityAddBinding.btnAdd.setOnClickListener(view -> {
            String newName = activityAddBinding.editLastName.getText().toString();
            String newPhone = activityAddBinding.editNumber.getText().toString();
            String newEmail = activityAddBinding.editEmail.getText().toString();
            if(newName.trim().isEmpty()){
                activityAddBinding.editLastName.setError("Cần có tên");
                activityAddBinding.editLastName.requestFocus();
                return;
            }
            if(newPhone.trim().isEmpty()){
                activityAddBinding.editNumber.setError("Cần có số điện thoại");
                activityAddBinding.editNumber.requestFocus();
                return;
            }
            insertContact.setName(newName);
            insertContact.setMobile(newPhone);
            insertContact.setEmail(newEmail);
            insertContact.setPicture(imageBytes);
            try {
                dbAidl.insertContact(insertContact);
            } catch (RemoteException e) {
                Toast.makeText(AddActivity.this, "Cannot add to the database", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void ConnectService() {
        Intent serviceIntent = new Intent("BIND_SERVICE");
        serviceIntent.setClassName(TARGET_PACKAGE, TARGET_CLASS);
        bindService(serviceIntent,conn,BIND_AUTO_CREATE);
    }

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(AddActivity.this, "Connect Success", Toast.LENGTH_SHORT).show();
            dbAidl = IContactAidlInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            dbAidl = null;
        }
    };

    public static boolean checkAndRequestPermissions(final Activity context) {
        int WExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void chooseImage(Context context){
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Exit" }; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionsMenu[i].equals("Take Photo")){
                    // Open the camera and get the photo
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }
                else if(optionsMenu[i].equals("Choose from Gallery")){
                    // choose from  external storage
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                }
                else if (optionsMenu[i].equals("Exit")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(AddActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT)
                            .show();
                } else if (ContextCompat.checkSelfPermission(AddActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Your Storage.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    chooseImage(AddActivity.this);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        activityAddBinding.imageButtonAdd.setImageBitmap(selectedImage);
                        imageBytes = imageViewtoByte(activityAddBinding.imageButtonAdd);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                activityAddBinding.imageButtonAdd.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                imageBytes = imageViewtoByte(activityAddBinding.imageButtonAdd);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }

    private static byte[] imageViewtoByte(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }
}