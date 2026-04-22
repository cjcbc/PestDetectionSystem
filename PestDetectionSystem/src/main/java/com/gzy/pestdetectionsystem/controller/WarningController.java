package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.CreateWarningDTO;
import com.gzy.pestdetectionsystem.dto.UpdateWarningDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.service.WarningService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.TokenAuthHelper;
import com.gzy.pestdetectionsystem.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warning")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;
    private final TokenAuthHelper tokenAuthHelper;

    @PostMapping
    public Result<WarningItemVO> createWarning(@RequestBody CreateWarningDTO dto,
                                               HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        Long userId = user.getUserId();
        Integer roleId = user.getRoleId();
        return Result.ok(warningService.createWarning(userId, roleId, dto));
    }

        @PatchMapping("/{warningId}")
        public Result<WarningItemVO> updateWarning(@PathVariable String warningId,
                               @RequestBody UpdateWarningDTO dto,
                               HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        return Result.ok(warningService.updateWarning(
            user.getUserId(),
            user.getRoleId(),
            parseId(warningId, "warningId"),
            dto
        ));
        }

        @PatchMapping("/{warningId}/status-toggle")
        public Result<WarningItemVO> toggleWarningStatus(@PathVariable String warningId,
                                 HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        return Result.ok(warningService.toggleWarningStatus(
            user.getUserId(),
            user.getRoleId(),
            parseId(warningId, "warningId")
        ));
        }

        @DeleteMapping("/{warningId}")
        public Result<?> deleteWarning(@PathVariable String warningId,
                       HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        warningService.deleteWarning(
            user.getUserId(),
            user.getRoleId(),
            parseId(warningId, "warningId")
        );
        return Result.ok("删除成功");
        }

    @GetMapping
    public Result<PageVO<WarningItemVO>> getWarnings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        TokenAuthHelper.AuthUser authUser = tokenAuthHelper.optionalUser(request);
        Long userId = authUser == null ? null : authUser.getUserId();
        Integer roleId = authUser == null ? null : authUser.getRoleId();
        return Result.ok(warningService.getWarnings(userId, roleId, page, pageSize, status, severity, region, keyword));
    }

    @PostMapping("/{warningId}/read")
    public Result<WarningReadVO> markRead(@PathVariable String warningId,
                                          HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(warningService.markRead(userId, parseId(warningId, "warningId")));
    }

    @GetMapping("/unread/count")
    public Result<UnreadWarningCountVO> getUnreadCount(HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(warningService.getUnreadCount(userId));
    }

    @GetMapping("/{warningId}")
    public Result<WarningItemVO> getWarningDetail(@PathVariable String warningId,
                                                  HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        return Result.ok(warningService.getWarningDetail(user.getUserId(), user.getRoleId(), parseId(warningId, "warningId")));
    }

    private Long parseId(String raw, String fieldName) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new BusinessException(40001, fieldName + " 格式不正确");
        }
    }
}
