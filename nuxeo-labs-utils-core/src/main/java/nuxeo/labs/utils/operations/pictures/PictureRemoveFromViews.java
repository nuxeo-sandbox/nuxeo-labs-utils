/*
 * (C) Copyright Hyland (http://hyland.com/)  and others.
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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;

/**
 * Remove a view (aka rendition) from picture:views
 * 
 * @since 2021.27
 */
@Operation(id = PictureRemoveFromViews.ID, category = Constants.CAT_DOCUMENT, label = "Picture Remove View", description = ""
        + "Remove the view viewName (case insensitive) from the input document. Save the document if saveDoc is true. Returns the document. "
        + "The operation does nothing if input document does not have the picture schema, or if the view is not found.")
public class PictureRemoveFromViews {

    public static final String ID = "Labs.PictureRemoveFromViews";

    @Context
    protected CoreSession session;

    @Param(name = "viewName", required = true)
    protected String viewName;

    @Param(name = "saveDoc", required = false)
    protected Boolean saveDoc = false;

    @OperationMethod
    public DocumentModel run(DocumentModel input) {
        
        if(input.hasSchema("picture")) {
            
            MultiviewPicture mvp = input.getAdapter(MultiviewPicture.class);
            // This operation states viewName is case insensitive
            if(mvp.getView(viewName) == null) {
                String viewNameLowerCase = viewName.toLowerCase();
                for(PictureView pv : mvp.getViews()) {
                    if(pv.getTitle().toLowerCase().equals(viewNameLowerCase)) {
                        viewName = pv.getTitle();
                        break;
                    }
                }
            }
            
            if(mvp.getView(viewName) != null) {
                mvp.removeView(viewName);
                if(saveDoc) {
                    input = session.saveDocument(input);
                }
            }
        }
        
        return input;
    }
}
