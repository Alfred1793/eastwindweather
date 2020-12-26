package tech.cncloud.eastwindweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherID;
    public Update update;
    public static class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
