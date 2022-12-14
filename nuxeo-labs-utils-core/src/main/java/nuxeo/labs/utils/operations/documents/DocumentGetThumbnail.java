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
package nuxeo.labs.utils.operations.documents;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;

/**
 * Return the the thumbnail of the input document
 * 
 * @since 2021.27
 */
@Operation(id = DocumentGetThumbnail.ID, category = Constants.CAT_DOCUMENT, label = "Get Thumbnail", description = ""
        + "Return the thumbnail of the input document.")
public class DocumentGetThumbnail {

    public static final String ID = "Labs.DocumentGetThumbnail";

    @Context
    protected CoreSession session;

    @Context
    protected ThumbnailService thumbnailService;

    @OperationMethod
    public Blob run(DocumentModel input) {

        return thumbnailService.getThumbnail(input, session);
    }
}
