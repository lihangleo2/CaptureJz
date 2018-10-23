package com.leo.mycarm.widget.mosaicview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AADate {
	/**
	 * 获取当前时间（含时分秒）
	 * 
	 * @return
	 */
	public static String getTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sDateFormat.format(new Date());
	}

	public static String getStrTime(Date date) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sDateFormat.format(date);
	}

	/**
	 * 获取订单时间选择
	 * 
	 * @return
	 */
	public static String getDdChoiceTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		return sDateFormat.format(new Date());
	}

	public static String getTimeFromDdChoiceTime(String time) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.format(sDateFormat.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取当前时间（含时分）yyyy-MM-dd HH:mm
	 * 
	 * @return
	 */
	public static String getMinTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sDateFormat.format(new Date());
	}

	/**
	 * 获取当前日期（无时分秒）yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String getDate() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return sDateFormat.format(new Date());
	}

	/**
	 * 获取当前日期（无时分秒）yyyyMMddHHmm
	 * 
	 * @return
	 */
	public static String getDateStrName() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		return sDateFormat.format(new Date());
	}

	/**
	 * 从字符串日期转为Java日期
	 * 
	 * @param strtime
	 * @return
	 */
	public static Date getShotDateFromStr(String strdate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(strdate);
			return date;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从字符串日期时间转为Java日期时间yyyy-MM-dd HH:mm
	 * 
	 * @param strtime
	 * @return
	 */
	public static Date getMinShotTimeFromStr(String strdate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = sdf.parse(strdate);
			return date;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取几天后或几天前的时间，参数为正整数或负整数
	 * 
	 * @param count
	 * @return
	 */
	public static String getDateForward(int count) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.add(Calendar.DAY_OF_YEAR, count);
		Date date = calendar.getTime();
		return sdf.format(date);
	}

	/**
	 * 获取两个日期间隔几天 yyyy-MM-dd,time1减去time2
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getDateBetw(String time1, String time2) {
		SimpleDateFormat sdf;
		int days = 0;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = sdf.parse(time1);
			Date date2 = sdf.parse(time2);
			long diff = date1.getTime() - date2.getTime();
			days = (int) (diff / (1000 * 60 * 60 * 24));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -11;
		}
		return days;
	}

	/**
	 * 获取两个时间间隔几分钟,返回整数,time1减去time2
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getTimeBetw(String time1, String time2) {
		SimpleDateFormat sdf;
		int mins = 0;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date1 = sdf.parse(time1);
			Date date2 = sdf.parse(time2);
			long diff = date1.getTime() - date2.getTime();
			mins = (int) (diff / (1000 * 60));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -11;
		}
		return mins;
	}

	/**
	 * 获取当前日期星期数1~7
	 * 
	 * @param time
	 * @return
	 */
	public static String getWeekNum(String time) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(time);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (w == 0)
				w = 7;
			return String.valueOf(w);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将字符串时间转换为date yyyy-MM-dd HH:mm:ss
	 * 
	 * @param time
	 * @return
	 */
	public static Date getDateFromStr(String time) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = sdf.parse(time);
			return date;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取星期 yyyy年M月d日 E
	 * 
	 * @param time
	 * @return
	 */
	public static String getWeekTime(String time) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(time);
			sdf = new SimpleDateFormat("yyyy年M月d日 E", Locale.CHINA);
			return sdf.format(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isCommonMon(String time1, String time2) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = sdf.parse(time1);
			Date date2 = sdf.parse(time2);
			if (date1.getYear() == date2.getYear()
					&& date1.getMonth() == date2.getMonth()) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static Date getShotMonFromStr(String time) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM");
			Date date = sdf.parse(time);
			return date;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getShotStrMon() {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat("yyyy-MM");
			String strdate = sdf.format(new Date());
			return strdate;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
