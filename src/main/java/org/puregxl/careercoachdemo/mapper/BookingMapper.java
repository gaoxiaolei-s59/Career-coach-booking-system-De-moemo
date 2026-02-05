package org.puregxl.careercoachdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.puregxl.careercoachdemo.model.Booking;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.puregxl.careercoachdemo.model.BookingStatus;

import java.util.List;

@Mapper
public interface BookingMapper {

    @Insert("INSERT INTO bookings (user_id, coach_name, coach_email, user_email, start_time, end_time, cal_booking_uid, status) " +
            "VALUES (#{userId}, #{coachName}, #{coachEmail}, #{userEmail}, #{startTime}, #{endTime}, #{calBookingUid}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Booking booking);

    @Select("SELECT * FROM bookings WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<Booking> findByUserId(String userId);

    @Select("SELECT * FROM bookings WHERE cal_booking_uid = #{uid}")
    Booking findByCalUid(String uid);

    @Update("UPDATE bookings SET status = #{status} WHERE cal_booking_uid = #{uid}")
    void updateStatus(@Param("uid") String uid, @Param("status") BookingStatus status);
}
