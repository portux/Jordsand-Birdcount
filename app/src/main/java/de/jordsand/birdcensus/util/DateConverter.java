package de.jordsand.birdcensus.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Rico Bergmann
 */
public class DateConverter {

    public String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return format.format(date);
    }

    public Date retrieveDate(String rawDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = format.parse(rawDate);
        } catch (ParseException e) {
            date = null;
        }
        return date;
    }
}
