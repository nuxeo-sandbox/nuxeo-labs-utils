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

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.ImagingService;

/**
 * Return the ImageInfo in a context variable
 * 
 * @since 2021.27
 */
@Operation(id = PictureGetInfo.ID, category = Constants.CAT_CONVERSION, label = "Get Picture Infos", description = "Get input blob info, returns it in the varName Context variable. If varName is not passed, use nxlabs_ImageInfo. This context variable contains a Java PictureGetInfo, with width, height, format, colorSpace and depth fields.")
public class PictureGetInfo {

    public static final String ID = "Labs.PictureGetInfo";

    protected static final String DEFAULT_CONTEXT_VAR_NAME = "nxlabs_ImageInfo";

    @Context
    protected OperationContext ctx;

    @Context
    protected ImagingService imagingService;

    @Param(name = "varName", required = false)
    protected String varName;

    @OperationMethod
    public Blob run(Blob input) {

        if (StringUtils.isEmpty(varName)) {
            varName = DEFAULT_CONTEXT_VAR_NAME;
        }

        ctx.put(varName, imagingService.getImageInfo(input));

        return input;
    }
}
