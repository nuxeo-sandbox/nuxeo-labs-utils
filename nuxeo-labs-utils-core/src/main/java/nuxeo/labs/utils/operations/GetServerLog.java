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

import java.io.File;
import java.io.IOException;

import org.nuxeo.common.Environment;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.utils.BlobUtils;

/**
 * Returns the zipped server.log
 * Notice it can't be unit tested, since during unit test, logs are not
 * written in the "regular" place.
 * 
 * @since 2021.27
 */
@Operation(id = GetServerLog.ID, category = Constants.CAT_SERVICES, label = "Get server.log", description = "Zip server.log and returns it.")
public class GetServerLog {

    public static final String ID = "Labs.GetServerLog";

    @OperationMethod
    public Blob run() throws IOException {

        String logPath = (String) Framework.getProperty(Environment.NUXEO_LOG_DIR);
        if (!logPath.endsWith("/")) {
            logPath += "/";
        }
        logPath += "server.log";

        File logFile = new File(logPath);
        Blob logBlob = new FileBlob(logFile);
        Blob zipped = BlobUtils.zip(logBlob, "server.log.zip");

        return zipped;
    }
}
