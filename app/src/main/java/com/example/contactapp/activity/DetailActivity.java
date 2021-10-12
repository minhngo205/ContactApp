package com.example.contactapp.activity;

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
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.R;
import com.example.contactapp.databinding.ActivityDetailBinding;
import com.example.contactapp.model.Contact;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    private ActivityDetailBinding binding;
    private Contact data;
    byte[] imageBytes;

    IContactAidlInterface dbAidl;
    private static final String TARGET_PACKAGE =
            "com.example.contactapp";
    private static final String TARGET_CLASS =
            "com.example.contactapp.service.ContactService";

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(DetailActivity.this, "Connect Success", Toast.LENGTH_SHORT).show();
            dbAidl = IContactAidlInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            dbAidl = null;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_detail);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        View mainView = binding.getRoot();
        setContentView(mainView);

        ConnectService();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent rcvIntent = getIntent();
        if(rcvIntent!=null){
            data = rcvIntent.getParcelableExtra("data");
            imageBytes = rcvIntent.getByteArrayExtra("image");
//            Log.d(TAG, "onCreate: "+data.getName());
//            Log.d(TAG, "onCreate: "+data.getEmail());
//            Log.d(TAG, "onCreate: "+data.getMobile());
//            Log.d(TAG, "onCreate: "+ Arrays.toString(data.getPicture()));
        }


        Log.d(TAG, "onCreate: "+ Arrays.toString(imageBytes));
        if(imageBytes!=null){
            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            binding.profileImage.setImageBitmap(bmp);
//            binding.profileImage.setImageURI(Uri.parse(data.getPicture()));
        }
        binding.setContact(data);

        outEditMode();

        binding.btnEdit.setOnClickListener(view -> inEditMode());

        binding.btnCanceledit.setOnClickListener(view -> outEditMode());

        binding.btnSave.setOnClickListener(view -> {
            String newName = binding.etName.getText().toString();
            String newPhone = binding.etNumber.getText().toString();
            String newEmail = binding.etEmail.getText().toString();
            if(newName.trim().isEmpty()){
                binding.etName.setError("Cần có tên");
                binding.etName.requestFocus();
                return;
            }
            if(newPhone.trim().isEmpty()){
                binding.etNumber.setError("Cần có số điện thoại");
                binding.etNumber.requestFocus();
                return;
            }

            if(data.getName().equals(newName) && data.getMobile().equals(newPhone) && data.getEmail().equals(newEmail) && Arrays.equals(imageBytes, imageViewtoByte(binding.profileImage))){
                Toast.makeText(DetailActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();
            } else {
                imageBytes = imageViewtoByte(binding.profileImage);
                try {
                    data.setName(newName);
                    data.setMobile(newPhone);
                    data.setEmail(newEmail);
                    data.setPicture(imageBytes);
                    dbAidl.editContact(data);
                    binding.setContact(data);
                    outEditMode();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.profileImage.setOnTouchListener((view, motionEvent) -> {
            inEditMode();
            if(checkAndRequestPermissions(DetailActivity.this)){
                chooseImage(DetailActivity.this);
            }
            return true;
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                this.finish();
                return true;
            }
            case R.id.action_delete:{
                try {
                    dbAidl.deleteContact(data);
                    Toast.makeText(getApplicationContext(), "Deleted Contact: "+data.getName(), Toast.LENGTH_SHORT).show();
                    this.finish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void inEditMode(){
        binding.btnSave.setVisibility(View.VISIBLE);
        binding.btnCanceledit.setVisibility(View.VISIBLE);
        binding.btnEdit.setVisibility(View.INVISIBLE);

        enableEditText(binding.etName);
        enableEditText(binding.etNumber);
        enableEditText(binding.etEmail);
    }

    private void outEditMode(){
        binding.btnSave.setVisibility(View.INVISIBLE);
        binding.btnCanceledit.setVisibility(View.INVISIBLE);
        binding.btnEdit.setVisibility(View.VISIBLE);

        disableEditText(binding.etName);
        disableEditText(binding.etNumber);
        disableEditText(binding.etEmail);
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
    }

    private void enableEditText(EditText editText) {
        editText.setFocusableInTouchMode(true);
    }

    private void ConnectService() {
        Intent serviceIntent = new Intent("BIND_SERVICE");
        serviceIntent.setClassName(TARGET_PACKAGE, TARGET_CLASS);
        bindService(serviceIntent,conn,BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.detail_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
                if (ContextCompat.checkSelfPermission(DetailActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT)
                            .show();
                } else if (ContextCompat.checkSelfPermission(DetailActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Your Storage.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    chooseImage(DetailActivity.this);
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
                        binding.profileImage.setImageBitmap(selectedImage);
//                        imageBytes = imageViewtoByte(binding.profileImage);
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
                                binding.profileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//                                imageBytes = imageViewtoByte(binding.profileImage);
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