/*
 * (C) Copyright 2022 Hyland (http://hyland.com/) and others.
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
import biweekly.component.VEvent;
import biweekly.io.text.ICalWriter;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.util.Duration;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.io.IOException;
import java.util.Date;
import java.util.Calendar;

/**
 * Return a blob, an .ics file buyilt from the parameters
 * 
 * @since 2023.11
 */
@Operation(id = CreateICS.ID, category = Constants.CAT_SERVICES, label = "Create .ics File", description = ""
        + "Creates an .ics blob from the parameters. label and startDate are required."
        + " For the end date, you can pass either endDate or duration, which is a java Period (like PT1H30M)."
        + " location is string, like 'Room 1'."
        + "é Warning: you can pass an ISO date for startDate and endDate. Make sure to specify a timezone.")
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

    @Param(name = "description", required = false)
    protected String description;

    @Param(name = "location", required = false)
    protected String location;

    @Param(name = "url", required = false)
    protected String url;

    @Param(name = "organizerMail", required = false)
    protected String organizerMail;

    @OperationMethod
    public Blob run() {

        ICalendar ical = new ICalendar();

        VEvent event = new VEvent();
        
        event.setSummary(label);

        event.setDateStart(startDate.getTime());

        if (endDate == null && StringUtils.isBlank(duration)) {
            throw new IllegalArgumentException("Both endDate and duration cannot be empty");
        }
        if (endDate != null) {
            event.setDateEnd(endDate.getTime());
        } else {
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

        event.setUid(java.util.UUID.randomUUID().toString());

        ical.addEvent(event);

        Blob icsBlob;
        try {
            icsBlob = Blobs.createBlobWithExtension(".ics");

            try (ICalWriter iCalWriter = new ICalWriter(icsBlob.getFile(), ICalVersion.V2_0)) {
                iCalWriter.write(ical);
                icsBlob.setMimeType("text/calendar");
                icsBlob.setFilename(label + ".ics");

            } catch (IOException e) {
                throw new NuxeoException("Error writing the ics file", e);
            }

        } catch (IOException e) {
            throw new NuxeoException("Error creatin/writing the ics file", e);
        }

        return icsBlob;
    }
}
