package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.BusinessException;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ActivitySaveRequest;
import com.mixc.smartconcierge.entity.Activity;
import com.mixc.smartconcierge.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public PageResult<Activity> page(String keyword, int pageNum, int pageSize) {
        Page<Activity> page = keyword != null && !keyword.isBlank()
                ? activityRepository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(Math.max(pageNum - 1, 0), pageSize))
                : activityRepository.findAll(PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        return new PageResult<>(page.getTotalElements(), page.getContent());
    }

    @Transactional
    public Activity save(ActivitySaveRequest req) {
        Activity activity = req.getId() != null
                ? activityRepository.findById(req.getId()).orElseThrow(() -> new BusinessException("活动不存在"))
                : new Activity();
        activity.setTitle(req.getTitle());
        activity.setBannerUrl(req.getBannerUrl());
        activity.setStartDate(req.getStartDate());
        activity.setEndDate(req.getEndDate());
        activity.setLocation(req.getLocation());
        activity.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            activity.setStatus(req.getStatus());
        }
        refreshStatus(activity);
        return activityRepository.save(activity);
    }

    @Transactional
    public void delete(Long id) {
        activityRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoEndActivities() {
        List<Activity> expired = activityRepository.findByStatusAndEndDateBefore(1, LocalDateTime.now());
        expired.forEach(a -> {
            a.setStatus(0);
            activityRepository.save(a);
        });
    }

    private void refreshStatus(Activity activity) {
        if (activity.getEndDate() != null && activity.getEndDate().isBefore(LocalDateTime.now())) {
            activity.setStatus(0);
        } else {
            activity.setStatus(1);
        }
    }
}
