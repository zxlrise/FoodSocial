package com.xiao.commons.model.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 公共返回对象
 */
@Getter
@Setter
@ApiModel(value = "返回说明")
public class ResultInfo<T> implements Serializable {
    
    @ApiModelProperty(value = "成功标识0=失败，1=成功")
    private Integer code;
    @ApiModelProperty(value = "描述信息")
    private String message;
    @ApiModelProperty(value = "访问路径")
    private String path;
    @ApiModelProperty(value = "返回数据对象")
    private T data;
    
}