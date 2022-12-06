/*
 * (C) Copyright 2022 Nuxeo (http://nuxeo.com/) and others.
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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.labs.utils.operations.pictures.PictureGetInfo;

/**
 *
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
@Deploy("org.nuxeo.ecm.platform.commandline.executor")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestPictureGetInfo {

    public static final String TEST_IMAGE_FILE = "files/Desert.jpg";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldGetPictureInfo() throws OperationException, IOException {

        File f = FileUtils.getResourceFileFromContext(TEST_IMAGE_FILE);
        Blob input = Blobs.createBlob(f, "image/jpeg");

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        Map<String, Object> params = new HashMap<>();
        params.put("varName", "imageInfo");
        automationService.run(ctx, PictureGetInfo.ID, params);

        Object o = ctx.get("imageInfo");
        assertNotNull(o);
        assertTrue(o instanceof ImageInfo);
        ImageInfo ii = (ImageInfo) o;
        // Specific to this TEST_IMAGE_FILE
        assertEquals(ii.getColorSpace(), "sRGB");
        assertEquals(ii.getFormat(), "JPEG");
        assertEquals(ii.getWidth(), 1024);
        assertEquals(ii.getHeight(), 768);
    }
}
