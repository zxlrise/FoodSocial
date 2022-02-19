package com.xiao.feeds.controller;

import com.xiao.commons.model.domain.ResultInfo;
import com.xiao.commons.model.pojo.Feeds;
import com.xiao.commons.model.vo.FeedsVO;
import com.xiao.commons.utils.ResultInfoUtil;
import com.xiao.feeds.service.FeedsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class FeedsController {

    @Resource
    private FeedsService feedsService;
    @Resource
    private HttpServletRequest request;

    /**
     * 分页获取关注的 Feed 数据
     *
     * @param page
     * @param access_token
     * @return
     */
    @GetMapping("{page}")
    public ResultInfo selectForPage(@PathVariable Integer page, String access_token) {
        List<FeedsVO> feedsVOS = feedsService.selectForPage(page, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), feedsVOS);
    }

    /**
     * 变更 Feed
     *
     * @return
     */
    @PostMapping("updateFollowingFeeds/{followingDinerId}")
    public ResultInfo addFollowingFeeds(@PathVariable Integer followingDinerId,
                                        String access_token, @RequestParam int type) {
        feedsService.addFollowingFeed(followingDinerId, access_token, type);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "操作成功");
    }

    /**
     * 删除 Feed
     *
     * @param id
     * @param access_token
     * @return
     */
    @DeleteMapping("{id}")
    public ResultInfo delete(@PathVariable Integer id, String access_token) {
        feedsService.delete(id, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "删除成功");
    }

    /**
     * 添加 Feed
     *
     * @param feeds
     * @param access_token
     * @return
     */
    @PostMapping
    public ResultInfo<String> create(@RequestBody Feeds feeds, String access_token) {
        feedsService.create(feeds, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
    }

}