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
package nuxeo.labs.utils.automationhelpers;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * A set of Automation functions to handle misc utilities, small functions
 * that regularly are missing and require more JS or workarounds.
 * 
 * @since 2021.27
 */
public class NxLabs implements ContextHelper {

    /**
     * Returns the file extension for a given path, using org.apache.commons.io.FilenameUtils:
     *    foo.txt      --> "txt"
     *    a/b/c.jpg    --> "jpg"
     *    a/b.txt/c    --> ""
     *    a/b/c        --> ""
     * 
     * @param path
     * @return the file extension
     * @throws IOException
     * @since 2021.27
     */
    public String getFileExtension(String path) {
        
        return FilenameUtils.getExtension(path);
    }
    
    /**
     * Returns the file extension for a given path, using org.apache.commons.io.FilenameUtils:
     *    a/b/c.txt --> c
     *    a.txt     --> a
     *    a/b/c     --> c
     *    a/b/c/    --> ""
     * 
     * @param path
     * @return the file extension
     * @throws IOException
     * @since 2021.27
     */
    public String getBaseName(String path) {
        
        return FilenameUtils.getBaseName(path);
    }
    
    /**
     * returns First name + " " + Last name.
     * HAadle cases where there is no first name and/or no last name (return then the userId)
     * 
     * @param userId
     * @return the user full name
     * @since 2021.27
     */
    public String getUserFullName(String userId) {
        
        UserManager userManager = Framework.getService(UserManager.class);
        NuxeoPrincipal pcipal = userManager.getPrincipal(userId);
        if(pcipal == null) {
            return "";
        }
        
        String fullName = "";
        String firstName = pcipal.getFirstName();
        String lastName = pcipal.getLastName();
        
        if(StringUtils.isNotBlank(firstName)) {
            fullName = firstName;
        }
        
        if(StringUtils.isNotBlank(lastName)) {
          if(StringUtils.isNotBlank(firstName)) {
              fullName += " ";
          }
          fullName += lastName;
        }
        
        if(StringUtils.isBlank(fullName)) {
            fullName = userId;
        }
        
        return fullName;
    }
    
    /**
     * Commit current transaction, starts a new one.
     * Useful in a loop, when modifying/saving a lot of documents.
     * 
     * @since 2021.27
     */
    public void commitAndStartTransaction() {
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
    }
    
    /**
     * Sleeps ms milliseconds
     * 
     * @param ms
     * @throws InterruptedException
     * @since 2023.5
     */
    public void threadSleep (long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

}
