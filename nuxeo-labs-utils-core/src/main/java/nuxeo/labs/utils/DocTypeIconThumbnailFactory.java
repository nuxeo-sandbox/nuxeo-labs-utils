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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailFactory;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.thumbnail.ThumbnailConstants;
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

    public static final String NO_THUMBNAIL_FALLBACK_NAME = "noThumbnail.png";

    protected static Blob noThumbnailFallbackBlob = null;

    protected static File noThumbnailFallbackFile = null;

    @Override
    public Blob getThumbnail(DocumentModel doc, CoreSession session) {

        try {
            if (doc.hasFacet(ThumbnailConstants.THUMBNAIL_FACET)) {
                Blob thumbnail = (Blob) doc.getPropertyValue(ThumbnailConstants.THUMBNAIL_PROPERTY_NAME);
                if (thumbnail != null) {
                    return thumbnail;
                }
            }
        } catch (PropertyException e) {
            // Ignore
        }

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
            String path = "nuxeo.war";
            if (!iconPath.startsWith(File.separator)) {
                path += File.separator;
            }
            path += iconPath;
            File iconFile = FileUtils.getResourceFileFromContext(path);
            // iconFile may be null in unit tests, where I could not find a way to access nuxeo.war...
            if (iconFile != null && iconFile.exists()) {
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

        return getThumbnailFallback();
    }

    protected Blob getThumbnailFallback() {

        // Never computed, or temporary file deleted
        if (noThumbnailFallbackBlob == null || (noThumbnailFallbackFile != null && !noThumbnailFallbackFile.exists())) {
            // Thanks to CoPilot ;-)
            String text = "(No thumbnail)";
            Font font = new Font("Arial", Font.PLAIN, 12);
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int width = fm.stringWidth(text) + 40;
            int height = fm.getHeight() + 20;
            g2d.dispose();

            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(0, 0, width - 1, height - 1);
            g2d.drawString(text, 10, fm.getAscent() + 10);
            g2d.dispose();

            try {
                noThumbnailFallbackBlob = Blobs.createBlobWithExtension(".png");
                noThumbnailFallbackFile = noThumbnailFallbackBlob.getFile();
                boolean ok = ImageIO.write(img, "png", noThumbnailFallbackFile);
                noThumbnailFallbackBlob.setMimeType("image/png");
                noThumbnailFallbackBlob.setFilename(NO_THUMBNAIL_FALLBACK_NAME);
                if (!ok) {
                    // Ignore
                }
            } catch (IOException ex) {
                // We have to give up
                ex.printStackTrace();
            }
        }

        return noThumbnailFallbackBlob;

    }

}
