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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.automation.context.ContextService;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.labs.utils.automationhelpers.NxLabs;


/**
 * 
 * 
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core")
@Deploy("nuxeo.labs.utils.nuxeo-labs-utils-core:automation-scripting-contrib.xml")
public class TestAutomationHelpers {
    
    protected static final String PDF_TEST_FILE_PATH = "lorem-ipsum.pdf";

    @Inject
    CoreSession session;

    @Inject
    AutomationService automationService;

    @Inject
    ContextService ctxService;
    
    @Test
    public void testHelperIsAvailable() throws Exception {

        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        ContextHelper nxlabsHelper = contextHelperList.get("NxLabs");
        assertNotNull(nxlabsHelper);
        assertTrue(nxlabsHelper instanceof NxLabs);
    }
    
    @Test
    public void shouldGetFileExtension() throws Exception {
        
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("path", "blah/blih/bloh.txt");

        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetFileExtension", params);
        assertEquals("txt", result);
        
        // No extension
        params.put("path", "blah/blih/bloh");
        result = (String) automationService.run(ctx, "TestHelpers.GetFileExtension", params);
        assertTrue(StringUtils.isBlank(result));
                
    }

    
    @Test
    public void shouldGetBaseName() throws Exception {
        
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("path", "blah/blih/bloh.txt");

        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetBaseName", params);
        assertEquals("bloh", result);
        

        params.put("path", "blah/blih/");
        result = (String) automationService.run(ctx, "TestHelpers.GetFileExtension", params);
        assertTrue(StringUtils.isBlank(result));
                
    }

    
    @Test
    public void shouldGetUserFullName() throws Exception {
        
        UserManager userManager = Framework.getService(UserManager.class);
        DocumentModel userDoc = userManager.getBareUserModel();
        userDoc.setProperty("user", "username", "jdoe");
        userDoc.setProperty("user", "firstName", "John");
        userDoc.setProperty("user", "lastName", "Doe");
        userManager.createUser(userDoc);
        
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "jdoe");
        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetUserFullName", params);
        assertEquals("John Doe", result);

        params.put("userId", "Administrator");
        // Chain defined in automation-scripting-contrib.xml
        result = (String) automationService.run(ctx, "TestHelpers.GetUserFullName", params);
        assertEquals("Administrator", result); // No first/Last name
    }
    
    @Test
    public void shouldGetTheMD5OfABlob() throws OperationException {
        
        File testFile = FileUtils.getResourceFileFromContext(PDF_TEST_FILE_PATH);
        Blob blob = new FileBlob(testFile);
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blob);
        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetMD5");
        
        assertNotNull(result);
        assertEquals("7d5bc8da9154e0d7470500c16b225459", result);
    }
    
    @Test
    public void shouldGetTheBase64OfABlob() throws OperationException {
        
        File testFile = FileUtils.getResourceFileFromContext(PDF_TEST_FILE_PATH);
        Blob blob = new FileBlob(testFile);
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(blob);
        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetBase64Blob");
        
        assertNotNull(result);
        assertTrue(result.length() > blob.getLength());

    }
    
    @Test
    public void shouldGetTheBase64OfAString() throws OperationException {
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput("Hello there, Nuxeo rocks!");
        // Chain defined in automation-scripting-contrib.xml
        String result = (String) automationService.run(ctx, "TestHelpers.GetBase64String");
        
        assertNotNull(result);
        assertEquals("SGVsbG8gdGhlcmUsIE51eGVvIHJvY2tzIQ==", result);

    }

}
