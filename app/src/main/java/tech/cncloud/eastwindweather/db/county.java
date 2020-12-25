package tech.cncloud.eastwindweather.db;


import org.litepal.crud.LitePalSupport;

public class county extends LitePalSupport {
    private int id;
    private String countyName;
    private String weatherID;
    private int cityId;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    public String getCountyName(){
        return countyName;
    }

    public void setCountyName(String countyName){
        this.countyName=countyName;
    }

    public int getCityId(){
        return cityId;
    }
    public void setCityId(int cityId){
        this.cityId=cityId;
    }

    public String getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(String weatherID) {
        this.weatherID = weatherID;
    }
}
