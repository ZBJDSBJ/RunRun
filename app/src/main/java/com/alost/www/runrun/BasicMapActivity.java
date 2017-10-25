package com.alost.www.runrun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alost.www.runrun.util.DrawUtil;
import com.alost.www.runrun.util.ToastUtil;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BasicMapActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener {

    //    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.basicmap)
    Button mBasicmap;
    @BindView(R.id.rsmap)
    Button mRsmap;
    @BindView(R.id.nightmap)
    Button mNightmap;
    @BindView(R.id.navimap)
    Button mNavimap;


    private AMap aMap;
    private MyLocationStyle myLocationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map);
        ButterKnife.bind(this);
        DrawUtil.resetDensity(this);

        mapView = (MapView) findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();

    }


    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        setUpMap();
    }


    /**
     * 设置一些amap的属性
     */
    private BitmapDescriptor mCurrentMarker;
    private AMapLocationClient mlocationClient;
    private ArrayList<LatLng> points = new ArrayList<LatLng>(); //绘制当前定位轨迹线路
    private int mLocationTimes = 0;


    private void setUpMap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.position_location);
        Bitmap locationDefault = DrawUtil.resizeImage(bitmap, DrawUtil.dip2px(18), DrawUtil.dip2px(18));
        mCurrentMarker = BitmapDescriptorFactory.fromBitmap(locationDefault);

        // 如果要设置定位的默认状态，可以在此处进行设置
        myLocationStyle = new MyLocationStyle();

        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。

        myLocationStyle.myLocationIcon(mCurrentMarker);//设置定位蓝点的icon图标方法，需要用到BitmapDescriptor类对象作为参数。
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(60, 255, 222, 0));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(0f);// 设置圆形的边框粗细


        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//        aMap.setLocationSource(this);  //通过aMap对象设置定位数据源的监听
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。


        // 控件交互
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);  //是否允许显示缩放按钮
        uiSettings.setMyLocationButtonEnabled(false); //显示默认的定位按钮
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);//设置logo位置

        mlocationClient = new AMapLocationClient(this);
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {

                Log.i("zou", "<BasicMapActivity> setUpMap mLatitude =  " + aMapLocation.getLatitude()
                        + " mLongitude = " + aMapLocation.getLatitude() + " location.getLocationType() = " + aMapLocation.getLocationType());

                mLocationTimes++;
                if (mLocationMarker != null) {
                    mLocationMarker.remove();
                }

                if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {  //第一次定位成功
                    aMap.animateCamera(CameraUpdateFactory.zoomTo(18), 1000, null); //地图的缩放级别一共分为 17 级，从 3 到 19。数字越大，展示的图面信息越精细
                }

                if (mLocationTimes == 2) {
                    mLocationTimes = 0;
                    aMap.animateCamera(CameraUpdateFactory.zoomTo(18), 1000, null);
                }

                ToastUtil.showToast(BasicMapActivity.this, "locType = " + aMapLocation.getLocationType());
            }
        });
        initLocation(1);

        mlocationClient.startLocation();

        aMap.setOnMyLocationChangeListener(this);
        aMap.animateCamera(CameraUpdateFactory.zoomTo(18), 1000, null);

    }

    private void initLocation(int gpsTime) {
        if (mlocationClient == null) {
            AMapLocationClientOption locationOption = new AMapLocationClientOption();
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置是否返回地址信息（默认返回地址信息）
            locationOption.setNeedAddress(true);
            //设置是否只定位一次,默认为false
            locationOption.setOnceLocation(false);
            //设置是否强制刷新WIFI，默认为强制刷新
            locationOption.setWifiScan(true);
            //设置是否允许模拟位置,默认为false，不允许模拟位置
            locationOption.setMockEnable(false);
            //设置定位间隔,单位毫秒,默认为2000ms
            locationOption.setInterval(gpsTime * 1000);
            //给定位客户端对象设置定位参数
            mlocationClient.setLocationOption(locationOption);
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
    }

    @OnClick({R.id.basicmap, R.id.rsmap, R.id.nightmap, R.id.navimap})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.basicmap:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式

                break;
            case R.id.rsmap:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式

                break;
            case R.id.nightmap:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式

                break;
            case R.id.navimap:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);//导航地图模式

                break;
        }
    }


    private double mLatitude = 0;
    private double mLongitude = 0;
    private Marker mLocationMarker;

    @Override
    public void onMyLocationChange(Location location) {


        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();


        Log.i("zou", "<BasicMapActivity> onMyLocationChange mLatitude =  " + mLatitude + " mLongitude = " + mLongitude
                + " location.getAccuracy() = " + location.getAccuracy());


        LatLng currentPoint = new LatLng(mLatitude, mLongitude);
        drawLine(currentPoint);
    }


    private void drawLine(LatLng currentPoint) {
        points.add(currentPoint);
        PolylineOptions ooPolyline = new PolylineOptions().width(14).zIndex(9)
                .color(0xFFFF8366).addAll(points);
        aMap.addPolyline(ooPolyline);  //绘制折线

        //绘制地图上定位图标
        MarkerOptions option = new MarkerOptions()
                .position(new LatLng(currentPoint.latitude - 0.00002, currentPoint.longitude)).zIndex(10)
                .icon(mCurrentMarker).setFlat(true);
        //在地图上添加Marker，并显示
//        mLocationMarker = aMap.addMarker(option);
    }
}
