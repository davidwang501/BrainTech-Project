package react;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.justin.braintech.R;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 * @author wang.david.501@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    public static final String PREFS = "prefs";


    //constants
    final long MAX = 4_000; //the latest time when the screen may turn from set to go
    final long MIN = 1_000; //the earliest time when the screen may turn from set to go
    private final long SLOTH_RT = 999999999; //the presumed reaction time of a sloth

    // variables
    private String state;   //the current state of the buttom
    Handler h;  //used for scheduling a screen color change
    Runnable r; //used for describing the color change
    final FileHandler logFile = new FileHandler("Android/data/gmu.braintech.react", "log.csv");

    //time zone
    long initialTime;  //used for storing the initial and the final times (when the screen changes and when the user taps)
    private long record = SLOTH_RT;
    private long redDuration; //duration of the red screen

    //declarations used for sound play
    private static final int MAX_STREAMS = 5;
    private AudioManager audioManager;
    private SoundPool soundPool;
    private float volume;
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private int sound;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // read record from shared preference
        SharedPreferences sp = getSharedPreferences(PREFS, 0);
        long stored_record = sp.getLong("record", this.SLOTH_RT);
        if (stored_record != this.SLOTH_RT) {
            record = stored_record;
            setTitle(String.format("Reaction - Record: %d ms", record));
        }

        this.state = "ready";   //initialize the state
        Button button = (Button) findViewById(R.id.button);

        changeColor(button, Color.parseColor("#ffcc00"), "Ready"); //initialize the color
        this.h = new Handler();
        this.r = new Runnable() {
            public void run() {
                state = "go";
                playSound();
                changeColor(Color.GREEN, "GO!");
                initialTime = SystemClock.uptimeMillis();
            }
        };

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    click(v);
                    return true;
                }
                return false;
            }
        });

        loadSound();
    }


    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences sp = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("record", record);
        editor.commit();
    }


    /**
     * Load sounds
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadSound() {
        // AudioManager audio settings for adjusting the volume
        this.audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        float currentVolumeIndex = (float) this.audioManager.getStreamVolume(streamType); // Current volumn Index of particular stream type.
        float maxVolumeIndex = (float) this.audioManager.getStreamMaxVolume(streamType); // Get the maximum volume index for a particular stream type
        this.volume = currentVolumeIndex / maxVolumeIndex;    // Volume (0 --> 1)

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

        this.soundPool = builder.build();

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Load sound file (beep.wav into SoundPool.
        this.sound = this.soundPool.load(this, R.raw.beep, 1);
    }


    /**
     * Play sounds
     */
    public void playSound() {
        if (loaded) {
            float leftVolume = volume;
            float rightVolume = volume;
            int streamId = this.soundPool.play(this.sound, leftVolume, rightVolume, 1, 0, 1f);  // Play sound of gunfire. Returns the ID of the new stream.
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Toast.makeText(MainActivity.this, "- portrait -", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.this, "- landscape -", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }

        switch (id) {
            case R.id.action_resetscore:
                this.record = this.SLOTH_RT;
                setTitle("Reaction");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Define what to do when the user click the button
     *
     * @param view the tapped view (in this case a button)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void click(View view) {
        Button button = (Button) view;  // the button is R.id.button

        switch (this.state) {
            case "ready":
                this.state = "set";
                changeColor(button, Color.RED, "Set");
                this.h.postDelayed(this.r, this.redDuration = this.MIN + (long) (Math.random() * (this.MAX - this.MIN)));
                break;
            case "set":
                this.h.removeCallbacks(this.r);
                this.state = "tooFast";
                changeColor(button, Color.parseColor("#0066ff"), "Too fast !");
                break;
            case "go":
                this.state = "ready";
                long reactionTime = SystemClock.uptimeMillis() - initialTime;
                this.logFile.write(String.format("\"%s\",\"%d\",\"%d\"\n", getCurrentTime(), reactionTime, this.redDuration));
                if (reactionTime < this.record) {
                    this.record = reactionTime;
                    setTitle(String.format("Reaction - Record: %d ms", reactionTime));
                }
                String message = String.format("Ready\n%d ms", reactionTime);
                changeColor(button, Color.parseColor("#ffcc00"), message);
                break;
            case "tooFast":
                state = "ready";
                this.logFile.write(String.format("\"%s\",\"%d\",\"%d\"\n", getCurrentTime(), -1, this.redDuration));
                changeColor(button, Color.parseColor("#ffcc00"), "Ready");
                break;
            default:
                break;
        }
    }


    /**
     * Change the color of the statusbar, actionbar, button and navigation bar
     *
     * @param button the button whose color is changed
     * @param color  the new color
     * @param text   the new text of the button
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void changeColor(Button button, int color, String text) {
        this.getWindow().setStatusBarColor(color);   //change the status bar color
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));   //change the actionbar color
        button.setBackgroundColor(color);    //change the button color
        this.getWindow().setNavigationBarColor(color);   //change the navigation bar color

        button.setText(text);    //change button text
    }


    /**
     * Change the color of the statusbar, actionbar, button and navigation bar
     *
     * @param color the new color
     * @param text  the new text of R.id.button button
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void changeColor(int color, String text) {
        Button button = (Button) findViewById(R.id.button);

        this.getWindow().setStatusBarColor(color);   //change the status bar color
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));   //change the actionbar color
        button.setBackgroundColor(color);    //change the button color
        this.getWindow().setNavigationBarColor(color);   //change the navigation bar color

        button.setText(text);    //change button text
    }

    /**
     * @return the current date and time
     */
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
