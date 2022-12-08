/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.labs.automation.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;

/**
 * A set of Automation functions to handle files on disk (we are server side in all cases).
 * <p>
 * The original purpose of these helpers was to be able to quickly create/handle demo data from JavaScript Automation,
 * just creating a few hundred of documents and avoiding creating a Java plug-in, the marketplace package, installing in
 * the test server, etc.
 * <p>
 * <b>IMPORTANT WARNING ABOUT SECURITY</b><br/>
 * The helpers, by essence, run server side of course. And some helpers here can create/write/delete files and/or
 * folders => <b>MAKE SURE YOU DON't ALLOW EXTERNAL CALLS TO ACCESS FILES/FOLDERS OF YOUR SERVER</b><br/>
 * A helper cannot be called directly by itself, it must be used inside an operation, inside an Automation Chain.<br/>
 * So: please BE VERY CAREFUL, and hard code your values, and/or make sure the paths cannot be get/set from a REST call.
 * Typical example of very, very wrong way of using these helpers would be a chain that accepts a "path" parameter, and
 * call FileUtils.deleteFile() with this path for example. Don't do that.
 * <p>
 * 
 * @since 7.4
 */
public class NxLabs implements ContextHelper {

    /**
     * Load the whole content of the file and return the corresponding blob
     * 
     * @param inPath
     * @return
     * @throws IOException
     * @since 7.4
     */
    public void blahBlah() {
    }


}
