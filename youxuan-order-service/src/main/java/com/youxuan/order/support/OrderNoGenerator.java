package com.youxuan.order.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 * 生成订单号，格式为 YX + 年月日时分秒毫秒 + 四位随机数。
 */
@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String next() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "YX" + LocalDateTime.now().format(FORMATTER) + random;
    }
}
