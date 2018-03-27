package com.example.ck.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CY-ChuKuang on 2018/3/27.
 */

public class ImageUtils {
    /**
     *
     * @param photoBitmap bitmap图片
     * @param path 本地保存路径
     * @param photoName 保存文件名
     * @return
     */
    public static String savePhoto(Bitmap photoBitmap, String path, String photoName) {
        String localPath = null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".jpg");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) { // 转换完成
                        localPath = photoFile.getPath();
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }

    /**
     *
     * @param filePath 图片本地路径
     * @param httpUrl 上传地址
     * @return
     * @throws Exception
     */
    public static String uploadImg(String filePath, String httpUrl) throws Exception {
        File imgFile = new File(filePath);
        URL url = new URL(httpUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----123456789");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStream os = new DataOutputStream(conn.getOutputStream());
        StringBuilder body = new StringBuilder();
        body.append("------123456789\r\n");
        body.append("Content-Disposition: form-data; name='img'; filename='" + imgFile.getName() + "'\r\n");
        body.append("Content-Type: image/jpeg\r\n\r\n");
        os.write(body.toString().getBytes());
        InputStream is = new FileInputStream(imgFile);
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = is.read(b)) != -1) {
            os.write(b, 0, len);
        }
        String end = "\r\n------123456789--";
        os.write(end.getBytes());
        // 输出返回结果
        InputStream input = conn.getInputStream();
        byte[] res = new byte[1024];
        int resLen = input.read(res);
        os.close();
        is.close();
        input.close();
        return new String(res, 0, resLen);
    }

    /**
     *
     * @param context
     * @param uri 裁剪前原始Uri
     * @param cropUri 裁剪后保存Uri
     */
    public static void startPhotoZoom(Activity context, Uri uri, Uri cropUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        // 这段代码判断，在安卓7.0以前版本是不需要的。特此注意。不然这里也会抛出异常
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        intent.putExtra("scale", true);

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        context.startActivityForResult(intent, 2);
    }
}
