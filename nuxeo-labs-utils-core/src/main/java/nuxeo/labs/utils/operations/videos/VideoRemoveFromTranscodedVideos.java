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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.video.TranscodedVideo;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;

/**
 * Remove an entry to the transcoded videos field.
 * 
 * @since 2021.27
 */
@Operation(id = VideoRemoveFromTranscodedVideos.ID, category = Constants.CAT_DOCUMENT, label = "Remove from Transcoded Videos", description = ""
        + "Remove an entry from vid:transcodedVideos. "
        + "If renditionName is not found orif the document does not have the video schema, the operation does nothing. "
        + "Returns the modified document (saved if saveDoc is passed and true). ")
public class VideoRemoveFromTranscodedVideos {

    public static final String ID = "Labs.VideoRemoveFromTranscodedVideos";

    public static final String TRANSCODED_VIDEOS_FIELD = "vid:transcodedVideos";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "renditionName", required = true)
    protected String renditionName;

    @Param(name = "saveDoc", required = false)
    protected Boolean saveDoc = false;

    @OperationMethod
    public DocumentModel run(DocumentModel input) {
        
        if(!input.hasSchema("video")) {
            return input;
        }

        // This code is inspired from RecomputeTranscodedVideosComputation#saveRendition and
        // VideoConversionWork#saveNewTranscodedVideo
        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> transcodedVideos = (List<Map<String, Serializable>>) input.getPropertyValue(
                TRANSCODED_VIDEOS_FIELD);
        if (transcodedVideos != null) {
            transcodedVideos = transcodedVideos.stream()
                                               .filter(map -> !renditionName.equals(map.get("name")))
                                               .collect(Collectors.toList());
            input.setPropertyValue(TRANSCODED_VIDEOS_FIELD, (Serializable) transcodedVideos);

            if (saveDoc) {
                input = session.saveDocument(input);
            }
        }

        return input;
    }
}
