package com.remote.control;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup.LayoutParams;
import android.hardware.ConsumerIrManager;
import android.content.Context;
import android.view.WindowManager;

public class MainActivity extends Activity {

    ConsumerIrManager irManager;

    String[][] layout = {
        {"⭕", "LE-7003"},
        {"1", "2", "3"},
        {"4", "5", "6"},
        {"7", "8", "9"},
        {"-", "0"},
        {"− Vol", "Mute", "+ Vol"},
        {"🏠"},
        {"▲"},
        {"◄", "Ok", "►"},
        {"Return", "▼", "Exit"},
        {"◄◄", "►►"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        irManager = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.BLACK);
        rootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(24, 24, 24, 24);
        mainLayout.setBackgroundColor(Color.parseColor("#1C1C1C"));
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mainLayout.setWeightSum(layout.length + 1);

        for (int row = 0; row < layout.length; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setPadding(6, 10, 6, 10);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                0, 1f
            ));

            for (int col = 0; col < layout[row].length; col++) {
                final String label = layout[row][col];

                if (row == 0 && label.equals("LE-7003")) {
                    TextView textView = new TextView(this);
                    textView.setText("LE-7003");
                    textView.setTextColor(Color.parseColor("#472c2c"));
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    textView.setTextSize(18);
                    textView.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
                    textParams.setMargins(6, 6, 6, 6);
                    textView.setLayoutParams(textParams);
                    rowLayout.addView(textView);
                    continue;
                }

                if (label.equals("Settings") || label.equals("⏸") || label.equals("▶")) {
                    continue;
                }

                Button btn = new Button(this);
                btn.setText(label);
                btn.setTextColor(Color.WHITE);
                btn.setTypeface(Typeface.DEFAULT_BOLD);
                btn.setSingleLine(true);
                btn.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(28);

                if (label.equals("⭕")) {
                    drawable.setColor(Color.parseColor("#472c2c"));
                } else if (label.equals("Ok")) {
                    drawable.setColor(Color.parseColor("#2c3847"));
                } else if (label.equals("Mute") || label.equals("Exit") || label.equals("Return")) {
                    drawable.setColor(Color.parseColor("#171616"));
                } else {
                    drawable.setColor(Color.parseColor("#272a33"));
                }

                btn.setBackground(drawable);
                btn.setAllCaps(false);
                btn.setPadding(16, 16, 16, 16);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
                params.setMargins(6, 6, 6, 6);
                btn.setLayoutParams(params);

                final Handler holdHandler = new Handler();
                final Runnable[] holdRunnable = new Runnable[1];

                btn.setOnTouchListener(new View.OnTouchListener() {
                    private boolean isHolding = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Button b = (Button) v;

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            b.setAlpha(0.5f);
                            b.setScaleX(1.1f);
                            b.setScaleY(1.1f);

                            if (label.equals("− Vol") || label.equals("+ Vol") || label.equals("◄◄") || label.equals("►►")) {
                                isHolding = true;
                                holdRunnable[0] = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isHolding) {
                                            Integer code = getIRCode(label);
                                            if (code != null) sendIR(code);
                                            holdHandler.postDelayed(this, 150);
                                        }
                                    }
                                };
                                holdHandler.post(holdRunnable[0]);
                            }

                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            b.setAlpha(1f);
                            b.setScaleX(1f);
                            b.setScaleY(1f);

                            if (isHolding) {
                                isHolding = false;
                                holdHandler.removeCallbacks(holdRunnable[0]);
                            }

                            if (!(label.equals("− Vol") || label.equals("+ Vol") || label.equals("◄◄") || label.equals("►►"))) {
                                Integer code = getIRCode(label);
                                if (code != null) sendIR(code);
                            }
                        }

                        return true;
                    }
                });

                rowLayout.addView(btn);
            }

            mainLayout.addView(rowLayout);
        }

        TextView credits = new TextView(this);
        credits.setText("Created by: Guilherme Soares");
        credits.setTextColor(Color.parseColor("#472c2c"));
        credits.setTextSize(10);
        credits.setGravity(Gravity.CENTER);
        credits.setPadding(0, 10, 0, 10);
        credits.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        mainLayout.addView(credits);

        rootLayout.addView(mainLayout);
        setContentView(rootLayout);
    }

    private Integer getIRCode(String label) {
        switch (label) {
            case "⭕": return 0xE0E040BF;
            case "1": return 0xE0E020DF;
            case "2": return 0xE0E0A05F;
            case "3": return 0xE0E0609F;
            case "4": return 0xE0E010EF;
            case "5": return 0xE0E0906F;
            case "6": return 0xE0E050AF;
            case "7": return 0xE0E030CF;
            case "8": return 0xE0E0B04F;
            case "9": return 0xE0E0708F;
            case "0": return 0xE0E08877;
            case "-": return 0xE0E0C837;
            case "+ Vol": return 0xE0E0E01F;
            case "− Vol": return 0xE0E0D02F;
            case "Mute": return 0xE0E0F00F;
            case "🏠": return 0xE0E09E61;
            case "◄": return 0xE0E0A659;
            case "►": return 0xE0E046B9;
            case "▲": return 0xE0E006F9;
            case "▼": return 0xE0E08679;
            case "Ok": return 0xE0E016E9;
            case "Return": return 0xE0E01AE5;
            case "Exit": return 0xE0E0B44B;
            case "◄◄": return 0xE0E0A25D;
            case "►►": return 0xE0E012ED;
            default: return null;
        }
    }

    private void sendIR(int code) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(this, "Device without IR emitter", Toast.LENGTH_SHORT).show();
            return;
        }
        int frequency = 38000;
        int[] pattern = buildPattern(code);
        irManager.transmit(frequency, pattern);
    }

    private int[] buildPattern(int code) {
        int[] pattern = new int[2 + 32 * 2 + 1];
        int i = 0;
        pattern[i++] = 9000;
        pattern[i++] = 4500;
        for (int bit = 0; bit < 32; bit++) {
            pattern[i++] = 560;
            pattern[i++] = ((code & 0x80000000) != 0) ? 1690 : 560;
            code <<= 1;
        }
        pattern[i] = 560;
        return pattern;
    }
}