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
package nuxeo.labs.utils.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import nuxeo.labs.utils.operations.documents.DocumentGetThumbnail;

/**
 *
 */
@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, ImagingFeature.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.thumbnail")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestDocumentOperations {

    public static final String TEST_IMAGE_FILE = "files/Desert.jpg";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected TransactionalFeature txFeature;
    
    protected Blob createBlobFromTestImage() throws IOException {
        File f = FileUtils.getResourceFileFromContext(TEST_IMAGE_FILE);
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        
        return blob;
    }

    @Test
    public void shouldGetThumbnailFromPictureDoc() throws Exception {
        
        // Create Picture doc, waiting for renditions
        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, null, true);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        Blob thumbnail = (Blob) automationService.run(ctx, DocumentGetThumbnail.ID);
        
        assertNotNull(thumbnail);
        // Must be an image
        assertTrue(thumbnail.getMimeType().startsWith("image/"));
        
    }
}
