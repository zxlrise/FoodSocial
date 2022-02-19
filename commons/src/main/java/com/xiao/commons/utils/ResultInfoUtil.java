package com.xiao.commons.utils;


import com.xiao.commons.constant.ApiConstant;
import com.xiao.commons.model.domain.ResultInfo;

/**
 * 公共返回对象工具类
 */
public class ResultInfoUtil {

    /**
     * 请求出错返回
     *
     * @param path 请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildError(String path) {
        ResultInfo<T> resultInfo = build(ApiConstant.ERROR_CODE,
                ApiConstant.ERROR_MESSAGE, path, null);
        return resultInfo;
    }

    /**
     * 请求出错返回
     *
     * @param errorCode 错误代码
     * @param message   错误提示信息
     * @param path      请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildError(int errorCode, String message, String path) {
        ResultInfo<T> resultInfo = build(errorCode, message, path, null);
        return resultInfo;
    }

    /**
     * 请求成功返回
     *
     * @param path 请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildSuccess(String path) {
        ResultInfo<T> resultInfo = build(ApiConstant.SUCCESS_CODE,
                ApiConstant.SUCCESS_MESSAGE, path, null);
        return resultInfo;
    }

    /**
     * 请求成功返回
     *
     * @param path 请求路径
     * @param data 返回数据对象
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildSuccess(String path, T data) {
        ResultInfo<T> resultInfo = build(ApiConstant.SUCCESS_CODE,
                ApiConstant.SUCCESS_MESSAGE, path, data);
        return resultInfo;
    }

    /**
     * 构建返回对象方法
     *
     * @param code
     * @param message
     * @param path
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> build(Integer code, String message, String path, T data) {
        if (code == null) {
            code = ApiConstant.SUCCESS_CODE;
        }
        if (message == null) {
            message = ApiConstant.SUCCESS_MESSAGE;
        }
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(code);
        resultInfo.setMessage(message);
        resultInfo.setPath(path);
        resultInfo.setData(data);
        return resultInfo;
    }

}