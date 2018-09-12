package com.ikats.scheduler.repository;

import java.util.List;
import java.util.Map;
import com.ikats.scheduler.entity.bean.GYSkuBean;

/**
 * Repository
 * 
 * 管易商品对接表数据操作接口
 * 
 * 自动生成
 *
 * @author over3
 *
 * @version
 *       1.0, 2017-12-05 17:29:55
 */
public interface IGYSkuRepository {

    /** 添加单条记录 */
    int insert(GYSkuBean record);

    /** 添加一批记录 */
    int insertList(List<GYSkuBean> records);

    /** 删除记录 - 根据主键 */
    int delete(Long id);

    /** 更新记录 - 根据主键 */
    int update(GYSkuBean record);

    int updateByCode(GYSkuBean bean);

    /** 查询记录 - 根据主键 */
    GYSkuBean selectByKey(Long id);

    List<GYSkuBean> selectByNo(String code);

    /** 查询所有记录计数 */
    Long selectCount(Map<String, String> express);

    /** 筛选记录 */
    List<GYSkuBean> selectByQuery(Map<String, String> express);

    /** 分页查询 */
    List<GYSkuBean> pageByQuery(int pageNum, int pageSize, Map<String, String> express);

    List<GYSkuBean> getSkuSendJob();
}
