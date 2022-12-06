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
package nuxeo.labs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.ecm.platform.video.adapter.VideoDocumentAdapter;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import nuxeo.labs.utils.operations.videos.VideoGetInfo;

/**
 *
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
@Deploy("org.nuxeo.ecm.platform.commandline.executor")
@Deploy("org.nuxeo.ecm.platform.video")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestVideoOperations {

    public static final String TEST_VIDEO_FILE = "files/Sunset Video.mp4";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected TransactionalFeature transactionalFeature;
    
    protected Blob createBlobFromTestVideo() throws IOException {
        File f = FileUtils.getResourceFileFromContext(TEST_VIDEO_FILE);
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        
        return blob;
    }

    protected DocumentModel createVideoWithTestVideo(Blob videoBlob, boolean checkRenditionsAreCalculated) throws IOException {

        // Get the test file and create a Picture document with it
        if(videoBlob == null) {
            videoBlob = createBlobFromTestVideo();
        }

        DocumentModel doc;
        doc = session.createDocumentModel("/", "testDoc", "Video");
        doc.setPropertyValue("file:content", (Serializable) videoBlob);
        doc = session.createDocument(doc);
        // Wait for default conversions to be computed
        transactionalFeature.nextTransaction();
        doc.refresh();

        if (checkRenditionsAreCalculated) {
            assertNotNull(doc.getPropertyValue("vid:transcodedVideos"));
            VideoDocumentAdapter adapter = new VideoDocumentAdapter(doc);
            assertNotNull(adapter);
            assertTrue(adapter.getTranscodedVideos().size() > 0);
        }

        return doc;
    }

    @Test
    public void shouldGetVideoInfo() throws Exception {

        Blob input = createBlobFromTestVideo();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        automationService.run(ctx, VideoGetInfo.ID);

        Object o = ctx.get("nxlabs_blobVideoInfo");
        assertNotNull(o);
        assertTrue(o instanceof VideoInfo);
        VideoInfo vi = (VideoInfo) o;
        // Specific to this TEST_VIDEO_FILE
        assertEquals(null, vi.getDuration(), 11.85, 0);
        assertEquals(null, vi.getFrameRate(), 29.97, 0);
        assertEquals(vi.getWidth(), 320);
        assertEquals(vi.getHeight(), 180);
        assertTrue(vi.getFormat().indexOf("mov") > -1 && vi.getFormat().indexOf("mp4") > -1);
    }
}