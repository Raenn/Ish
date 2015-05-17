package com.lackbeard.capn.ish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 26/04/2015.
*/
public class IshService extends CanvasWatchFaceService {

    //update once a minute
    private static final long NORMAL_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long MUTE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    private static float timeFontHeight, extraFontHeight;
    private static final float TIME_PADDING = 2;
    private static final float EXTRA_PADDING = 18;

    private ArrayList<DrawableWord> wordsToDraw;

    private class DrawableWord {
        private final String word;
        private final float height;
        private float padding;
        private final Paint paint;

        public DrawableWord(String word, float height, float padding, Paint paint) {
            this.word = word;
            this.height = height;
            this.padding = padding;
            this.paint = paint;
        }

        public String getWord() {
            return word;
        }

        public float getHeight() {
            return height;
        }

        public Paint getPaint() {
            return paint;
        }

        public float getWidth() {
            return paint.measureText(word);
        }

        public float getPadding() {
            return padding;
        }
    }

    @Override
    public Engine onCreateEngine() {
        /* Watch face implementation goes here */
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        Paint mBackgroundPaint;
        Paint mPrefixPaint;
        Paint mIshPaint;
        Paint mHourPaint;
        Paint mMinutePaint;
        Time mTime;

        //shut up I name my variables what I want
        RoughTimeConverter ishGenerator;
        RoughTime roughTime;

        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Log.i("IshService", "Handling update message");
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = NORMAL_UPDATE_RATE_MS - (timeMs % NORMAL_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        /* receiver to update the time zone */
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            /* init watch face */

            Log.d("IshService", "creating!");

            /* Config the system UI */
            setWatchFaceStyle(new WatchFaceStyle.Builder(IshService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            ishGenerator = new RoughTimeConverter();

            mPrefixPaint = new Paint();
            mPrefixPaint.setTextSize(20);
            mPrefixPaint.setColor(Color.WHITE);
            mPrefixPaint.setAntiAlias(true);
            mPrefixPaint.setStrokeCap(Paint.Cap.ROUND);

            mIshPaint = new Paint();
            mIshPaint.setTextSize(20);
            mIshPaint.setColor(Color.WHITE);
            mIshPaint.setAntiAlias(true);
            mIshPaint.setStrokeCap(Paint.Cap.ROUND);

            mHourPaint = new Paint();
            mHourPaint.setTextSize(40);
            mHourPaint.setColor(Color.WHITE);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);

            mMinutePaint = new Paint();
            mMinutePaint.setTextSize(40);
            mMinutePaint.setColor(Color.WHITE);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);

            timeFontHeight = -mHourPaint.ascent() + mHourPaint.descent();
            extraFontHeight = -mPrefixPaint.ascent() + mPrefixPaint.descent();

            Resources resources = IshService.this.getResources();
//            Drawable backgroundDrawable = resources.getDrawable(R.drawable.bg);
//            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            wordsToDraw = new ArrayList<>();

            mTime = new Time();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /* the time changed */
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
            // should do some setAntiAlias etc on painted objects, when I have some
            //TODO: investigate
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /* draw your watch face */
            mTime.setToNow();
            roughTime = ishGenerator.convertToRoughTime(mTime);

            wordsToDraw.clear();
            wordsToDraw.add(
                    new DrawableWord("it's",
                            extraFontHeight,
                            3, //TODO: padding looks dodgy when there's no 'ish'. was previously using EXTRA_PADDING,
                            mPrefixPaint)
            );
            wordsToDraw.add(
                    new DrawableWord(
                            roughTime.getHourString(),
                            timeFontHeight,
                            TIME_PADDING,
                            mHourPaint)
            );
            if (!roughTime.getMinuteString().equals("")) {
                wordsToDraw.add(
                        (roughTime.isMinutesBeforeHours() ? 1 : 2),
                        new DrawableWord(
                                roughTime.getMinuteString(),
                                timeFontHeight,
                                TIME_PADDING,
                                mMinutePaint)
                );
            }
            if (!roughTime.getIshString().equals("")) {
                wordsToDraw.add(
                        (roughTime.isIshBeforeTime() ? 1 : 3),
                        new DrawableWord(
                                roughTime.getIshString(),
                                extraFontHeight,
                                EXTRA_PADDING,
                                mIshPaint)
                );
            }

            float totalHeight = 0;
            for (int i=0; i < wordsToDraw.size(); i++) {
                totalHeight += wordsToDraw.get(i).getHeight();
            }

            float yOffset = (bounds.height() - totalHeight) / 2;
            canvas.drawColor(Color.BLACK);

            for (int i=0; i < wordsToDraw.size(); i++) {
                DrawableWord word = wordsToDraw.get(i);
                canvas.drawText(
                        word.getWord(),
                        bounds.centerX() - word.getWidth() / 2,
                        yOffset + (word.getPadding() == TIME_PADDING ? TIME_PADDING : 0),
                        word.getPaint());
                yOffset += word.getHeight() + (i == wordsToDraw.size() - 1 ? 0 : word.getPadding());
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode), so we may need to start or stop the timer
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            IshService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            IshService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        static final int MSG_UPDATE_TIME = 0;

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }

}
