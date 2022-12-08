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
 * Add the input blob to the transcoded videos field, after getting all its VideoInfo.
 * 
 * @since 2021.27
 */
@Operation(id = VideoAddToTranscodedVideos.ID, category = Constants.CAT_DOCUMENT, label = "Add to Transcoded Videos", description = ""
        + "Add a new rendition (renditionName) to the vid:transcodedVideos field of the document referenced by the document parameter (id or path). "
        + "It gets the info on the video (width, height, streams, ...) before adding it. "
        + "If renditionName already exists, it is replaced. "
        + "If the document does not have the video schema, the operation does nothing. "
        + "Returns the modified document (saved if saveDoc is passed and true). ")
public class VideoAddToTranscodedVideos {

    public static final String ID = "Labs.VideoAddToTranscodedVideos";

    public static final String TRANSCODED_VIDEOS_FIELD = "vid:transcodedVideos";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "document", required = true)
    protected DocumentModel doc;

    @Param(name = "renditionName", required = true)
    protected String renditionName;

    @Param(name = "saveDoc", required = false)
    protected Boolean saveDoc = false;

    @OperationMethod
    public DocumentModel run(Blob input) {

        if (!doc.hasSchema("video")) {
            return doc;
        }

        VideoInfo info = VideoHelper.getVideoInfo(input);
        TranscodedVideo transcodedInput = TranscodedVideo.fromBlobAndInfo(renditionName, input, info);

        // This code is inspired from RecomputeTranscodedVideosComputation#saveRendition and
        // VideoConversionWork#saveNewTranscodedVideo
        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> transcodedVideos = (List<Map<String, Serializable>>) doc.getPropertyValue(
                TRANSCODED_VIDEOS_FIELD);
        if (transcodedVideos == null) {
            transcodedVideos = new ArrayList<>();
        } else {
            transcodedVideos = transcodedVideos.stream()
                                               .filter(map -> !transcodedInput.getName().equals(map.get("name")))
                                               .collect(Collectors.toList());
        }
        transcodedVideos.add(transcodedInput.toMap());
        doc.setPropertyValue(TRANSCODED_VIDEOS_FIELD, (Serializable) transcodedVideos);

        if (saveDoc) {
            doc = session.saveDocument(doc);
        }

        return doc;
    }
}
