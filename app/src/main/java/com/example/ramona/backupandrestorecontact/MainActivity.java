package com.example.ramona.backupandrestorecontact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private Button mBtnBackup;
    private TextView mTotalContact;
    private Cursor mCursor;
    private String mVFile;
    private String mStorageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControl();
        initData();
        initEvent();
        mVFile = "Contacts" + "_" + System.currentTimeMillis() + ".vcf";
        mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        int count = mCursor.getCount();
        mTotalContact.setText(getString(R.string.total_contact_count, count));
        checkPermissionWriteContact();
    }

    private void initControl() {
        mBtnBackup = findViewById(R.id.btn_backup);
        mTotalContact = findViewById(R.id.tv_total_contact);
    }

    private void initData() {

    }

    private void initEvent() {
        mBtnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Backup Contact");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "File backup contact");
                File file = new File(mStorageFilePath);
                if (!file.exists() || !file.canRead()) {
                    return;
                }
                Uri uri = Uri.fromFile(file);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
            }
        });
    }

    private void checkPermissionWriteContact() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getVCF();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    public void getVCF() {

        mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++) {
            String lookupKey = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);

            AssetFileDescriptor fd;
            try {
                fd = getContentResolver().openAssetFileDescriptor(uri, "r");
                FileInputStream fis = fd.createInputStream();
                byte[] buf = new byte[(int) fd.getDeclaredLength()];
                fis.read(buf);
                String VCard = new String(buf);
                mStorageFilePath = Environment.getExternalStorageDirectory().toString() + File.separator + mVFile;
                FileOutputStream mFileOutputStream = new FileOutputStream(mStorageFilePath, true);
                mFileOutputStream.write(VCard.toString().getBytes());
                mCursor.moveToNext();
                Log.d("Vcard", VCard);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getVCF();
                } else {
                    Toast.makeText(this, "Permission Denied !", Toast.LENGTH_SHORT).show();
                }
        }
    }
}
