package com.lz.fixflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.cache.DashboardCache;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.service.DashBoardService;
import com.lz.fixflow.vo.DashBoardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardServiceImpl implements DashBoardService {

    private final TicketMapper ticketMapper;
    private final CategoryMapper categoryMapper;
    
    private final JsonMapper jsonMapper;
    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(30);

    @Override
    @Audit("管理人员查看了统计数据")
    public DashBoardVO getDashBoard() {
        String cacheJson = getCacheJson();

		if (!cacheJson.isBlank() && cacheJson!=null) {
			try{
				return jsonMapper.readValue(cacheJson, DashboardCache.TYPE);
			}catch(JacksonException e){
				log.warn("Dashboard cache deserialization failed, reload from database");
				deleteCache(); //防止缓存json格式问题解析失败，导致缓存一直不可用
			}
		}

		DashBoardVO result = loadDateFromDB();
		saveCache(result);

        return result;
    }

    private String getCacheJson(){
        try{
            return redisTemplate.opsForValue().get(DashboardCache.KEY);
        }catch(DataAccessException e ){
			log.warn("Redis unavailable, fallback to database",e);
			return null;
		}
    }
	private void saveCache(DashBoardVO dashBoard){
		try{
			String value = jsonMapper.writeValueAsString(dashBoard); //对象转换为json格式的String
			redisTemplate.opsForValue().set(DashboardCache.KEY, value, TTL);
		}catch(JacksonException | DataAccessException e){
			log.warn("Failed to save dashboard cache",e);
		}
	}
	private void deleteCache(){
		try{
			redisTemplate.delete(DashboardCache.KEY);
		}catch(DataAccessException e ){
			log.warn("Dashboard cache delete failed");
		}
	}
	private DashBoardVO loadDateFromDB(){
        DashBoardVO vo = new DashBoardVO();

        vo.setTotalCount(ticketMapper.selectCount(null));

        vo.setOpenCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.OPEN.getValue())
        ));

        vo.setInProgressCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
        ));

        vo.setResolvedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.RESOLVED.getValue())
        ));

        vo.setClosedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getStatus, TicketStatus.CLOSED.getValue())
        ));

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        vo.setTodayCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .ge(Ticket::getCreatedAt,start)
                        .lt(Ticket::getCreatedAt,end)
        ));

        vo.setCategoryCount(categoryMapper.countCategory());

		return vo;
	}
}
