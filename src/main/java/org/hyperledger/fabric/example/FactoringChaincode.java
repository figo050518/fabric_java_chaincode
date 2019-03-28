/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.example;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import io.netty.handler.ssl.OpenSsl;
import io.netty.util.internal.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.example.bean.Factor;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

public class FactoringChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(FactoringChaincode.class);

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            _logger.info("Init java factoring chaincode");
            String func = stub.getFunction();
            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }
            _logger.info("begin test keepAlive");
            stub.putStringState("keepAliveTest","keepAliveTestValue");
            _logger.info("end test keepAlive");
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke java factoring chaincode");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            _logger.debug("Invoke function is "+stub.getFunction()+"Parameter is "+params.toString());
            if(params.isEmpty() || params.size()<1 || params.get(0).length()==0){
                return newErrorResponse("the invoke args not exist  or arg[0] is emptyt");
            }
            if (func.equals("SaveData")) {
                return saveData(stub, params);
            }
            if (func.equals("KeepaliveQuery")) {
                return keepaliveQuery(stub, params);
            }
            if (func.equals("QueryDataByFabricTxId")) {
                return queryDataByFabricTxId(stub, params);
            }
            if (func.equals("QueryDataByBusinessNo")) {
                return queryDataByBusinessNo(stub, params);
            }
            return newErrorResponse("Invalid invoke function name. ");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    private Response keepaliveQuery(ChaincodeStub stub, List<String> params) {
        String tarValue = stub.getStringState("keepAliveTest");
        if(!"keepAliveTestValue".equals(tarValue)){
            return newErrorResponse("ERROR! KeepaliveQuery get result is "+tarValue);
        }
        return newSuccessResponse("Reached".getBytes());
    }

    private Response queryDataByBusinessNo(ChaincodeStub stub, List<String> params) {
        String txId =new String(stub.getState(params.get(0)));
        _logger.info("query data by businessNo:"+params.get(0)+",txId is"+txId);
        return this.queryDataByFabricTxId(stub, Lists.newArrayList(txId));
    }

    private Response queryDataByFabricTxId(ChaincodeStub stub, List<String> params) {
        _logger.info("queryData by txId:"+params.get(0));
       byte[] res  =  stub.getState(params.get(0));
       return newSuccessResponse(new String(res),res);
    }

    private Response saveData(ChaincodeStub stub, List<String> args) {
        if(args.isEmpty() ||  args.size()<1){
            return newSuccessResponse("saveData wrong args");
        }try{
            Factor f = JSONArray.toJavaObject(JSONArray.parseObject(args.get(0)),Factor.class);
            String businessNo = f.getBusinessNo();
            if(StringUtil.isNullOrEmpty(businessNo)){
                return newErrorResponse("businessNo must exist");
            }
            String txId = stub.getTxId();
            stub.putState(txId,args.get(0).getBytes());
            stub.putState(businessNo,txId.getBytes());
            _logger.info("save data successfully with txid "+txId+",and businessNo"+businessNo);
        }catch (Exception e){
            return newErrorResponse(e.getMessage());
        }
        return newSuccessResponse();
    }



    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new FactoringChaincode().start(args);
    }

}
