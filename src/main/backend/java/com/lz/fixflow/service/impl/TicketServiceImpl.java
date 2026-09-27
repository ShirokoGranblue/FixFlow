package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.dto.TicketAssignDTO;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.entity.*;
import com.lz.fixflow.enums.TicketAction;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.enums.UserRole;
import com.lz.fixflow.mapper.*;
import com.lz.fixflow.service.PermissionService;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.vo.TicketCommentVO;
import com.lz.fixflow.vo.TicketDetailVO;
import com.lz.fixflow.vo.TicketHistoryVO;
import com.lz.fixflow.vo.TicketVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final TicketCommentMapper ticketCommentMapper;
    private final UserMapper userMapper;
    private final TicketHistoryMapper ticketHistoryMapper;
    private final CategoryMapper categoryMapper;
    private final PermissionService permissionService;

    private TicketVO convertToVO(Ticket entity,Category category) {

        TicketVO vo = new TicketVO();

        if(category != null){
            vo.setCategoryName(category.getName());
        }

        vo.setCategoryId(entity.getCategoryId());

        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setPriority(entity.getPriority());
        vo.setStatus(entity.getStatus());

        vo.setAssigneeId(entity.getAssigneeId());
        vo.setCreatorId(entity.getCreatorId());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setResolvedAt(entity.getResolvedAt());

        return vo;
    }
    private TicketVO convertTicketToVO(Ticket entity,Map<Long ,Category> categoryMap) {

        TicketVO vo = new TicketVO();
        Category category = categoryMap.get(entity.getCategoryId());

        vo.setCategoryId(entity.getCategoryId());

        if(category != null) {
            vo.setCategoryName(category.getName());
        }

        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setPriority(entity.getPriority());
        vo.setStatus(entity.getStatus());
        vo.setCreatorId(entity.getCreatorId());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setResolvedAt(entity.getResolvedAt());
        vo.setAssigneeId(entity.getAssigneeId());

        return vo;
    }
    private TicketCommentVO convertTicketCommentToVO(TicketComment entity) {

        TicketCommentVO vo = new TicketCommentVO();

        vo.setId(entity.getId());
        vo.setTicketId(entity.getTicketId());
        vo.setAuthorId(entity.getAuthorId());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setContent(entity.getContent());

        return vo;
    }
    private TicketHistoryVO convertHistoryToVO(TicketHistory ticketHistory){

        TicketHistoryVO vo = new TicketHistoryVO();

        vo.setId(ticketHistory.getId());
        vo.setCreatedAt(ticketHistory.getCreatedAt());
        vo.setAction(ticketHistory.getAction());
        vo.setFromStatus(ticketHistory.getFromStatus());
        vo.setToStatus(ticketHistory.getToStatus());
        vo.setOperatorid(ticketHistory.getOperatorId());

        return vo;
    }
    private void recordHistory(
            String ticketid,
            String operatorid,
            String action,
            String fromStatus,
            String toStatus
    ) {

        TicketHistory ticketHistory = new TicketHistory();

        ticketHistory.setId(UUID.randomUUID().toString());
        ticketHistory.setTicketId(ticketid);
        ticketHistory.setOperatorId(operatorid);
        ticketHistory.setAction(action);
        ticketHistory.setFromStatus(fromStatus);
        ticketHistory.setToStatus(toStatus);
        ticketHistory.setCreatedAt(LocalDateTime.now());

        ticketHistoryMapper.insert(ticketHistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Audit("用户创建工单")
    public TicketVO createTicket(TicketCreateDTO dto){

        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = new Ticket();

        Category category = categoryMapper.selectById(dto.getCategoryId());

        if (category == null) {
            throw new BusinessException("工单分类不存在");
        }

        ticket.setCategoryId(dto.getCategoryId());

        ticket.setId(UUID.randomUUID().toString());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(TicketStatus.OPEN.getValue());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCreatorId(userid);

        ticketMapper.insert(ticket);

        recordHistory(
                ticket.getId(),
                userid,
                TicketAction.CREATE.getValue(),
                TicketStatus.NULL.getValue(),
                TicketStatus.OPEN.getValue()
        );

        return convertToVO(ticket,category);
    }

    @Override
    @Transactional
    @Audit("用户取消工单")
    public TicketVO cancelTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();

        User user = userMapper.selectById(userid);

        permissionService.requireTicketAccess(userid,ticketMapper.selectById(id));

        int row = ticketMapper.update(null ,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getId,id)
                        .eq(Ticket::getCreatorId,user.getId())
                        .eq(Ticket::getStatus,TicketStatus.OPEN.getValue())
                        .isNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus,TicketStatus.CANCELLED.getValue())
                        .set(Ticket::getUpdatedAt,LocalDateTime.now())
        );

        if(row == 0){
            throw new BusinessException("工单已经取消或不存在");
        }

        recordHistory(
                id,
                userid,
                TicketAction.CANCEL.getValue(),
                TicketStatus.OPEN.getValue(),
                TicketStatus.CANCELLED.getValue()
        );

        Ticket ticket = ticketMapper.selectById(id);
        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Transactional
    @Audit("工单重新发布")
    public TicketVO reOpenTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();

        User user = userMapper.selectById(userid);

        permissionService.requireTicketAccess(userid,ticketMapper.selectById(id));

        int row = ticketMapper.update(
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getId,id)
                        .eq(Ticket::getStatus,TicketStatus.RESOLVED.getValue())
                        .eq(Ticket::getCreatorId,user.getId())
                        .isNotNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
                        .set(Ticket::getUpdatedAt,LocalDateTime.now())
                        .set(Ticket::getResolvedAt,null)
        );

        if(row == 0){
            throw new BusinessException("工单已重新提交或工单不存在");
        }

        recordHistory(
                id,
                userid,
                TicketAction.REOPEN.getValue(),
                TicketStatus.RESOLVED.getValue(),
                TicketStatus.IN_PROGRESS.getValue()
        );

        Ticket ticket = ticketMapper.selectById(id);
        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Audit("用户读取工单")
    public List<TicketVO> showTickets() {

        String userid = StpUtil.getLoginIdAsString();

        List<Ticket> list = ticketMapper.selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getCreatorId,userid)
                .orderByDesc(Ticket::getCreatedAt)
        );

        if(list == null || list.isEmpty()){
            throw new BusinessException("当前无工单");
        }

        List<Long> categoryIds = list.stream()
                .map(Ticket::getCategoryId)
                .distinct()
                .toList();

        List<Category> categories;

        if (categoryIds.isEmpty()){
            categories = List.of();
        }else{
            categories = categoryMapper.selectBatchIds(categoryIds);
        }

        Map<Long,Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category->category));

        return list.stream()
                .map(ticket->convertTicketToVO(ticket,categoryMap))
                .toList();
    }

    @Override
    @Audit("用户读取工单")
    public TicketVO showTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = ticketMapper.selectById(id);

        permissionService.requireTicketAccess(userid,ticket);

        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Audit("工作人员读取待处理工单")
    public List<TicketVO> getOpenTickets() {

        List<Ticket> list = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getStatus, TicketStatus.OPEN.getValue())
                    .isNull(Ticket::getAssigneeId)
                    .orderByAsc(Ticket::getCreatedAt)
        );

        if (list==null|| list.isEmpty()){
            throw new BusinessException("当前无待处理工单");
        }

        List<Long> categoryIds = list.stream()
                .map(Ticket::getCategoryId)
                .distinct()
                .toList();

        List<Category> categories;
        if (categoryIds.isEmpty()){
            categories = List.of();
        }else{
            categories = categoryMapper.selectBatchIds(categoryIds);
        }

        Map<Long , Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category->category));

        return list.stream()
                .map(ticket -> convertTicketToVO(ticket,categoryMap))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Audit("工单被接取")
    public TicketVO handleTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();

        int row = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.OPEN.getValue())
                        .eq(Ticket::getId,id)
                        .isNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
                        .set(Ticket::getAssigneeId,userid)
                );

        if(row==0){
            throw new BusinessException("工单不存在或已被处理");
        }

        Ticket ticket = ticketMapper.selectById(id);

        recordHistory(
                id,
                userid,
                TicketAction.HANDLE.getValue(),
                TicketStatus.OPEN.getValue(),
                TicketStatus.IN_PROGRESS.getValue()
        );

        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Audit("工作人员读取处理中工单")
    public List<TicketVO> getAssignedTickets() {

        String userid = StpUtil.getLoginIdAsString();

        List<Ticket> list = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getAssigneeId,userid)
                        .eq(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
                        .orderByDesc(Ticket::getUpdatedAt)
        );

        if (list==null|| list.isEmpty()){
            throw new BusinessException("当前无处理中的工单");
        }

        List<Long> categoryIds = list.stream()
                .map(Ticket::getCategoryId)
                .distinct()
                .toList();

        List<Category> categories;
        if (categoryIds.isEmpty()){
            categories = List.of();
        }else{
            categories = categoryMapper.selectBatchIds(categoryIds);
        }

        Map<Long,Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category->category));

        return list.stream()
                .map(ticket->convertTicketToVO(ticket,categoryMap))
                .toList();
    }

    @Override
    @Transactional (rollbackFor = Exception.class)
    @Audit("工单已被处理完成")
    public TicketVO resolveTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();

        int row = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getId,id)
                        .eq(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
                        .eq(Ticket::getAssigneeId,userid)
                        .set(Ticket::getStatus,TicketStatus.RESOLVED.getValue())
                        .set(Ticket::getUpdatedAt,LocalDateTime.now())
                        .set(Ticket::getResolvedAt,LocalDateTime.now())
                );

        if(row == 0){
            throw new BusinessException("工单不存在或已被处理");
        }

        Ticket ticket = ticketMapper.selectById(id);

        recordHistory(
                id,
                userid,
                TicketAction.RESOLVE.getValue(),
                TicketStatus.IN_PROGRESS.getValue(),
                TicketStatus.RESOLVED.getValue()
        );

        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Transactional
    @Audit("工单已关闭")
    public TicketVO closeTicket(String id) {

        String userid = StpUtil.getLoginIdAsString();

        int row = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getId,id)
                        .eq(Ticket::getStatus,TicketStatus.RESOLVED.getValue())
                        .eq(Ticket::getAssigneeId,userid)
                        .set(Ticket::getStatus,TicketStatus.CLOSED.getValue())
                        .set(Ticket::getUpdatedAt,LocalDateTime.now())
        );

        if(row == 0){
            throw  new BusinessException("工单不存在或当前状态无法关闭");
        }

        Ticket ticket = ticketMapper.selectById(id);

        recordHistory(
                id,
                userid,
                TicketAction.CLOSE.getValue(),
                TicketStatus.RESOLVED.getValue(),
                TicketStatus.CLOSED.getValue()
        );

        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Audit("用户访问工单历史")
    public List<TicketHistoryVO> getTicketHistory(String id){

        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = ticketMapper.selectById(id);
        permissionService.requireTicketAccess(userid,ticket);

        List<TicketHistory> list = ticketHistoryMapper.selectList(
                new LambdaQueryWrapper<TicketHistory>()
                        .eq(TicketHistory::getTicketId,id)
                        .orderByDesc(TicketHistory::getCreatedAt)
        );

        return list.stream()
                .map(this::convertHistoryToVO)
                .toList();
    }

    @Override
    @Audit("工作人员查询搜索工单页")
    public PageResult<TicketVO> searchTickets(TicketQueryDTO dto) {

        long pageSize = Math.min(dto.getPageSize(),100);

        Page<Ticket> page = new Page<>(dto.getCurrent(),pageSize);
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        if(dto.getKeyword()!=null && !dto.getKeyword().isBlank()){

            wrapper.like(Ticket::getTitle,dto.getKeyword());
        }

        if(dto.getStatus()!=null && !dto.getStatus().isBlank()){

            wrapper.eq(Ticket::getStatus,dto.getStatus());
        }

        if (dto.getPriority()!=null && !dto.getPriority().isBlank()){

            wrapper.eq(Ticket::getPriority,dto.getPriority());
        }

        if(dto.getCategoryId()!=null){

            wrapper.eq(Ticket::getCategoryId,dto.getCategoryId());
        }

        switch (dto.getSort()) {
            case "create_asc" -> wrapper.orderByAsc(Ticket::getCreatedAt);
            case "priority_asc" -> wrapper.last("""
                    ORDER BY CASE priority
                        WHEN 'URGENT' THEN 4
                        WHEN 'HIGH' THEN 3
                        WHEN 'MEDIUM' THEN 2
                        WHEN 'LOW' THEN 1
                    END ASC
                    """);
            case "priority_desc" -> wrapper.last("""
                            ORDER BY CASE priority
                                WHEN 'URGENT' THEN 4
                                WHEN 'HIGH' THEN 3
                                WHEN 'MEDIUM' THEN 2
                                WHEN 'LOW' THEN 1
                            END DESC
                    """);
            case null, default -> wrapper.orderByDesc(Ticket::getCreatedAt);
        }

        Page<Ticket> result = ticketMapper.selectPage(page, wrapper);

        List<Ticket> tickets = result.getRecords();
        List<Long> categoryIds = tickets.stream()
                .map(Ticket::getCategoryId)
                .distinct()
                .toList();

        List<Category> categories;
        if (categoryIds.isEmpty()) {
            categories = List.of();
        }else {
            categories = categoryMapper.selectBatchIds(categoryIds);
        }

        Map<Long,Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId,category -> category));

        List<TicketVO> ticketVO = tickets.stream()
                .map(ticket -> convertTicketToVO(ticket,categoryMap))
                .toList();

        return new PageResult<>(
                ticketVO,
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
        );
    }

    @Override
    @Transactional
    @Audit("管理员分派工单处理人")
    public TicketVO assignTicket(String id, TicketAssignDTO dto){

        String userid = StpUtil.getLoginIdAsString();

        User user = userMapper.selectById(dto.getAssignId());

        if (user == null) {
            throw new BusinessException("处理人员不存在");
        }

        if (UserRole.USER.getValue().equals(user.getRole())) {
            throw new BusinessException("无法分配工单给普通用户");
        }
        int row = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.OPEN.getValue())
                        .eq(Ticket::getId,id)
                        .isNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
                        .set(Ticket::getAssigneeId,dto.getAssignId())
                        .set(Ticket::getUpdatedAt,LocalDateTime.now())
        );

        if (row == 0){
            throw new BusinessException("工单不存在或已被处理");
        }

        recordHistory(
                id,
                userid,
                TicketAction.ASSIGN.getValue(),
                TicketStatus.OPEN.getValue(),
                TicketStatus.IN_PROGRESS.getValue()
        );

        Ticket ticket = ticketMapper.selectById(id);

        Category category = categoryMapper.selectById(ticket.getCategoryId());

        return convertToVO(ticket,category);
    }

    @Override
    @Audit("用户读取工单信息")
    public TicketDetailVO getTicketDetail(String id) {

        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = ticketMapper.selectById(id);

        permissionService.requireTicketAccess(userid,ticket);

        List<TicketComment> comments = ticketCommentMapper.selectList(
                new LambdaQueryWrapper<TicketComment>()
                        .eq(TicketComment::getTicketId,id)
                        .orderByDesc(TicketComment::getCreatedAt)
        );

        List<TicketHistory> histories = ticketHistoryMapper.selectList(
                new LambdaQueryWrapper<TicketHistory>()
                        .eq(TicketHistory::getTicketId,id)
                        .orderByDesc(TicketHistory::getCreatedAt)
        );

        List<TicketCommentVO> commentVO = comments.stream()
                .map(this::convertTicketCommentToVO)
                .toList();

        List<TicketHistoryVO> historyVO = histories.stream()
                .map(this::convertHistoryToVO)
                .toList();

        TicketDetailVO detail = new TicketDetailVO();
        Category category =  categoryMapper.selectById(ticket.getCategoryId());

        detail.setTicket(convertToVO(ticket,category));
        detail.setTicketComments(commentVO);
        detail.setTicketHistory(historyVO);

        return detail;
    }
}
