package kamin.com.objviewer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    Loader loader;

    private int programId;
    private FloatBuffer vertexData, vertexAxisData;
    private ShortBuffer sb;
    private int uColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;

    private float[] projectionMatrix = new float[16];
    private float[] cameraMatrix = new float[16];
    private float[] resultMatrix = new float[16];


    public OpenGLRenderer(Context context) {
        this.context = context;
        loader = new Loader(context);
    }


    private void bindData() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT,
                false, 0, vertexData);

        // цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color");

        // матрица
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");
    }

    private void prepareData() {
        float l = 3;
        float[] verticesAxis = {
                // ось X
                -l, 0, 0,
                l, 0, 0,
                // ось Y
                0, -l, 0,
                0, l, 0,
                // ось Z
                0, 0, -l,
                0, 0, l,
        };
        vertexAxisData = ByteBuffer
                .allocateDirect(verticesAxis.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexAxisData.put(verticesAxis);

        File[] files = loader.parse(R.raw.cube);
        vertexData = loader.LoadFloatBuffer(files[0]);
        sb = loader.LoadShortBuffer(files[3]);

        for (int i = 0; i < sb.capacity(); i++) {
            Log.d("vertexData", " " + i + " " + sb.get(i));
        }

    }

    public void createViewMatrix() {
        // точка положения камеры
        float coef = (float) (SystemClock.uptimeMillis() % 10000) / 10000;
        float angle = coef * 2 * 3.1415926f;
        float eyeX = (float) (Math.cos(angle) * 4f);
        float eyeY = 3;
        float eyeZ = (float) (Math.sin(angle) * 4f);

        // точка направления камеры
        float centerX = 0;
        float centerY = 0;
        float centerZ = 0;

        // up-вектор
        float upX = 0;
        float upY = 1;
        float upZ = 0;

        Matrix.setLookAtM(cameraMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    private void createProjectionMatrix(int width, int height) {
        float ratio = 1;
        float left = -1;
        float right = 1;
        float bottom = -1;
        float top = 1;
        float near = 2;
        float far = 8;
        if (width > height) {
            ratio = (float) width / height;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) height / width;
            bottom *= ratio;
            top *= ratio;
        }

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }


    private void bindMatrix() {
        Matrix.multiplyMM(resultMatrix, 0, projectionMatrix, 0, cameraMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, resultMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        programId = Utils.createProgram(context, R.raw.vertex_shader, R.raw.fragment_shader);
        glUseProgram(programId);
        prepareData();
        bindData();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
        createProjectionMatrix(i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        createViewMatrix();
        bindMatrix();
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        glEnableVertexAttribArray(aPositionLocation);

        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        //short[] sh = new short[]{1,2,3, 3,2,4, 3,4,5, 5,4,6, 5,6,7, 7,6,8, 7,8,1, 1,8,2, 2,8,4, 4,8,6, 7,1,5, 5,1,3};
        //short[] sh =   new short[]{1,2,3, 3,2,4, 3,4,5, 5,4,6, 5,6,7, 7,6,8, 7,8,1, 1,8,2, 2,8,4, 4,8,6, 7,1,5, 5,1,3};
        //short[] sh = new short[]{1, 2, 3, 3, 2, 4, 3, 4, 5, 5, 4, 6, 5, 6, 7, 7, 6, 8, 7, 8, 1, 1, 8, 2, 2, 8, 4, 4, 8, 6, 7, 1, 5, 5, 1, 3};
/*        for (int i = 0; i < sh.length; i++) {
            sh[i] -= 1;
        }*/
        glUniform4f(uColorLocation, 0.8f, 0.8f, 0.8f, 0.3f);
        //ShortBuffer sb = ShortBuffer.wrap(sh);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, sb.capacity(), GLES20.GL_UNSIGNED_SHORT, sb);
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        //GLES20.glDrawElements(GLES20.GL_LINES, 4, GLES20.GL_UNSIGNED_SHORT, sb);
        //  }
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 1, 4);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 2, 5);
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 4, 7);

        glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 4, 7);
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 5, 8);


    }
}
