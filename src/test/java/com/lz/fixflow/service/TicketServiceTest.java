package com.lz.fixflow.service;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Category;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.enums.TicketAction;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketCommentMapper;
import com.lz.fixflow.mapper.TicketHistoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.impl.TicketServiceImpl;
import com.lz.fixflow.vo.TicketVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Locale;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class TicketServiceTest {

    private static final String TICKET_ID = "a0210aae-cc52-4824-ac94-62a60f83fd5d";
    private static final String STAFF_ID = "f1185aa8-8eef-4002-8f97-2d0c5df3c38d";

    @Mock private TicketMapper ticketMapper;
    @Mock private TicketHistoryMapper ticketHistoryMapper;
    @Mock private TicketHistoryService ticketHistoryService;
    @Mock private TicketCommentMapper ticketCommentMapper;
    @Mock private UserMapper userMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private PermissionService permissionService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private StpLogic originalLogic;

    @BeforeAll
    static void initMyBatisMetadata() {
        // LambdaUpdateWrapper 要解析 Ticket 的字段映射，单元测试里没有启动 Spring。
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("test.Ticket");
        TableInfoHelper.initTableInfo(assistant, Ticket.class);
    }

    @BeforeEach
    void mockCurrentUser() {
        // handleTicket() 从 Sa-Token 获取用户 ID，而不是把工作人员 ID 作为参数传入。
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override
            public String getLoginIdAsString() {
                return STAFF_ID;
            }
        });
    }

    @AfterEach
    void restoreLoginLogic() {
        StpUtil.setStpLogic(originalLogic);
    }

    @Test
    @DisplayName("接单成功时状态变更并记录历史")
    void success() {
        // Mapper 的 update(entity, wrapper) 是 BaseMapper 提供的，不需要自定义 claim()。
        when(ticketMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        Ticket ticket = new Ticket();
        ticket.setId(TICKET_ID);
        ticket.setStatus(TicketStatus.IN_PROGRESS.getValue());
        ticket.setAssigneeId(STAFF_ID);
        ticket.setCategoryId(1L);
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(ticket);

        Category category = new Category();
        category.setId(1L);
        category.setName("网络");
        when(categoryMapper.selectById(1L)).thenReturn(category);

        TicketVO result = ticketService.handleTicket(TICKET_ID);

        assertEquals(TICKET_ID, result.getId());
        assertEquals(TicketStatus.IN_PROGRESS.getValue(), result.getStatus());
        assertEquals(STAFF_ID, result.getAssigneeId());
        assertEquals("网络", result.getCategoryName());

        verify(ticketMapper).update(isNull(), any(Wrapper.class));
        verify(ticketHistoryService).recordHistory(
                TICKET_ID, STAFF_ID, TicketAction.HANDLE.getValue(),
                TicketStatus.OPEN.getValue(), TicketStatus.IN_PROGRESS.getValue());
    }

    @Test
    @DisplayName("工单已被接取时拒绝重复接单且不写入历史")
    void failure() {
        when(ticketMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

        assertThrows(BusinessException.class,
                () -> ticketService.handleTicket(TICKET_ID));

        verify(ticketMapper).update(isNull(), any(Wrapper.class));
        verify(ticketMapper, never()).selectById(TICKET_ID);
        verifyNoInteractions(ticketHistoryService, ticketHistoryMapper);
    }

@Test
@DisplayName("接单条件：只允许未接单的 OPEN 工单，并设置处理人")
void handleTicket_updateWrapper_containsExpectedConditions() {

        when(ticketMapper.update(
            isNull(), any(Wrapper.class)
        )).thenReturn(0);

        assertThrows(
            BusinessException.class,
            () -> ticketService.handleTicket(TICKET_ID)
        );

        ArgumentCaptor<Wrapper> captor =
            ArgumentCaptor.forClass(Wrapper.class);

        verify(ticketMapper).update(
            isNull(), captor.capture()
        );

        LambdaUpdateWrapper<?> wrapper = assertInstanceOf(
            LambdaUpdateWrapper.class,
            captor.getValue()
        );

        String whereSql = wrapper.getSqlSegment()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ");

        String setSql = wrapper.getSqlSet()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ");

        assertTrue(whereSql.contains("status ="), whereSql);
        assertTrue(whereSql.contains("id ="), whereSql);
        assertTrue(whereSql.contains("assignee_id is null"), whereSql);

        assertTrue(setSql.contains("status="), setSql);
        assertTrue(setSql.contains("assignee_id="), setSql);

        var values = wrapper.getParamNameValuePairs().values();

        assertTrue(values.contains(TicketStatus.OPEN.getValue()));
        assertTrue(values.contains(TICKET_ID));
        assertTrue(values.contains(TicketStatus.IN_PROGRESS.getValue()));
        assertTrue(values.contains(STAFF_ID));
    }
}
