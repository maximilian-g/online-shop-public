package com.online.shop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {

    private DateUtil() {}

    public static final String CURRENT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CURRENT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(CURRENT_DATE_FORMAT);
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(CURRENT_DATE_TIME_FORMAT);

    public static Date getDateFromString(String date) throws ParseException {
        return new SimpleDateFormat(CURRENT_DATE_FORMAT).parse(date);
    }

    public static Date getDateTimeFromString(String date) throws ParseException {
        return new SimpleDateFormat(CURRENT_DATE_TIME_FORMAT).parse(date);
    }

    public static synchronized String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static synchronized String formatDateTime(Date date) {
        return dateTimeFormatter.format(date);
    }

    public static Date removeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
