package com.xiao.commons.model.domain;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 登录认证对象
 */
@Getter
@Setter
public class SignInIdentity implements UserDetails {

    // 主键
    private Integer id;
    // 用户名
    private String username;
    // 昵称
    private String nickname;
    // 密码
    private String password;
    // 手机号
    private String phone;
    // 邮箱
    private String email;
    // 头像
    private String avatarUrl;
    // 角色
    private String roles;
    // 是否有效 0=无效 1=有效
    private int isValid;
    // 角色集合, 不能为空
    private List<GrantedAuthority> authorities;

    // 获取角色信息
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (StrUtil.isNotBlank(this.roles)) {
            // 获取数据库中的角色信息
            Lists.newArrayList();
            this.authorities = Stream.of(this.roles.split(",")).map(role -> {
                return new SimpleGrantedAuthority(role);
            }).collect(Collectors.toList());
        } else {
            // 如果角色为空则设置为 ROLE_USER
            this.authorities = AuthorityUtils
                    .commaSeparatedStringToAuthorityList("ROLE_USER");
        }
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isValid == 0 ? false : true;
    }

}