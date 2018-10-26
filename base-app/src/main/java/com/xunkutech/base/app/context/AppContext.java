package com.xunkutech.base.app.context;

import com.xunkutech.base.app.context.uaparser.Client;
import com.xunkutech.base.app.context.uaparser.Parser;
import com.xunkutech.base.app.util.AesUtils;
import com.xunkutech.base.app.util.InvalidException;
import com.xunkutech.base.model.JsonSerializable;
import com.xunkutech.base.model.util.CodecException;
import com.xunkutech.base.model.util.CodecUtils;
import com.xunkutech.base.model.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;

public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);

    public static final String RATELIMIT_KEY_SPEL = "T(com.xunkutech.base.app.context.AppContextHolder).currentAppContext().getClientInfo().getUuid()+'@'+#type+#method";

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_DEVICE_PIXEL = "UA-Pixels";      //UA-pixels: <n>x<m>
    private static final String HEADER_GEO_POSITION = "Geo-Position";   //Geo-Position: -10.28;60.84;120 epu=50 hdn=45 spd=15
    private static final String HEADER_DEVICE_MANUFACTURE = "X-Device-Manufacture";
    private static final String HEADER_DEVICE_MODEL = "X-Device-Model";
    private static final String HEADER_NETWORK_INFORMATION = "X-Network-Information"; //"bluetooth", "cellular", "ethernet", "mixed", "none", "other", "unknown", "wifi", "wimax"
    private static final String HEADER_API_VERSION = "X-Api-Version";

    private static final String TOKEN_NAME = "ctx-token";
    private static final byte[] TOKEN_SEED = "i7aFm1".getBytes();
    private static final int TOKEN_MAX_AGE = 60 * 60 * 4;//4 hours

    @Getter
    private transient HttpServletRequest request;

    @Getter
    private transient HttpServletResponse response;

    @Getter
    private transient Locale locale;

    @Getter
    private transient ClientInfo clientInfo;

    private transient Map<Object, Object> attributes = new HashMap<>();

    public AppContext(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        this.request = request;
        this.response = response;
        this.locale = locale;
        resolveClientInfo();
    }

    private void resolveClientInfo() {
        if (null == request) return;

        String apiVersion = request.getHeader(HEADER_API_VERSION);
        String uuid = null;
        Agent.AgentBuilder agent = Agent.builder().family("unknown");
        Os.OsBuilder os = Os.builder().family("unknown");
        Gis.GisBuilder gis = Gis.builder();
        Device.DeviceBuilder device = Device.builder()
                .family(request.getHeader(HEADER_DEVICE_MANUFACTURE))
                .manufacture(request.getHeader(HEADER_DEVICE_MANUFACTURE))
                .model(request.getHeader(HEADER_DEVICE_MODEL))
                .networkType(request.getHeader(HEADER_NETWORK_INFORMATION))
                .remoteAddr(request.getRemoteAddr())
                .schema(request.getScheme());

        String pixel = request.getHeader(HEADER_DEVICE_PIXEL);

        if (null != pixel) {
            String[] p = pixel.split("x");
            if (p.length == 2) {
                device.screenWidth(Integer.parseInt(p[0]));
                device.screenHeight(Integer.parseInt(p[1]));
            }
        }

        String uaString = request.getHeader(HEADER_USER_AGENT);

        if (null != uaString) {
            Client c = new Parser().parse(uaString);
            os.family(c.os.family).major(c.os.major).minor(c.os.minor).patch(c.os.patch);
            agent.family(c.userAgent.family).major(c.userAgent.major).minor(c.userAgent.minor).patch(c.userAgent.patch);
            device.family(c.device.family);
        }

        String geoString = request.getHeader(HEADER_GEO_POSITION);
        if (null != geoString) {
            //Geo-Position: -10.28;60.84;120 epu=50 hdn=45 spd=15

            String[] items = geoString.trim().split(" ");
            if (items.length > 0) {
                String[] lanlonalt = items[0].split(";");
                if (lanlonalt.length > 0) {
                    gis.lat(Double.valueOf(lanlonalt[0]));
                    if (lanlonalt.length > 1) {
                        gis.lon(Double.valueOf(lanlonalt[1]));
                        if (lanlonalt.length > 2) {
                            gis.alt(Double.valueOf(lanlonalt[2]));
                        }
                    }
                }

                for (int i = 1; i < items.length; i++) {
                    String[] v = items[i].split("=");
                    if (v.length == 2) {
                        switch (v[0].toLowerCase()) {
                            case "epu":
                                gis.epu(Double.valueOf(v[1]));
                                break;
                            case "hdn":
                                gis.hdn(Double.valueOf(v[1]));
                                break;
                            case "spd":
                                gis.spd(Double.valueOf(v[1]));
                                break;
                        }
                    }
                }
            }
        }

        CtxToken token = refreshToken();
        uuid = token.getUuid();

        clientInfo = new ClientInfo(apiVersion, uuid, agent.build(), os.build(), device.build(), gis.build());
        logger.debug("Resolved client info: \n{}", clientInfo.printJson());
    }

    private CtxToken refreshToken() {
        CtxToken newToken = new CtxToken();
        if (null != request && null != request.getCookies()) {
            Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(TOKEN_NAME))
                    .findAny()
                    .ifPresent(c -> {
                        try {
                            CtxToken oldToken = JsonUtils.fromBin(AesUtils.decrypt(CodecUtils.fromBase64(c.getValue()), TOKEN_SEED), CtxToken.class);
                            if (null != oldToken
                                    && null != oldToken.getTimestamp()
                                    && null != oldToken.getUuid()
                                    && oldToken.getTimestamp().plusSeconds(TOKEN_MAX_AGE).isAfter(Instant.now())) {
                                newToken.setUuid(oldToken.getUuid());
                            }
                        } catch (InvalidException | CodecException e) {
                            logger.debug("Bad token {}", e.getMessage());
                        }
                    });
        }

        if (null == newToken.getUuid()) {
            newToken.setUuid(UUID.randomUUID().toString().replace("-", ""));
        }

        if (null != response) {
            String tokenValue = CodecUtils.toBase64(AesUtils.encrypt(newToken.toJsonBin(), TOKEN_SEED));
            Cookie newCookie = new Cookie(TOKEN_NAME, tokenValue);
            newCookie.setMaxAge(TOKEN_MAX_AGE);
            newCookie.setPath("/");
            response.addCookie(newCookie);
        }

        return newToken;
    }

    public void put(Object obj) {
        if (null != obj) {
            put(obj.getClass(), obj);
        }
    }

    public void put(Object key, Object obj) {
        if (null != obj && null != key) {
            this.attributes.put(key, obj);
        }
    }

    public <T> T get(Class<T> clz) {
        return get(clz, clz);
    }

    public <T> T get(Object key, Class<T> clz) {
        if (null != key && null != clz) {
            return (T) this.attributes.get(key);
        }
        return null;
    }

    @Getter
    @Setter
    public static class CtxToken implements JsonSerializable {
        String uuid;
        Instant timestamp = Instant.now();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ClientInfo implements JsonSerializable {
        String apiVersion;                  //x-api-version
        String uuid;
        Agent agent;
        Os os;
        Device device;
        Gis gis;
    }

    @Getter
    @Setter
    @Builder
    public static class Os {
        String family;
        String major;
        String minor;
        String patch;
    }

    @Getter
    @Setter
    @Builder
    public static class Agent {
        String family;
        String major;
        String minor;
        String patch;
    }

    @Getter
    @Setter
    @Builder
    public static class Device {
        String family = "unknown";
        Integer screenWidth = -1;
        Integer screenHeight = -1;
        String manufacture = "unknown";     //x-device-manufacture
        String model = "unknown";           //x-device-model
        String networkType = "unknown";     //x-network-type
        String schema;
        String remoteAddr = "127.0.0.1";
    }

    @Getter
    @Setter
    @Builder
    public static class Gis {
        //-10.28;60.84;120 epu=50 hdn=45 spd=15
        Double lat;
        Double lon;
        Double alt;
        Double epu;
        Double hdn;
        Double spd;
    }


    public static void main(String[] args) {
        String uaString1 = "Mozilla/5.0 (iPhone; CPU iPhone OS 5_1_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B206 Safari/7534.48.3";

        String uaString2 = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/60.0.3112.78 Chrome/60.0.3112.78 Safari/537.36";

//        String uaString3 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8";
        String uaString3 = "";
        Client c;
        Parser uaParser = new Parser();

        c = uaParser.parse(uaString1);
        System.out.println(JsonUtils.printJson(c));

        c = uaParser.parse(uaString2);
        System.out.println(JsonUtils.printJson(c));

        c = uaParser.parse(uaString3);
        System.out.println(JsonUtils.printJson(c));

    }
}