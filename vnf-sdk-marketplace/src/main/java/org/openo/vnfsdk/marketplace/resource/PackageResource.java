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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openo.vnfsdk.marketplace.entity.response.UploadPackageResponse;
import org.openo.vnfsdk.marketplace.wrapper.PackageWrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * csar package service.
 * 
 * @author 10189609
 * 
 */
@Path("/PackageResource")
@Api(tags = {"Package Resource"})
public class PackageResource {

  @Path("/csars")
  @POST
  @ApiOperation(value = "upload csar package", response = UploadPackageResponse.class)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponses(value = {
      @ApiResponse(code = HttpStatus.NOT_FOUND_404, message = "microservice not found",
          response = String.class),
      @ApiResponse(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415,
          message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
      @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "resource grant error",
          response = String.class)})
  public Response uploadPackage(
      @ApiParam(value = "file inputstream",
          required = true) @FormDataParam("file") InputStream uploadedInputStream,@FormDataParam("params") String details,
      @ApiParam(value = "file detail",
          required = false) @FormDataParam("file") FormDataContentDisposition fileDetail,
      @ApiParam(value = "http header") @Context HttpHeaders head) throws Exception {
    return PackageWrapper.getInstance().uploadPackage(uploadedInputStream, fileDetail, details, head);
  }
}
