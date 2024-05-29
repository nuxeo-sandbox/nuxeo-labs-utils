/*
 * (C) Copyright 2024 Hyland (http://hyland.com/) and others.
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
package nuxeo.labs.utils.operations;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.Related;
import biweekly.property.Trigger;
import biweekly.util.Duration;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.PeriodAndDuration;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.io.IOException;
import java.util.Calendar;

/**
 * Return a blob, an .ics file built from the parameters
 * 
 * @since 2023.11
 */
@Operation(id = CreateICS.ID, category = Constants.CAT_SERVICES, label = "Create .ics File", description = ""
        + "Creates an .ics blob from the parameters. label and startDate are required."
        + " For the end date, you can pass either endDate or duration, which is a java Period (like PT1H30M)."
        + " Also, if fullDays is true, endDate and duration are optional"
        + " location is a string, like 'Room 1'. Can be a link to a Zoom or Teams meeting, etc."
        + " organizerMail is a string. If not passed, most calendar applications will consider current user as the organizer."
        + " attendees is a list of emails, separated by a comma."
        + " alarm is a java period, only days, hours and minutes accepted. P1DT2H30M (1 day, 2 hours, 30mn)"
        + " Warning: dates must be provided either as Java Calendar object or an ISO date string, make sure to specify a timezone.")
public class CreateICS {

    public static final String ID = "Labs.CreateICS";

    @Param(name = "label", required = true)
    protected String label;

    @Param(name = "startDate", required = true)
    Calendar startDate;

    @Param(name = "endDate", required = false)
    Calendar endDate;

    @Param(name = "duration", required = false)
    String duration;
    
    @Param(name = "fullDays", required = false)
    Boolean fullDays = false;

    @Param(name = "description", required = false)
    protected String description;

    @Param(name = "location", required = false)
    protected String location;

    @Param(name = "url", required = false)
    protected String url;

    @Param(name = "organizerMail", required = false)
    protected String organizerMail;
    
    @Param(name = "attendees", required = false)
    String attendees;
    
    @Param(name = "alarm", required = false)
    protected String alarm;

    @OperationMethod
    public Blob run() {

        ICalendar ical = new ICalendar();

        VEvent event = new VEvent();
        
        event.setSummary(label);
        
        if(fullDays) {
            event.setDateStart(startDate.getTime(), false);
        } else {
            event.setDateStart(startDate.getTime());
        }

        if (!fullDays && endDate == null && StringUtils.isBlank(duration)) {
            throw new IllegalArgumentException("Both endDate and duration cannot be empty");
        }
        if (endDate != null) {
            event.setDateEnd(endDate.getTime());
            if(fullDays) {
                event.setDateEnd(endDate.getTime(), false);
            } else {
                event.setDateEnd(endDate.getTime());
            }
        } else if(StringUtils.isNotBlank(duration)) {
            event.setDuration(Duration.parse(duration));
        }

        if (StringUtils.isNotBlank(description)) {
            event.setDescription(description);
        }

        if (StringUtils.isNotBlank(location)) {
            event.setLocation(location);
        }

        if (StringUtils.isNotBlank(url)) {
            event.setUrl(url);
        }

        if (StringUtils.isNotBlank(organizerMail)) {
            event.setOrganizer(organizerMail);
        }
        
        if(StringUtils.isNotBlank(attendees)) {
            String [] attendeesArray = attendees.split(",");
            for(String attendee : attendeesArray) {
                attendee = attendee.trim();
                event.addAttendee(attendee);
            }
        }
        
        if (StringUtils.isNotBlank(alarm)) {
            PeriodAndDuration pd = PeriodAndDuration.parse(alarm);
            int days = Math.abs(pd.period.getDays());
            int hours = Math.abs(pd.duration.toHoursPart());
            int minutes = Math.abs(pd.duration.toMinutesPart());
            
            Duration duration = Duration.builder()
                                        .prior(true)
                                        .days(days)
                                        .hours(hours)
                                        .minutes(minutes)
                                        .build();
            Trigger trigger = new Trigger(duration, Related.START);
            VAlarm eventAlarm = VAlarm.display(trigger, "");
            event.addAlarm(eventAlarm);
        }
        
        event.setUid(java.util.UUID.randomUUID().toString());

        ical.addEvent(event);

        Blob icsBlob;
        try {
            icsBlob = Blobs.createBlobWithExtension(".ics");

            try (ICalWriter iCalWriter = new ICalWriter(icsBlob.getFile(), ICalVersion.V2_0)) {
                iCalWriter.write(ical);
                icsBlob.setMimeType("text/calendar");
                // Cool to use Nuxeo utilities :-)
                label = FileUtils.getSafeFilename(label);
                icsBlob.setFilename(label + ".ics");

            } catch (IOException e) {
                throw new NuxeoException("Error writing the ics file", e);
            }

        } catch (IOException e) {
            throw new NuxeoException("Error creating/writing the ics file", e);
        }

        return icsBlob;
    }
}
