package com.example.myfile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.util.Objects;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements UploadCallBacks {
    Button upload, next;
    ImageView imageView;
    public static final String uploadUrl = "http://192.168.0.107:8090/fileupload/upload.php";
    IUploadAPI mservice;
    public static final int REQUEST_PERMISSION = 1000;
    private int FILE_PICK_REQUEST = 1111;
    Uri selectedFileUri;
    ProgressDialog dialog;

    private IUploadAPI getAPIUpload() {
        return RetrofitClient.getClient(uploadUrl).create(IUploadAPI.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mservice = getAPIUpload();


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }

        upload = findViewById(R.id.upload);
        next = findViewById(R.id.next);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChoooose();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadFile();
            }
        });

    }

    private void UploadFile() {
        if (selectedFileUri != null) {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMax(100);
            dialog.setMessage("Its loading....");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            File file = FileUtils.getFile(this, selectedFileUri);
            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mservice.uploadFile(body).enqueue(new Callback<String>()  {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            dialog.dismiss();
                            Log.e("TAG1", "onResponse: "+response.message().toString());
                            Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            dialog.dismiss();
                            Log.e("TAG2", "onResponse: "+ t.getMessage());
                            Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }

    private void FileChoooose() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),FILE_PICK_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ff", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_PICK_REQUEST) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    if (selectedFileUri != null && !Objects.requireNonNull(selectedFileUri.getPath()).isEmpty()) {
                        Log.e("TAG11", "onActivityResult..........: "+selectedFileUri );
                       // imageView.setImageURI(selectedFileUri);
                    } else {
                        Log.e("TAG22", "onActivityResult: "+selectedFileUri );
                        Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        dialog.setProgress(percentage);
    }
}