package com.zywl.app.base.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类
 *
 * @author Aaron
 * @date 2016-4-7
 */
public class DateUtil {

    public static final SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 年月日时分秒
     */
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 年-月日
     */
    private static final SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MMdd");

    public static final SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static final SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMM");

    public static final SimpleDateFormat sdf7 = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");

    public static final SimpleDateFormat sdf8 = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat sdf9 = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat sdf10 = new SimpleDateFormat("MM月dd日 HH时mm分");
    private static final String[] WeekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public static String format7(String date) {
        try {
            synchronized (sdf7) {
                synchronized (sdf5) {
                    return sdf7.format(sdf5.parse(date));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String format(Date date) {
        synchronized (sdf0) {
            return sdf0.format(date);
        }
    }

    public static String format(String date) {
        try {
            synchronized (sdf0) {
                synchronized (sdf3) {
                    return sdf0.format(sdf3.parse(date));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String format0(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (sdf0) {
            return sdf0.format(date);
        }
    }

    public static final String format1(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (sdf1) {
            return sdf1.format(date);
        }
    }

    public static final String format2(Date date) {
        synchronized (sdf2) {
            return sdf2.format(date);
        }
    }

    public static final String format9(Date date) {
        synchronized (sdf9) {
            return sdf9.format(date);
        }
    }


    public static final String format10(Date date) {
        synchronized (sdf10) {
            return sdf10.format(date);
        }
    }


    public static final String format5(Date date) {
        synchronized (sdf5) {
            return sdf5.format(date);
        }
    }

    public static final String getWeekDay(Date date) {
        synchronized (sdf2) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return WeekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        }
    }

    public static final String getCurrent0() {
        synchronized (sdf0) {
            return sdf0.format(new Date());
        }
    }

    public static final String getCurrent1() {
        synchronized (sdf1) {
            return sdf1.format(new Date());
        }
    }

    public static final String getCurrent2() {
        synchronized (sdf2) {
            return sdf2.format(new Date());
        }
    }

    /**
     * 得到时间  HH:mm:ss
     *
     * @param timeStamp 时间戳
     * @return
     */
    public static String getTradeFormat(Long timeStamp) {
        synchronized (sdf0) {
            return sdf0.format(getTradeDate(timeStamp));
        }
    }

    public static Date getTradeDate(Long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new Date(calendar.getTimeInMillis() + timeStamp * 1000);
    }

    /**
     * 计算两个时间相差的秒数
     *
     * @param startDate
     * @return
     */
    public static int calSeconds(Date startDate) {
        long a = new Date().getTime();
        long b = startDate.getTime();
        int c = (int) ((a - b) / 1000);
        return c;
    }

    /**
     * 年月日时分秒
     * 宝付交易日期
     *
     * @return
     */
    public static final String getCurrent3() {
        synchronized (sdf3) {
            return sdf3.format(new Date());
        }
    }


    public static final String getHfOrderExpireTime(){
        synchronized (sdf3) {
            Date dateByM = getDateByM(new Date(), 10);
            return sdf3.format(dateByM);
        }
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public static final String getCurrent4() {
        synchronized (sdf4) {
            return sdf4.format(new Date());
        }
    }

    public static final String getCurrent2_Trade() {
        synchronized (sdf2) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, 1);
            return sdf2.format(calendar.getTime());
        }
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public static final String getCurrent5() {
        synchronized (sdf5) {
            return sdf5.format(new Date());
        }
    }
    public static final String getCurrent9() {
        synchronized (sdf9) {
            return sdf9.format(new Date());
        }
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public static final String getCurrent6() {
        synchronized (sdf6) {
            return sdf6.format(new Date());
        }
    }
    /**
     * 获取当前日期整数形式 yyyyMMdd
     * @return int
     */
    public static int getTodayInt() {
        synchronized (sdf9) {
            return Integer.parseInt(sdf9.format(new Date()));
        }
    }


    /**
     * 取得当前日期所在周的第一天
     *
     * @param date
     * @return
     */
    public static String getFirstDayOfWeek(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(currentDate);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return format2(calendar.getTime());
    }

    /**
     * 取得当前日期所在周的最后一天
     *
     * @param date
     * @return
     */
    public static String getLastDayOfWeek(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(currentDate);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() + 6);
        return format2(calendar.getTime());
    }

    /**
     * 返回指定日期的月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfMonth(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        return format2(calendar.getTime());
    }

    /**
     * 返回月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        return calendar.getTime();
    }


    /**
     * 返回指定日期的月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        calendar.roll(Calendar.DATE, -1);
        return format2(calendar.getTime());
    }

    /**
     * 返回指定日期的上个月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return format2(calendar.getTime());
    }

    /**
     * 返回指定日期的上个月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        return format2(calendar.getTime());
    }

    /**
     * 获取指定日期所在的一周日期
     *
     * @param mdate
     * @return
     */
    public static List<Date> Date2Week(Date date) {
        List<Date> list = new ArrayList<Date>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        for (int i = 0; i < 7; i++) {
            list.add(cal.getTime());
            cal.add(Calendar.DATE, 1);
        }
        return list;
    }

    /**
     * 比较时间
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDate(String date1, String date2) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date dt1 = format.parse(date1);
            Date dt2 = format.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }


    /**
     * 比较时间
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDate(Date dt1, Date dt2) {
        try {
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取指定天数的日期
     *
     * @return
     */
    public static Date getDate(String date, int year) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.YEAR, year);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取上周的日期
     *
     * @return
     */
    public static String getLastWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -8);
        Date monday = calendar.getTime();
        String preMonday = sdf.format(monday);
        return preMonday;
    }

    /**
     * 获取当前时间之前或之后几小时 hour
     *
     * @param date
     * @param hour
     * @return
     */
    public static String getTimeByHour(String date, int hour) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);
        return format.format(calendar.getTime());
    }
    public static Date getDateByHour(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);
        return calendar.getTime();
    }

    public static Date getTimeByHour2(String date, int hour) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);
        return calendar.getTime();
    }

    /**
     * 获取几分钟之后的时间戳
     *
     * @param date
     * @param hour
     * @return
     */
    public static Long getTimeByOneMin() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
        return calendar.getTime().getTime();
    }

    /**
     * 获取几秒之后的时间戳
     *
     * @param date
     * @param hour
     * @return
     */
    public static Long getTimeByM(int m) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + m);
        return calendar.getTime().getTime();
    }

    /**
     * 获取几秒之后的时间
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date getDateByM(int m) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + m);
        return calendar.getTime();
    }

    public static Date getDateByM(Long m) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + Integer.parseInt(m.toString()));
        return calendar.getTime();
    }


    public static Date getDateByDay(int day) {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, day);
        Date newDate = calendar.getTime();
        return newDate;
    }

    public static Date getSixDaysLaterEndOfDay() {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 6 天后的日期
        LocalDateTime sixDaysLater = now.plusDays(6);

        // 设置为当天的 23:59:59
        LocalDateTime endOfDay = sixDaysLater.with(LocalTime.MAX);

        // 转换为 Date 对象
        return localDateTimeToDate(endOfDay);
    }
    public static Date getDaysLaterEndByDay(int day) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // day 天后的日期
        LocalDateTime sixDaysLater = now.plusDays(day);

        // 设置为当天的 23:59:59
        LocalDateTime endOfDay = sixDaysLater.with(LocalTime.MAX);

        // 转换为 Date 对象
        return localDateTimeToDate(endOfDay);
    }

    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getDateByDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + day);
        return calendar.getTime();
    }

    /**
     * 获取指定时间几秒之后的时间
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date getDateByM(Date date, int m) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + m);
        return calendar.getTime();
    }

    public static Date getSpeedTime(Date date) {
        long sub = (date.getTime() - System.currentTimeMillis()) / 2;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND) + (int) sub);
        return calendar.getTime();
    }


    /**
     * 获取指定几秒之后的时间戳
     *
     * @param date
     * @param hour
     * @return
     */
    public static Long getTimeByM(Date date, int m) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + m);
        return calendar.getTime().getTime();
    }


    /**
     * 当前时间加天数后的时间
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date getTimeByDay(int day) {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, day);
        Date newDate = calendar.getTime();
        return newDate;
    }


    /**
     * 获取日期
     *
     * @param day
     * @return
     */
    public static String getDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, day);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    /**
     * 取得当前日期上周的第一天
     *
     * @param date
     * @return
     */
    public static String getFirstDayOfLastWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.DATE, -1 * 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return format2(calendar.getTime());
    }

    /**
     * 取得当前日期上周的最后一天
     *
     * @param date
     * @return
     */
    public static String getLastDayOfLastWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.DATE, -1 * 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return format2(calendar.getTime());
    }

    /**
     * 字符串转时间
     *
     * @param date
     * @param format
     * @return
     */
    public static Date parseDate(String date, String format) {
        if (date == null || date.equals("")) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public static String formatDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        if (format == null) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        return new SimpleDateFormat(format).format(date);
    }


    /**
     * 转换Date为cron表达式 ,eg.  "0 06 10 15 1 ? 2014"
     *
     * @param date : 时间点
     * @return
     */
    public static String getCron(Date date) {
        String dateFormat = "ss mm HH dd MM ? yyyy";
        return formatDate(date, dateFormat);
    }

    /**
     * 取当前年
     *
     * @param t
     * @return
     */
    public static int getYear(Date date) {
        Calendar cld = Calendar.getInstance();
        cld.setTime(date);
        return cld.get(Calendar.YEAR);
    }

    /**
     * 取当前月
     *
     * @param t
     * @return
     */
    public static int getMonth(Date date) {
        Calendar cld = Calendar.getInstance();
        cld.setTime(date);
        return cld.get(Calendar.MONTH) + 1;
    }

    public static long getTodayLastMillis() {
        String day = DateUtil.getCurrent2();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parseDate(day, "yyyy-MM-dd"));
        calendar.add(Calendar.MILLISECOND, 86400000);
        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    /**
     * 是否是同一天
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return false;
        return DateUtils.isSameDay(date1, date2);
    }

    public static long getYestdayBegin() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }


    //今天起始
    public static long getToDayBegin() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static Date getToDayDateBegin() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate;
    }

    public static long getToDayDateByHour(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp = calendar.getTimeInMillis();
        return timestamp;
    }



    public static Date getDateBeginByDay(int day) {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, day);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate;
    }


    public static int getMonthValue() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue();
    }

    //获取本月最大天数
    public static int getThisMonthDays() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    //获取传入的日期是几号
    public static int getDay(Date date) {
        Calendar cld = Calendar.getInstance();
        cld.setTime(date);
        return cld.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isIn(Date date) {
        long begin = getYestdayBegin();
        long end = getToDayBegin();
        if (date.getTime() > begin && date.getTime() < end) {
            return true;
        }
        return false;
    }


    //获取下一个整点小时的时间戳
    public static long getLastHourTime() {
        Calendar cld = Calendar.getInstance();
        //一个小时后的时间
        cld.set(Calendar.MINUTE, cld.get(Calendar.MINUTE) + 60);
        //一个小时后的
        int h = cld.get(Calendar.HOUR_OF_DAY);
        cld.set(Calendar.HOUR_OF_DAY, Integer.valueOf(h));
        cld.set(Calendar.SECOND, 0);
        cld.set(Calendar.MINUTE, 0);
        cld.set(Calendar.MILLISECOND, 0);
        Date date = cld.getTime();
        return date.getTime();
    }

    //到下一个整点的毫秒差
    public static long getTopNeed() {
        return getLastHourTime() - System.currentTimeMillis();
    }

    public static long getTomorrow7Hour() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long getTomorrowBegin() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long getTomorrowBeginActive() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 3);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }
    public static Date getTomorrowBeginDate() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate;
    }

    public static long getTomorrow1Hour() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long gettest() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        calendar.set(Calendar.MINUTE, 53);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long getAddStaticsDate() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime() - System.currentTimeMillis();
    }


    public static long getTaskNeed() {
        return getTomorrow7Hour() - System.currentTimeMillis();
    }

    public static Date getOneDaysAgoBegin() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate;
    }

    public static Date getDaysAgoBegin(int day) {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate;
    }

    public static long getDzTaskDate() {
        int i = 1;
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if(0<=hourOfDay && hourOfDay<8){
            i = 0;
        }
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, i);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long getToday8Date() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long getDzPeriodsTaskDate() {
        Calendar calendar = Calendar.getInstance();
        // 加上指定天数
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.SECOND, 40);
        calendar.set(Calendar.MILLISECOND, 0);
        Date newDate = calendar.getTime();
        return newDate.getTime();
    }

    public static long time9() {
        long Time = System.currentTimeMillis();
        // 创建 Calendar 对象
        Calendar calendar = Calendar.getInstance();
        // 将小时、分钟、秒和毫秒都设置为0
        calendar.set(Calendar.HOUR_OF_DAY, 9);//9
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long time0() {
        Calendar calendar = Calendar.getInstance();
        // 将小时、分钟、秒和毫秒都设置为0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,3);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getActivityNeed() {

        return getTomorrowBeginActive() - System.currentTimeMillis();
    }

    public static long time5() {
        Calendar calendar = Calendar.getInstance();
        // 将小时、分钟、秒和毫秒都设置为0
        calendar.set(Calendar.HOUR_OF_DAY, 5);//5
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long time8() {
        Calendar calendar = Calendar.getInstance();
        // 将小时、分钟、秒和毫秒都设置为0
        calendar.set(Calendar.HOUR_OF_DAY, 8);//8
        calendar.set(Calendar.MINUTE, 0);//0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Date getThisWeekBeginDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date firstDayOfWeekTime = calendar.getTime();
        return firstDayOfWeekTime;
    }

    public static Date getThisWeekEndDate(){
        Calendar calendar = Calendar.getInstance();
        // 设置到本周的第一天（周日）
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        // 在本周第一天的基础上加7天，得到下周的第一天，即本周最后一天的下一天
        calendar.add(Calendar.DAY_OF_WEEK, 7);
        // 设置时间为本周最后一天的24点
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        // 设置分钟和秒为0，确保时间为24点
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date end = calendar.getTime();
        return end;
    }
    public static List<String> oneWeekStr() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(format9(getTimeByDay(-i)));
        }
        return list;
    }

    /*
        获取两个日期相差的天数
     */
    public static long dateDifference(Date date1, Date date2) {
        Instant instant = date1.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate1 = instant.atZone(zoneId).toLocalDate();
        Instant instant2 = date2.toInstant();
        ZoneId zoneId2 = ZoneId.systemDefault();
        LocalDate localDate2 = instant2.atZone(zoneId2).toLocalDate();
        long daysDiff = ChronoUnit.DAYS.between(localDate1, localDate2);
        return daysDiff;
    }

    public static long calculateDaysCrossed(Date date1, Date date2) {
        // 将 Date 转换为 LocalDate（只关注日期部分）
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 计算日期之间的天数差
        return ChronoUnit.DAYS.between(localDate1, localDate2);
    }

    public static int getWeekNumber() {
        LocalDate currentDate = LocalDate.now();
        WeekFields weekFields = WeekFields.of(java.time.DayOfWeek.of(1), 1);
        int weekNumber = currentDate.get(weekFields.weekOfWeekBasedYear());
        return weekNumber;
    }

    public static Date getDateTimeByString(String dateString){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
             date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //本周剩余时间 ms
    public static long thisWeekRemainingTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.DATE, 1 * 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);//8
        calendar.set(Calendar.MINUTE, 0);//0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }
    //服务于 绝世仙门，获取当前时间整点分钟数，往后一分钟
    public static long getJSXMHourMinute(){
        Calendar cld = Calendar.getInstance();
        int h = cld.get(Calendar.HOUR_OF_DAY);
        cld.set(Calendar.HOUR_OF_DAY, h);
        cld.set(Calendar.SECOND, 0);
        cld.set(Calendar.MINUTE, cld.get(Calendar.MINUTE) + 1);
        cld.set(Calendar.MILLISECOND, 0);
        return cld.getTime().getTime()/1000;
    }
    //服务于 绝世仙门，定时任务执行时间
    public static long getTaskJSXMTime(){
        Calendar cld = Calendar.getInstance();
        int h = cld.get(Calendar.HOUR_OF_DAY);
        cld.set(Calendar.HOUR_OF_DAY, h);
        cld.set(Calendar.SECOND, 0);
        cld.set(Calendar.MINUTE, cld.get(Calendar.MINUTE) + 30);
        cld.set(Calendar.MILLISECOND, 0);
        return cld.getTime().getTime();
    }

    //服务于 绝世仙门，获取昨日凌晨时间戳
    public static long getYesterdayLongTime(){
        Calendar cld = Calendar.getInstance();
        cld.add(Calendar.DATE, -1);
        cld.set(Calendar.HOUR_OF_DAY,0);
        cld.set(Calendar.SECOND, 0);
        cld.set(Calendar.MINUTE, 0);
        cld.set(Calendar.MILLISECOND, 0);
        return cld.getTime().getTime()/1000;
    }


    public static long getNextRewardTimeStamp(){
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算次日凌晨 2 点
        LocalDateTime nextRewardTime = now.toLocalDate().atTime(2, 0);

        // 如果当前时间已经过了凌晨 2 点，设置为次日凌晨 2 点
        if (now.isAfter(nextRewardTime)) {
            nextRewardTime = nextRewardTime.plusDays(1);
        }

        // 转换为系统默认时区的毫秒级时间戳
        ZonedDateTime zonedNextRewardTime = nextRewardTime.atZone(ZoneId.systemDefault());
        return zonedNextRewardTime.toInstant().toEpochMilli();
    }


    public static boolean isOpenPVP(int beginTime,int endTime){
        LocalTime now = LocalTime.now(); // 获取当前时间
        LocalTime start = LocalTime.of(beginTime, beginTime); // 0 点
        LocalTime end = LocalTime.of(endTime, beginTime); // 2 点
        if (!now.isAfter(start) || !now.isBefore(end)) {
           return false;
        } else {
            return true;
        }
    }


    //两个日期之间间隔多少分钟
    public static long calculateMinutesDifference(Date date1, Date date2) {
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime localDateTime2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.between(localDateTime2, localDateTime1);
        return duration.toMinutes(); // 获取分钟差
    }

    //两个日期之间间隔多少天
    public static long calculateDayDifference(Date date1, Date date2) {
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime localDateTime2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.between(localDateTime2, localDateTime1);
        return duration.toDays(); // 获取天数差
    }

    //获取两个小时直接间隔多少个小时
    public static long getSubHour(Date before,Date now){
        Instant startInstant = before.toInstant();
        Instant endInstant = now.toInstant();

        // 计算时间间隔（秒）
        Duration duration = Duration.between(startInstant, endInstant);

        // 获取小时数
        return  duration.toHours();  // 获取完整的小时数，舍去分钟
    }



}
