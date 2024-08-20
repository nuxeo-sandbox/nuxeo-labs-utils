/*
 * (C) Copyright 2024 Hyland (https://hyland.com/nuxeo) and others.
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
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.platform.convert.plugins.CommandLineConverter;

public class ConcatenateImagesConverter extends CommandLineConverter {

    // As defined in xml extension
    public static final String converterName = "concatenateImages";

    /**
     * REQUIRED INPUT PARAMETERS (in parameters)
     * "destMimeType" is required, it will set the result blob's mime type
     * "targetFileName" is required. Just the file name _with its extension_
     * "horizontalAppend" (optional): by default, we append vertically. If horizontalAppend is passed
     * and true, we append horizontally
     * WARNING: The class handles only 2 files, anc concatenate appends the 2d after the first in the blobHolder.
     * To concatenate more, just re-call the converter with the result of previous conversion
     */
    @Override
    public BlobHolder convert(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {

        List<Blob> blobs = blobHolder.getBlobs();
        // Check we have only 2
        if (blobs.size() != 2) {
            throw new NuxeoException(
                    "The concatenateImages converter can handle only exactly 2 input blobs. We received "
                            + blobs.size());
        }

        // And we have a destMimeType
        String destMimeType = (String) parameters.get("destMimeType");
        if (destMimeType == null) {
            throw new NuxeoException("The concatenateImages converter expects a required parameter, destMimeType");
        }

        parameters.put("image1Path", blobs.get(0).getFile().getAbsolutePath());
        parameters.put("image2Path", blobs.get(1).getFile().getAbsolutePath());
        // Caller set targetFileName with the correct file extension
        String horizontal = (String) parameters.get("horizontalAppend");
        if (horizontal != null && "true".equals(horizontal.toLowerCase())) {
            parameters.put("HorV", "+");
        } else {
            parameters.put("HorV", "-");
        }
        
        BlobHolder result = super.convert(blobHolder, parameters);
        Blob resultBlob = result.getBlob();
        resultBlob.setMimeType(destMimeType);

        return new SimpleBlobHolder(resultBlob);
    }

}
