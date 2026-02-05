package org.puregxl.careercoachdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.puregxl.careercoachdemo.model.User;

@Mapper
public interface UserMapper {
    User findById(@Param("id") String id);
    User findByEmail(@Param("email") String email);
    void insert(User user);
    void update(User user);
}
