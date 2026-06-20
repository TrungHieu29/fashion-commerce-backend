package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopAnalyticsResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.TimelineDataDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopAnalyticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopAnalyticsServiceImpl implements ShopAnalyticsService {

    private final OrderShopRepository orderShopRepository;

    public ShopAnalyticsServiceImpl(OrderShopRepository orderShopRepository) {
        this.orderShopRepository = orderShopRepository;
    }

    @Override
    public ShopAnalyticsResponseDto getShopAnalytics(Long shopId, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        String formatPattern;

        // 1. Xác định khoảng thời gian và định dạng hiển thị trục X bằng Java
        if ("7days".equalsIgnoreCase(period)) {
            startDate = endDate.minusDays(7).withHour(0).withMinute(0).withSecond(0);
            formatPattern = "dd/MM"; // Group theo Ngày
        } else if ("30days".equalsIgnoreCase(period)) {
            startDate = endDate.minusDays(30).withHour(0).withMinute(0).withSecond(0);
            formatPattern = "dd/MM"; // Group theo Ngày
        } else {
            // Mặc định là "today" (Hôm nay)
            startDate = endDate.withHour(0).withMinute(0).withSecond(0);
            formatPattern = "HH:00"; // Group theo Giờ
        }

        // 2. Lấy danh sách đơn hàng thực tế thỏa mãn điều kiện
        List<OrderShop> orders = orderShopRepository.findAnalyticsOrders(shopId, startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = orders.size();

        // Tính tổng doanh thu tích lũy toàn bộ đơn trong khoảng thời gian lọc
        for (OrderShop os : orders) {
            if (os.getFinalPrice() != null) {
                totalRevenue = totalRevenue.add(os.getFinalPrice());
            }
        }

        // 3. Tính Giá trị trung bình đơn hàng (AOV)
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        // 4. Dùng Stream để Group By danh sách đơn hàng thành mảng TimelineDataDto
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);

        Map<String, List<OrderShop>> groupedOrders = orders.stream()
                .collect(Collectors.groupingBy(os -> os.getOrder().getCreatedAt().format(formatter)));

        List<TimelineDataDto> timelineRevenue = new ArrayList<>();

        groupedOrders.forEach((label, orderList) -> {
            BigDecimal revenue = orderList.stream()
                    .map(OrderShop::getFinalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = orderList.size();
            timelineRevenue.add(new TimelineDataDto(label, revenue, count));
        });

        // Sắp xếp timeline theo thứ tự thời gian tăng dần để biểu đồ hiển thị đúng mạch
        timelineRevenue.sort(Comparator.comparing(TimelineDataDto::getLabel));

        // Các chỉ số bổ trợ mẫu
        double growthRate = 0.0;
        double conversionRate = 3.5;

        return new ShopAnalyticsResponseDto(
                totalRevenue,
                averageOrderValue,
                growthRate,
                totalOrders,
                conversionRate,
                timelineRevenue
        );
    }
}