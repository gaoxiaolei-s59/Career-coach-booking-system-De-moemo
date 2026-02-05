$BaseUrl = "http://localhost:8080/api"

# 1. Create User (Demo)
Write-Host "1. Create User"
try {
    $user = Invoke-RestMethod -Uri "$BaseUrl/demo/users?name=TestUser&email=user@test.com" -Method Post
    $userId = $user.id
    Write-Host "User ID: $userId"
} catch {
    Write-Host "Error creating user: $_"
    exit
}

# 2. Get Booking URL
Write-Host "2. Get Booking URL"
try {
    $bookingUrl = Invoke-RestMethod -Uri "$BaseUrl/booking-url?userId=$userId" -Method Post
    Write-Host "Booking URL: $bookingUrl"
} catch {
    Write-Host "Error getting booking URL: $_"
}

# 3. Simulate Webhook BOOKING_CREATED
Write-Host "3. Simulate Webhook BOOKING_CREATED"
$webhookPayload = @{
    triggerEvent = "BOOKING_CREATED"
    payload = @{
        id = 123
        uid = "cal_uid_123"
        startTime = "2023-10-05T10:00:00.000Z"
        endTime = "2023-10-05T10:30:00.000Z"
        attendees = @(
            @{ email = "user@test.com"; name = "TestUser" }
        )
        organizer = @{
            email = "coach@test.com"
            name = "Coach A"
        }
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$BaseUrl/webhook/cal" -Method Post -Body $webhookPayload -ContentType "application/json"
    Write-Host "Webhook sent."
} catch {
    Write-Host "Error sending webhook: $_"
}

# 4. Get User Bookings
Write-Host "4. Get User Bookings"
try {
    $bookings = Invoke-RestMethod -Uri "$BaseUrl/bookings?userId=$userId" -Method Get
    Write-Host "Bookings Count: $($bookings.Count)"
    if ($bookings.Count -gt 0) {
        Write-Host "Booking Status: $($bookings[0].status)"
    }
} catch {
    Write-Host "Error getting bookings: $_"
}

# 5. Cancel Booking
Write-Host "5. Cancel Booking"
try {
    $cancelUrl = Invoke-RestMethod -Uri "$BaseUrl/bookings/cancel?userId=$userId" -Method Post
    Write-Host "Cancel URL: $cancelUrl"
} catch {
    Write-Host "Error canceling booking: $_"
}

# 6. Simulate Webhook BOOKING_CANCELLED
Write-Host "6. Simulate Webhook BOOKING_CANCELLED"
$cancelPayload = @{
    triggerEvent = "BOOKING_CANCELLED"
    payload = @{
        uid = "cal_uid_123"
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$BaseUrl/webhook/cal" -Method Post -Body $cancelPayload -ContentType "application/json"
    Write-Host "Cancel Webhook sent."
} catch {
    Write-Host "Error sending cancel webhook: $_"
}

# 7. Verify Status Update
Write-Host "7. Verify Status Update"
try {
    $bookings = Invoke-RestMethod -Uri "$BaseUrl/bookings?userId=$userId" -Method Get
    if ($bookings.Count -gt 0) {
        Write-Host "Booking Status After Cancel: $($bookings[0].status)"
    }
} catch {
    Write-Host "Error verifying status: $_"
}
