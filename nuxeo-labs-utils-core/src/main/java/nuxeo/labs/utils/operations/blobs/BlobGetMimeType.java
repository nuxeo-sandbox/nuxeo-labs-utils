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
        + "Return the mimetype of the input blob inthe nxlabs_mimetype Context Variable. " + "Returns the input blob unchanged.")
public class BlobGetMimeType {

    public static final String ID = "Labs.BlobGetMimeType";
    
    public static final String CTX_VAR_NAME = "nxlabs_mimetype";

    @Context
    protected OperationContext ctx;

    @Context
    protected MimetypeRegistry mimetypeService;

    @Param(name = "varName", required = false)
    protected String varName;

    @OperationMethod
    public Blob run(Blob input) {

        ctx.put(CTX_VAR_NAME, mimetypeService.getMimetypeFromBlob(input));

        return input;
    }
}
