package com.lackbeard.capn.ish;

import android.text.format.Time;

import java.util.Random;

/**
 * Created by Dan on 28/04/2015.
 */
public class RoughTime {
    private boolean ishBeforeTime = false;
    private boolean minutesBeforeHours = false;

    private String hourString;
    private String minuteString;
    private String toString;

    public boolean isIshBeforeTime() {
        return ishBeforeTime;
    }

    public boolean isMinutesBeforeHours() {
        return minutesBeforeHours;
    }

    public String getHourString() {
        return hourString;
    }

    public String getMinuteString() {
        return minuteString;
    }

    public String getToString() {
        return toString;
    }

    public String getIshString() {
        return ishString;
    }

    public void setIshString(String ishString) {
        this.ishString = ishString;
    }

    public void setIshBeforeTime(boolean ishBeforeTime) {
        this.ishBeforeTime = ishBeforeTime;
    }

    public void setMinutesBeforeHours(boolean minutesBeforeHours) {
        this.minutesBeforeHours = minutesBeforeHours;
    }

    public void setHourString(String hourString) {
        this.hourString = hourString;
    }

    public void setMinuteString(String minuteString) {
        this.minuteString = minuteString;
    }

    public void setToString(String toString) {
        this.toString = toString;
    }

    private String ishString;

}
