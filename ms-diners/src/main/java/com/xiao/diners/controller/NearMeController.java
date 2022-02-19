package com.xiao.diners.controller;

import com.xiao.commons.model.domain.ResultInfo;
import com.xiao.commons.model.vo.NearMeDinerVO;
import com.xiao.commons.utils.ResultInfoUtil;
import com.xiao.diners.service.NearMeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("nearme")
public class NearMeController {

    @Resource
    private HttpServletRequest request;
    @Resource
    private NearMeService nearMeService;

    /**
     * 更新食客坐标
     *
     * @param access_token
     * @param lon
     * @param lat
     * @return
     */
    @PostMapping
    public ResultInfo updateDinerLocation(String access_token,
                                          @RequestParam Float lon,
                                          @RequestParam Float lat) {
        nearMeService.updateDinerLocation(access_token, lon, lat);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "更新成功");
    }

    /**
     * 获取附近的人
     *
     * @param access_token
     * @param radius
     * @param lon
     * @param lat
     * @return
     */
    @GetMapping
    public ResultInfo nearMe(String access_token,
                             Integer radius,
                             Float lon, Float lat) {
        List<NearMeDinerVO> nearMe = nearMeService.findNearMe(access_token, radius, lon, lat);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), nearMe);
    }

}
