package com.xiao.commons.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiao.commons.model.pojo.Reviews;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel(description = "餐厅评论实体类")
public class ReviewsVO extends Reviews {

    @ApiModelProperty("食客信息")
    private ShortDinerInfo dinerInfo;
    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createDate;

}