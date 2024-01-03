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
package nuxeo.labs.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailFactory;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 2023
 */
public class DocTypeIconThumbnailFactory implements ThumbnailFactory {

    public static final String NO_PREVIEW_ICON_PATH = "img" + File.separator + "preview_not_available.png";

    public static final String NULL_DOC_TYPE = "(null)";

    // Caching the blobs
    protected static Map<String, Blob> docTypesAndBlobs = new HashMap<String, Blob>();

    @Override
    public Blob getThumbnail(DocumentModel doc, CoreSession session) {
        return getDefaultThumbnail(doc);
    }

    @Override
    public Blob computeThumbnail(DocumentModel doc, CoreSession session) {
        return getDefaultThumbnail(doc);
    }

    public Blob getDefaultThumbnail(DocumentModel doc) {

        Blob thumbnail;
        String docType;

        if (doc == null) {
            docType = NULL_DOC_TYPE;
        } else {
            docType = doc.getType();
        }

        thumbnail = docTypesAndBlobs.get(docType);
        if (thumbnail != null) {
            return thumbnail;
        }

        String iconPath = null;
        if (doc == null) {
            iconPath = NO_PREVIEW_ICON_PATH;
        } else {
            TypeInfo typeInfo = doc.getAdapter(TypeInfo.class);
            iconPath = typeInfo.getBigIcon();
            if (iconPath == null) {
                iconPath = typeInfo.getIcon();
            }
            if (iconPath == null) {
                iconPath = NO_PREVIEW_ICON_PATH;
            }
        }

        try {
            File iconFile = FileUtils.getResourceFileFromContext("nuxeo.war" + File.separator + iconPath);
            if (iconFile.exists()) {
                MimetypeRegistry mimetypeRegistry = Framework.getService(MimetypeRegistry.class);
                String mimeType = mimetypeRegistry.getMimetypeFromFile(iconFile);
                if (mimeType == null) {
                    mimeType = mimetypeRegistry.getMimetypeFromFilename(iconPath);
                }
                thumbnail = Blobs.createBlob(iconFile, mimeType);
                docTypesAndBlobs.put(docType, thumbnail);
                return thumbnail;
            }
        } catch (IOException e) {
            // Ignore
        }

        return null;
    }

}
