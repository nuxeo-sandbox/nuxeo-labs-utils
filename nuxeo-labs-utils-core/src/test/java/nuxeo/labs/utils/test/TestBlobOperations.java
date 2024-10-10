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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.core.blob.binary.BinaryManager;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import nuxeo.labs.utils.operations.blobs.BlobGetMimeType;
import nuxeo.labs.utils.operations.blobs.VerifyBinaryHash;

/**
 *
 */
@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestBlobOperations {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected TransactionalFeature txFeature;

    @Test
    public void shouldGetMimeType() throws Exception {

        Blob input = TestUtils.createBlobFromTestImage();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        automationService.run(ctx, BlobGetMimeType.ID);

        String mimeType = (String) ctx.get(BlobGetMimeType.CTX_VAR_NAME);
        assertEquals(TestUtils.TEST_IMAGE_MIME_TYPE, mimeType);

    }

    @Test
    public void shouldNotFindDigestString() throws Exception {

        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("digest", "123");
        String result = (String) automationService.run(ctx, VerifyBinaryHash.ID, params);

        assertNull(result);

    }

    @Test
    @Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core:test-default-binary-manager.xml")
    public void shouldFindExistingDigestString() throws Exception {

        Blob blob = TestUtils.createBlobFromTestImage();

        // Create a store blob in BinaryManager
        BlobManager mgr = Framework.getService(BlobManager.class);
        for (Entry<String, BlobProvider> prov : mgr.getBlobProviders().entrySet()) {
            if ("test".equals(prov.getKey())) {
                try {
                    String digest = prov.getValue().writeBlob(blob);
                    assertEquals(TestUtils.TEST_IMAGE_MD5, digest);
                    break;
                } catch (Exception ex) {
                }
            }
        }

        // Find it
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("digest", TestUtils.TEST_IMAGE_MD5);
        String result = (String) automationService.run(ctx, VerifyBinaryHash.ID, params);

        assertNotNull(result);
        assertEquals(TestUtils.TEST_IMAGE_MD5, result);

    }
    
    @Test
    public void shouldNotFindDigestBlob() throws Exception {
        
        Blob blob = TestUtils.createBlobFromTestImage();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blob);
        String result = (String) automationService.run(ctx, VerifyBinaryHash.ID);

        assertNull(result);

    }
    
    @Test
    @Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core:test-default-binary-manager.xml")
    public void shouldFindExistingDigestForBlob() throws Exception {

        Blob blob = TestUtils.createBlobFromTestImage();

        // Create a store blob in BinaryManager
        BlobManager mgr = Framework.getService(BlobManager.class);
        for (Entry<String, BlobProvider> prov : mgr.getBlobProviders().entrySet()) {
            if ("test".equals(prov.getKey())) {
                try {
                    String digest = prov.getValue().writeBlob(blob);
                    assertEquals(TestUtils.TEST_IMAGE_MD5, digest);
                    break;
                } catch (Exception ex) {
                }
            }
        }

        // Find it
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blob);
        String result = (String) automationService.run(ctx, VerifyBinaryHash.ID);

        assertNotNull(result);
        assertEquals(TestUtils.TEST_IMAGE_MD5, result);

    }
}
