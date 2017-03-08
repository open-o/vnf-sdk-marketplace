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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openo.vnfsdk.marketplace.entity.response.CsarFileUriResponse;
import org.openo.vnfsdk.marketplace.entity.response.PackageMeta;
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
	  @GET
	  @ApiOperation(value = "get csar package list by condition", response = PackageMeta.class,
	      responseContainer = "List")
	  @Produces(MediaType.APPLICATION_JSON)
	  @ApiResponses(value = {
	      @ApiResponse(code = HttpStatus.NOT_FOUND_404, message = "microservice not found",
	          response = String.class),
	      @ApiResponse(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415,
	          message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
	      @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "resource grant error",
	          response = String.class)})
	  public Response queryPackageListByCond(
	      @ApiParam(value = "csar name") @QueryParam("name") String name, @ApiParam(
	          value = "csar provider") @QueryParam("provider") String provider, @ApiParam(
	          value = "csar version") @QueryParam("version") String version, @ApiParam(
	          value = "delay to delete") @QueryParam("deletionPending") String deletionPending,
	      @ApiParam(value = "csar type") @QueryParam("type") String type) {
	    return PackageWrapper.getInstance().queryPackageListByCond(name, provider, version,
	        deletionPending, type);
	  }
	 
	  @Path("/csars/{csarId}")
	  @GET
	  @ApiOperation(value = "get csar package list", response = PackageMeta.class,
	      responseContainer = "List")
	  @Produces(MediaType.APPLICATION_JSON)
	  @ApiResponses(value = {
	      @ApiResponse(code = HttpStatus.NOT_FOUND_404, message = "microservice not found",
	          response = String.class),
	      @ApiResponse(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415,
	          message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
	      @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "resource grant error",
	          response = String.class)})
	  public Response queryPackageById(
	      @ApiParam(value = "csar id") @PathParam("csarId") String csarId) {
	    return PackageWrapper.getInstance().queryPackageById(csarId);
	  }
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
  
  @Path("/csars/{csarId}")
  @DELETE
  @ApiOperation(value = "delete a package")
  @ApiResponses(value = {
      @ApiResponse(code = HttpStatus.NOT_FOUND_404, message = "microservice not found",
          response = String.class),
      @ApiResponse(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415,
          message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
      @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "resource grant error",
          response = String.class)})
  public Response delPackage(@ApiParam(value = "csar Id") @PathParam("csarId") String csarId) {
    return PackageWrapper.getInstance().delPackage(csarId);
  }
  
  @Path("/csars/{csarId}/files")
  @GET
  @ApiOperation(value = "get csar file uri by csarId", response = CsarFileUriResponse.class)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponses(value = {
      @ApiResponse(code = HttpStatus.NOT_FOUND_404, message = "microservice not found",
          response = String.class),
      @ApiResponse(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415,
          message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
      @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "resource grant error",
          response = String.class)})
  public Response getCsarFileUri(
      @ApiParam(value = "csar Id", required = true) @PathParam("csarId") String csarId
      /*@ApiParam(value = "csar file path",
          required = true) @QueryParam("relativePath") String relativePath*/) {
    return PackageWrapper.getInstance().getCsarFileUri(csarId);
  }
}
