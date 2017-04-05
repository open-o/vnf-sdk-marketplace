/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.vnfsdk.marketplace.msb;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.vnfsdk.marketplace.common.CommonConstant;
import org.openo.vnfsdk.marketplace.common.JsonUtil;
import org.openo.vnfsdk.marketplace.rest.RestfulUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MsbRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsbRegistration.class);

    private static final String MSB_REGISTION_FILE = "etc/microservice/marketplace_rest.json";
    private static final String MSB_REGISTION_URL = "/openoapi/microservices/v1/services?createOrUpdate=false";
    private static final String NODES = "nodes";
    private static final String IP = "ip";
    public static final int REPEAT_REG_TIME = 30 * 1000;

    /**
     * Interface to handle MSB Registration
     * @return
     */
    public static int register() 
    {               
        File file = new File(MSB_REGISTION_FILE);
        if(!file.exists()) 
        {
            LOGGER.info("Stop registering as can't find msb registion file:" + file.getAbsolutePath());          
            return CommonConstant.MsbRegisterCode.MSDB_REGISTER_FILE_NOT_EXISTS;
        }

        Map<?, ?> msbRegistionBodyMap = null;
        try 
        {
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = Files.readAllBytes(Paths.get(MSB_REGISTION_FILE));
            msbRegistionBodyMap = mapper.readValue(bytes, Map.class);
        } 
        catch(IOException e) 
        {
            LOGGER.error("Failed to get microservice bus registration body, " + e);
            return CommonConstant.MsbRegisterCode.MSDB_REGISTER_FAILED;
        }

        replaceLocalIp(msbRegistionBodyMap);

        LOGGER.info("Registering body: " + JsonUtil.toJson(msbRegistionBodyMap));

        return sendRequest(msbRegistionBodyMap) 
                ? CommonConstant.MsbRegisterCode.MSDB_REGISTER_SUCESS 
                        : CommonConstant.MsbRegisterCode.MSDB_REGISTER_FAILED;
    }

    /**
     * Send MSB Registration request
     * @param msbRegistionBodyMap
     * @return
     */
    private static boolean sendRequest(Map<?, ?> msbRegistionBodyMap)  
    {
        LOGGER.info("Start registering to microservice bus");
        
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(CommonConstant.HttpContext.URL, MSB_REGISTION_URL);  
        paramsMap.put(CommonConstant.HttpContext.METHOD_TYPE, CommonConstant.MethodType.POST);          
        String rawData = JsonUtil.toJson(msbRegistionBodyMap);
        
        RestfulResponse response = RestfulUtil.sendRestRequest(paramsMap, rawData, null);
        return isSuccess(response.getStatus()) ? true : false;
    }
    
   
    @SuppressWarnings("unchecked")
    private static void replaceLocalIp(Map<?, ?> msbRegistionBodyMap) 
    {
        List<Map<String, String>> nodes = (List<Map<String, String>>)msbRegistionBodyMap.get(NODES);
        Map<String, String> node = nodes.get(0);
        if(StringUtils.isNotEmpty(node.get(IP))) 
        {
            return;
        }

        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            String ipAddress = addr.getHostAddress();
            node.put(IP, ipAddress);

            LOGGER.info("Local ip: " + ipAddress);
        } 
        catch(UnknownHostException e) 
        {
            LOGGER.error("Unable to get IP address, " + e);
        }
    }

    private static boolean isSuccess(int httpCode) 
    {
        return (httpCode == 200 || httpCode == 201) ? true : false;
    }
}
