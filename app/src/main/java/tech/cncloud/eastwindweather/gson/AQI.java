package tech.cncloud.eastwindweather.gson;

public class AQI {
    public AQICity city;
    public static class AQICity{
        public String aqi;
        public String pm25;
    }

}
