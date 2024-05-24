/*
 * (C) Copyright 2022 Hyland (http://hyland.com/)  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package nuxeo.labs.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.labs.utils.operations.CreateICS;

/**
 * Actually, we can't unit test this, since during a unit test, logs
 * are not output in nuxeo.log.dir
 * 
 * @since 2021.28
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestCreateICS {

    protected static final String LABEL = "My Meeting";

    protected static final String DURATION = "PT1H";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    protected ZonedDateTime NOW;

    protected String NOW_ISO;

    protected String NOW_VCAL;

    @Before
    public void init() {

        NOW = ZonedDateTime.now();
        NOW_ISO = toISO(NOW);
        NOW_VCAL = toPartialVCal(NOW_ISO);
        // 2024-05-23T15:02:47.753757+02:00
    }

    protected String toISO(ZonedDateTime zdt) {

        String iso = zdt.format(DateTimeFormatter.ISO_DATE_TIME);
        // Failed to parse ISO 8601 date: 2024-05-23T14:37:02.511285+02:00[Europe/Paris]
        int idx = iso.indexOf("[");
        if (idx > 0) {
            iso = iso.substring(0, idx);
        }

        return iso;
    }

    /*
     * We just convert up to the Time (so we don't handle time offset)
     * 2024-05-23T15:02:47.753757+02:00 => 20240523T
     */
    protected String toPartialVCal(String isoDate) {
        String str = isoDate.substring(0, 11);
        str = str.replaceAll("-", "").replaceAll(":", "");

        return str;
    }

    @Test
    public void shouldCreateSimpleICSWithDuration() throws Exception {

        OperationContext ctx = new OperationContext(session);

        Map<String, Object> params = new HashMap<>();
        params.put("label", LABEL);
        params.put("startDate", NOW_ISO);
        params.put("duration", DURATION);

        Blob icsBlob = (Blob) automationService.run(ctx, CreateICS.ID, params);

        assertNotNull(icsBlob);
        assertEquals(LABEL + ".ics", icsBlob.getFilename());

        String raw = icsBlob.getString();

        assertTrue(raw.indexOf(LABEL) > -1);
        assertTrue(raw.indexOf("DTSTART:" + NOW_VCAL) > -1);
        assertTrue(raw.indexOf("DURATION:" + DURATION) > -1);

    }

    @Test
    public void shouldCreateSimpleICSWithEndDate() throws Exception {

        OperationContext ctx = new OperationContext(session);

        Map<String, Object> params = new HashMap<>();
        params.put("label", LABEL);
        params.put("startDate", NOW_ISO);

        ZonedDateTime inOneHour = NOW.plusHours(1);
        String inOneHourISO = toISO(inOneHour);
        params.put("endDate", inOneHourISO);

        Blob icsBlob = (Blob) automationService.run(ctx, CreateICS.ID, params);

        assertNotNull(icsBlob);
        assertEquals(LABEL + ".ics", icsBlob.getFilename());

        String raw = icsBlob.getString();

        assertTrue(raw.indexOf(LABEL) > -1);
        assertTrue(raw.indexOf("DTSTART:" + NOW_VCAL) > -1);
        String inOneHourVCal = toPartialVCal(inOneHourISO);
        assertTrue(raw.indexOf("DTEND:" + inOneHourVCal) > -1);

    }

    @Test
    public void shouldCreateFullICS() throws Exception {

        OperationContext ctx = new OperationContext(session);

        Map<String, Object> params = new HashMap<>();
        params.put("label", LABEL);
        params.put("startDate", NOW_ISO);
        params.put("duration", DURATION);
        params.put("description", "The description");
        params.put("location", "Room #1");
        params.put("url", "https://someserver.abc/a/b/c.html");
        params.put("organizerMail", "abc@def");
        params.put("attendees", "someone@abd.def, ghi@jkl, mno@pqr");
        params.put("alarm", "PT30M");

        Blob icsBlob = (Blob) automationService.run(ctx, CreateICS.ID, params);

        assertNotNull(icsBlob);
        assertEquals(LABEL + ".ics", icsBlob.getFilename());

        String raw = icsBlob.getString();

        assertTrue(raw.indexOf(LABEL) > -1);
        assertTrue(raw.indexOf("DTSTART:" + NOW_VCAL) > -1);
        assertTrue(raw.indexOf("DURATION:" + DURATION) > -1);
        assertTrue(raw.indexOf("ATTENDEE:mailto:someone@abd.def") > -1);
        assertTrue(raw.indexOf("ATTENDEE:mailto:ghi@jkl") > -1);
        assertTrue(raw.indexOf("ATTENDEE:mailto:mno@pqr") > -1);
        
        // Let's get rid of new lines types for this test
        String rawNoLines = raw.replaceAll("\\r", "").replaceAll("\\n", "");
        String alarm = "BEGIN:VALARM" + "ACTION:DISPLAY" + "TRIGGER;RELATED=START:-P0DT0H30M";
        assertTrue(rawNoLines.indexOf(alarm) > -1);
    }

    @Test
    public void shouldCreateSimpleICSFullDay() throws Exception {

        OperationContext ctx = new OperationContext(session);

        Map<String, Object> params = new HashMap<>();
        params.put("label", LABEL);
        // Time should be ignored
        params.put("startDate", "2030-05-28T03:04:05+02:00");
        params.put("endDate", "2030-05-31T05:04:05+02:00");
        params.put("fullDays", true);

        Blob icsBlob = (Blob) automationService.run(ctx, CreateICS.ID, params);

        assertNotNull(icsBlob);
        assertEquals(LABEL + ".ics", icsBlob.getFilename());

        String raw = icsBlob.getString();

        assertTrue(raw.indexOf(LABEL) > -1);
        assertTrue(raw.indexOf("DTSTART;VALUE=DATE:20300528") > -1);
        assertTrue(raw.indexOf("DTEND;VALUE=DATE:20300531") > -1);

    }

    @Test
    public void shouldCreateSimpleICSFullDayWithDuration() throws Exception {

        OperationContext ctx = new OperationContext(session);

        Map<String, Object> params = new HashMap<>();
        params.put("label", LABEL);
        // Time will be ignored
        params.put("startDate", "2030-05-28");
        params.put("duration", "P3D");
        params.put("fullDays", true);

        Blob icsBlob = (Blob) automationService.run(ctx, CreateICS.ID, params);

        assertNotNull(icsBlob);
        assertEquals(LABEL + ".ics", icsBlob.getFilename());

        String raw = icsBlob.getString();

        assertTrue(raw.indexOf(LABEL) > -1);
        assertTrue(raw.indexOf("DTSTART;VALUE=DATE:20300528") > -1);
        assertTrue(raw.indexOf("DURATION:P3D") > -1);

    }

}
