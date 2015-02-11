package com.github.perfmatters.customview;

import android.graphics.Rect;
import android.support.v4.util.Pools;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CardView cards = (CardView)findViewById(R.id.cards);
        final SeekBar seek = (SeekBar)findViewById(R.id.seekBar);
        final Button up = (Button)findViewById(R.id.up);
        final Button down = (Button)findViewById(R.id.down);
        final CheckBox rects = (CheckBox)findViewById(R.id.rects);
        final CheckBox clip = (CheckBox)findViewById(R.id.clip);
        final CheckBox bitmaps = (CheckBox)findViewById(R.id.bitmaps);

        seek.setMax(cards.getMaxCardsToDraw());
        cards.setMaxCardsToDraw(1);
        seek.setProgress(cards.getMaxCardsToDraw());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cards.setMaxCardsToDraw(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seek.setProgress(seek.getProgress() + 1);
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seek.setProgress(seek.getProgress() - 1);
            }
        });


        rects.setChecked(cards.isDrawRects());
        rects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cards.setDrawRects(rects.isChecked());
            }
        });
        bitmaps.setChecked(cards.isDrawBitmaps());
        bitmaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cards.setDrawBitmaps(bitmaps.isChecked());
            }
        });
        clip.setChecked(cards.isClip());
        clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cards.setClip(clip.isChecked());
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
