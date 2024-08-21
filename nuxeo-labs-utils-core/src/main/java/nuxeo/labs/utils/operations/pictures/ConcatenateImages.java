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
package nuxeo.labs.utils.operations.pictures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;

/**
 * Return the ImageInfo in the nxlabs_ImageInfo context variable
 * 
 * @since 2021.27
 */
@Operation(id = ConcatenateImages.ID, category = Constants.CAT_CONVERSION, label = "Concatenate Images", description = ""
        + "Concatenates images found at files:files. "
        + "destMimeType is required, and so is targetFileName, which must also contains the file extension. "
        + "concatHorizontally is optional. By default, concatenation is vertical. "
        + "If there is only one blob, it is returned unchanged, not converted.")
public class ConcatenateImages {

    public static final String ID = "Labs.ConcatenateImages";

    private static final Logger log = LogManager.getLogger(ConcatenateImages.class);

    public static final String DEFAULT_XPATH = "files:files";

    @Context
    ConversionService conversionService;

    @Param(name = "targetFileName", required = true)
    protected String targetFileName;

    @Param(name = "destMimeType", required = true)
    protected String destMimeType;

    @Param(name = "concatHorizontally", required = false)
    protected Boolean concatHorizontally = false;

    @OperationMethod
    public Blob run(DocumentModel input) {

        Blob result = null;

        ArrayList<Blob> blobs = new ArrayList<Blob>();
        List<Map<String, Serializable>> files = (List<Map<String, Serializable>>) input.getPropertyValue(DEFAULT_XPATH);
        for (Map<String, Serializable> map : files) {
            Blob blob = (Blob) map.get("file");
            blobs.add(blob);
        }

        result = run(new BlobList(blobs));

        return result;
    }

    @OperationMethod
    public Blob run(BlobList blobs) {

        Blob result = null;

        if (blobs == null || blobs.size() == 0) {
            log.warn("No blob to process");
            return null;
        }

        if (blobs.size() == 1) {
            return blobs.get(0);
        }

        // Concat first 2 ones
        result = concatenate(blobs.get(0), blobs.get(1));
        // Now the others
        for (int i = 2; i < blobs.size(); i++) {
            result = concatenate(result, blobs.get(i));
        }

        return result;
    }

    protected Blob concatenate(Blob b1, Blob b2) {

        List<Blob> twoBlobs = new ArrayList<Blob>();
        twoBlobs.add(b1);
        twoBlobs.add(b2);
        SimpleBlobHolder sbh = new SimpleBlobHolder(twoBlobs);

        Map<String, Serializable> params = new HashMap<>();
        params.put("targetFileName", targetFileName);
        params.put("destMimeType", destMimeType);
        if (concatHorizontally != null && concatHorizontally) {
            params.put("horizontalAppend", "true");// All params must be strings
        }

        BlobHolder result = conversionService.convert("concatenateImages", sbh, params);
        Blob resultBlob = result.getBlob();

        return resultBlob;
    }
}
