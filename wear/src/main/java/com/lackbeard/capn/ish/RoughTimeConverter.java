package com.lackbeard.capn.ish;

import android.text.format.Time;
import android.util.Log;

import java.util.Random;

/**
 * Created by Dan on 28/04/2015.
 */
public class RoughTimeConverter {
    public RoughTimeConverter() {

    }

    private String[] hourWords = {"twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven"};
    private String[] minuteWords = {"", "ten", "twenty", "thirty", "twenty to", "ten to"};
    private String[] beforeWords = {"almost", "nearly"};
    private String[] afterWords = {"just gone"};

    private String convertHourToWord(int src) {
        boolean isAM = (src <= 11);

        if (src % 12 == 0) {
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

        //work out whether to diplay 'almost' or 'just gone'
        int minutesSincePreviousMarker = exact.minute % 10;

        //deal with special cases - noon and midnight
        if (isAlmostSpecial(exact.hour, exact.minute)) {
            Log.i("roughTime", "special time found");
            if (exact.hour == 0) {
                ret.setHourString("midnight");
            } else {
                ret.setHourString("noon");
            };
            ret.setMinuteString("");
            ret.setIshString(getIshString(minutesSincePreviousMarker));
            return ret;
        }

        //if minutes >= 3, then the minutes go BEFORE the words
        // i.e. "nearly twenty to ten" (not "nearly ten twenty to"!)
        if (exact.minute >= 35) {
            ret.setMinutesBeforeHours(true);
        }

        if (minutesSincePreviousMarker == 0) {
            ret.setMinuteString("");
            ret.setHourString(convertHourToWord(exact.hour));
            ret.setIshString(getIshString(minutesSincePreviousMarker));
        } else if (minutesSincePreviousMarker < 5) {
            ret.setMinuteString(convertMinuteToWord(10 * (exact.minute / 10)));
            ret.setHourString(convertHourToWord(exact.hour));
            ret.setIshString(getIshString(minutesSincePreviousMarker));
        } else /*if (minutesSincePreviousMarker >= 5)*/ {
            //special case to deal with mins > 55 i.e. need to display next hour - "nearly 12" at 11:55
            int tensOfMinutesElapsed = 1 + (exact.minute / 10);
            if (tensOfMinutesElapsed == 6) {
                ret.setHourString(convertHourToWord(exact.hour + 1));
                ret.setMinuteString(convertMinuteToWord((10 * tensOfMinutesElapsed) % 60));
            } else {
                ret.setHourString(convertHourToWord(exact.hour));
                ret.setMinuteString(convertMinuteToWord((10 * tensOfMinutesElapsed)));
            }

            ret.setIshString(getIshString(minutesSincePreviousMarker));
        }

        //should probably throw an exception if something goes wrong, //TODO
        return ret;
    }
}
