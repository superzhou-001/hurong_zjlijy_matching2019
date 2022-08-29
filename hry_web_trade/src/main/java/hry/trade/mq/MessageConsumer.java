package hry.trade.mq;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import hry.trade.mq.service.MessageProducer;

public class MessageConsumer implements MessageListener {
   private Logger logger = Logger.getLogger(MessageConsumer.class);  
   
   @Resource
   private MessageProducer messageProducer;
   
   @Override
   public void onMessage(Message message) {
        logger.info("consumer receive message------->"+ new String(message.getBody())); 
        	int i = 10/0;
			
//        messageProducer.toTrade(message);
//        messageProducer.toAccount(message);
   }

}
