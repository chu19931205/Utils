package com.example.ck.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    // 拍照按钮
    private Button take_photo;
    // 显示裁剪后的图片
    private ImageView photo_iv;
    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;
    private Uri cropUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        take_photo = findViewById(R.id.button);
        photo_iv = findViewById(R.id.imageView);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 小于6.0版本直接操作

                setAvatar();

            }
        });
    }

    private void setAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("設置頭像");
        String[] items = {"選擇本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                cropUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg"));
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE: // 拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
                        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            ImageUtils.startPhotoZoom(MainActivity.this, tempUri, cropUri);
        }
        if (requestCode == CHOOSE_PICTURE && resultCode == RESULT_OK) {
            ImageUtils.startPhotoZoom(MainActivity.this, data.getData(), cropUri);
        }
        if (requestCode == 2) {
            try {
                Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri));
                photo_iv.setImageBitmap(photo);
                final String localPath = ImageUtils.savePhoto(photo, Environment.getExternalStorageDirectory().getAbsolutePath(), System.currentTimeMillis() + ".jpg");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImageUtils.uploadImg(localPath, "http://192.168.1.117/upload_file.php");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


}
