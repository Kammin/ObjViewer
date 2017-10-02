package kamin.com.objviewer;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    Context context;
    FloatBuffer floatBuffer;
    File file;
    public Loader(Context context){
        this.context = context;
    };

    private void writeToFile(FloatBuffer floatBuffer, File file) {
        file.delete();
        try {
            RandomAccessFile aFile = new RandomAccessFile(file, "rw");
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floatBuffer.capacity() * 4).order(ByteOrder.nativeOrder());
            byteBuffer.asFloatBuffer().put(floatBuffer);
            FileChannel channel = aFile.getChannel();
            channel.write(byteBuffer);
            channel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FloatBuffer LoadBuff(File file) {
            try {
                RandomAccessFile rFile = new RandomAccessFile(file, "rw");
                if (file.exists()) {
                    FileChannel inChannel = rFile.getChannel();
                    ByteBuffer buf_in = ByteBuffer.allocateDirect((int) file.length()).order(ByteOrder.nativeOrder());
                    inChannel.read(buf_in);
                    buf_in.rewind();
                    FloatBuffer result = buf_in.asFloatBuffer();
                    inChannel.close();
                    return result;
                } else
                    Log.d("File1 ", "not exist " + file.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public File[] parse(int fileRes) {
        File[] files = new File[3];
        String fileEntryName = context.getResources().getResourceEntryName(fileRes);
        Log.d("COUNT ", "START");
        List<String> vList = new ArrayList<>();
        List<String> vtList = new ArrayList<>();
        List<String> vnList = new ArrayList<>();
        String[] lineArray;
        InputStream inputStream = context.getResources().openRawResource(fileRes);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        long start = System.currentTimeMillis();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    lineArray = line.substring(2).trim().split(" ");
                    for (String s : lineArray) {
                        vList.add(s);
                    }
                }
                if (line.startsWith("vt ")) {
                    lineArray = line.substring(3).trim().split(" ");
                    for (String s : lineArray) {
                        vtList.add(s);
                    }
                }
                if (line.startsWith("vn ")) {
                    lineArray = line.substring(3).trim().split(" ");
                    for (String s : lineArray) {
                        vnList.add(s);
                    }
                }
                if (line.startsWith("f ")) {
                    lineArray = line.substring(2).trim().split(" ");
                    for (String s : lineArray) {
                    }
                }

            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Log.d("COUNT ", "START V");
        floatBuffer = FloatBuffer.allocate(vList.size());
        for (int i = 0; i < vList.size(); i++) {
            floatBuffer.put(i, Float.valueOf(vList.get(i)));
        }
        file = new File(context.getFilesDir() + File.separator + fileEntryName+".v");
        writeToFile(floatBuffer, file);
        files[0]=file;

        Log.d("COUNT ", "START VN");
        floatBuffer = FloatBuffer.allocate(vnList.size());
        float[] vn = new float[vnList.size()];
        for (int i = 0; i < vnList.size(); i++) {
            vn[i] = Float.valueOf(vnList.get(i));
        }
        file = new File(context.getFilesDir() + File.separator + fileEntryName+".vn");
        writeToFile(floatBuffer, file);
        files[1]=file;


        Log.d("COUNT ", "START VT");
        floatBuffer = FloatBuffer.allocate(vtList.size());
        float[] vt = new float[vtList.size()];
        for (int i = 0; i < vtList.size(); i++) {
            floatBuffer.put(i, Float.valueOf(vtList.get(i)));
        }
        file = new File(context.getFilesDir() + File.separator + fileEntryName+".vt");
        writeToFile(floatBuffer, file);
        files[2]=file;

        Log.d("COUNT ", "END");
      return  files;
    }

}
