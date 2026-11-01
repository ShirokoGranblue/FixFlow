# 性能基线与并发验证（待实测）

此文件提供**实验步骤而非实验结果**。不要虚构 QPS、P95 或优化百分比。

## 测试环境

1. 仅使用 `fixflow_test` 数据库。不要使用真实业务数据。测试前确认 `SELECT DATABASE()` 返回 `fixflow_test`。
2. 为 1 万条、10 万条模拟工单准备独立测试数据；记录 MySQL 版本、CPU、内存、连接池上限与网络延迟。
3. 分别测量列表第 1 页、第 100 页和大 OFFSET 页；两组需使用相同数据与条件。

## SQL 执行计划

在测试数据库上执行：

```sql
EXPLAIN ANALYZE
SELECT id, title, creator_id, category_id, status, created_at
FROM ticket
WHERE creator_id = '替换为测试用户 UUID'
ORDER BY created_at DESC, id DESC LIMIT 10 OFFSET 0;

EXPLAIN ANALYZE
SELECT id, title, creator_id, category_id, status, created_at
FROM ticket
WHERE status = 'OPEN' AND assignee_id IS NULL
ORDER BY created_at ASC, id ASC LIMIT 10 OFFSET 0;

EXPLAIN ANALYZE
SELECT id, title, creator_id, category_id, status, updated_at
FROM ticket
WHERE assignee_id = '替换为测试人员 UUID' AND status = 'IN_PROGRESS'
ORDER BY updated_at DESC, id DESC LIMIT 10 OFFSET 0;
```

已存在 `idx_ticket_creator_created_at` 和 `idx_ticket_status_created_at`。先对照执行计划检查扫描行数及回表行为，再决定是否新增 `(assignee_id,status,updated_at,id)` 等联合索引。不要无依据重复建索引。注意 UUID 主键已隐含于 InnoDB 二级索引中，是否显式包含 `id` 应由执行计划确认。

## 验收记录表

| 指标 | 旧列表 API | 新分页 API | 备注 |
|---|---:|---:|---|
| 一次请求 SQL 次数 | 待测 | 待测 | 开启 SQL 统计，不计 Actuator |
| P50 响应时间 | 待测 | 待测 | 固定并发，预热后测量 |
| P95 响应时间 | 待测 | 待测 | 重复至少 3 轮 |
| 最大响应对象大小 | 待测 | 待测 | 相同筛选条件 |
| 一组工单同时接单成功数 | 待测 | 待测 | 预期最多 1 次成功 |

## 并发边界

- `OPEN → IN_PROGRESS`：接单与管理员分配同时开始，仅一个条件更新成功，历史仅记录成功者。
- `RESOLVED → CLOSED / IN_PROGRESS`：创建者关闭与重开并发，仅一个更新成功。
- 模拟历史写入失败：验证工单状态与历史同事务回滚；独立失败审计仍提交。
- 先用现有 JUnit 集成测试验证正确性，再用 k6/JMeter 做负载测试；压测数据和吞吐量不能互相替代。
