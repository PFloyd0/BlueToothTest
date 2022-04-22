package com.example.bluetoothtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bluetoothtest.Adapter.MainAdapter;
import com.example.bluetoothtest.IBeaconRecord;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "MainActivity";

    //扫描状态 true开启 false关闭
    private Boolean mScanning = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private Button bt_start;
    private Button bt_end;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<IBeaconRecord> recordList = new ArrayList<>();
    private static List<String> beaconList = new ArrayList<>();


    private RecyclerView rv_list;

    private MainAdapter mAdapter;

    //Android5.0以上扫描实例
    private BluetoothLeScanner mBLEScanner;

    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        setContentView(R.layout.activity_main);
        Log.d("PATH**********", String.valueOf( this.getFilesDir().getPath()));
        bt_start = findViewById(R.id.bt_main_start);
        bt_end = findViewById(R.id.bt_main_end);
        rv_list = findViewById(R.id.rv_main_list);



        bt_start.setOnClickListener(this);
        bt_end.setOnClickListener(this);

        beaconList.add("test1");
        beaconList.add("test2");
        beaconList.add("test3");
        beaconList.add("test4");
        //申请位置权限
        getBlePermissionFromSys();
        getFilePermissionFromSys();

        //实例化线性布局
        LinearLayoutManager lm = new LinearLayoutManager(this);
        //传入线性布局
        rv_list.setLayoutManager(lm);

        Log.d("start*******", "start call location");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LocationService lo = new LocationService();
                while(true)
                {
                    if (recordList.size() == 4) {
                        double x1 = 0, x2 = 0, x3 = 0, x4 = 0;
                        for (IBeaconRecord i : recordList) {
                            switch (i.name) {
                                case "test1":
                                    x1 = i.rssi;
                                    break;
                                case "test2":
                                    x2 = i.rssi;
                                    break;
                                case "test3":
                                    x3 = i.rssi;
                                    break;
                                case "test4":
                                    x4 = i.rssi;
                                    break;
                            }

                        }
                        Log.d("LOCATION!!!!!!!!!!!", lo.predict(x1, x2, x3, x4).toString());
                    }
                    else {
                        Log.d("LOCATION!!!!!!!!!!!", "NO LOCATION INFO");

                    }
                    try{Thread.sleep(30*1000);}catch(InterruptedException e){e.printStackTrace();}
                }
            }
        });
        t.start();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main_start:
                //获取BluetoothManager
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                //如果蓝牙是关闭状态，则请求开启蓝牙
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                if (mBluetoothAdapter.isEnabled()) {
                    //执行扫描方法
                    try {
                        scan();
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                    Log.d("!!!!!!!!!", "END SCAN");
                    //传入Adapter
                    show();
                } else {
                    return;
                }
                break;
            case R.id.bt_main_end:
                //停止扫描
                stopScan();
                break;
        }
    }


    /**
     * 扫描方法
     */
    public void scan() throws JAXBException, IOException, ParserConfigurationException, SAXException {

//        LocationService lo = new LocationService();
        if (!mScanning) {
            //Android 5.0以前
            if (Build.VERSION.SDK_INT < 21) {
                //设为扫描开启状态
                mScanning = true;
                //开始扫描
                mBluetoothAdapter.startLeScan(leScancallback);
            }
            //Android 5.0以后
            else {
                //设为扫描开启状态
                mScanning = true;
                if (mBLEScanner == null) {
                    //mBLEScanner是5.0新添加的扫描类，通过BluetoothAdapter实例获取。
                    mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                }
                recordList.clear();
                mBLEScanner.startScan(mScanCallback);

            }

            //设定一个扫描10秒的线程，10秒后执行run方法，停止扫描
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Android 5.0以前
                    if (Build.VERSION.SDK_INT < 21) {
                        //设为扫描关闭状态
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(leScancallback);
                    }
                    //Android 5.0以后
                    else {
                        //设为扫描关闭状态
                        mScanning = false;
                        mBLEScanner.stopScan(mScanCallback);
                    }
                }
            }, 3000000);
        }
    }

    /**
     * 停止扫描方法
     */
    public void stopScan() {
        //Android 5.0以前
        if (Build.VERSION.SDK_INT < 21) {
            //设为扫描关闭状态
            mScanning = false;
            mBluetoothAdapter.stopLeScan(leScancallback);
        }
        //Android 5.0以后
        else {
            //设为扫描关闭状态
            mScanning = false;
            mBLEScanner.stopScan(mScanCallback);
        }
    }

    /**
     * Android 5.0以前
     * 扫描回调方法，每扫描到一个设备，则将设备信息存入集合
     */
    public BluetoothAdapter.LeScanCallback leScancallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "onLeScan: " + device.getName() + "/t" + device.getAddress() + "/t" + device.getBondState());
            //如果该数据已经存在列表，则不再添加，反之则添加进列表
            if (!bluetoothDeviceList.contains(device)) {
                bluetoothDeviceList.add(device);
            }
            //刷新RecyclerView的数据
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Android 5.0以后
     * 扫描回调方法，每扫描到一个设备，则将设备信息存入集合
     */
    public ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(TAG, "onLeScan: " + result.getDevice().getName() + "/t" + result.getDevice().getAddress() + "/t" + result.getDevice().getBondState());
            if (beaconList.contains(result.getDevice().getName())) {
                boolean flag = true;
                Log.i("tttttttttttttttttttt", String.valueOf(result.getRssi()));

                for(IBeaconRecord i:recordList){
                    if(i.name.equals(result.getDevice().getName())) {
                        flag = false;
                        i.rssi = (i.rssi + result.getRssi()) / 2;
                        break;
                    }
                }
                if(flag){
                    IBeaconRecord temp = new IBeaconRecord();
                    temp.name = result.getDevice().getName();
                    temp.address = result.getDevice().getAddress();
                    temp.rssi = result.getRssi();
                    recordList.add(temp);
                }
            }
            //刷新RecyclerView的数据
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 放入Adapter
     */
    public void show() {
        mAdapter = new MainAdapter(recordList, MainActivity.this, mBluetoothAdapter);
        rv_list.setAdapter(mAdapter);
    }

    /**
     * 动态申请位置权限
     */
    public void getBlePermissionFromSys() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 102;
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    public void getFilePermissionFromSys() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 103;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }


    /**
     * 定义蓝牙Gatt回调类
     */
    public class getBluetoothGattCallback extends BluetoothGattCallback {

        /**
         * 连接状态回调
         *
         * @param gatt
         * @param status   连接状态可能异常码 0连接正常 !0连接异常
         * @param newState 连接状态 STATE_CONNECTED已连接(2) STATE_CONNECTING正在连接(1) STATE_CONNECTED断开连接(0) STATE_DISCONNECTING正在断开连接(3)
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange status:" + status + " newState:" + newState);
            //newState == 已连接状态，说明连接上了
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //status非0则说明可能连接异常
                if (status != 0) {
                    //弹出提示并断开连接
                    Toast.makeText(MainActivity.this, "连接异常", Toast.LENGTH_SHORT).show();
                    //释放所有资源
                    gatt.close();
                } else {
                    //开始尝试发现服务，当发现服务时会执行发现服务的回调
                    gatt.discoverServices();
                }
            }
            //newState == 已断开状态，说明已经断开连接
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //释放所有资源
                gatt.close();
            }
        }

        /**
         * 发现服务回调
         *
         * @param gatt
         * @param status 连接状态可能异常码 0连接正常 !0连接异常
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //如果status == 成功，说明成功发现了服务，这代表着真正的连接到了设备，接下来就可以执行读写等操作了
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //遍历打印一下服务的uuid和服务的特征的uuid
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.i(TAG, "serviceDiscoverd - " + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.i(TAG, " characteristicDiscoverd -" + characteristic.getUuid().toString());
                    }
                }
                Log.i(TAG, "连接成功");
            } else {
                //断开连接
                gatt.disconnect();
            }
        }
    }
}