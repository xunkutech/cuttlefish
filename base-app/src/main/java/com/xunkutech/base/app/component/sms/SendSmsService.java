package com.xunkutech.base.app.component.sms;

import com.xunkutech.base.model.util.CodecUtils;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * Created by jason on 16-3-10.
 */
@Getter
@Setter
@Service
public class SendSmsService {

    private static final Logger logger = LoggerFactory.getLogger(SendSmsService.class);

    private static final String MT_URL = "http://esms100.10690007.net/sms/mt";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();


    @Value(value = "${empp.spid:9145}")
    private String spid;

    @Value(value = "${empp.password:wdxwl9145}")
    private String sppassword;

    private String checkMobile(String phoneNumber) {
        Objects.requireNonNull(phoneNumber);
        String pn = phoneNumber.trim().replace(" ", "");
        if (pn.length() < 11) {
            throw new IllegalArgumentException("Bad phone number");
        }
        if (pn.length() == 14) {
            if (!pn.startsWith("+86")) {
                throw new IllegalArgumentException("Bad phone number");
            }
            pn = pn.substring(3);
        } else if (pn.length() == 13) {
            if (!pn.startsWith("86")) {
                throw new IllegalArgumentException("Bad phone number");
            }
            pn = pn.substring(2);
        } else if (pn.length() == 11) {
            if (!pn.startsWith("1")) {
                throw new IllegalArgumentException("Bad phone number");
            }
        } else {
            throw new IllegalArgumentException("Bad phone number");
        }
        return pn;
    }

    private Boolean emppSms(String phoneNumber, String content, Boolean voice) {

        String smsid, smspasswd;
        smsid = this.spid;
        smspasswd = this.sppassword;
        logger.info("Send sms to {}: {}", phoneNumber, content);
        String command = "MT_REQUEST";
        //sp服务代码，可选参数，默认为 00
        String spsc = "00";
        //下行内容以及编码格式，必填参数
        int dc = 15;
        String encode = "GBK";
        String sm = null;
        try {
            sm = new String(CodecUtils.toHex(content.getBytes(encode)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = new StringBuffer(MT_URL)
                .append("?command=").append(command)
                .append("&spid=").append(smsid)
                .append("&sppassword=").append(smspasswd)
                .append("&spsc=").append(spsc)
                .append("&da=86").append(phoneNumber)
                .append("&sm=").append(sm)
                .append("&dc=").append(dc).toString();

        final StringBuffer responseStr = new StringBuffer();
        Request request = new Request.Builder().url(url).build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

        return true;
    }

//
//    #短信接口配置
//    empp.spid=9145
//    empp.password=wdxwl9145

    public static void main(String[] args) {
        SendSmsService service = new SendSmsService();
        service.setSpid("9145");
        service.setSppassword("wdxwl9145");
        service.emppSms("13818262168", "hello", false);
    }
}
