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
 *     Damon Brown
 *     Thibaud Arguillere
 */
package nuxeo.labs.utils.operations.blobs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.core.blob.binary.Binary;
import org.nuxeo.ecm.core.blob.binary.BinaryManager;
import org.nuxeo.ecm.core.blob.binary.LazyBinary;
import org.nuxeo.runtime.api.Framework;

/**
 * Return the mimetype of the input blob in the nxlabs_mimetype context variable
 * 
 * @since 2021.27
 */
@Operation(id = VerifyBinaryHash.ID, category = Constants.CAT_BLOB, label = "Check Binary by Hash", description = ""
        + "digest parameter is required if no inbput blob is passed."
        + " If input is a blob, digest is ignored. If the blob has a digest, uses it else calculates a md5 digest for the blob."
        + " Check to see if a binary with the digest is found within the system. Returns the same digest if found, null otherwise.")
public class VerifyBinaryHash {

    public static final String ID = "Labs.VerifyBinaryHash";

    @Context
    protected CoreSession session;

    @Param(name = "digest", required = false)
    protected String digest;

    @Param(name = "provider", required = false)
    protected String provider;

    @OperationMethod
    public String run(Blob blob) throws IOException {

        digest = blob.getDigest();

        if (StringUtils.isBlank(digest)) {
            InputStream in = blob.getStream();
            digest = DigestUtils.md5Hex(in);
        }

        return run();
    }

    @OperationMethod
    public String run() {

        if(StringUtils.isBlank(digest)) {
            throw new IllegalArgumentException("digest parameter( is required when no blob is passed as input.");
        }
        
        BlobManager mgr = Framework.getService(BlobManager.class);
        Map<String, BlobProvider> providers = mgr.getBlobProviders();
        if (provider != null) {
          BlobProvider bp = providers.get(provider);
          BinaryManager bmgr = bp.getBinaryManager();
          if (bmgr != null) {
            return checkDigest(bmgr);
          }
        } else {
          for (Entry<String, BlobProvider> prov : providers.entrySet()) {
            BlobProvider bp = prov.getValue();
            BinaryManager bmgr = bp.getBinaryManager();
            if (bmgr == null) {
              continue;
            }
            return checkDigest(bmgr);
          }
        }
        
        return null;
    }

    private String checkDigest(BinaryManager bmgr) {
      Binary bin = bmgr.getBinary(digest);
      if (bin != null) {
        if (bin instanceof LazyBinary) {
          if (((LazyBinary) bin).getFile() != null) {
            return digest;
          }
        }
        return digest;
      }
      return null;
    }
}
