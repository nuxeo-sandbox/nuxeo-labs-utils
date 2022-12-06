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
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.ImagingService;

/**
 * Resizes an image using the default platform service
 * 
 * @since 2021.27
 */
@Operation(id = PictureRotate.ID, category = Constants.CAT_CONVERSION, label = "Rotate Picture", description = "Rotate the input blob using the platform default ImagingComponent. "
        + "angle is a required integer")
public class PictureRotate {

    public static final String ID = "Labs.PictureRotate";

    @Context
    protected ImagingService imagingService;

    @Param(name = "angle", required = true)
    protected Integer angle;

    @OperationMethod
    public Blob run(Blob input) {

        Blob rotated = imagingService.rotate(input, angle);
        // It often returns a blob with a .null extension...
        String fileName = rotated.getFilename();
        String ext = FilenameUtils.getExtension(fileName);
        if (StringUtils.isBlank(ext) || "null".equals(ext)) {
            rotated.setFilename(input.getFilename());
        }

        return rotated;
    }
}
