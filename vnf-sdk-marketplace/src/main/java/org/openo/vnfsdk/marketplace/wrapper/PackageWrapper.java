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
package org.openo.vnfsdk.marketplace.wrapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.openo.vnfsdk.marketplace.common.CommonConstant;
import org.openo.vnfsdk.marketplace.common.RestUtil;
import org.openo.vnfsdk.marketplace.common.ToolUtil;
import org.openo.vnfsdk.marketplace.db.entity.PackageData;
import org.openo.vnfsdk.marketplace.db.exception.MarketplaceResourceException;
import org.openo.vnfsdk.marketplace.db.resource.PackageManager;
import org.openo.vnfsdk.marketplace.entity.request.PackageBasicInfo;
import org.openo.vnfsdk.marketplace.entity.response.PackageMeta;
import org.openo.vnfsdk.marketplace.entity.response.UploadPackageResponse;
import org.openo.vnfsdk.marketplace.filemanage.FileManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageWrapper {
  private static PackageWrapper packageWrapper;
  private static final Logger LOG = LoggerFactory.getLogger(PackageWrapper.class);

  /**
   * get PackageWrapper instance.
   * @return package wrapper instance
   */
  public static PackageWrapper getInstance() {
    if (packageWrapper == null) {
      packageWrapper = new PackageWrapper();
    }
    return packageWrapper;
  }

  /**
   * query package list by condition.
   * @param name package name
   * @param provider package provider
   * @param version package version
   * @param deletionPending package deletionPending
   * @param type package type
   * @return Response
   */
  public Response queryPackageListByCond(String name, String provider, String version,
      String deletionPending, String type) {
    ArrayList<PackageData> dbresult = new ArrayList<PackageData>();
    ArrayList<PackageMeta> result = new ArrayList<PackageMeta>();
    LOG.info("query package info.name:" + name + " provider:" + provider + " version" + version
        + " deletionPending" + deletionPending + " type:" + type);
    try {
      dbresult =
          PackageManager.getInstance().queryPackage(name, provider, version, deletionPending, type);
      result = PackageWrapperUtil.packageDataList2PackageMetaList(dbresult);
      return Response.ok(ToolUtil.objectToString(result)).build();
    } catch (MarketplaceResourceException e1) {
      LOG.error("query package by csarId from db error ! " + e1.getMessage());
      return RestUtil.getRestException(e1.getMessage());
    }
  }

  /**
   * query package by id.
   * @param csarId package id
   * @return Response
   */
  public Response queryPackageById(String csarId) {
    PackageData dbResult = new PackageData();
    PackageMeta result = new PackageMeta();
    dbResult = PackageWrapperUtil.getPackageInfoById(csarId);
    result = PackageWrapperUtil.packageData2PackageMeta(dbResult);
    return Response.ok(ToolUtil.objectToString(result)).build();
  }
  
  /**
   * upload package.
   * @param uploadedInputStream inputStream
   * @param fileDetail package detail
   * @param head http header
   * @return Response
   * @throws Exception e
   */
  public Response uploadPackage(InputStream uploadedInputStream,
      FormDataContentDisposition fileDetail, String details, HttpHeaders head) throws Exception {
    
	  int fileSize = 0;
	    if (uploadedInputStream == null) {
	      LOG.info("the uploadStream is null");
	      return Response.serverError().build();
	    }
	    if (fileDetail == null) {
	      LOG.info("the fileDetail is null");
	      return Response.serverError().build();
	    }
	    LOG.info("the fileDetail = " + ToolUtil.objectToString(fileDetail));
	    String contentRange = null;
	    String fileName = "";
	    fileName = ToolUtil.processFileName(fileDetail.getFileName());
	    String tempDirName = null;
	    tempDirName = ToolUtil.getTempDir(CommonConstant.CATALOG_CSAR_DIR_NAME, fileName);
	    if (head != null) {
	      contentRange = head.getHeaderString(CommonConstant.HTTP_HEADER_CONTENT_RANGE);
	    }
	    LOG.info("store package chunk file, fileName:" + fileName + ",contentRange:" + contentRange);
	    if (ToolUtil.isEmptyString(contentRange)) {
	      fileSize = uploadedInputStream.available();
	      contentRange = "0-" + fileSize + "/" + fileSize;
	    }
	    String fileLocation =
	        ToolUtil.storeChunkFileInLocal(tempDirName, fileName, uploadedInputStream);
	    LOG.info("the fileLocation when upload package is :" + fileLocation);
	    uploadedInputStream.close();

	    Boolean isEnd = PackageWrapperUtil.isUploadEnd(contentRange, fileName);
	    PackageData packateDbData = new PackageData();
	    UploadPackageResponse result = new UploadPackageResponse();
	    if (isEnd) {
	      PackageBasicInfo basicInfo = new PackageBasicInfo();
	      basicInfo = PackageWrapperUtil.getPacageBasicInfo(fileLocation);
	      if (null == basicInfo.getType() || null == basicInfo.getProvider()
	          || null == basicInfo.getVersion()) {
	        LOG.error(
	            "Package basicInfo is incorrect ! basicIonfo = " + ToolUtil.objectToString(basicInfo));
	        return Response.serverError().build();
	      }
	      String path =
	          basicInfo.getType().toString() + File.separator + basicInfo.getProvider() + File.separator
	              + fileName.replace(".csar", "") + File.separator + basicInfo.getVersion();
	      LOG.info("dest path is : " + path);
	      PackageMeta packageMeta = new PackageMeta();
	      packageMeta = PackageWrapperUtil.getPackageMeta(fileName, fileLocation, basicInfo, details);
	      String dowloadUri = File.separator + path + File.separator;
	      packageMeta.setDownloadUri(dowloadUri);
	      LOG.info("packageMeta = " + ToolUtil.objectToString(packageMeta));
	      
	      PackageData packageData = PackageWrapperUtil.getPackageData(packageMeta);
	      String destPath = File.separator + path;
	      boolean uploadResult = FileManagerFactory.createFileManager().upload(tempDirName, destPath);
	      if (uploadResult == true) {
	        packateDbData = PackageManager.getInstance().addPackage(packageData);
	        packateDbData = packageData;
	        LOG.info("Store package data to database succed ! packateDbData = "
	            + ToolUtil.objectToString(packateDbData));
	       
	        //validate package
	        //TODO validate,life cycle and function test hooks to be added
	        //HookService.validatePackage(fileLocation);
	        
	        //function test package
	        //TODO validate,life cycle and function test hooks to be added
//	        DeployPackageResponse res = HookService.functionTest(fileLocation,packageData.getCsarId());
//	        result.setFunctestReport(res.getReportPath());
	        LOG.info("upload package file end, fileName:" + fileName);
	        result.setCsarId(packateDbData.getCsarId());
	        if (tempDirName != null) {
	          ToolUtil.deleteDir(new File(tempDirName));
	        }
	      }
	    }
	    return Response.ok(ToolUtil.objectToString(result), MediaType.APPLICATION_JSON).build();
  }

  /**
   * delete package by package id.
   * @param csarId package id
   * @return Response
   */
  public Response delPackage(String csarId) {
    LOG.info("delete package  info.csarId:" + csarId);
    if (ToolUtil.isEmptyString(csarId)) {
      LOG.error("delete package  fail, csarid is null");
      return Response.serverError().build();
    }
    String packagePath = PackageWrapperUtil.getPackagePath(csarId);
    if (packagePath == null) {
      LOG.error("package path is null! ");
    }
    FileManagerFactory.createFileManager().delete(packagePath);
    //delete package data from database
    try {
      PackageManager.getInstance().deletePackage(csarId);
    } catch (MarketplaceResourceException e1) {
      LOG.error("delete package  by csarId from db error ! " + e1.getMessage(), e1);
    }
    return Response.ok().build();
  }
  
  /**
   * download package by package id.
   * @param csarId package id
   * @return Response
   */
  public Response downloadCsarPackagesById(String csarId) {
    PackageData packageData = PackageWrapperUtil.getPackageInfoById(csarId);
    String packageName = null;
    packageName = packageData.getName();
    String path = "."+File.separatorChar+".."+File.separatorChar
    		+ "webapps"+File.separatorChar+"Demo"+File.separatorChar+"WEB-INF"+File.separatorChar+
    		"tomcat"+File.separatorChar+"webapps"+File.separatorChar+"ROOT"+File.separatorChar+packageData.getType()+File.separatorChar+
    		packageData.getProvider()+File.separatorChar+packageName+File.separatorChar+"v1.0";
    File csarFile = new File(path);
    if (!csarFile.exists()) {
      return Response.status(Status.NOT_FOUND).build();
    }

    try {
      String fileName=null;
      String filePath = null;
      for(File files:csarFile.listFiles())
      {
       if(files.isFile())
       {
    	   filePath = files.getAbsolutePath();
    	   fileName = files.getName();
    	   break;
       }
      }
      InputStream fis = new BufferedInputStream(new FileInputStream(filePath));
      return Response.ok(fis)
          .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
          .build();
    } catch (Exception e1) {
      LOG.error("download vnf package fail.", e1);
      return RestUtil.getRestException(e1.getMessage());
    }
  }
  
  /**
   * get package file uri.
   * @param csarId package id
   * @param relativePath file relative path
   * @return Response
   */
  public Response getCsarFileUri(String csarId) {
      return downloadCsarPackagesById(csarId);
  }
}
