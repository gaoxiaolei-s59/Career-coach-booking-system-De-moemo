package org.puregxl.careercoachdemo.controller;


import org.puregxl.careercoachdemo.model.Booking;
import org.puregxl.careercoachdemo.request.CalWebhookRequest;
import org.puregxl.careercoachdemo.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // A. 去预约
    @PostMapping("/booking-url")
    public Map<String, String> getBookingUrl(@RequestParam String userId) {
        String url = bookingService.generateBookingUrl(userId);
        return Map.of("url", url);
    }

    // B. 我的预约
    @GetMapping("/bookings")
    public List<Booking> getMyBookings(@RequestParam String userId) {
        return bookingService.getMyBookings(userId);
    }

    // C. 去取消
    @PostMapping("/bookings/cancel")
    public Map<String, String> cancelBooking(@RequestBody Map<String, String> body) {
        // 前端传 { "bookingUid": "xxx" }
        String uid = body.get("bookingUid");
        String url = bookingService.getCancelUrlByUid(uid);
        return Map.of("cancellationUrl", url);
    }

    // D. Webhook 接收
    @PostMapping("/webhook/cal")
    public void handleCalWebhook(@RequestBody CalWebhookRequest request) {
        System.out.println("收到 Webhook: " + request.getTriggerEvent());
        bookingService.handleWebhook(request);
    }
}