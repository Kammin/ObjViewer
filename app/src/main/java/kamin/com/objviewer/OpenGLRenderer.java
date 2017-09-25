package kamin.com.objviewer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.nio.FloatBuffer;

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
    private FloatBuffer vertexData;

    private int uColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;

    private float[] projectionMatrix = new float[16];
    private float[] cameraMatrix = new float[16];
    private float[] resultMatrix = new float[16];



    public OpenGLRenderer(Context context) {
        this.context = context;
        loader = new Loader(context);
        prepareData();
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(0f, 0f, 0f, 1f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        programId = Utils.createProgram(context, R.raw.vertex_shader, R.raw.fragment_shader);
        glUseProgram(programId);
        prepareData();
        bindData();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES30.glViewport(0, 0, i, i1);
        createProjectionMatrix(i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        createViewMatrix();
        bindMatrix();
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT |GLES30.GL_COLOR_BUFFER_BIT);

        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 3);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 1, 4);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 2, 5);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 3, 6);

        glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 4, 7);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 5, 8);

        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 6, 9);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 7, 10);

/*
        // оси
        GLES30.glLineWidth(1);

        glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_LINES, 12, 2);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_LINES, 14, 2);

        glUniform4f(uColorLocation, 1.0f, 0.5f, 0.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_LINES, 16, 2);
*/

    }

    private void bindData() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT,
                true, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color");

        // матрица
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");
    }

    private void prepareData() {
/*        float s = 0.4f;
        float d = 0.9f;
        float l = 3;

        float[] vertices = {

                // первый треугольник
                -2*s, -s, d,
                2*s, -s, d,
                0, s, d,

                // второй треугольник
                -2*s, -s, -d,
                2*s, -s, -d,
                0, s, -d,

                // третий треугольник
                d, -s, -2*s,
                d, -s, 2*s,
                d, s, 0,

                // четвертый треугольник
                -d, -s, -2*s,
                -d, -s, 2*s,
                -d, s, 0,

                // ось X
                -l, 0,0,
                l,0,0,

                // ось Y
                0,-l,0,
                0,l,0,

                // ось Z
                0,0,-l,
                0,0,l,
        };

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);*/
        File file = loader.parse(R.raw.cube)[0];
        vertexData = loader.LoadBuff(file);
        for(int i =0;i<vertexData.capacity();i++){
            Log.d("vertexData"," "+i+" "+vertexData.get(i));
        }

    }

    private void createViewMatrix() {
        // точка положения камеры
        float coef = (float)(SystemClock.uptimeMillis() % 10000) / 10000;
        float angle = coef  *  2 * 3.1415926f;
        float eyeX = (float) (Math.cos(angle) * 4f);
        float eyeY = 3;
        float eyeZ = (float) (Math.sin(angle) * 4f);
        Log.d("motionEvent",""+coef);

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
}
