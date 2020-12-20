package tech.cncloud.eastwindweather.db;

public class county {
    private int id;
    private String countyName;
    private int weatherID;
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

    public int getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(int weatherID) {
        this.weatherID = weatherID;
    }
}
