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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPictureAdapter;
import org.nuxeo.ecm.platform.picture.core.ImagingFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import nuxeo.labs.utils.operations.pictures.ConcatenateImages;
import nuxeo.labs.utils.operations.pictures.PictureAddToViews;
import nuxeo.labs.utils.operations.pictures.PictureCrop;
import nuxeo.labs.utils.operations.pictures.PictureGetInfo;
import nuxeo.labs.utils.operations.pictures.PictureRemoveFromViews;
import nuxeo.labs.utils.operations.pictures.PictureRotate;

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
public class TestPictureOperations {

    public static final String TEST_IMAGE_FILE = "files/Desert.jpg";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected ImagingService imagingService;

    @Inject
    protected TransactionalFeature txFeature;

    @Inject
    ConversionService conversionService;

    @Test
    public void shouldGetPictureInfo() throws Exception {

        Blob input = TestUtils.createBlobFromTestImage();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        automationService.run(ctx, PictureGetInfo.ID);

        Object o = ctx.get(PictureGetInfo.CTX_VAR_NAME);
        assertNotNull(o);
        assertTrue(o instanceof ImageInfo);
        ImageInfo ii = (ImageInfo) o;
        // Specific to this TEST_IMAGE_FILE
        assertEquals(ii.getColorSpace(), "sRGB");
        assertEquals(ii.getFormat(), "JPEG");
        assertEquals(ii.getWidth(), 1024);
        assertEquals(ii.getHeight(), 768);
    }

    @Test
    public void shouldAddtoView() throws Exception {

        Blob input = TestUtils.createBlobFromTestImage();
        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, input, true);

        // Check they were computed
        doc.refresh();
        assertNotNull(doc.getPropertyValue("picture:views"));
        MultiviewPictureAdapter adapter = new MultiviewPictureAdapter(doc);
        assertNotNull(adapter);
        assertTrue(adapter.getViews().length != 0);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        Map<String, Object> params = new HashMap<>();
        params.put("document", doc.getId());
        params.put("viewName", "new-view");
        params.put("fileName", "new-view-" + input.getFilename());
        params.put("description", "the description");
        params.put("saveDoc", true);
        doc = (DocumentModel) automationService.run(ctx, PictureAddToViews.ID, params);
        adapter = new MultiviewPictureAdapter(doc);
        PictureView view = adapter.getView("new-view");
        assertNotNull(view);
        assertEquals(input.getDigest(), view.getBlob().getDigest());

    }

    @Test
    public void shouldRemoveFromViewWithSave() throws Exception {

        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, null, true);
        MultiviewPictureAdapter adapter = new MultiviewPictureAdapter(doc);
        assertNotNull(adapter.getView("Small")); // One of the default rendition

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap<>();
        params.put("viewName", "Small");
        params.put("saveDoc", true);
        doc = (DocumentModel) automationService.run(ctx, PictureRemoveFromViews.ID, params);
        adapter = new MultiviewPictureAdapter(doc);
        assertNull(adapter.getView("Small"));

    }

    @Test
    public void shouldRemoveFromViewCaseInsensitive() throws Exception {

        DocumentModel doc = TestUtils.createPictureWithTestImage(session, txFeature, null, true);
        MultiviewPictureAdapter adapter = new MultiviewPictureAdapter(doc);
        assertNotNull(adapter.getView("Small")); // One of the default rendition

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap<>();
        params.put("viewName", "SMALL");
        doc = (DocumentModel) automationService.run(ctx, PictureRemoveFromViews.ID, params);
        adapter = new MultiviewPictureAdapter(doc);
        assertNull(adapter.getView("Small"));

    }

    @Test
    public void shouldCropImage() throws Exception {

        Blob input = TestUtils.createBlobFromTestImage();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        Map<String, Object> params = new HashMap<>();
        params.put("top", 10);
        params.put("left", 10);
        params.put("width", 100);
        params.put("height", 110);
        Blob cropped = (Blob) automationService.run(ctx, PictureCrop.ID, params);
        ImageInfo ii = imagingService.getImageInfo(cropped);
        assertEquals(ii.getWidth(), 100);
        assertEquals(ii.getHeight(), 110);

    }

    @Test
    public void shouldRotateAnImage() throws Exception {

        Blob input = TestUtils.createBlobFromTestImage();

        ImageInfo ii = imagingService.getImageInfo(input);
        int originalW = ii.getWidth();
        int originalH = ii.getHeight();
        // Just for the sake of this test, W and H must be different
        assertTrue(originalW != originalH);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        Map<String, Object> params = new HashMap<>();
        params.put("angle", 90);
        Blob rotated = (Blob) automationService.run(ctx, PictureRotate.ID, params);
        ii = imagingService.getImageInfo(rotated);
        int newW = ii.getWidth();
        int newH = ii.getHeight();

        assertEquals(originalW, newH);
        assertEquals(originalH, newW);

    }

    @Test
    public void TestConcatenateImagesOperation() throws Exception {

        List<Blob> twoBlobs = new ArrayList<Blob>();

        File f = FileUtils.getResourceFileFromContext("files/Chrysanthemum.jpg");
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        twoBlobs.add(blob);

        f = FileUtils.getResourceFileFromContext("files/Desert.jpg");
        blob = Blobs.createBlob(f, "image/jpeg");
        twoBlobs.add(blob);

        BlobList blobList = new BlobList(twoBlobs);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blobList);
        Map<String, Object> params = new HashMap<>();
        params.put("targetFileName", "final.jpg");
        params.put("destMimeType", "image/jpeg");
        Blob result = (Blob) automationService.run(ctx, ConcatenateImages.ID, params);

        Assert.assertNotNull(result);


    }

    @SuppressWarnings("deprecation")
    @Test
    public void TestConcatenateImagesOperationWith1ImageShouldNotConvert() throws Exception {

        List<Blob> blobs = new ArrayList<Blob>();

        File f = FileUtils.getResourceFileFromContext("files/Chrysanthemum.jpg");
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        blobs.add(blob);

        BlobList blobList = new BlobList(blobs);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blobList);
        Map<String, Object> params = new HashMap<>();
        params.put("targetFileName", "final.jpg");
        params.put("destMimeType", "image/jpeg");
        Blob result = (Blob) automationService.run(ctx, ConcatenateImages.ID, params);

        Assert.assertNotNull(result);
        // here the operation should have done nothing, we wanted
        // the same file extension, same mimetype => operation should have returned the blob
        String sourceHash = Files.asByteSource(f).hash(Hashing.md5()).toString();
        File resultFile = result.getFile();
        String resultHash = Files.asByteSource(resultFile).hash(Hashing.md5()).toString();
        assertEquals(sourceHash, resultHash);

    }

    @SuppressWarnings("deprecation")
    @Test
    public void TestConcatenateImagesOperationWith1ImageShouldConvert() throws Exception {

        List<Blob> blobs = new ArrayList<Blob>();

        File f = FileUtils.getResourceFileFromContext("files/Chrysanthemum.jpg");
        Blob blob = Blobs.createBlob(f, "image/jpeg");
        blobs.add(blob);

        BlobList blobList = new BlobList(blobs);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blobList);
        Map<String, Object> params = new HashMap<>();
        params.put("targetFileName", "final.png");
        params.put("destMimeType", "image/png");
        Blob result = (Blob) automationService.run(ctx, ConcatenateImages.ID, params);

        Assert.assertNotNull(result);
        // here the operation should have done nothing, we wanted
        // the same file extension, same mimetype => operation should have returned the blob
        String sourceHash = Files.asByteSource(f).hash(Hashing.md5()).toString();
        File resultFile = result.getFile();
        String resultHash = Files.asByteSource(resultFile).hash(Hashing.md5()).toString();
        assertNotSame(sourceHash, resultHash);

    }

}
