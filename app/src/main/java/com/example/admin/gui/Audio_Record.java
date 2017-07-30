package com.example.admin.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat; //The AudioFormat class is used to access a number of audio format and channel configuration constants. They are for instance used in AudioTrack and AudioRecord
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
An activity represents a single screen with a user interface just like window or frame of Java.
Android activity is the subclass of ContextThemeWrapper class.
*/
public class Audio_Record extends Activity {

    private static int RECORDER_SAMPLERATE;
    private static int RECORDER_CHANNELS;
    private static int RECORDER_AUDIO_ENCODING;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private static int[] mSampleRates = new int[]{44100, 22050, 11025, 8000};
    private static Button play;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //This is the first callback and called when the activity is first created.
        setContentView(R.layout.activity_audio__record); //statement loads UI components from res/layout/activity_main.xml file

        // play = (Button)findViewById(R.id.button);

        findAudioRecord(mSampleRates);
y
        setButtonHandlers();
        enableButtons(false);
    }

    private void setButtonHandlers() {
        (findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        (findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        //play.setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        (findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void StartRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        //NoiseSuppressor.create(recorder.getAudioSessionId());
        recorder.startRecording();

        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {

        String filePath = "/sdcard/wav/test2.pcm";
        //String filePath = "/storage/sdcard1/wav/test2.pcm";
        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {

            // gets the voice output from microphone to byte format
            recorder.read(sData, 0, BufferElements2Rec);
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                if (os != null) {
                    os.write(bData, 0, BufferElements2Rec * BytesPerElement);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        Intent i = new Intent(Audio_Record.this, xy_graph.class);
        startActivity(i);

    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    StartRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    stopRecording();

                    File f1 = new File("/storage/sdcard1/wav/test2.pcm"); // The location of your PCM file
                    // File f2 = new File("/sdcard/wav/test3.mp3");
                    File f2 = new File("/storage/sdcard1/wav/test3.wav");

//                    File f1 = new File("/storage/sdcard1/wav/test2.pcm"); // The location of your PCM file
//                    File f2 = new File("/storage/sdcard1/wav/test3.wav"); // The location where you want your WAV file
                    try {
                        rawToWave(f1, f2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
    };

    @SuppressLint("LongLogTag")
    public void findAudioRecord(int sampleRates[]) {

        for (int sampleRate : sampleRates) {
            try {
                Log.i("Indexing " + sampleRate + "Hz Sample Rate", "");
                int tmpBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);


                // Test the minimum allowed buffer size with this configuration on this device.
                if (tmpBufferSize != AudioRecord.ERROR_BAD_VALUE) {

                    // Seems like we have ourself the optimum AudioRecord parameter for this device.

                    AudioRecord tmpRecoder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            tmpBufferSize);

                    // Test if an AudioRecord instance can be initialized with the given parameters.
                    if (tmpRecoder.getState() == AudioRecord.STATE_INITIALIZED) {

                        RECORDER_SAMPLERATE = sampleRate;
                        RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
                        RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


                        tmpRecoder.release();
                        tmpRecoder = null;

                        return;
                    }
                } else {
                    Log.i("Incorrect buffer. Continue sweeping", "");
                }
            } catch (IllegalArgumentException e) {
                Log.i("The " + sampleRate + "Hz Sampling Rate is not supported on this device", "");
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        	
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        initTunnelPlayerWorkaround();
////        init();
//    }
//
//    @Override
//    protected void onPause() {
////        cleanUp();
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        cleanUp();
//        super.onDestroy();
//    }

//    private void init()
//    {
////        mPlayer = MediaPlayer.create(this, Uri.parse("/sdcard/wav/test3.wav"));
////        mPlayer.start();
//
//        // We need to link the visualizer view to the media player so that
//        // it displays something
//        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
////        mVisualizerView.link(mPlayer);
//
//        // Start with just line renderer
////        addLineRenderer();
//        Paint paint = new Paint();
//        paint.setStrokeWidth(50f);
//        paint.setAntiAlias(true);
//        paint.setColor(Color.argb(200, 56, 138, 252));
//        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
//        mVisualizerView.addRenderer(barGraphRendererBottom);
//
//        Paint paint2 = new Paint();
//        paint2.setStrokeWidth(12f);
//        paint2.setAntiAlias(true);
//        paint2.setColor(Color.argb(200, 181, 111, 233));
//        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
//        mVisualizerView.addRenderer(barGraphRendererTop);
//
//        paint = new Paint();
//        paint.setStrokeWidth(8f);
//        paint.setAntiAlias(true);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
//        paint.setColor(Color.argb(255, 222, 92, 143));
//        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
//        mVisualizerView.addRenderer(circleBarRenderer);
//
//        Paint linePaint = new Paint();
//        linePaint.setStrokeWidth(1f);
//        linePaint.setAntiAlias(true);
//        linePaint.setColor(Color.argb(88, 0, 128, 255));
//
//        Paint lineFlashPaint = new Paint();
//        lineFlashPaint.setStrokeWidth(5f);
//        lineFlashPaint.setAntiAlias(true);
//        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
//        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
//        mVisualizerView.addRenderer(lineRenderer);
//    }
//
//    private void cleanUp()
//    {
////        if (mPlayer != null)
////        {
////            mVisualizerView.release();
////            mPlayer.release();
////            mPlayer = null;
////        }
////
////        if (mSilentPlayer != null)
////        {
////            mSilentPlayer.release();
////            mSilentPlayer = null;
////        }
//    }
//
//    // Workaround (for Galaxy S4)
//    //
//    // "Visualization does not work on the new Galaxy devices"
//    //    https://github.com/felixpalmer/android-visualizer/issues/5
//    //
//    // NOTE:
//    //   This code is not required for visualizing default "test.mp3" file,
//    //   because tunnel player is used when duration is longer than 1 minute.
//    //   (default "test.mp3" file: 8 seconds)
//    //
//    private void initTunnelPlayerWorkaround() {
//        // Read "tunnel.decode" system property to determine
//        // the workaround is needed
//        if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(this)) {
//            mSilentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(this);
//        }
//    }
//
//    // Methods for adding renderers to visualizer
////    private void addBarGraphRenderers()
////    {
////        Paint paint = new Paint();
////        paint.setStrokeWidth(50f);
////        paint.setAntiAlias(true);
////        paint.setColor(Color.argb(200, 56, 138, 252));
////        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
////        mVisualizerView.addRenderer(barGraphRendererBottom);
////
////        Paint paint2 = new Paint();
////        paint2.setStrokeWidth(12f);
////        paint2.setAntiAlias(true);
////        paint2.setColor(Color.argb(200, 181, 111, 233));
////        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
////        mVisualizerView.addRenderer(barGraphRendererTop);
////    }
//
////    private void addCircleBarRenderer()
////    {
////        Paint paint = new Paint();
////        paint.setStrokeWidth(8f);
////        paint.setAntiAlias(true);
////        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
////        paint.setColor(Color.argb(255, 222, 92, 143));
////        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
////        mVisualizerView.addRenderer(circleBarRenderer);
////    }
//
////    private void addCircleRenderer()
////    {
////        Paint paint = new Paint();
////        paint.setStrokeWidth(3f);
////        paint.setAntiAlias(true);
////        paint.setColor(Color.argb(255, 222, 92, 143));
////        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
////        mVisualizerView.addRenderer(circleRenderer);
////    }
//
////    private void addLineRenderer()
////    {
////        Paint linePaint = new Paint();
////        linePaint.setStrokeWidth(1f);
////        linePaint.setAntiAlias(true);
////        linePaint.setColor(Color.argb(88, 0, 128, 255));
////
////        Paint lineFlashPaint = new Paint();
////        lineFlashPaint.setStrokeWidth(5f);
////        lineFlashPaint.setAntiAlias(true);
////        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
////        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
////        mVisualizerView.addRenderer(lineRenderer);
////    }
//
//    // Actions for buttons defined in xml
////    public void startPressed(View view) throws IllegalStateException, IOException
////    {
////        if(mPlayer.isPlaying())
////        {
////            mPlayer.stop();
////        }
////        mPlayer.prepare();
////        mPlayer.start();
////    }
//
//    //public void stopPressed(View view)
////    {
////        mPlayer.stop();
////    }
//
////    public void barPressed(View view)
////    {
////        addBarGraphRenderers();
////    }
//
////    public void circlePressed(View view)
////    {
////        addCircleRenderer();
////    }
////
////    public void circleBarPressed(View view)
////    {
////        addCircleBarRenderer();
////    }
////
////    public void linePressed(View view)
////    {
////        addLineRenderer();
////    }
////
////    public void clearPressed(View view)
////    {
////        mVisualizerView.clearRenderers();
////    }
//
//
//}
}