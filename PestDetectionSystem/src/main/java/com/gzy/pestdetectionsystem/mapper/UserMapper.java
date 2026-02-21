package com.gzy.pestdetectionsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzy.pestdetectionsystem.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(String phone);

    @Select("SELECT * FROM user " +
            "WHERE phone = #{account} OR email = #{account} " +
            "LIMIT 1")
    User selectByAccount(String account);
}
