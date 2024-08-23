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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 *
 */
@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class, ImagingFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.commandline.executor")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
public class TestConverters {

    public static final String TEST_IMAGE_FILE = "files/Desert.jpg";

    @Inject
    ConversionService conversionService;

    @Test
    public void TestConcatenateImagesConverter() throws Exception {

        List<Blob> twoBlobs = new ArrayList<Blob>();

        File f = FileUtils.getResourceFileFromContext("files/Chrysanthemum.jpg");
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        twoBlobs.add(blob);

        f = FileUtils.getResourceFileFromContext("files/Desert.jpg");
        blob = Blobs.createBlob(f, "image/jpeg");
        twoBlobs.add(blob);

        SimpleBlobHolder sbh = new SimpleBlobHolder(twoBlobs);

        Map<String, Serializable> params = new HashMap<>();
        params.put("targetFileName", "final.jpg");

        params.put("destMimeType", "image/jpeg");

        BlobHolder result = conversionService.convert("concatenateImages", sbh, params);
        Blob resultBlob = result.getBlob();
        Assert.assertNotNull(resultBlob);

    }

}
