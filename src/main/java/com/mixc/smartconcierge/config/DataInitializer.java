package com.mixc.smartconcierge.config;

import com.mixc.smartconcierge.entity.AdminUser;
import com.mixc.smartconcierge.entity.Shop;
import com.mixc.smartconcierge.repository.AdminUserRepository;
import com.mixc.smartconcierge.repository.ShopRepository;
import com.mixc.smartconcierge.service.DashboardService;
import com.mixc.smartconcierge.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemConfigService systemConfigService;
    private final DashboardService dashboardService;

    @Override
    public void run(String... args) {
        systemConfigService.loadAll();
        initAdminUsers();
        initSampleShops();
        dashboardService.computeDailyStats(LocalDate.now());
        log.info("Data initialization completed");
    }

    private void initAdminUsers() {
        createUserIfAbsent("admin", "系统管理员", "管理员");
        createUserIfAbsent("zhangli", "张丽", "客服");
        createUserIfAbsent("wangjing", "王静", "运营");
        createUserIfAbsent("zhaoshang", "李招商", "招商");
    }

    private void createUserIfAbsent(String username, String realName, String role) {
        if (!adminUserRepository.existsByUsername(username)) {
            AdminUser user = new AdminUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName(realName);
            user.setRole(role);
            user.setStatus(1);
            adminUserRepository.save(user);
        }
    }

    private void initSampleShops() {
        if (shopRepository.count() > 0) {
            return;
        }
        saveShop("海底捞", "L4", "美食", "火锅", "[\"不辣\",\"适合聚餐\",\"网红\"]", 120, "工作日午市8.8折");
        saveShop("喜茶", "L1", "美食", "茶饮", "[\"网红\",\"适合约会\"]", 35, "第二杯半价");
        saveShop("优衣库", "L2", "服饰", "快时尚", "[\"亲子\",\"安静\"]", 200, "满300减50");
        saveShop("万达影城", "L5", "娱乐", "电影", "[\"亲子\",\"玩乐\"]", 45, "会员购票9折");
        saveShop("奈尔宝", "L3", "亲子", "儿童乐园", "[\"亲子\",\"网红\"]", 198, "亲子套票优惠");
        saveShop("西西弗书店", "L2", "娱乐", "书店", "[\"安静\",\"适合约会\"]", 60, null);
    }

    private void saveShop(String name, String floor, String category, String subCategory,
                          String tags, int avgPrice, String discount) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setFloor(floor);
        shop.setCategory(category);
        shop.setSubCategory(subCategory);
        shop.setTags(tags);
        shop.setAvgPrice(avgPrice);
        shop.setDiscountInfo(discount);
        shop.setLikeCount((int) (Math.random() * 100));
        shop.setStatus(1);
        shopRepository.save(shop);
    }
}
