package hry.util;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddressesUtil {
	
	
	 /** 
     *  根據ip地址返回省市區
     * @param content 
     *            请求的参数 格式为：name=xxx&pwd=xxx 
     * @param encoding 
     *            服务器端请求编码。如GBK,UTF-8等 
     * @return 
     * @throws UnsupportedEncodingException 
     */  
    public static String getAddresses(String content, String encodingString)  
            throws UnsupportedEncodingException {  
        // 这里调用淘宝API  
        String urlStr = "http://ip.taobao.com/service/getIpInfo.php";  
        // 从http://whois.pconline.com.cn取得IP所在的省市区信息  
        String returnStr = getResult(urlStr, content, encodingString);  
        if (returnStr != null) {  
            // 处理返回的省市区信息  
            System.out.println("(1) unicode转换成中文前的returnStr : " + returnStr);  
            returnStr = decodeUnicode(returnStr);  
            System.out.println("(2) unicode转换成中文后的returnStr : " + returnStr);  
            String[] temp = returnStr.split(",");  
            if(temp.length<3){  
                return "0";//无效IP，局域网测试  
            }  
            
            
            return returnStr;  
            
            
        }  
        return null;  
    }  
    /** 
     * @param urlStr 
     *            请求的地址 
     * @param content 
     *            请求的参数 格式为：name=xxx&pwd=xxx 
     * @param encoding 
     *            服务器端请求编码。如GBK,UTF-8等 
     * @return 
     */  
    private static String getResult(String urlStr, String content, String encoding) {  
        URL url = null;  
        HttpURLConnection connection = null;  
        try {  
            url = new URL(urlStr);  
            connection = (HttpURLConnection) url.openConnection();// 新建连接实例  
            connection.setConnectTimeout(2000);// 设置连接超时时间，单位毫秒  
            connection.setReadTimeout(2000);// 设置读取数据超时时间，单位毫秒  
            connection.setDoOutput(true);// 是否打开输出流 true|false  
            connection.setDoInput(true);// 是否打开输入流true|false  
            connection.setRequestMethod("POST");// 提交方法POST|GET  
            connection.setUseCaches(false);// 是否缓存true|false  
            connection.connect();// 打开连接端口  
            DataOutputStream out = new DataOutputStream(connection  
                    .getOutputStream());// 打开输出流往对端服务器写数据  
            out.writeBytes(content);// 写数据,也就是提交你的表单 name=xxx&pwd=xxx  
            out.flush();// 刷新  
            out.close();// 关闭输出流  
            BufferedReader reader = new BufferedReader(new InputStreamReader(  
                    connection.getInputStream(), encoding));// 往对端写完数据对端服务器返回数据  
            // ,以BufferedReader流来读取  
            StringBuffer buffer = new StringBuffer();  
            String line = "";  
            while ((line = reader.readLine()) != null) {  
                buffer.append(line);  
            }  
            reader.close();  
            return buffer.toString();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (connection != null) {  
                connection.disconnect();// 关闭连接  
            }  
        }  
        return null;  
    }  
    /** 
     * unicode 转换成 中文 
     *  
     * @author fanhui 2007-3-15 
     * @param theString 
     * @return 
     */  
    public static String decodeUnicode(String theString) {  
        char aChar;  
        int len = theString.length();  
        StringBuffer outBuffer = new StringBuffer(len);  
        for (int x = 0; x < len;) {  
            aChar = theString.charAt(x++);  
            if (aChar == '\\') {  
                aChar = theString.charAt(x++);  
                if (aChar == 'u') {  
                    int value = 0;  
                    for (int i = 0; i < 4; i++) {  
                        aChar = theString.charAt(x++);  
                        switch (aChar) {  
                        case '0':  
                        case '1':  
                        case '2':  
                        case '3':  
                        case '4':  
                        case '5':  
                        case '6':  
                        case '7':  
                        case '8':  
                        case '9':  
                            value = (value << 4) + aChar - '0';  
                            break;  
                        case 'a':  
                        case 'b':  
                        case 'c':  
                        case 'd':  
                        case 'e':  
                        case 'f':  
                            value = (value << 4) + 10 + aChar - 'a';  
                            break;  
                        case 'A':  
                        case 'B':  
                        case 'C':  
                        case 'D':  
                        case 'E':  
                        case 'F':  
                            value = (value << 4) + 10 + aChar - 'A';  
                            break;  
                        default:  
                            throw new IllegalArgumentException(  
                                    "Malformed      encoding.");  
                        }  
                    }  
                    outBuffer.append((char) value);  
                } else {  
                    if (aChar == 't') {  
                        aChar = '\t';  
                    } else if (aChar == 'r') {  
                        aChar = '\r';  
                    } else if (aChar == 'n') {  
                        aChar = '\n';  
                    } else if (aChar == 'f') {  
                        aChar = '\f';  
                    }  
                    outBuffer.append(aChar);  
                }  
            } else {  
                outBuffer.append(aChar);  
            }  
        }  
        return outBuffer.toString();  
    }  
    // 测试  
    public static void main(String[] args) {  
        // 参数ip  
        String ip = "219.136.134.157";  
        // json_result用于接收返回的json数据  
        String json_result = null;  
        try {  
            json_result = AddressesUtil.getAddresses("ip=" + ip, "utf-8");  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
	   	 com.alibaba.fastjson.JSONObject json = JSON.parseObject(json_result);
		 
	     System.out.println("json数据： " + json);  
	     
	     String country = json.getJSONObject("data").getString("country");
	     String region = json.getJSONObject("data").getString("region");  
	     String city = json.getJSONObject("data").getString("city");  
	     String county =json.getJSONObject("data").getString("county");  
	     String isp = json.getJSONObject("data").getString("isp");  
	     String area = json.getJSONObject("data").getString("area");    
        System.out.println("国家： " + country);  
        System.out.println("地区： " + area);  
        System.out.println("省份: " + region);  
        System.out.println("城市： " + city);  
        System.out.println("区/县： " + county);  
        System.out.println("互联网服务提供商： " + isp);  
          
        String address = country + "/";  
        address += region + "/";  
        address += city + "/";  
        address += county;  
        System.out.println(address);  
        // 输出结果为：广东省,广州市,越秀区  
    }  
    
    public static String getAddres(String ip){
        // json_result用于接收返回的json数据  
        String json_result = null;  
        try {  
            json_result = AddressesUtil.getAddresses("ip=" + ip, "utf-8");  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        } 
    	 com.alibaba.fastjson.JSONObject json = JSON.parseObject(json_result);
    	 
         System.out.println("json数据： " + json);  
         
         String country = json.getJSONObject("data").getString("country");
         String region = json.getJSONObject("data").getString("region");  
         String city = json.getJSONObject("data").getString("city");  
         String county =json.getJSONObject("data").getString("county");  
         String isp = json.getJSONObject("data").getString("isp");  
         String area = json.getJSONObject("data").getString("area");  
         System.out.println("国家： " + country);  
         System.out.println("地区： " + area);  
         System.out.println("省份: " + region);  
         System.out.println("城市： " + city);  
         System.out.println("区/县： " + county);  
         System.out.println("互联网服务提供商： " + isp);  
           
      /*   String address = country + "/";  
         address += region + "/";  
         address += city + "/";  
         address += county;  */
         String address = city;
         System.out.println(address);
		return address;
    	
    }
    
    
    
    //
    public static String getOutIp(){
    	String ip = "";
    	String chinaz = "http://ip.chinaz.com";
    	
    	StringBuilder inputLine = new StringBuilder();
    	String read = "";
    	URL url = null;
    	HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
    	try {
    		url = new URL(chinaz);
    		urlConnection = (HttpURLConnection) url.openConnection();
    	    in = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
    		while((read=in.readLine())!=null){
    			inputLine.append(read+"\r\n");
    		}
    		//System.out.println(inputLine.toString());
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}finally{
    		if(in!=null){
    			try {
    				in.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	
    	Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
    	Matcher m = p.matcher(inputLine.toString());
    	if(m.find()){
    		String ipstr = m.group(1);
    		ip = ipstr;
    		//System.out.println(ipstr);
    	}
    	return ip;
    }
}  
	
