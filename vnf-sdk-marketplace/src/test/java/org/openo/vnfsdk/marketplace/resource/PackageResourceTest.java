/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
 */

package org.openo.vnfsdk.marketplace.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.openo.vnfsdk.marketplace.wrapper.PackageWrapper;

public class PackageResourceTest {

    private PackageResource packageResource;

    private String instanceId = "1234567";

    @Before
    public void setUp() {
    	packageResource = new PackageResource();
    }

 // @Ignore
    @Test
    public void testUploadPackage() throws Exception {
      InputStream ins = null;
      Response result = null;
      Response result1 = null;
      Response result2 = null;
      // PackageData packageData = new PackageData();
      // packageData = getPackageData();

      FormDataContentDisposition fileDetail =
          FormDataContentDisposition.name("fileName").fileName("clearwater_ns.csar").build();

      final String filename = "clearwater_ns.csar";
      File packageFile = new File("src//test//resources//clearwater_ns.csar");
      try {
        ins = new FileInputStream(packageFile);
      } catch (FileNotFoundException e2) {
        e2.printStackTrace();
      }
      if (ins != null) {
        try {
//          result = PackageWrapper.getInstance().uploadPackage(ins, fileDetail, null,null);
        } catch (Exception e3) {
          e3.printStackTrace();
        }
      }
//      assertNotNull(result);
//      assertEquals(200, result.getStatus());
     // assertNotNull(result.getEntity());

      try {
        result1 = PackageWrapper.getInstance().uploadPackage(null, fileDetail, null,null);
      } catch (Exception e4) {
        e4.printStackTrace();
      }
      //assertEquals(500, result1.getStatus());

      try {
        result2 = PackageWrapper.getInstance().uploadPackage(ins, null, null,null);
      } catch (Exception e5) {
        e5.printStackTrace();
      }
      //assertEquals(500, result2.getStatus());
    }
}
