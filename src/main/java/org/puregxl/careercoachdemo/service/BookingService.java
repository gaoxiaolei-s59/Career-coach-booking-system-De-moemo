
package org.puregxl.careercoachdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.puregxl.careercoachdemo.mapper.BookingMapper;
import org.puregxl.careercoachdemo.model.Booking;
import org.puregxl.careercoachdemo.model.BookingStatus;
import org.puregxl.careercoachdemo.request.CalWebhookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookingService {

    private final BookingMapper bookingMapper;


    @Value("${app.cal.cal-base-url:https://cal.com/chau-chau-smfbef/30min}")
    private String calBaseUrl;

    public BookingService(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    /**
     * A. 生成预约链接
     * 利用 Metadata 透传 UserId，形成业务闭环
     */
    public String generateBookingUrl(String userId) {
        String url = calBaseUrl + "?metadata[userId]=" + userId;
        log.info("Generated booking URL for user [{}]: {}", userId, url);
        return url;
    }

    /**
     * B. 获取我的预约列表
     */
    public List<Booking> getMyBookings(String userId) {
        return bookingMapper.findByUserId(userId);
    }

    /**
     * C. 获取取消链接
     * 逻辑：查找该用户当前 "即将开始 (BOOKING_CREATED)" 的最近一次预约
     */
    public String generateCancelUrl(String userId) {
        // 查询最近的一个有效预约
        Booking activeBooking = bookingMapper.findLatestActiveBooking(userId);

        if (activeBooking == null) {
            log.warn("User [{}] requested cancel URL but has no active bookings.", userId);
            throw new RuntimeException("您当前没有可取消的预约");
        }

        // 拼接 Cal.com 官方取消链接格式
        String cancelUrl = "https://app.cal.com/booking/" + activeBooking.getCalBookingUid() + "?cancel=true";
        log.info("Generated cancel URL for user [{}], Booking UID [{}]: {}", userId, activeBooking.getCalBookingUid(), cancelUrl);
        return cancelUrl;
    }

    /**
     * D. 处理 Webhook (核心逻辑)
     * 包含幂等性检查、数据清洗、防御性编程
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleWebhook(CalWebhookRequest request) {
        String event = request.getTriggerEvent();
        var payload = request.getPayload();
        String uid = payload.getUid();

        log.info("Webhook Received -> Event: [{}], UID: [{}]", event, uid);

        // 基础校验
        if (!StringUtils.hasText(uid)) {
            log.error("Webhook payload invalid: UID is missing.");
            return;
        }

        try {
            switch (event) {
                case "BOOKING_CREATED" -> handleBookingCreated(payload, uid);
                case "BOOKING_CANCELLED" -> handleBookingCancelled(uid);
                default -> log.warn("Ignored unsupported event: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing webhook for UID: {}", uid, e);
            throw e; // 抛出异常以触发事务回滚
        }
    }

    // --- 内部 Helper 方法 ---
    private void handleBookingCreated(CalWebhookRequest.Payload payload, String uid) {
        // 1. [关键] 幂等性检查：防止 Cal.com 重复发送导致数据重复
        if (bookingMapper.findByCalUid(uid) != null) {
            log.warn("Idempotency Check: Booking already exists for UID [{}]. Skipping insert.", uid);
            return;
        }

        Booking booking = new Booking();
        booking.setCalBookingUid(uid);
        // 2. 时间解析 (安全处理)
        booking.setStartTime(parseTimeSafe(payload.getStartTime()));
        booking.setEndTime(parseTimeSafe(payload.getEndTime()));

        // 3. 提取用户信息
        if (payload.getAttendees() != null && payload.getAttendees().length > 0) {
            booking.setUserEmail(payload.getAttendees()[0].getEmail());
        }

        // 4. 提取导师信息
        if (payload.getOrganizer() != null) {
            booking.setCoachName(payload.getOrganizer().getName());
            booking.setCoachEmail(payload.getOrganizer().getEmail());
        }

        // 5. [关键] 提取 Metadata 中的 UserId
        Map<String, String> metadata = payload.getMetadata();
        if (metadata != null && StringUtils.hasText(metadata.get("userId"))) {
            booking.setUserId(metadata.get("userId"));
        } else {
            log.error("CRITICAL: Booking created without userId in metadata! UID: {}", uid);
            booking.setUserId("UNKNOWN_USER"); // 避免数据库报错，方便后续人工排查
        }

        booking.setStatus(BookingStatus.BOOKING_CREATED);
        bookingMapper.insert(booking);
        log.info("Booking persisted successfully. UID: [{}]", uid);
    }

    private void handleBookingCancelled(String uid) {
        int rows = bookingMapper.updateStatus(uid, BookingStatus.BOOKING_CANCELLED);
        if (rows > 0) {
            log.info("Booking status updated to CANCELLED. UID: [{}]", uid);
        } else {
            log.warn("Received CANCEL event but booking not found in DB. UID: [{}]", uid);
        }
    }


    private LocalDateTime parseTimeSafe(String isoTime) {
        if (!StringUtils.hasText(isoTime)) return null;
        try {
            System.out.println("1111");
            return ZonedDateTime.parse(isoTime).toLocalDateTime();

        } catch (DateTimeParseException e) {
            log.error("Failed to parse time string: {}", isoTime);
            return null;
        }
    }



    public String getCancelUrlByUid(String bookingUid) {
        if (!StringUtils.hasText(bookingUid)) {
            throw new IllegalArgumentException("Booking UID cannot be empty");
        }
        // 直接拼接 Cal.com 官方格式
        return "https://app.cal.com/booking/" + bookingUid + "?cancel=true";
    }
}