package tech.cncloud.eastwindweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.litepal.LitePal;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import tech.cncloud.eastwindweather.db.City;
import tech.cncloud.eastwindweather.db.Province;
import tech.cncloud.eastwindweather.db.county;
import tech.cncloud.eastwindweather.util.HttpUtil;
import tech.cncloud.eastwindweather.util.Utility;

public class ChooseAreaFragment extends Fragment {
    public static final int level_province=0;
    public static final int level_city=1;
    public static final int level_county=2;
    private ProgressDialog progressDialog;
    private ImageButton backButton;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<county> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private county selectedCounty;

    private int currentLevel;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState)
    {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        //初始化并返回view实例
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(ImageButton) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(Objects.requireNonNull(getContext()),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        //根据当前所处列表层级不同展示不同信息
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==level_province){
                    selectedProvince=provinceList.get(position);
                    Log.e("info:",selectedProvince.getProvinceName());
                    queryCities();
                }
                else if(currentLevel==level_city){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==level_county){
                    String weatherId=countyList.get(position).getWeatherID();
                    //根据是在天气页面改变城市还是初始化城市两种情况进行区分
                    if(getActivity() instanceof MainActivity)
                    {
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof  WeatherActivity){
                        WeatherActivity activity=(WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //若返回上一级从数据库中加载信息
                if(currentLevel==level_county)
                {
                    queryCities();
                }
                else if(currentLevel==level_city)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        titleText.setText("中国");
        Log.e("info","getpro!!!!!");
        backButton.setVisibility(View.GONE);
        //从数据库查找
        provinceList= LitePal.findAll(Province.class);
        //若省信息不为空输出
        // 为空通过服务器获取
        if(provinceList.size()>0)
        {
            //清空展示列表
            dataList.clear();;
            //将省名进行添加
            for(Province province:provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            //通知adapter已经改变
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=level_province;
        }
        else
        {
            String address="http://guolin.tech/api/china";
            //通过服务器获取相关信息
            queryFromServer(address,"province");
        }
    }
    //获取市一级信息
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        Log.e("info","getci!!!!!");
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        //若从数据库中能够找到信息则添加输出，
        // 否则从服务器中请求
        if(cityList.size()>0)
        {
            //清空展示列表
            dataList.clear();;
            //将数据库中获取的城市名添加
            for(City city:cityList)
            {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=level_city;
        }
        else
        {
            Log.e("info","getweb!!!!!");
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;;
            queryFromServer(address,"city");
        }
    }
    //获取区一级
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(county.class);
        if(countyList.size()>0)
        {
            dataList.clear();;
            for(county county:countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=level_county;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    //从服务器获取信息
    private void queryFromServer(String address,final String type)
    {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //输出失败信息
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseText=response.body().string();
                boolean result=false;
                //根据输入的数据需要调用工具类进行处理，并存入数据库
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                //若上面的过程成功则从数据库载入刚存入的数据
                if(result){
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }
                            else if("city".equals(type)){
                                queryCities();
                            }
                            else {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }
    //显示正在加载
    private void showProgressDialog()
    {
        if(progressDialog==null)
        {
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载！");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    //关闭正在加载
    private void closeProgressDialog(){
        if(progressDialog!=null)
        {
            progressDialog.dismiss();
        }
    }
}

