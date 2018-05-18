package com.csci4140.project.manualcamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mCamera {
    private Context mainContext;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private TextureView textureView;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private int isStopPreview = 0;
    int oldIso=100;
    long oldSs=20000000;
    int previewMode = 0;
    int oldScene = 1;
    Integer fNo = 1;

    public void initCame(Context iContext,TextureView mtextureView) {
        mainContext = iContext;
        textureView = mtextureView;
        mtextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    CameraManager manager = (CameraManager) mainContext.getSystemService(Context.CAMERA_SERVICE);
                    cameraId = manager.getCameraIdList()[0];
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    imageDimension = map.getOutputSizes(SurfaceTexture.class)[0]; // need to modify to custom sizes
                    if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    manager.openCamera(cameraId, stateCallback, null);

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Thread");
        mBackgroundThread.start();
        mBackgroundHandler = new android.os.Handler(mBackgroundThread.getLooper());
    }

    void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview(0,100,20000000,oldScene);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    void closeCamera() {
        if(cameraDevice!=null)
        {
            cameraDevice.close();
            cameraDevice = null;
        }
        if(imageReader!=null)
        {
            imageReader.close();
            imageReader=null;
        }
    }

    public void createCameraPreview(int previewMode,int niso,long nss,int scene) {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1280, 720);
            Surface surface = new Surface(texture);

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            if (scene==2) {
                previewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CameraMetadata.CONTROL_EFFECT_MODE_MONO);
            }
            if (previewMode==1) {
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                previewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,oldSs);
                previewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,oldIso);
            }
            else if (previewMode==2) {
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                previewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,oldSs);
            }
            else if (previewMode==3) {
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                previewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,oldIso);
            }
            else if (previewMode==4) {
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON);
                previewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,-6);
            }
            else if (previewMode==5) {
                previewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CameraMetadata.CONTROL_EFFECT_MODE_MONO);
            }
            else if (previewMode>6) {
                previewMode = previewMode - 6;
                if (previewMode==1) {
                    previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    previewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,oldSs);
                    previewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,oldIso);
                    Log.e("enter mode 1 in mode 6","end");
                }
                else if (previewMode==2) {
                    previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    previewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,oldSs);
                }
                else if (previewMode==3) {
                    previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    previewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,oldIso);
                }
                else if (previewMode==4) {
                    previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON);
                    previewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,-6);
                }
                else if (previewMode==5) {
                    previewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                }
                Integer temp = oldIso;
                Log.e("enter mode 6, oldSs",temp.toString());
            }
            previewBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    try {
                        session.setRepeatingRequest(previewBuilder.build(),null,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void takePicture(int captureMode,int iso,long ss,int scene) {
        if (cameraDevice == null) {
            return;
        }
        oldIso = iso;
        oldSs = ss;
        oldScene = scene;
        CameraManager manager = (CameraManager) mainContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (cameraCharacteristics != null) {
                jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighResolutionOutputSizes(ImageFormat.JPEG);
            }
            int height;
            int width;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                height = jpegSizes[0].getHeight();
                width = jpegSizes[0].getWidth();
            }
            else {
                height = 720;
                width = 1280;
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            if (scene==2) {
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                Log.e("cam","set mono");
            }
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
            switch (captureMode) {
                case 0:
                    break;
                case 1:
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,(long) ss);
                    captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,iso);
                    break;
                case 2:
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,(long) ss);
                    break;
                case 3:
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,iso);
                    break;
                case 4:
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,-6);
                    break;
                case 5:
                    captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                    break;
                case 6:
                    break;
            }
            captureBuilder.addTarget(reader.getSurface());

            File dir = new File(Environment.getExternalStorageDirectory() + "/DCIM");
            if(dir.exists() && dir.isDirectory()) {
                Log.e("Image Directory ","exist");
            } else {
                dir.mkdir();
            }
            final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/_DSC"+fNo.toString()+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        OutputStream output = null;

                        try {
                            output = new FileOutputStream(file);
                            fNo = fNo+1;
                            output.write(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (null != output) {
                                try {
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
            };

            if(captureMode<6) {
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                isStopPreview = 0;
                previewMode = captureMode;
            }
            else {
                reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Log.e("No thing","saved");

                    }
                },mBackgroundHandler);
                isStopPreview = 1;
                previewMode = captureMode;
            }

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    createCameraPreview(previewMode,100,20000000,oldScene);
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
