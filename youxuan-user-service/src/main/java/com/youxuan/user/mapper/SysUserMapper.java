package com.youxuan.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问接口。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
