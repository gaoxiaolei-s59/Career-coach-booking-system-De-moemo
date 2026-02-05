package org.puregxl.careercoachdemo.service;

import org.puregxl.careercoachdemo.mapper.BookingMapper;
import org.puregxl.careercoachdemo.model.Booking;
import org.puregxl.careercoachdemo.model.BookingStatus;
import org.puregxl.careercoachdemo.request.CalWebhookRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BookingService {

    private final BookingMapper bookingMapper;
    // Cal.com 预约基准链接
    private static final String CAL_BASE_URL = "https://cal.com/chau-chau-smfbef/30min";

    public BookingService(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    // A. 生成预约链接
    public String generateBookingUrl(String userId) {
        // 关键点：利用 metadata 将 userId 传给 Cal.com
        // 这样 Webhook 回调时，我们才知道是哪个 userId 预约的
        return CAL_BASE_URL + "?metadata[userId]=" + userId;
    }

    // B. 获取我的预约
    public List<Booking> getMyBookings(String userId) {
        return bookingMapper.findByUserId(userId);
    }

    // C. 获取取消链接
    public String generateCancelUrl(String userId) {
        // 真实场景通常需要前端传 bookingUid，这里简化逻辑
        // 假设这里需要返回最近一次预约的取消链接，或者让前端在列表里点取消
        // Cal.com 的通用取消链接格式: https://app.cal.com/booking/{uid}
        // 为了演示 API，我们假设前端必须传 bookingUid 给这个接口，或者我们返回列表让前端拼
        return "请调用 /api/bookings 接口获取 bookingUid，取消链接为: https://app.cal.com/booking/{bookingUid}?cancel=true";
    }

    // C的重载：根据UID生成真实链接
    public String getCancelUrlByUid(String bookingUid) {
        return "https://app.cal.com/booking/" + bookingUid + "?cancel=true";
    }

    // D. 处理 Webhook
    @Transactional
    public void handleWebhook(CalWebhookRequest request) {
        String event = request.getTriggerEvent();
        var payload = request.getPayload();

        if ("BOOKING_CREATED".equals(event)) {
            Booking booking = new Booking();
            booking.setCalBookingUid(payload.getUid());

            // 解析时间 (ISO 8601)
            booking.setStartTime(ZonedDateTime.parse(payload.getStartTime()).toLocalDateTime());
            booking.setEndTime(ZonedDateTime.parse(payload.getEndTime()).toLocalDateTime());

            // 获取 User Info (从 attendees[0] 获取)
            if (payload.getAttendees() != null && payload.getAttendees().length > 0) {
                booking.setUserEmail(payload.getAttendees()[0].getEmail());
            }

            // 获取 Coach Info
            if (payload.getOrganizer() != null) {
                booking.setCoachName(payload.getOrganizer().getName());
                booking.setCoachEmail(payload.getOrganizer().getEmail());
            }

            // 获取 metadata 中的 userId (关键闭环)
            if (payload.getMetadata() != null) {
                booking.setUserId(payload.getMetadata().get("userId"));
            } else {
                booking.setUserId("unknown"); // 容错
            }

            booking.setStatus(BookingStatus.BOOKING_CREATED);
            bookingMapper.insert(booking);

        } else if ("BOOKING_CANCELLED".equals(event)) {
            bookingMapper.updateStatus(payload.getUid(), BookingStatus.BOOKING_CANCELLED);
        }
    }
}
