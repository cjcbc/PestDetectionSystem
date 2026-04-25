package com.gzy.pestdetectionsystem.service.impl.warning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzy.pestdetectionsystem.dto.warning.CreateWarningDTO;
import com.gzy.pestdetectionsystem.dto.warning.UpdateWarningDTO;
import com.gzy.pestdetectionsystem.entity.user.User;
import com.gzy.pestdetectionsystem.entity.warning.WarningRead;
import com.gzy.pestdetectionsystem.entity.warning.WarningRecord;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.mapper.warning.WarningReadMapper;
import com.gzy.pestdetectionsystem.mapper.warning.WarningRecordMapper;
import com.gzy.pestdetectionsystem.service.warning.WarningService;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.article.PageVO;
import com.gzy.pestdetectionsystem.vo.warning.UnreadWarningCountVO;
import com.gzy.pestdetectionsystem.vo.warning.WarningItemVO;
import com.gzy.pestdetectionsystem.vo.warning.WarningReadVO;
import com.gzy.pestdetectionsystem.vo.warning.WarningUnreadSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final WarningRecordMapper warningRecordMapper;
    private final WarningReadMapper warningReadMapper;
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningItemVO createWarning(Long publisherId, Integer roleId, CreateWarningDTO dto) {
        requireAdmin(roleId, "仅管理员可新增预警");
        if (dto == null || isBlank(dto.getTitle()) || isBlank(dto.getContent())) {
            throw new BusinessException(40090, "标题和内容不能为空");
        }
        validateTitle(dto.getTitle());
        validateSeverity(dto.getSeverity());
        validateStatus(dto.getStatus());

        long now = System.currentTimeMillis();
        WarningRecord warning = new WarningRecord();
        warning.setId(snowflakeIdGenerator.nextId());
        warning.setTitle(dto.getTitle().trim());
        warning.setContent(dto.getContent().trim());
        warning.setRegion(emptyToNull(dto.getRegion()));
        warning.setPestName(emptyToNull(dto.getPestName()));
        warning.setSeverity(dto.getSeverity());
        warning.setStatus(dto.getStatus());
        warning.setPublishTime(now);
        warning.setPublisherId(publisherId);
        warning.setViewCount(0);
        warning.setCreatedTime(now);
        warning.setUpdatedTime(now);
        warning.setDeleted(0);

        warningRecordMapper.insert(warning);
        User publisher = userMapper.selectById(publisherId);
        return toWarningVO(warning, publisher, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningItemVO updateWarning(Long operatorId, Integer roleId, Long warningId, UpdateWarningDTO dto) {
        requireAdmin(roleId, "仅管理员可编辑预警");
        WarningRecord warning = requireExistingWarning(warningId);

        if (dto == null) {
            throw new BusinessException(40096, "更新内容不能为空");
        }

        boolean changed = false;
        long now = System.currentTimeMillis();
        Integer originalStatus = warning.getStatus();

        if (dto.getTitle() != null) {
            validateTitle(dto.getTitle());
            warning.setTitle(dto.getTitle().trim());
            changed = true;
        }
        if (dto.getContent() != null) {
            if (isBlank(dto.getContent())) {
                throw new BusinessException(40097, "预警内容不能为空");
            }
            warning.setContent(dto.getContent().trim());
            changed = true;
        }
        if (dto.getRegion() != null) {
            warning.setRegion(emptyToNull(dto.getRegion()));
            changed = true;
        }
        if (dto.getPestName() != null) {
            warning.setPestName(emptyToNull(dto.getPestName()));
            changed = true;
        }
        if (dto.getSeverity() != null) {
            validateSeverity(dto.getSeverity());
            warning.setSeverity(dto.getSeverity());
            changed = true;
        }
        if (dto.getStatus() != null) {
            validateStatus(dto.getStatus());
            warning.setStatus(dto.getStatus());
            if (!Objects.equals(originalStatus, dto.getStatus()) && Objects.equals(dto.getStatus(), 1)) {
                warning.setPublishTime(now);
            }
            changed = true;
        }

        if (!changed) {
            throw new BusinessException(40098, "至少提供一个可更新字段");
        }

        warning.setUpdatedTime(now);
        warningRecordMapper.updateById(warning);

        WarningRecord latest = warningRecordMapper.selectById(warningId);
        User publisher = userMapper.selectById(latest.getPublisherId());
        return toWarningVO(latest, publisher, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningItemVO toggleWarningStatus(Long operatorId, Integer roleId, Long warningId) {
        requireAdmin(roleId, "仅管理员可上下架预警");
        WarningRecord warning = requireExistingWarning(warningId);

        long now = System.currentTimeMillis();
        int nextStatus = Objects.equals(warning.getStatus(), 1) ? 0 : 1;
        warning.setStatus(nextStatus);
        warning.setUpdatedTime(now);
        if (nextStatus == 1) {
            warning.setPublishTime(now);
        }
        warningRecordMapper.updateById(warning);

        WarningRecord latest = warningRecordMapper.selectById(warningId);
        User publisher = userMapper.selectById(latest.getPublisherId());
        return toWarningVO(latest, publisher, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarning(Long operatorId, Integer roleId, Long warningId) {
        requireAdmin(roleId, "仅管理员可删除预警");
        WarningRecord warning = requireExistingWarning(warningId);
        warning.setDeleted(1);
        warning.setUpdatedTime(System.currentTimeMillis());
        warningRecordMapper.updateById(warning);
    }

    @Override
    public PageVO<WarningItemVO> getWarnings(Long userId, Integer roleId, Integer page, Integer pageSize,
                                             Integer status, Integer severity, String region, String keyword) {
        int pageNum = normalizePage(page);
        int size = normalizePageSize(pageSize, 20, 100);
        boolean isAdmin = Objects.equals(roleId, 0);

        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getDeleted, 0);

        if (status != null) {
            wrapper.eq(WarningRecord::getStatus, status);
        } else if (!isAdmin) {
            wrapper.eq(WarningRecord::getStatus, 1);
        }

        if (severity != null) {
            wrapper.eq(WarningRecord::getSeverity, severity);
        }
        if (!isBlank(region)) {
            wrapper.like(WarningRecord::getRegion, region.trim());
        }
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(WarningRecord::getTitle, kw)
                    .or().like(WarningRecord::getContent, kw)
                    .or().like(WarningRecord::getPestName, kw));
        }

        wrapper.orderByDesc(WarningRecord::getPublishTime);

        Page<WarningRecord> warningPage = warningRecordMapper.selectPage(new Page<>(pageNum, size), wrapper);
        List<WarningRecord> records = warningPage.getRecords();
        if (records.isEmpty()) {
            return new PageVO<>(warningPage.getTotal(), pageNum, size, pages(warningPage.getTotal(), size), Collections.emptyList());
        }

        Set<Long> publisherIds = records.stream().map(WarningRecord::getPublisherId).collect(Collectors.toSet());
        Set<Long> warningIds = records.stream().map(WarningRecord::getId).collect(Collectors.toSet());

        Map<Long, User> publisherMap = userMapper.selectBatchIds(publisherIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        Map<Long, WarningRead> readMap = loadReadMap(userId, warningIds);

        List<WarningItemVO> list = records.stream().map(w -> {
            WarningRead read = readMap.get(w.getId());
            return toWarningVO(w, publisherMap.get(w.getPublisherId()), read, read != null);
        }).collect(Collectors.toList());

        return new PageVO<>(warningPage.getTotal(), pageNum, size, pages(warningPage.getTotal(), size), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningReadVO markRead(Long userId, Long warningId) {
        WarningRecord warning = requireOnlineWarning(warningId);

        WarningRead existing = warningReadMapper.selectOne(new LambdaQueryWrapper<WarningRead>()
                .eq(WarningRead::getWarningId, warningId)
                .eq(WarningRead::getUserId, userId)
                .last("limit 1"));

        if (existing != null) {
            return new WarningReadVO(String.valueOf(existing.getWarningId()), String.valueOf(existing.getUserId()), existing.getReadTime());
        }

        long now = System.currentTimeMillis();
        WarningRead read = new WarningRead();
        read.setId(snowflakeIdGenerator.nextId());
        read.setWarningId(warning.getId());
        read.setUserId(userId);
        read.setReadTime(now);
        warningReadMapper.insert(read);

        return new WarningReadVO(String.valueOf(warningId), String.valueOf(userId), now);
    }

    @Override
    public UnreadWarningCountVO getUnreadCount(Long userId) {
        List<WarningRecord> allOnlineWarnings = warningRecordMapper.selectList(new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getDeleted, 0)
                .eq(WarningRecord::getStatus, 1)
                .orderByDesc(WarningRecord::getPublishTime));
        if (allOnlineWarnings.isEmpty()) {
            return new UnreadWarningCountVO(0L, Collections.emptyList());
        }

        Set<Long> warningIds = allOnlineWarnings.stream().map(WarningRecord::getId).collect(Collectors.toSet());
        Set<Long> readIds = warningReadMapper.selectList(new LambdaQueryWrapper<WarningRead>()
                        .eq(WarningRead::getUserId, userId)
                        .in(WarningRead::getWarningId, warningIds))
                .stream().map(WarningRead::getWarningId).collect(Collectors.toSet());

        List<WarningRecord> unread = allOnlineWarnings.stream()
                .filter(w -> !readIds.contains(w.getId()))
                .collect(Collectors.toList());

        List<WarningUnreadSummaryVO> top3 = unread.stream().limit(3)
            .map(w -> new WarningUnreadSummaryVO(String.valueOf(w.getId()), w.getTitle(), w.getSeverity(), w.getPublishTime()))
                .collect(Collectors.toList());

        return new UnreadWarningCountVO((long) unread.size(), top3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningItemVO getWarningDetail(Long userId, Integer roleId, Long warningId) {
        WarningRecord warning = warningRecordMapper.selectById(warningId);
        if (warning == null || !Objects.equals(warning.getDeleted(), 0)) {
            throw new BusinessException(40094, "预警不存在");
        }
        if (!Objects.equals(roleId, 0) && !Objects.equals(warning.getStatus(), 1)) {
            throw new BusinessException(40095, "预警不存在或未上架");
        }

        warningRecordMapper.update(null, new LambdaUpdateWrapper<WarningRecord>()
                .eq(WarningRecord::getId, warningId)
                .setSql("view_count = view_count + 1"));

        WarningRecord latest = warningRecordMapper.selectById(warningId);

        if (Objects.equals(roleId, 0)) {
            User publisher = userMapper.selectById(latest.getPublisherId());
            return toWarningVO(latest, publisher, null, false);
        }

        WarningRead read = warningReadMapper.selectOne(new LambdaQueryWrapper<WarningRead>()
                .eq(WarningRead::getWarningId, warningId)
                .eq(WarningRead::getUserId, userId)
                .last("limit 1"));

        if (read == null && Objects.equals(latest.getStatus(), 1)) {
            long now = System.currentTimeMillis();
            read = new WarningRead();
            read.setId(snowflakeIdGenerator.nextId());
            read.setWarningId(warningId);
            read.setUserId(userId);
            read.setReadTime(now);
            warningReadMapper.insert(read);
        }

        User publisher = userMapper.selectById(latest.getPublisherId());
        return toWarningVO(latest, publisher, read, read != null);
    }

    private WarningRecord requireExistingWarning(Long warningId) {
        WarningRecord warning = warningRecordMapper.selectById(warningId);
        if (warning == null || !Objects.equals(warning.getDeleted(), 0)) {
            throw new BusinessException(40094, "预警不存在");
        }
        return warning;
    }

    private WarningRecord requireOnlineWarning(Long warningId) {
        WarningRecord warning = requireExistingWarning(warningId);
        if (!Objects.equals(warning.getStatus(), 1)) {
            throw new BusinessException(40095, "预警不存在或未上架");
        }
        return warning;
    }

    private void requireAdmin(Integer roleId, String message) {
        if (!Objects.equals(roleId, 0)) {
            throw new BusinessException(403, message);
        }
    }

    private void validateTitle(String title) {
        if (isBlank(title)) {
            throw new BusinessException(40090, "标题和内容不能为空");
        }
        if (title.trim().length() > 200) {
            throw new BusinessException(40091, "标题长度不能超过200");
        }
    }

    private void validateSeverity(Integer severity) {
        if (severity == null || severity < 1 || severity > 3) {
            throw new BusinessException(40092, "severity 必须是 1-3");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(40093, "status 必须是 0 或 1");
        }
    }

    private Map<Long, WarningRead> loadReadMap(Long userId, Set<Long> warningIds) {
        if (userId == null || warningIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return warningReadMapper.selectList(new LambdaQueryWrapper<WarningRead>()
                        .eq(WarningRead::getUserId, userId)
                        .in(WarningRead::getWarningId, warningIds))
                .stream()
                .collect(Collectors.toMap(WarningRead::getWarningId, Function.identity(), (a, b) -> a));
    }

    private WarningItemVO toWarningVO(WarningRecord warning, User publisher, WarningRead read, boolean isRead) {
        return new WarningItemVO(
            String.valueOf(warning.getId()),
                warning.getTitle(),
                warning.getContent(),
                warning.getRegion(),
                warning.getPestName(),
                warning.getSeverity(),
                warning.getStatus(),
                warning.getPublishTime(),
            String.valueOf(warning.getPublisherId()),
                publisher == null ? "未知发布者" : publisher.getUsername(),
                warning.getViewCount() == null ? 0 : warning.getViewCount(),
                isRead,
                read == null ? null : read.getReadTime(),
                warning.getCreatedTime()
        );
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize, int defaultSize, int maxSize) {
        int size = pageSize == null || pageSize < 1 ? defaultSize : pageSize;
        return Math.min(size, maxSize);
    }

    private int pages(long total, int pageSize) {
        return (int) ((total + pageSize - 1) / pageSize);
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
