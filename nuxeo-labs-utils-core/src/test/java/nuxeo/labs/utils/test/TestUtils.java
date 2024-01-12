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
import java.io.Serializable;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPictureAdapter;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

/**
 * @since TODO
 */
public class TestUtils {

    public static final String TEST_IMAGE_FILE = "files/Desert.jpg";

    public static Blob createBlobFromTestImage() throws IOException {
        File f = FileUtils.getResourceFileFromContext(TEST_IMAGE_FILE);
        Blob blob = Blobs.createBlob(f, "image/jpeg");

        return blob;
    }

    public static DocumentModel createPictureWithTestImage(CoreSession session, TransactionalFeature txFeature,
            Blob imageBlob, boolean checkViewsAreCalculated) throws Exception {

        // Get the test file and create a Picture document with it
        if (imageBlob == null) {
            imageBlob = createBlobFromTestImage();
        }

        DocumentModel doc;
        doc = session.createDocumentModel("/", "testDoc", "Picture");
        doc.setPropertyValue("file:content", (Serializable) imageBlob);
        doc = session.createDocument(doc);
        // Wait for default views to be computed
        txFeature.nextTransaction();
        
        doc.refresh();
        if (checkViewsAreCalculated) {
            assertNotNull(doc.getPropertyValue("picture:views"));
            MultiviewPictureAdapter adapter = new MultiviewPictureAdapter(doc);
            assertNotNull(adapter);
            assertTrue(adapter.getViews().length != 0);
        }

        return doc;
    }

}
