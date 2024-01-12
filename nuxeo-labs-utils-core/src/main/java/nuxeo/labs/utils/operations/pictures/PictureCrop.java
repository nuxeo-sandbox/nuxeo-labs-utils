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
package nuxeo.labs.utils.operations.pictures;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

/**
 * Crop an image using the default platform service
 * 
 * @since 2021.27
 */
@Operation(id = PictureCrop.ID, category = Constants.CAT_CONVERSION, label = "Crop Picture", description = "Crop the input blob using the platform default ImagingComponent. "
        + "top, left width and height are required and integers (pixels)")
public class PictureCrop {

    public static final String ID = "Labs.PictureCrop";
    
    private static final Logger log = LogManager.getLogger(PictureCrop.class);

    @Context
    protected ImagingService imagingService;

    @Param(name = "top", required = true)
    protected Integer top;

    @Param(name = "left", required = true)
    protected Integer left;

    @Param(name = "width", required = true)
    protected Integer width;

    @Param(name = "height", required = true)
    protected Integer height;

    @OperationMethod
    public Blob run(Blob input) {

        Blob cropped = imagingService.crop(input, left, top, width, height);
        // Unfortunately, crop() does not set a file extension for the returned image. And we cannot make sure
        // it will always be a jpg, because it is configurable. So, we need to extract the info, which
        // is "costly"...
        String ext = FilenameUtils.getExtension(cropped.getFilename());
        if(ext.equalsIgnoreCase("null") || StringUtils.isBlank(ext)) {
            MimetypeRegistry mimeTypeService = Framework.getService(MimetypeRegistry.class);
            String baseName = FilenameUtils.getBaseName(cropped.getFilename());
            try {
                String mimeType = mimeTypeService.getMimetypeFromBlob(cropped);
                cropped.setMimeType(mimeType);
                switch(mimeType) {
                case "image/jpg":
                case "image/jpeg":
                  cropped.setFilename(baseName + ".jpg");
                  break;

                case "image/png":
                  cropped.setFilename(baseName + ".png");
                  break;

                default:
                    // Give up...
                  ext = null;
                  break;
                }
            } catch (Exception e) {
                log.error("PictureCrop: Error when getting the mimetype from the cropped blob", e);
            }
        }
        

        return cropped;
    }
}
