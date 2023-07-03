package com.ellabook.saasdemo;

import androidx.annotation.Keep;
import androidx.annotation.StringDef;

import com.xsbase.utils.MD5Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * bookCode   书籍编号
 * appKey     创建应用生成的Key
 * noncestr   随机字符串（建议使用UUID）
 * timestamp  当前时间戳
 * sign       签名
 * notifyUrl  （可选）回调地址：用于异步通知客户订单成功并下发订单数据，如果客户服务端返回失败，一共有10次回调，第10次还是返回失败，要获取订单状态就要通过outTradeNo查询订单，详情请见查询订单接口文档
 * outTradeNo （可选）三方订单号：客户服务端生成的订单号
 * readType （必填）阅读类型：TRIAL_READ：试读 ，FORMAL_READ：正式阅读
 */
@Keep
public class SignBean {
    private String bookCode;
    private String appKey;
    private String noncestr;
    private String timestamp;
    private String sign;
    private String notifyUrl;//可选,注意如果不传,为null的情况下在json序列化时如果有值(notifyUrl=null),那么签名也应当加上;如果序列化时没此值,那么签名时此字段不应当加上
    private String outTradeNo;//同上
    private String readType;

    public SignBean(String bookCode, String appKey, String noncestr,
                    String timestamp, @SignBean.ReadTypeMode.ReadType String readType) {
        this.bookCode = bookCode;
        this.appKey = appKey;
        this.noncestr = noncestr;
        this.timestamp = timestamp;
        this.readType = readType;
    }

    //todo null值字段加入与否,根据生成的json串中是否存在null值决定
    public void genSign(String appSecret) {
        //按字典序加入签名字段
        TreeMap<String, String> treeMap = new TreeMap<>();
        Class signClass = getClass();
        Field[] fields = signClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                //null值字段加入与否,根据生成的json串中是否存在null值决定
                if (field.get(this) != null && !"sign".equals(field.getName())) {
                    treeMap.put(field.getName(), String.valueOf(field.get(this)));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        StringBuilder signResult = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            signResult.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        signResult.append("key").append("=").append(appSecret);
        sign = MD5Utils.getStrMD5(signResult.toString());
    }

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getReadType() {
        return readType;
    }

    public void setReadType(String readType) {
        this.readType = readType;
    }

    @Override
    public String toString() {
        return "SignBean{" +
                "bookCode='" + bookCode + '\'' +
                ", appKey='" + appKey + '\'' +
                ", noncestr='" + noncestr + '\'' +
                ", timestamp=" + timestamp +
                ", sign='" + sign + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", outTradeNo='" + outTradeNo + '\'' +
                ", readType='" + readType + '\'' +
                '}';
    }

    @Keep
    public static final class ReadTypeMode {
        /**
         * 试读模式
         */
        public static final String TRIAL_READ = "TRIAL_READ";

        /**
         * 普通阅读模式
         */
        public static final String FORMAL_READ = "FORMAL_READ";

        @StringDef({TRIAL_READ, FORMAL_READ})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ReadType {
        }
    }
}
