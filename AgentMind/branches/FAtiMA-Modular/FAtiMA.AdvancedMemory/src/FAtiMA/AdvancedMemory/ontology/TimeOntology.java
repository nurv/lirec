/** 
 * TimeOntology.java - A class for abstracting times and dates.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 29/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 29/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeOntology implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final short ABSTRACTION_MODE_PART_OF_DAY = 0;
	public static final short ABSTRACTION_MODE_DAY_OF_WEEK = 1;
	public static final short ABSTRACTION_MODE_YEAR_MONTH_DAY = 2;

	private static final String[] PARTS_OF_DAY = { "Night", "Morning", "Afternoon", "Evening" };
	private static final String[] DAYS_OF_WEEK = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private short abstractionMode;

	public short getAbstractionMode() {
		return abstractionMode;
	}

	public void setAbstractionMode(short abstractionMode) {
		this.abstractionMode = abstractionMode;
	}

	/**
	 * Takes a variable number of dates/times and returns a description matching
	 * all of them.
	 * 
	 * @param timesInMillis
	 *            array of dates/times, use Calendar.getTimeInMillis()
	 * @return formatted date/time string
	 */
	public static String getGeneralisedDateTimeStr(long[] timesInMillis) {
		if (timesInMillis == null || timesInMillis.length == 0) {
			return null;
		} else {
			boolean sameYear = true;
			boolean sameMonth = true;
			boolean sameDayOfMonth = true;
			boolean sameHourOfDay = true;
			boolean sameMinute = true;
			boolean sameSecond = true;
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timesInMillis[0]);
			for (int i = 1; i < timesInMillis.length; i++) {
				Calendar calendarNew = Calendar.getInstance();
				calendarNew.setTimeInMillis(timesInMillis[i]);
				if (calendarNew.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) {
					sameYear = false;
				}
				if (calendarNew.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
					sameMonth = false;
				}
				if (calendarNew.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) {
					sameDayOfMonth = false;
				}
				if (calendarNew.get(Calendar.HOUR_OF_DAY) != calendar.get(Calendar.HOUR_OF_DAY)) {
					sameHourOfDay = false;
				}
				if (calendarNew.get(Calendar.MINUTE) != calendar.get(Calendar.MINUTE)) {
					sameMinute = false;
				}
				if (calendarNew.get(Calendar.SECOND) != calendar.get(Calendar.SECOND)) {
					sameSecond = false;
				}
			}
			String timeStr = null;
			// British date/time formatting
			String formatStr = "";
			if (sameYear) {
				formatStr += "yyyy";
				if (sameMonth) {
					formatStr = "MM/" + formatStr;
					if (sameDayOfMonth) {
						formatStr = "dd/" + formatStr;
						if (sameHourOfDay) {
							formatStr += " HH";
							if (sameMinute) {
								formatStr += ":mm";
								if (sameSecond) {
									formatStr += ":ss";
								}
							} else {
								formatStr = formatStr.substring(0, 10);
								timeStr = getPartOfDayStr(getPartOfDay(timesInMillis[0]));
							}
						} else {
							timeStr = getGeneralisedPartOfDayStr(getGeneralisedPartOfDay(timesInMillis));
						}
					}
				}
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
			String returnStr = simpleDateFormat.format(calendar.getTime());
			if (timeStr != null) {
				returnStr += " " + timeStr;
			}
			return returnStr;
		}
	}

	private static int getDayOfWeek(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Returns the name of a day of the week.
	 * 
	 * @param dayOfWeek
	 *            day of week from 0 to 6
	 * @return name of day
	 */
	public static String getDayOfWeekStr(int dayOfWeek) {
		return DAYS_OF_WEEK[dayOfWeek];
	}

	/**
	 * Returns the name of a day of the week for a given date/time.
	 * 
	 * @param timeInMillis
	 *            date/time, use Calendar.getTimeInMillis
	 * @return name of day
	 */
	public static String getDayOfWeekStr(long timeInMillis) {
		return DAYS_OF_WEEK[getDayOfWeek(timeInMillis)];
	}

	private static int getPartOfDay(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (hourOfDay >= 0 && hourOfDay < 6) {
			return 0;
		} else if (hourOfDay >= 6 && hourOfDay < 12) {
			return 1;
		} else if (hourOfDay >= 12 && hourOfDay < 18) {
			return 2;
		} else if (hourOfDay >= 18 && hourOfDay < 24) {
			return 3;
		}
		return -1;
	}

	/**
	 * Returns the name of a part of the day.
	 * 
	 * @param partOfDay
	 *            part of day from 0 to 3
	 * @return name of part
	 */
	public static String getPartOfDayStr(int partOfDay) {
		return PARTS_OF_DAY[partOfDay];
	}

	/**
	 * Returns the name of a part of the day for a given date/time.
	 * 
	 * @param timeInMillis
	 *            date/time, use Calendar.getTimeInMillis
	 * @return name of part
	 */
	public static String getPartOfDayStr(long timeInMillis) {
		return PARTS_OF_DAY[getPartOfDay(timeInMillis)];
	}

	/**
	 * Returns year, month, day
	 * 
	 * @param timeInMillis
	 *            date/time, use Calendar.getTimeInMillis()
	 * @return String in format yyyy-MM-dd
	 */
	public static String getYearMonthDayStr(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		SimpleDateFormat sdfYearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
		return sdfYearMonthDay.format(calendar.getTime());
	}

	/**
	 * Returns the abstraction for a given date/time.
	 * 
	 * @param timeInMillis
	 *            date/time, use Calendar.getTimeInMillis()
	 * @return abstracted date/time
	 */
	public String getAbstractedStr(long timeInMillis) {
		switch (abstractionMode) {
		case ABSTRACTION_MODE_PART_OF_DAY:
			return getPartOfDayStr(timeInMillis);
		case ABSTRACTION_MODE_DAY_OF_WEEK:
			return getDayOfWeekStr(timeInMillis);
		case ABSTRACTION_MODE_YEAR_MONTH_DAY:
			return getYearMonthDayStr(timeInMillis);
		default:
			return null;
		}
	}

	/**
	 * Takes a variable number of dates/times and the part of the day matching
	 * all of them.
	 * 
	 * @param timesInMillis
	 *            array of dates/times, use Calendar.getTimeInMillis()
	 * @return part of day from 0 to 3 or -1 if dates/times are not matching
	 */
	public static int getGeneralisedPartOfDay(long[] timesInMillis) {
		if (timesInMillis == null || timesInMillis.length == 0) {
			return -1;
		} else {
			int generalisedPartOfDay = getPartOfDay(timesInMillis[0]);
			for (int i = 1; i < timesInMillis.length; i++) {
				if (getPartOfDay(timesInMillis[i]) != generalisedPartOfDay) {
					generalisedPartOfDay = -1;
				}
			}
			return generalisedPartOfDay;
		}
	}

	/**
	 * Returns the name for a given part of the day or "".
	 * 
	 * @param generalisedPartOfDay
	 *            part of day from -1 to 3
	 * @return name of part or "" for -1
	 */
	public static String getGeneralisedPartOfDayStr(int generalisedPartOfDay) {
		if (generalisedPartOfDay == -1) {
			return "";
		} else {
			return getPartOfDayStr(generalisedPartOfDay);
		}
	}

}
