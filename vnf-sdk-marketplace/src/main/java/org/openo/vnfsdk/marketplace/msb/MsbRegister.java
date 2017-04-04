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

import javax.servlet.http.HttpServlet;

import org.openo.vnfsdk.marketplace.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("serial")
public class MsbRegister extends HttpServlet 
{
    private static final Logger logger = LoggerFactory.getLogger(MsbRegister.class);
    
    public void init( ServletConfig servletConfig ) throws ServletException 
    {
        logger.info("VNF-SDK Market Place MSB Register called");
        super.init(servletConfig);

        ExecutorService es = Executors.newFixedThreadPool(CommonConstant.ONBOARDING_THREAD_COUNT);
        es.submit(new Callable<Integer>()
        {
            public Integer call() throws Exception 
            {
                MsbRegistrar oMsbRegisterTimer = new MsbRegistrar();
                oMsbRegisterTimer.handleMsbRegistration();
                return CommonConstant.SUCESS;
            }
        });    
    }
        
    public class MsbRegistrar
    {        
        public void handleMsbRegistration() 
        {
            int retry = 0;
            while(CommonConstant.MsbRegisterCode.MSDB_REGISTER_RETRIES >= retry) 
            {
                retry++;
                logger.info("VNF-SDK Market Place microservice register.retry:" + retry);
                               
                int retCode = MsbRegistration.register();
                if(CommonConstant.MsbRegisterCode.MSDB_REGISTER_FILE_NOT_EXISTS == retCode)
                {
                    logger.info("microservice register failed, MSB Register File Not Exists !");
                    break;
                }
                
                if(CommonConstant.MsbRegisterCode.MSDB_REGISTER_SUCESS != retCode) 
                {
                    logger.warn("microservice register failed, sleep 15-Sec and try again.");
                    threadSleep(CommonConstant.MsbRegisterCode.MSDB_REGISTER_RETRY_SLEEP);
                } 
                else 
                {
                    logger.info("microservice register success !");
                    break;
                }               
            }
            logger.info("VNF-SDK Market Place microservice register end.");
        }
        
        private void threadSleep(int second) 
        {
            try 
            {
                Thread.sleep(second);
            } 
            catch(InterruptedException error) 
            {
                logger.error("thread sleep error.errorMsg:", error);
                Thread.currentThread().interrupt();
            }
        }
    }
} 

