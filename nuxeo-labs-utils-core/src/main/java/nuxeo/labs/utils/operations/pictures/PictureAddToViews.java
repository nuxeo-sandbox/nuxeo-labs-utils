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
package nuxeo.labs.utils.operations.pictures;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.PictureViewImpl;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;

/**
 * Add a view (aka rendition) to picture:views
 * 
 * @since 2021.27
 */
@Operation(id = PictureAddToViews.ID, category = Constants.CAT_DOCUMENT, label = "Add blob to picture:views", description = ""
        + "Add a new view (viewName) to the picture:views field of the document referenced by the document parameter (id or path). "
        + "If viewName already exists, it is replaced. If fileName is not passed, the blob's file name is used. "
        + "If the document does not have the picture schema, the operation does nothing. "
        + "Returns the modified document (saved if saveDoc is passed and true). ")
public class PictureAddToViews {

    public static final String ID = "Labs.PictureAddToViews";

    @Context
    protected CoreSession session;

    @Context
    protected ImagingService imagingService;

    @Param(name = "document", required = true)
    protected DocumentModel doc;

    @Param(name = "viewName", required = true)
    protected String viewName;

    @Param(name = "description", required = false)
    protected String description;

    @Param(name = "fileName", required = false)
    protected String fileName;

    @Param(name = "saveDoc", required = false)
    protected Boolean saveDoc = false;

    @OperationMethod
    public DocumentModel run(Blob input) {

        if (doc.hasSchema("picture")) {

            if (StringUtils.isEmpty((fileName))) {
                fileName = input.getFilename();
            }

            ImageInfo info = imagingService.getImageInfo(input);
            PictureView view = new PictureViewImpl();
            view.setBlob(input);
            view.setDescription(description);
            view.setFilename(fileName);
            view.setHeight(info.getHeight());
            view.setImageInfo(info);
            view.setTitle(viewName);
            view.setWidth(info.getWidth());

            MultiviewPicture mvp = doc.getAdapter(MultiviewPicture.class);
            mvp.addView(view);

            if (saveDoc) {
                doc = session.saveDocument(doc);
            }
        }

        return doc;
    }
}
