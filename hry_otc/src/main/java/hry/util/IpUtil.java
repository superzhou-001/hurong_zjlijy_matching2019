/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      Yuan Zhicheng
 * @version:      V1.0
 * @Date:        2015年9月16日 上午11:04:39
 */
package hry.util;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.hardware.Networks;
import org.nutz.mapl.Mapl;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * IP工具类
 *
 * @author:      Yuan Zhicheng
 *
 */
public class IpUtil {

	/**
	 * 获得IP地址
	 *
	 * @return
	 */
	public static String getIp() {
		return Networks.ipv4();
	}

	/**
	 * 获得IP地址
	 *
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();

	}

	/**
	 * 通过IP获得地址信息，调用的是淘宝的IP接口
	 *
	 * @param ip
	 * @return
	 */
	public static String getAddr(String ip) {
		String info = "";
		Response resp = Http.get("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip);
		if (resp.isOK()) {
			String result = resp.getContent();
			Object obj = Json.fromJson(result);
			if (Mapl.cell(obj, "code").toString().equals("0")) {
				info += Mapl.cell(obj, "data.country") + " ";
				info += Mapl.cell(obj, "data.region") + " ";
				info += Mapl.cell(obj, "data.city") + " ";
				info += Mapl.cell(obj, "data.isp") + " ";
			}
		}
		return info;
	}

	public static String getIpAddress (HttpServletRequest request) {

		String ipAddress = request.getHeader("x-forwarded-for");

		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();

			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				//根据网卡获取本机配置的IP地址
				InetAddress inetAddress = null;
				try {
					inetAddress = InetAddress.getLocalHost();
				} catch (Exception e) {
					e.printStackTrace();
				}
				ipAddress = inetAddress.getHostAddress();
			}
		}

		//对于通过多个代理的情况，第一个IP为客户端真实的IP地址，多个IP按照','分割
		if (null != ipAddress && ipAddress.length() > 15) {
			//"***.***.***.***".length() = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}

		return ipAddress;
	}

	public static boolean  isMobileDevice(String requestHeader){
        /**
         * android : 所有android设备
         * mac os : iphone ipad
         * windows phone:Nokia等windows系统的手机
         */
        String[] deviceArray = new String[]{"android","mac os","windows phone"};
        if(requestHeader == null)
            return false;
        requestHeader = requestHeader.toLowerCase();
        for(int i=0;i<deviceArray.length;i++){
            if(requestHeader.indexOf(deviceArray[i])>0){
                return true;
            }
        }
        return false;
	}

	/**
	 * 获取下单的用户IP
	 * <p>
	 * TODO
	 * </p>
	 *
	 * @author: Zhang Lei
	 * @param: @param request
	 * @param: @return
	 * @return: String
	 * @Date : 2017年4月10日 上午10:25:27
	 * @throws:
	 */
	public static String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		return request.getHeader("x-forwarded-for");
	}
}
