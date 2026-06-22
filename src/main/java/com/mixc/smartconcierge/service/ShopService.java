package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.BusinessException;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ShopSaveRequest;
import com.mixc.smartconcierge.entity.Shop;
import com.mixc.smartconcierge.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final RecommendService recommendService;

    public List<com.mixc.smartconcierge.dto.ShopVO> listActive() {
        return shopRepository.findByStatusOrderByLikeCountDesc(1).stream()
                .map(recommendService::toVO)
                .collect(Collectors.toList());
    }

    public PageResult<com.mixc.smartconcierge.dto.ShopVO> page(String keyword, int pageNum, int pageSize) {
        Page<Shop> page = keyword != null && !keyword.isBlank()
                ? shopRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(Math.max(pageNum - 1, 0), pageSize))
                : shopRepository.findAll(PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        List<com.mixc.smartconcierge.dto.ShopVO> list = page.getContent().stream()
                .map(recommendService::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotalElements(), list);
    }

    @Transactional
    public Shop save(ShopSaveRequest req) {
        Shop shop = req.getId() != null
                ? shopRepository.findById(req.getId()).orElseThrow(() -> new BusinessException("店铺不存在"))
                : new Shop();
        shop.setName(req.getName());
        shop.setFloor(req.getFloor());
        shop.setCategory(req.getCategory());
        shop.setSubCategory(req.getSubCategory());
        shop.setTags(req.getTags());
        shop.setAvgPrice(req.getAvgPrice());
        shop.setDiscountInfo(req.getDiscountInfo());
        shop.setActivityId(req.getActivityId());
        if (req.getLikeCount() != null) {
            shop.setLikeCount(req.getLikeCount());
        }
        if (req.getStatus() != null) {
            shop.setStatus(req.getStatus());
        }
        return shopRepository.save(shop);
    }

    @Transactional
    public void delete(Long id) {
        shopRepository.deleteById(id);
    }
}
