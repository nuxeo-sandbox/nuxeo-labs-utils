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
package nuxeo.labs.utils.operations.videos;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;

/**
 * Get the VideoInfo about a video file
 * 
 * @since 2021.27
 */
@Operation(id = VideoGetInfo.ID, category = Constants.CAT_CONVERSION, label = "Get Video Info", description = ""
        + "Set the nxlabs_blobVideoInfo context variable with the "
        + "Java VideoInfo about the input blob: width, height, format, duration,frameRate and an array of streams. Returns the input blob unchanged.")
public class VideoGetInfo {

    public static final String ID = "Labs.VideoGetInfo";
    
    public static final String CTX_VAR_NAME = "nxlabs_blobVideoInfo";

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public Blob run(Blob input) {

        VideoInfo info = VideoHelper.getVideoInfo(input);

        ctx.put(CTX_VAR_NAME, info);

        return input;
    }
}
