package com.example.admin.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class xy_graph extends AppCompatActivity implements MidiDriver.OnMidiStartListener,AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private static final String[]paths =
            {"Acoustic Grand Piano", "Bright Piano", "Electric Grand Piano", "Honky-tonk piano", "Electric Piano 1", "Electric Piano 2", "Harpsichord", "Clav",
                    "Celesta", "Glockenspiel", "Music Box", "Vibraphone", "Marimba", "Xylophone", "Tubular Bells", "Dulcimer",
                    "Drawbar Organ", "Percussive Organ", "Rock Organ", "Church Organ", "Reed Organ", "Accordian", "Harmonica", "Tango Accordian",
                    "Nylon String Guitar", "Steel String Guitar", "Jazz Guitar", "Clean Electric Guitar", "Muted Electric Guitar", "Overdrive Guitar", "Distortion Guitar", "Guitar Harmonics",
                    "Acoustic Bass", "Fingered Bass", "Picked Bass", "Fretless Bass", "Slap Bass 1", "Slap Bass 2", "Synth Bass 1", "Synth Bass 2",
                    "Violin", "Viola", "Cello", "Contrabass", "Tremolo Strings", "Pizzicato Strings", "Orchestral Harp", "Timpani",
                    "String Ensemble 1", "String Ensemble 2", "Synth Strings 1", "Synth Strings 2", "Choir Ahh", "Choir Oohh", "Synth Voice", "Orchestral Hit",
                    "Trumpet", "Trombone", "Tuba", "Muted Trumpet", "French Horn", "Brass Section", "Synth Brass 1", "Synth Brass 2",
                    "Soprano Sax", "Alto Sax", "Tenor Sax", "Baritone Sax", "Oboe", "English Horn", "Bassoon", "Clarinet",
                    "Piccolo", "Flute", "Recorder", "Pan Flute", "Blown Bottle", "Shakuhachi", "Whistle", "Ocarina",
                    "Square Wav", "Sawtooth Wav", "Caliope", "Chiff", "Charang", "Voice", "Fifth's", "Bass&Lead",
                    "New Age", "Warm", "Polysynth", "Choir", "Bowed", "Metallic", "Halo", "Sweep",
                    "FX Rain", "FX Soundtrack", "FX Crystal", "FX Atmosphere", "FX Brightness", "FX Goblins", "FX Echo Drops", "FX Star Theme",
                    "Sitar", "Banjo", "Shamisen", "Koto", "Kalimba", "Bagpipe", "Fiddle", "Shanai",
                    "Tinkle Bell", "Agogo", "Steel Drums", "Woodblock", "Taiko Drum", "Melodic Tom", "Synth Drum", "Reverse Cymbal",
                    "Guitar Fret Noise", "Breath Noise", "Seashore", "Bird Tweet", "Telephone Ring", "Helicopter", "Applause", "Gunshot"
            };


    private MidiDriver midiDriver;
    private byte[] event;
    private byte[] presChng;
    private byte[] programChng;
    private int[] config;
    byte[] bytes;
    double[] avg;
    int[] freq_arr;
    int index,index2;
    double x,y;
    EditText et;
    int limit;
    Bundle extras;
    double indices[],maxAmp;
    String filepath;
    private VisualizerView mVisualizerView;
    TextView tv;
    Button button;
    TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xy_graph);
        button = (Button)findViewById(R.id.button1);
//        final Button generateGraph = (Button)findViewById(R.id.button2);
//        final Button ok = (Button)findViewById(R.id.ok);

        et = (EditText)findViewById(R.id.editText);

      //  tv = (TextView)findViewById(R.id.textView1);

        tv1 = (TextView)findViewById(R.id.textView);

        extras = getIntent().getExtras();

        filepath = "/storage/sdcard1/wav/test2.pcm";

        if(extras!=null)
        {

            filepath = extras.getString("data");
        }


        // Instantiate the driver.
        midiDriver = new MidiDriver();
        // Set the listener.
        midiDriver.setOnMidiStartListener(this);

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(xy_graph.this,android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try {
            makeSignal();
        } catch (IOException e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limit = Integer.parseInt(et.getText().toString());


                solve();
            }
        });

    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        programChng = new byte[2];
        programChng[0] = (byte)0xC0;
        programChng[1] = (byte)position;
        midiDriver.write(programChng);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void makeSignal() throws IOException {
        File file = new File(filepath);
        File fil = new File("/storage/sdcard1/wav/");
        File fil2 = new File(fil,"abc3.txt");
        FileWriter fw = new FileWriter(fil2);


        bytes = new byte[(int) file.length()];
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bytes, 0, bytes.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("No of bytes :", Integer.toString(bytes.length));

        avg = new double[10000];
        index=index2=0;
        double av,temp,sum;
        maxAmp=0;

        for (int j = 0; j < bytes.length; j += 22050) {
            sum = 0;
            for (int i = j; i < j + 22050; i++) {
                if (2 * i < bytes.length) {
                    temp = (double) ((Math.abs(bytes[2 * i]) & 0xFF) | (Math.abs(bytes[2 * i + 1]) << 8)) / 32768.0F;
                    sum = sum + temp;
                }
                else
                {
                    j = bytes.length;
                    break;
                }

            }
            Log.i(index + "the Sum is : ", Double.toString(sum));
            av = sum / 44100.0;
            avg[index] = sum;
            if(sum>maxAmp)
            {
                maxAmp = sum;
            }
            //Log.i(index + "th Avg Amplitude : ", Double.toString(av));
            index++;
        }


//        Log.i("Index is : ",Integer.toString(index));


//        midiDriver.write(programChng);

//        for (int i = 0; i < bytes.length; i++) {
//            if (bytes[i] > 10){
//
//                Log.i(i + "th byte is : ", Byte.toString(bytes[i]));
//                fw.append(i + "th byte is " + Byte.toString(bytes[i]) + "\n");
//            }
//
//        }


    }

    public  void playMusic()
    {
        freq_arr = new int[10000];
        double amp[] = new double[10000];

        for(int i=0;i<index;i++)
        {
            if(indices[i]!=0)
            {
                int midi = calculateFFT2(bytes,i*22050);
                freq_arr[index2] = midi;
                amp[index2] = avg[i];
                index2++;
                Log.i(i + "th frequency is : ",Double.toString(midi));
            }
            else
            {
                freq_arr[index2] = 0;
                index2++;
            }
        }

        for(int i=0;i<index2;i++)
        {
            //Log.i(i+"th Midi notes is : ",Integer.toString(freq_arr[i]));
            if(freq_arr[i]!=0)
            {
                changePressure(freq_arr[i],(int)(amp[i]/maxAmp)*127);
                playNote(freq_arr[i]);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


            //stopNote(freq_arr[i]);
        }
    }

    public int calculateFFT2(byte[] signal,int startByte)
    {
        int factor = 16,c=0;
        double max = -1;
        final int mNumberOfFFTPoints = 1024*factor;
        double mMaxFFTSample;
        int mPeakPos, indexfft;

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints / 2];

        indexfft = 0;

        for (int i = startByte; i < startByte + mNumberOfFFTPoints; i++) {
            if (2 * i < signal.length) {
                temp = (double) ((signal[2 * i] & 0xFF) | (signal[2 * i + 1] << 8)) / 32768.0F;
                complexSignal[indexfft] = new Complex(temp, 0.0);
                indexfft++;
            }

        }

        y = FFT.fft(complexSignal); // --> Here I use FFT class

        //Log.i("Length of y : ", Integer.toString(y.length));

        mMaxFFTSample = 0.0;
        mPeakPos = 0;
        for (int i = 0; i < (mNumberOfFFTPoints/2); i++) {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if (absSignal[i] > mMaxFFTSample) {
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }

        double freq = (mPeakPos*(double)(44100/mNumberOfFFTPoints));
        int x = (int)Math.round(12*(Math.log(freq/440)/Math.log(2)))+69;

        return x;
    }

    public void solve()
    {
        int ind[] = new int[avg.length];
        indices = new double[avg.length];
        for(int i = 0; i < avg.length; i++)
        {
            ind[i] = i;
            indices[i] = 0;
        }

        int n = avg.length;
        for (int i = 0; i < n-1; i++)
        {
            for (int j = 0; j < n-i-1; j++)
            {
                if (avg[j] < avg[j+1])
                {
                    // swap temp and arr[i]
                    double temp = avg[j];
                    avg[j] = avg[j+1];
                    avg[j+1] = temp;
                    int in = ind[j];
                    ind[j] = ind[j+1];
                    ind[j+1] = in;
                }
            }
        }

        for(int i = 0; i < limit; i++)
        {
            indices[ind[i]] = 1;
        }
        playMusic();
    }

    private void changePressure(int x, int y)
    {
        presChng = new byte[3];
        presChng[0] = (byte)0xA0;
        presChng[1] = (byte)x;
        presChng[2] = (byte)y;
        midiDriver.write(presChng);
    }


    @Override
    protected void onResume() {
        super.onResume();
        midiDriver.start();

        // Get the configuration.
        config = midiDriver.config();

        // Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
    }

    @Override
    protected void onPause() {
        super.onPause();
        midiDriver.stop();
    }

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");
    }

    private void playNote(int x) {

        // Construct a note ON message for the middle C at maximum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] = (byte) x;  // 0x3C = middle C
        event[2] = (byte) 0x40;  // 0x7F = the maximum velocity (127)

        // Internally this just calls write() and can be considered obsoleted:
        //midiDriver.queueEvent(event);

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);

    }

    private void stopNote(int x) {

        // Construct a note OFF message for the middle C at minimum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = (byte) x;  // 0x3C = middle C
        event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);

    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // MotionEvent object holds X-Y values
//        if(event.getAction() == MotionEvent.ACTION_DOWN) {
//
//            Display mdisp = getWindowManager().getDefaultDisplay();
//            int maxX= mdisp.getWidth();
//            int maxY= mdisp.getHeight();
//            int x = (int)event.getX();
//            int y = (int)event.getY();
//            String text = "You click at x = " + x + " and y = " + y;
//            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//        }
//
//        return super.onTouchEvent(event);
//    }

}
