package kamin.com.objviewer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    OpenGLRenderer openGLRenderer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openGLRenderer = new OpenGLRenderer(this);
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float start = 0f;
                if(motionEvent.getAction()==MotionEvent.ACTION_POINTER_DOWN){
                    start = motionEvent.getX();
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_MOVE){

                }
                return true;
            }
        };
        if (!supportES3()) {
            Toast.makeText(this, "OpenGl ES 3.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(openGLRenderer);
        glSurfaceView.setOnTouchListener(onTouchListener);
        setContentView(glSurfaceView);
    }

    private boolean supportES3() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
