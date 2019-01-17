package com.huobi.service;


import com.huobi.domain.enums.ContractType;

/**
 * @Author: squirrel
 * @Date: 18-9-18 上午11:02
 */
public class BasicFunction {
public static String getContractSymbol(String symbol,String contractType){
    ContractType contractTypeTemp = ContractType.valueOf(contractType);
    return symbol + "_" + contractTypeTemp.getType();
}

}
