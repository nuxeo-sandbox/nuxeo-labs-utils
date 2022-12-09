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
package nuxeo.labs.utils.operations.blobs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;

/**
 * Return the mimetype of the input blob in the nxlabs_mimetype context variable
 * 
 * @since 2021.27
 */
@Operation(id = BlobGetMimeType.ID, category = Constants.CAT_BLOB, label = "Get Blob Mime-Type", description = ""
        + "Return the mimetype of the input blob inthe nxlabs_mimetype Context Variable. "
        + "Important: the operation first sniffs the binary, even if blob.getMimeType() if not empty, "
        + "or the file name has a valid extension."
        + "If the service fails to get the mimetype, set nxlabs_mimetype to null and details in the log as an error. "
        + "Returns the input blob unchanged. "
        + "Notice this call can be costly, the input blob temporarily duplicated on disk, etc.")
public class BlobGetMimeType {

    public static final String ID = "Labs.BlobGetMimeType";
    
    private static final Log log = LogFactory.getLog(BlobGetMimeType.class);
    
    public static final String CTX_VAR_NAME = "nxlabs_mimetype";

    @Context
    protected OperationContext ctx;

    @Context
    protected MimetypeRegistry mimetypeService;

    @Param(name = "varName", required = false)
    protected String varName;

    @OperationMethod
    public Blob run(Blob input) {
        
        String mimeType = null;
        
        try {
            mimeType = mimetypeService.getMimetypeFromBlob(input);
        } catch (Exception e) {
            // No idea why it's not working for videos... At least 2021.27, see the code
            // for getMimetypeFromBlob. Strangely, it creates a copy, with .bin extension
            // then try to get the mimetype from this... Works on image, fails on videos.
            if("no registered mimetype has extension: bin".equals(e.getMessage())) {
                try {
                    String fileName = input.getFilename();
                    String ext = FilenameUtils.getExtension(fileName);
                    if(StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(ext)) {
                        mimeType = mimetypeService.getMimetypeFromFilename(fileName);
                        log.warn("Mime type (" + mimeType + ") from filename (" + fileName + "), not from sniffing the blob");
                    } else {
                        log.error("Failed to get the mimetype", e);
                        mimeType = null;
                    }
                } catch (Exception e2) {
                    log.error("Failed to get the mimetype", e2);
                    mimeType = null;
                }
                
            } else {
                log.error("Failed to get the mimetype", e);
                mimeType = null;
            }
        }

        ctx.put(CTX_VAR_NAME, mimeType);

        return input;
    }
}
