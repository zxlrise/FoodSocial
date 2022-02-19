package com.xiao.commons.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ApiModel(description = "Feed显示信息")
public class FeedsVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("点赞数")
    private int praiseAmount;
    @ApiModelProperty("评论数")
    private int commentAmount;
    @ApiModelProperty("餐厅")
    private Integer fkRestaurantId;
    @ApiModelProperty("用户ID")
    private Integer fkDinerId;
    @ApiModelProperty("用户信息")
    private ShortDinerInfo dinerInfo;
    @ApiModelProperty("显示时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date createDate;

}