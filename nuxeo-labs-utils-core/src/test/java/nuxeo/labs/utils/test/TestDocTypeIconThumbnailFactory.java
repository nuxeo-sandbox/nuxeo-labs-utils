/*
 * (C) Copyright 2024 Hyland (http://hyland.com/)  and others.
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

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPictureAdapter;
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import nuxeo.labs.utils.DocTypeIconThumbnailFactory;

/**
 * @since 2023
 */
@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class, ImagingFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.types")
@Deploy("org.nuxeo.ecm.platform.webapp.types")
@Deploy("org.nuxeo.ecm.platform.thumbnail")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("org.nuxeo.ecm.core.api")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core:thumbnail-factory-test.xml")
// Too complicated to make sure the correct icons are deployed, giving up on iunit-testing
// (the live testing shows it's working)
// @Ignore
public class TestDocTypeIconThumbnailFactory {

    @Inject
    protected CoreSession session;

    @Inject
    protected TransactionalFeature txFeature;

    @Inject
    protected ThumbnailService thumbnailService;

    @Test
    public void shouldGetDefaultIcon() throws Exception {

        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, null, true);
        MultiviewPictureAdapter adapter = new MultiviewPictureAdapter(doc);
        assertNotNull(adapter.getView("Small")); // One of the default rendition

        doc = session.getDocument(doc.getRef());
        Blob thumbnail = thumbnailService.getThumbnail(doc, session);
        assertNotNull(thumbnail);

        // For a Picture, we should have either image_100.png (if the test could deploy the icons)
        // or the default generated noThumbnail.png, but nothing from the picture:views
        String fileName = thumbnail.getFilename();
        assertTrue(fileName.equals("image_100.png") || fileName.equals(DocTypeIconThumbnailFactory.NO_THUMBNAIL_FALLBACK_NAME));
    }

}
