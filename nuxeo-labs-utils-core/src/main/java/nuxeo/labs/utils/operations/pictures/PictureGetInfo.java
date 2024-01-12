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

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.ImagingService;

/**
 * Return the ImageInfo in the nxlabs_ImageInfo context variable
 * 
 * @since 2021.27
 */
@Operation(id = PictureGetInfo.ID, category = Constants.CAT_CONVERSION, label = "Get Picture Infos", description = "Get input blob info, returns it in the nxlabs_ImageInfo Context variable. This context variable contains a Java PictureGetInfo, with width, height, format, colorSpace and depth fields.")
public class PictureGetInfo {

    public static final String ID = "Labs.PictureGetInfo";
    
    public static final String CTX_VAR_NAME = "nxlabs_ImageInfo";

    @Context
    protected OperationContext ctx;

    @Context
    protected ImagingService imagingService;

    @OperationMethod
    public Blob run(Blob input) {

        ctx.put(CTX_VAR_NAME, imagingService.getImageInfo(input));

        return input;
    }
}
