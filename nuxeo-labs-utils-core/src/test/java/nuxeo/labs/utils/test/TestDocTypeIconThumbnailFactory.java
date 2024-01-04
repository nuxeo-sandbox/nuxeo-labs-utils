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

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

/**
 * 
 * @since 2023
 */
@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, ImagingFeature.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.types")
@Deploy("org.nuxeo.ecm.platform.webapp.types")
@Deploy("org.nuxeo.ecm.platform.thumbnail")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core:thumbnail-factory-test.xml")
// Too complicated to make sure the correct icons are deployed, giving up on iunit-testing
// (the live testing shows it's working)
@Ignore
public class TestDocTypeIconThumbnailFactory {

    @Inject
    protected CoreSession session;

    @Inject
    protected TransactionalFeature txFeature;

    @Context
    protected ThumbnailService thumbnailService;
    
    @Test
    public void shouldGetDefaultIcon() throws Exception {
        
     // Create Picture doc, waiting for renditions
        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, null, true);
        
        Thread.sleep(3000);
        txFeature.nextTransaction();
        
        doc = session.getDocument(doc.getRef());
        Blob thumbnail = thumbnailService.getThumbnail(doc, session);
        assertNotNull(thumbnail);
        
        // It should be the default Picture doc icon
        // To be continued once we can make sure pictuire__100.png is deployed...
    }

}
