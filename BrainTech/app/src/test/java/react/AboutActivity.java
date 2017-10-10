package react;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.justin.braintech.R;


/**
 *
 * @author wang.david.501@gmail.com
 */
public class AboutActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailto("braintech@gmu.edu");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setStatusBarColor(Color.parseColor("#BA1AEE"));
    }


    private void mailto(String email) {
        Intent launchBrowser = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",email, null));
        startActivity(launchBrowser);
    }
}
