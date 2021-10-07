package com.example.contactapp.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contactapp.IContactAidlInterface;
import com.example.contactapp.R;
import com.example.contactapp.databinding.ActivityDetailBinding;
import com.example.contactapp.model.Contact;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private Contact data;

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

            if(data.getName().equals(newName) && data.getMobile().equals(newPhone) && data.getEmail().equals(newEmail)){
                Toast.makeText(DetailActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    data.setName(newName);
                    data.setMobile(newPhone);
                    data.setEmail(newEmail);
                    dbAidl.editContact(data);
                    binding.setContact(data);
                    outEditMode();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
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


}