package com.ikats.scheduler.ibatis.mapper;

import com.ikats.scheduler.entity.bean.JSTCancelOrderBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Mapper
 * 
 * 聚水潭取消订单Ibatis接口
 * 
 * 自动生成
 *
 * @author over3
 *
 * @version
 *       1.0, 2017-12-18 09:53:44
 */
@Mapper
public interface JSTCancelOrderMapper {

    /** 添加单条记录 */
    int insert(JSTCancelOrderBean record);

    /** 添加一批记录 */
    int insertList(List<JSTCancelOrderBean> records);

    /** 删除记录 - 根据主键 */
    int delete(Long id);

    /** 更新记录 - 根据主键 */
    int update(JSTCancelOrderBean record);

    /** 查询记录 - 根据主键 */
    JSTCancelOrderBean selectByKey(Long id);

    /** 查询所有记录计数 */
    Long selectCount(@Param("express") Map<String, String> express);

    /** 筛选记录 */
    List<JSTCancelOrderBean> selectByQuery(@Param("express") Map<String, String> express);

    /** 分页查询 */
    List<JSTCancelOrderBean> pageByQuery(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("express") Map<String, String> express);

    int updateByOrderNo(JSTCancelOrderBean bean);

    List<JSTCancelOrderBean> selectByCode(String orderNo);

    List<JSTCancelOrderBean> getOrderSendJob();
}
