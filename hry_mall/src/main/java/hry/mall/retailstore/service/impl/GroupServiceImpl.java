/**
 * Copyright:   
 * @author:      luyue
 * @version:     V1.0 
 * @Date:        2019-05-09 17:54:25 
 */
package hry.mall.retailstore.service.impl;


import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.mall.goods.model.Goods;
import hry.mall.goods.model.GoodsSpec;
import hry.mall.goods.service.GoodsService;
import hry.mall.goods.service.GoodsSpecService;
import hry.mall.goods.service.SearchGoodsService;
import hry.mall.order.model.Order;
import hry.mall.order.service.OrderGoodsService;
import hry.mall.order.service.OrderService;
import hry.mall.platform.model.OrderAddress;
import hry.mall.platform.service.OrderAddressService;
import hry.mall.retailstore.dao.GroupDao;
import hry.mall.retailstore.model.ActivityGoods;
import hry.mall.retailstore.model.Group;
import hry.mall.retailstore.model.GroupDetail;
import hry.mall.retailstore.service.ActivityGoodsService;
import hry.mall.retailstore.service.GroupDetailService;
import hry.mall.retailstore.service.GroupService;
import hry.manage.remote.RemoteManageService;
import hry.manage.remote.model.User;
import hry.util.QueryFilter;
import hry.util.SpringUtil;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p> GroupService </p>
 * @author:         luyue
 * @Date :          2019-05-09 17:54:25  
 */
@Service("groupService")
public class GroupServiceImpl extends BaseServiceImpl<Group, Long> implements GroupService {
	@Resource
	public GroupDetailService groupDetailService;
	@Resource
	public OrderService orderService;
	@Resource
	public OrderAddressService orderAddressService;
	@Resource
	public OrderGoodsService orderGoodsService;
	@Resource
	public GoodsService goodsService;
	@Resource
	public GoodsSpecService goodsSpecService;
	@Resource
	public SearchGoodsService searchGoodsService;
	@Resource
	public ActivityGoodsService activityGoodsService;
	
	@Resource(name="groupDao")
	@Override
	public void setDao(BaseDao<Group, Long> dao) {
		super.dao = dao;
	}

	@Override
	public List<Group> findGroup(Map<String, String> map) {
		// TODO Auto-generated method stub
		List<Group> list=((GroupDao)dao).findGroup(map);
		return list;
	}

	@Override
	public Group saveGroup(ActivityGoods activityGoods, String memberId) {
		// TODO Auto-generated method stub
		Group group=new Group();
		      group.setActivityId(activityGoods.getActivityId());
		      group.setActivityGoodsId(activityGoods.getId());
		      group.setGoodsId(activityGoods.getGoodsId());
		      group.setGoodsName(activityGoods.getGoodsName());
		      group.setGoodsSpecId(activityGoods.getGoodsSpecId());
		      group.setFounderId(Long.valueOf(memberId));;
		      group.setCount(activityGoods.getPeopleCount());
		      group.setAlreadyCount(0);
		      group.setStatus(0);;
		      group.setLimitHour(activityGoods.getLimitHour());
		      group.setGoodsPrice(activityGoods.getSpecGoodsPrice());
		      group.setActivityPrice(activityGoods.getActivityPrice());
		      RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
			  User user=remoteManageService.findUserById(Long.valueOf(memberId));
			  group.setFounderName(user.getNickNameOtc());//????????????
		      group.setImage(user.getUserFace());
		      this.save(group);
		return group;
	}

	@Override
	public void groupSuccess(Group group) {
		// TODO Auto-generated method stub
		QueryFilter  filter=new QueryFilter(GroupDetail.class);
		filter.addFilter("groupId=", group.getId());
		filter.addFilter("status=", 1);
		List<GroupDetail> list=groupDetailService.find(filter);
		for(GroupDetail detail :list){
			if(detail.getIsRobot()!=1){
				Order order=this.saveOrder(detail);
				detail.setOrderId(order.getId());
				detail.setStatus(2);
				groupDetailService.update(detail);
			}
		}
	}

	@Override
	public Order saveOrder(GroupDetail detail) {
		// TODO Auto-generated method stub
		Order order=new Order();
		String number=orderService.createNumber();
		order.setOrderSn(number);
		order.setMemberId(detail.getMemberId());
		order.setOrderState(20);
		RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
		User user=remoteManageService.findUserById(detail.getMemberId());
		if(null!=user.getMobile() && !"".equals(user.getMobile())){
			order.setUserName(user.getMobile());
		}else if(null!=user.getEmail() && !"".equals(user.getEmail())){
			order.setUserName(user.getEmail());
			
		}
		order.setMemberName(user.getSurname()+"*"+user.getTruename());
   		order.setGoodsAmount(detail.getTotalPrice());
   		order.setDiscount(detail.getDiscount());//??????????????????
   		order.setOrderAmount(detail.getAmount());//??????????????????
   		order.setOrderTotalPrice(detail.getTotalPrice());//???????????????
   		order.setPaymentState(Integer.valueOf("1"));
   		//?????????????????? ???????????????
   		order.setOrderSort(Integer.valueOf(4));
   		order.setOrderSource("APP");
   		order.setShippingFee(detail.getShippingFee());
   		order.setPayType(detail.getPayType());//????????????,?????????:1,????????????2,???????????????3
   		order.setRmbMoney(detail.getRmbMoney());
   		order.setRmbFeeMoney(detail.getRmbFeeMoney());
   		order.setRmbFeeRate(detail.getRmbFeeRate());
   		//?????????????????????????????????????????????????????????id,????????????
   		if("1".equals(detail.getPayType())){
   			order.setPaymentId(Long.valueOf(detail.getPaymentId()));
   		}
   	     order.setCoinRate(detail.getCoinRate());
		 order.setCoinCode(detail.getCoinCode());
		 order.setCoinCount(detail.getCoinCount());//???????????????
		 order.setCoinFeeRate(detail.getCoinFeeRate());
  		 order.setCoinFeeCount(detail.getCoinFeeCount());
  		 order.setCoinPayStatus(detail.getCoinPayStatus());
  		 order.setRmbPayStatus(detail.getRmbPayStatus());
  		orderService.save(order);
  		//??????????????????
  		OrderAddress orderAddress=new OrderAddress();
		orderAddress.setMemberId(detail.getMemberId());
		orderAddress.setOrderId(order.getId());
		orderAddress.setProvinceId(detail.getProvinceId());
		orderAddress.setCityId(detail.getCityId());
		orderAddress.setCountyId(detail.getCountyId());
		orderAddress.setStreet(detail.getStreet());
		orderAddress.setDetailAddress(detail.getDetailAddress());
		orderAddress.setCellphone(detail.getCellphone());
		orderAddress.setReceiveName(detail.getReceiveName());
		orderAddress.setZipCode(detail.getZipCode());
		orderAddressService.save(orderAddress);
  		//????????????-????????????
		Group group=this.get(detail.getGroupId());
		Goods good=goodsService.get(group.getGoodsId());
		GoodsSpec goodspec=goodsSpecService.get(group.getGoodsSpecId());
		good.setActivityGoodsId(group.getActivityGoodsId());
		orderGoodsService.saveOrderGoods(good, goodspec, order, detail.getCount(),detail.getActivityPrice());
		//???????????????????????????????????????
		goodspec.setSpecGoodsStorage(goodspec.getSpecGoodsStorage()-detail.getCount());
		goodsSpecService.update(goodspec);
	 	ActivityGoods  activityGoods=activityGoodsService.get(group.getActivityGoodsId());
	 	activityGoods.setActivityStorage(activityGoods.getActivityStorage()-detail.getCount());
	 	activityGoodsService.update(activityGoods);
		//????????????????????????
		good.setSaleNum(good.getSaleNum()+detail.getCount());
		goodsService.update(good);
        //????????????solr
        searchGoodsService.updateSolrGoods(good.getId());
		return order;
		
	}
	

}
