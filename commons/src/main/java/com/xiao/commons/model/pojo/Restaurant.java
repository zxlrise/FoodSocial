package com.xiao.commons.model.pojo;

import com.xiao.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Restaurant extends BaseModel {
    
    @ApiModelProperty("英文名称")
    private String name;
    @ApiModelProperty("中文名称")
    private String cnName;
    @ApiModelProperty("纬度")
    private Float x;
    @ApiModelProperty("经度")
    private Float y;
    @ApiModelProperty("位置-英文")
    private String location;
    @ApiModelProperty("位置-中文")
    private String cnLocation;
    @ApiModelProperty("商圈")
    private String area;
    @ApiModelProperty("电话")
    private String telephone;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("官网")
    private String website;
    @ApiModelProperty("菜系")
    private String cuisine;
    @ApiModelProperty("均价，不显示具体金额")
    private String averagePrice;
    @ApiModelProperty("介绍")
    private String introduction;
    @ApiModelProperty("缩略图")
    private String thumbnail;
    @ApiModelProperty("喜欢")
    private int likeVotes;
    @ApiModelProperty("不喜欢")
    private int dislikeVotes;
    @ApiModelProperty("城市")
    private Integer cityId;

}