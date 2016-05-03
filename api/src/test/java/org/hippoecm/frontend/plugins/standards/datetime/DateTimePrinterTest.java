/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugins.standards.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.ThreadContext;
import org.easymock.classextension.EasyMock;
import org.hippoecm.frontend.session.UserSession;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTimePrinterTest {

    private Date date70;
    private Instant instant70;
    private Calendar cal70;

    private Locale locale = Locale.GERMANY;
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Before
    public void before() throws Exception {
        UserSession session = EasyMock.createNiceMock(UserSession.class);
        EasyMock.expect(session.getLocale()).andAnswer(() -> locale).anyTimes();
        EasyMock.expect(session.getTimeZone()).andAnswer(() -> timeZone).anyTimes();
        EasyMock.replay(session);
        ThreadContext.setSession(session);

        cal70 = Calendar.getInstance();
        cal70.setTimeInMillis(0);
        date70 = cal70.getTime();
        instant70 = cal70.toInstant();
    }

    @Test
    public void testNull() throws Exception{
        assertEquals("", DateTimePrinter.of((Date)null).print());
        assertEquals("", DateTimePrinter.of((Calendar)null).print());
        assertEquals("", DateTimePrinter.of((Instant)null).print());
    }

    @Test
    public void testDate() throws Exception {
        assertEquals("01.01.1970 00:00", DateTimePrinter.of(date70).print());
    }

    @Test
    public void testCalendar() throws Exception {
        assertEquals("01.01.1970 00:00", DateTimePrinter.of(cal70).print());
    }

    @Test
    public void testInstant() throws Exception {
        assertEquals("01.01.1970 00:00", DateTimePrinter.of(instant70).print());
    }

    @Test
    public void testLocale() throws Exception {
        locale = Locale.JAPAN;
        assertEquals("1970/01/01 0:00", DateTimePrinter.of(date70).print());
    }

    @Test
    public void testTimeZone() throws Exception {
        timeZone = TimeZone.getTimeZone("America/Aruba"); // -4
        assertEquals("31.12.1969 20:00", DateTimePrinter.of(date70).print());
    }

    @Test
    public void testStyleShort() throws Exception {
        assertEquals("01.01.70 00:00", DateTimePrinter.of(date70).print(FormatStyle.SHORT));
    }

    @Test
    public void testStyleMedium() throws Exception {
        assertEquals("01.01.1970 00:00:00", DateTimePrinter.of(date70).print(FormatStyle.MEDIUM));
    }

    @Test
    public void testStyleLong() throws Exception {
        assertEquals("1. Januar 1970 00:00:00 UTC", DateTimePrinter.of(date70).print(FormatStyle.LONG));
    }

    @Test
    public void testStyleFull() throws Exception {
        assertEquals("Donnerstag, 1. Januar 1970 00:00 Uhr UTC", DateTimePrinter.of(date70).print(FormatStyle.FULL));
    }

    @Test
    public void testDateStyle() throws Exception {
        assertEquals("01.01.70 00:00", DateTimePrinter.of(date70).print(FormatStyle.SHORT, FormatStyle.SHORT));
        assertEquals("01.01.1970 00:00", DateTimePrinter.of(date70).print(FormatStyle.MEDIUM, FormatStyle.SHORT));
        assertEquals("1. Januar 1970 00:00", DateTimePrinter.of(date70).print(FormatStyle.LONG, FormatStyle.SHORT));
        assertEquals("Donnerstag, 1. Januar 1970 00:00", DateTimePrinter.of(date70).print(FormatStyle.FULL, FormatStyle.SHORT));
    }

    @Test
    public void testTimeStyle() throws Exception {
        assertEquals("01.01.70 00:00", DateTimePrinter.of(date70).print(FormatStyle.SHORT, FormatStyle.SHORT));
        assertEquals("01.01.70 00:00:00", DateTimePrinter.of(date70).print(FormatStyle.SHORT, FormatStyle.MEDIUM));
        assertEquals("01.01.70 00:00:00 UTC", DateTimePrinter.of(date70).print(FormatStyle.SHORT, FormatStyle.LONG));
        assertEquals("01.01.70 00:00 Uhr UTC", DateTimePrinter.of(date70).print(FormatStyle.SHORT, FormatStyle.FULL));
    }

    @Test
    public void testDST() throws Exception {
        timeZone = TimeZone.getTimeZone("Europe/Amsterdam");
        LocalDate dstInNL = LocalDate.of(2016, Month.MAY, 1);
        Date dstDate = Date.from(dstInNL.atStartOfDay(timeZone.toZoneId()).toInstant());
        assertEquals("01.05.16 00:00", DateTimePrinter.of(dstDate).print(FormatStyle.SHORT, FormatStyle.SHORT));
        assertEquals("01.05.16 00:00 (DST)", DateTimePrinter.of(dstDate).appendDST().print(FormatStyle.SHORT, FormatStyle.SHORT));
    }
}
