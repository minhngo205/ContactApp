package com.example.contactapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.R;
import com.example.contactapp.databinding.ActivityAddBinding;
import com.example.contactapp.databinding.ActivityMainBinding;
import com.example.contactapp.model.Contact;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";

    IContactAidlInterface dbAidl;
    private static final String TARGET_PACKAGE =
            "com.example.contactapp";
    private static final String TARGET_CLASS =
            "com.example.contactapp.service.ContactService";

    ActivityAddBinding activityAddBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        activityAddBinding = ActivityAddBinding.inflate(getLayoutInflater());
        View viewRoot = activityAddBinding.getRoot();
        setContentView(viewRoot);

        ConnectService();

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
            Contact insertContact = new Contact(
                    newName,
                    newPhone,
                    newEmail
            );
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
}