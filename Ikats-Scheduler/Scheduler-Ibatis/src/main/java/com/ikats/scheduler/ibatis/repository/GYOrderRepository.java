package com.ikats.scheduler.ibatis.repository;

import java.util.List;
import java.util.Map;
import com.ikats.scheduler.entity.bean.GYOrderBean;
import com.ikats.scheduler.ibatis.mapper.GYOrderMapper;
import com.ikats.scheduler.repository.IGYOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Repository
 * 
 * 管易订单对接表数据操作
 * 
 * 自动生成
 *
 * @author over3
 *
 * @version
 *       1.0, 2017-12-05 09:31:12
 */
@Repository
public class GYOrderRepository implements IGYOrderRepository{

	@Autowired
	private GYOrderMapper mapper;

    /** 添加单条记录 */
    @Override
    public int insert(GYOrderBean record)
	{
		return mapper.insert(record);
	}

    /** 添加一批记录 */
    @Override
    public int insertList(List<GYOrderBean> records)
	{
		return mapper.insertList(records);
	}

    /** 删除记录 - 根据主键 */
    @Override
    public int delete(Long id)
	{
		return mapper.delete(id);
	}

    /** 更新记录 - 根据主键 */
    @Override
    public int update(GYOrderBean record)
	{
		return mapper.update(record);
	}

    /** 查询记录 - 根据主键 */
    @Override
    public GYOrderBean selectByKey(Long id)
	{
		return mapper.selectByKey(id);
	}

    /** 所有记录计数 */
    @Override
    public Long selectCount(Map<String, String> express)
	{
		return mapper.selectCount(express);
	}

    /** 筛选记录 */
    @Override
    public List<GYOrderBean> selectByQuery(Map<String, String> express)
	{
		return mapper.selectByQuery(express);
	}

    /** 分页查询 */
    @Override
    public List<GYOrderBean> pageByQuery(int offset, int pageSize, Map<String, String> express)
	{
		offset = pageSize * (offset - 1);
		return mapper.pageByQuery(offset,pageSize,express);
	}

	@Override
	public List<GYOrderBean> pageByQueryException(int offset, int pageSize, Map<String, String> express) {
		offset = pageSize * (offset - 1);
		return this.mapper.pageByQueryException(offset,pageSize,express);
	}

	@Override
	public void updateByOrderNo(GYOrderBean bean)
	{
		this.mapper.updateByOrderNo(bean);
	}

	@Override
	public List<GYOrderBean> selectByNo(Map<String, String> express) {
		return this.mapper.selectByNo(express);
	}

	@Override
	public List<GYOrderBean> getOrderSendJob() {
		return this.mapper.getOrderSendJob();
	}
}
