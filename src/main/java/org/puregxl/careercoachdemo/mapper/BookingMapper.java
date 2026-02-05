package org.puregxl.careercoachdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.puregxl.careercoachdemo.model.Booking;
import org.puregxl.careercoachdemo.model.BookingStatus;

import java.util.List;

@Mapper
public interface BookingMapper {

    //  插入预约
    // 使用 Options 获取自增主键，方便后续需要 ID 的场景
    @Insert("INSERT INTO bookings (user_id, coach_name, coach_email, user_email, start_time, end_time, cal_booking_uid, status) " +
            "VALUES (#{userId}, #{coachName}, #{coachEmail}, #{userEmail}, #{startTime}, #{endTime}, #{calBookingUid}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Booking booking);

    // .查询用户所有预约
    @Select("SELECT * FROM bookings WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<Booking> findByUserId(String userId);

    // 幂等性检查
    @Select("SELECT * FROM bookings WHERE cal_booking_uid = #{uid} LIMIT 1")
    Booking findByCalUid(String uid);

    // [新增] 查找最近一个“待开始”的预约
    @Select("SELECT * FROM bookings WHERE user_id = #{userId} AND status = 'BOOKING_CREATED' ORDER BY start_time ASC LIMIT 1")
    Booking findLatestActiveBooking(@Param("userId") String userId);

    //  更新状态
    @Update("UPDATE bookings SET status = #{status} WHERE cal_booking_uid = #{uid}")
    int updateStatus(@Param("uid") String uid, @Param("status") BookingStatus status);
}