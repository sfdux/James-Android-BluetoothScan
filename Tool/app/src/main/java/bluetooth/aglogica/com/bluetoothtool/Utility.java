package bluetooth.aglogica.com.bluetoothtool;

import java.text.SimpleDateFormat;

/**
 * Created by James.Shi on 12/30/14.
 */
public class Utility {
    /**
     * Get current date time
     *
     * @return String(now time)
     */
    public static String getCurrentDateTime() {
        return getCurrentDateTimeWithFormat(SystemConst.VALUE_DATETIME_FORMAT);
    }

    /**
     * Get current day
     *
     * @param seperator
     * @return String(Day)
     */
    public static String getCurrentDay(String seperator) {
        return getCurrentDateTimeWithFormat(String.format("yyyy%1$sMM%1$sdd", seperator));

    }

    /**
     * Format the current datetime
     *
     * @param dateFormat
     * @return String
     */
    public static String getCurrentDateTimeWithFormat(String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(System.currentTimeMillis());
    }
}
