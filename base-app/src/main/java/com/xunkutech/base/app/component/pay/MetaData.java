package com.xunkutech.base.app.component.pay;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.pingplusplus.net.APIResource;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaData {
    String userId;

    String phoneNumber;
    List<Good> goods;
    Gis gis;

    UserInfo userInfo;
    System system;
    Others others;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class Good {
        String id;
        int count;
        String name;
        String desc;
        String tag1;
        String tag2;
        String tag3;
        String tag4;
        String tag5;
        long price;
        long cost;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class Gis {
        double lati;
        @SerializedName("long")
        double longt;
        String country;
        String province;
        String city;
        String district;
        String street;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class UserInfo {
        Sex sex;
        int age;
        String birth;
    }

    static enum Sex {
        female,
        male
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class System {
        String os;
        String osVer;
        String browser;
        String browserVer;
        String networkType;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class Others {
        String source;
    }

    public String toJson() {
        Gson gson = APIResource.getGson();
        return gson.toJson(this);
    }

    public static MetaData fromJson(String src) {
        return APIResource.getGson().fromJson(src, MetaData.class);
    }

    public String toString() {
        Gson gson = APIResource.getPrettyPrintGson();
        return gson.toJson(this);
    }

    public static void main(String[] args) {
        Good good = Good.builder().cost(111).name("asdfa").id("2233").price(111).build();
        Gis gis = Gis.builder().lati(11.22).longt(33.22).build();

        MetaData metaData = MetaData.builder().phoneNumber("112222222").userId("ddddd").gis(gis).goods(Arrays.asList(good)).build();

        java.lang.System.out.println(metaData.toJson());

        java.lang.System.out.println(APIResource.getGson().toJson(APIResource.getGson().fromJson(metaData.toJson(), new TypeToken<Map<String, Object>>() {
        }.getType())));
    }


}
