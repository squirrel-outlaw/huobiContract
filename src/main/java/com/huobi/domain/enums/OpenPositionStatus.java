package com.huobi.domain.enums;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-31 上午11:24
 */
public enum  OpenPositionStatus {
    normal,
    carefulLong,
    carefulShort,
    reduceOpenLong,
    riseOpenShort,
    todayKlineBigGreen,
    todayKlineBigRed,
    afterForceClosePosition,
    forbid
}
