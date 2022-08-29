package hry.util;

import com.alibaba.fastjson.JSON;

import java.util.List;


/**
 * 
 * <p> TODO</p>
 * @author:         Shangxl 
 * @Date :          2017年8月18日 下午4:58:01
 */
public class ObjectUtil {
	/**
	 * 转化bean类型
	 * <p> TODO</p>
	 * @author:         Shangxl
	 * @param:    @param object
	 * @param:    @param clazz
	 * @param:    @return
	 * @return 
	 * @return: Object 
	 * @Date :          2017年8月18日 下午5:51:11   
	 * @throws:
	 */
	public static <T> T bean2bean(Object object,Class<T> clazz){
		if(object!=null){
			return JSON.parseObject(JSON.toJSONString(object),clazz);
		}else{
			return null;
		}
	}
	
	/**
	 * List转换
	 * @author denghf
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> beanList(Object object,Class<T> clazz){
		if(object!=null){
			return JSON.parseArray(JSON.toJSONString(object),clazz);
		}else{
			return null;
		}
	}
	
}
