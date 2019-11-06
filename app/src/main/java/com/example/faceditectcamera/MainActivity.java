package com.example.faceditectcamera;

// ---ライブラリの読み込み---


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {
    // ---変数の宣言---
    private CameraBridgeViewBase m_cameraView;
    // private static final String FILE_NAME = "haarcascade_frontalface_alt.xml";
    private static final String FILE_NAME = "haarcascade_frontalface_default.xml";
    protected CascadeClassifier cascadeClassifier;

    // ---OpenCVの読み込み---
    static {
        System.loadLibrary("opencv_java4");
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

            // カスケードファイルの読み込み
            File file = new File(getFilesDir().getPath() + File.separator + FILE_NAME);
            if (!file.exists()){
                try (InputStream inputStream = getAssets().open(FILE_NAME);
                     FileOutputStream fileOutputStream = new FileOutputStream(file, false)){
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // カスケード器の作成
            cascadeClassifier = new CascadeClassifier(file.getAbsolutePath());

        }
    };


    // ---メインの関数---
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissionCamera(this);

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), loaderCallback);
        } else {
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        m_cameraView = findViewById(R.id.camera_view);
        m_cameraView.setCameraPermissionGranted();
        m_cameraView.setCvCameraViewListener(this);
        m_cameraView.setCameraIndex(1);
        m_cameraView.enableView();
    }

    public static boolean getPermissionCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(activity, permissions, 0);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        MatOfRect matOfRect = new MatOfRect();
        cascadeClassifier.detectMultiScale(inputFrame, matOfRect);

        if (matOfRect.toArray().length > 0) {
            for (Rect rect : matOfRect.toArray()) {
                Imgproc.rectangle(inputFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 5);
            }
        }
        return inputFrame;
    }
}
