package com.lackbeard.capn.ish;

import android.text.format.Time;
import android.util.Log;

import java.util.Random;

/**
 * Created by Dan on 28/04/2015.
 */
public class RoughTimeConverter {
    private String[] hourWords = {"twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven"};
    private String[] minuteWords = {"", "ten past", "twenty", "thirty", "twenty to", "ten to"};
    private String[] beforeWords = {"almost", "nearly"};
    private String[] afterWords = {"just gone"};

    private String convertHourToWord(int src) {
        if (src == 0) {
            //TODO: special 'noon' and 'midnight' stuff?
            return "twelve";
        } else {
            return hourWords[(src % 12)];
        }
    }

    //NB: assumes you've already rounded the minutea to nearest ten - likely to change
    private String convertMinuteToWord(int src) {
        return minuteWords[src / 10];
    }

    private String getRandomStringFromArray(String[] array) {
        Random rng = new Random();
        return array[rng.nextInt(array.length)];
    }

    private boolean isAlmostSpecial(int hour, int minute) {
        if ((hour % 12 == 0 && minute < 5)
         || (hour % 12 == 11 && minute > 55)) {
            return true;
        }
        return false;
    }

    private String getIshString(int minutesSinceLastMarker) {
        if (minutesSinceLastMarker == 0) {
            return "";
        } else if (minutesSinceLastMarker < 5) {
            return getRandomStringFromArray(afterWords);
        } else /*if (minutesSinceLastMarker >= 5)*/ {
            return getRandomStringFromArray(beforeWords);
        }
    }

    public RoughTime convertToRoughTime(Time exact) {
        RoughTime ret = new RoughTime();
        int hourToUse = exact.hour;
        //make sure to use next hour if using a 'to'
        if (exact.minute >= 35) {
            hourToUse++;
        }

        //work out whether to display 'almost' or 'just gone'
        int minutesSincePreviousMarker = exact.minute % 10;

        //deal with special cases - noon and midnight
        if (isAlmostSpecial(exact.hour, exact.minute)) {
            Log.i("roughTime", "special time found");
            if (hourToUse == 12) {
                ret.setHourString("noon");
            } else {
                ret.setHourString("midnight");
            };
            ret.setMinuteString("");
            ret.setIshString(getIshString(minutesSincePreviousMarker));
            return ret;
        }

        //if minutes >= 3, then the minutes go BEFORE the words
        // i.e. "nearly twenty to ten" (not "nearly ten twenty to"!)
        if (exact.minute >= 35 ||
                (exact.minute >= 5 && exact.minute < 15)) {
            ret.setMinutesBeforeHours(true);
        }

        if (minutesSincePreviousMarker == 0) {
            ret.setMinuteString(convertMinuteToWord(exact.minute));
            ret.setHourString(convertHourToWord(hourToUse));
            ret.setIshString(getIshString(minutesSincePreviousMarker));
        } else if (minutesSincePreviousMarker < 5) {
            ret.setMinuteString(convertMinuteToWord(10 * (exact.minute / 10)));
            ret.setHourString(convertHourToWord(hourToUse));
            ret.setIshString(getIshString(minutesSincePreviousMarker));
        } else /*if (minutesSincePreviousMarker >= 5)*/ {
            ret.setHourString(convertHourToWord(hourToUse));
            ret.setMinuteString(
                convertMinuteToWord(
                    10 * ((1 + exact.minute / 10) % 6)
                )
            );
            ret.setIshString(getIshString(minutesSincePreviousMarker));
            ret.setIshBeforeTime(true);
        }

        //should probably throw an exception if something goes wrong, //TODO
        return ret;
    }
}
