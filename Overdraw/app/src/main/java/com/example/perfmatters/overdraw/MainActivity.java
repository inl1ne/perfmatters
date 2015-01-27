package com.example.perfmatters.overdraw;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {

    private static final int MAX_VALUE=200;

    private SeekBar mSlider;
    private TextView mLabel;
    private OverdrawView mOverdraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlider = (SeekBar)findViewById(R.id.overdrawSlider);
        mLabel = (TextView)findViewById(R.id.overdrawAmountLabel);
        mOverdraw = (OverdrawView) findViewById(R.id.overdrawView);

        mSlider.setMax(MAX_VALUE);
        mOverdraw.setOverdrawMax(MAX_VALUE);
        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mOverdraw.setOverdrawAmount(progress);
                mLabel.setText(String.format("%dx Overdraw",progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
