/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick;

import lombok.Getter;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * All the constants for the Broadwick framework are defined here.
 */
public final class BroadwickConstants {

    /**
     * Utility constructor (hidden).
     */
    private BroadwickConstants() {
    }

    /**
     * Convert a date object to an integer (number of days from a fixed start date, here 1/1/1900). All dates in the
     * database are stored as integer values using this method.
     * @param date the date object we are converting.
     * @return the number of days from a fixed 'zero date'.
     */
    public static int getDate(final DateTime date) {
        return Days.daysBetween(ZERO_DATE, date).getDays();
    }

    /**
     * Convert an integer (number of days from a fixed start date, here 1/1/1900) to a DateTime object. All dates in the
     * database are stored as integer values, this method converts that integer to a DateTime object.
     * @param date the integer date offset.
     * @return the datetime object corresponding to the 'zero date' plus date.
     */
    public static DateTime toDate(final int date) {
        return ZERO_DATE.plusDays(date);
    }

    /**
     * Convert a date object to an integer (number of days from a fixed start date, here 1/1/1900). All dates in the
     * database are stored as integer values using this method.
     * @param date       the date object we are converting.
     * @param dateFormat the format the date is in when doing the conversion.
     * @return the number of days from a fixed 'zero date'.
     */
    public static int getDate(final String date, final String dateFormat) {
        final DateTimeFormatter pattern = DateTimeFormat.forPattern(dateFormat);
        final DateTime dateTime = pattern.parseDateTime(date);
        return BroadwickConstants.getDate(dateTime);
    }

    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private static final DateTime ZERO_DATE = new DateTime(1900, 1, 1, 0, 0);
}
