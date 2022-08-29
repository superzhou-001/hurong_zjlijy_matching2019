package hry.cm4.mq.service;

import com.alibaba.fastjson.JSON;
import hry.cm4.dto.AccountaddTrade;
import hry.redis.common.utils.RedisService;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CmMessageProducer {
	private Logger logger = Logger.getLogger(CmMessageProducer.class);

    @Resource(name="amqpTemplate")
    private AmqpTemplate rabbitTemplate;

    @Resource
	private RedisService redisService;

	/**
	 * 发送主账户操作信息
	 * @param message
	 */
	public void toAccount(Object message)  {
		List<AccountaddTrade> accountaddlist = JSON.parseArray(message.toString(), AccountaddTrade.class);
		for(AccountaddTrade accountadd:accountaddlist){
			if(accountadd.getMoney().compareTo(new BigDecimal("999999999"))==1
					||accountadd.getMoney().compareTo(new BigDecimal("-999999999"))==-1){
				throw new RuntimeException("资金数据太大");
			}
		}
		String isStop = redisService.get("deal:stop");
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(isStop)){
			throw new RuntimeException("资金通道发送堵塞请不要进行资金操作");
		}

		try {
			rabbitTemplate.convertAndSend("toAccountKey", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送Cm账户操作信息
	 * @param message
	 */
	public void toCmAccount(Object message)  {
		List<AccountaddTrade> accountaddlist = JSON.parseArray(message.toString(), AccountaddTrade.class);
		for(AccountaddTrade accountadd:accountaddlist){
			if(accountadd.getMoney().compareTo(new BigDecimal("999999999"))==1
					||accountadd.getMoney().compareTo(new BigDecimal("-999999999"))==-1){
				throw new RuntimeException("资金数据太大");
			}
		}
		String isStop = redisService.get("deal:stop");
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(isStop)){
			throw new RuntimeException("资金通道发送堵塞请不要进行资金操作");
		}

		try {
			rabbitTemplate.convertAndSend("toCm4AccountKey", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
