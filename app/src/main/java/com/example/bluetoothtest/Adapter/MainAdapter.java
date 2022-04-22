package com.example.bluetoothtest.Adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.bluetooth.le.ScanResult;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetoothtest.Adapter.Holder.MainHolder;
import com.example.bluetoothtest.IBeaconRecord;
import com.example.bluetoothtest.MainActivity;
import com.example.bluetoothtest.R;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Handler;

public class MainAdapter extends RecyclerView.Adapter<MainHolder> {

    //声明一个用来存放数据的集合
    private List<IBeaconRecord> mList;
    private Context mContext;
    //获取mac地址
    private String mac;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    //实例化主页面类
    private MainActivity mMainActivity = new MainActivity();
    //实例化主页面类中的回调类
    private BluetoothGattCallback mCallback = mMainActivity.new getBluetoothGattCallback();

    public MainAdapter(List<IBeaconRecord> mList, Context mContext, BluetoothAdapter mBluetoothAdapter) {
        //创建构造器用来将获取的数据集合存入
        this.mList = mList;
        this.mContext = mContext;
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    @NonNull
    @Override
    //创建MainHolder实例
    public MainHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //实例化一个View用来存放布局
        //调用LayoutInflater.from(parent.getContext())获取上下文
        View view = LayoutInflater.from(parent.getContext())
                //调用LayoutInflater.inflate将item_main_list填充至父容器内
                .inflate(R.layout.item_main_list, parent, false);
        MainHolder holder = new MainHolder(view);
        //当点击连接键
        holder.bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                //获取mac地址
                mac = mList.get(position).address;
                //根据地址获取指定的设备
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);
                //对指定设备开始尝试连接服务
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mCallback);
            }
        });
        return holder;
    }

    @Override
    //用于对RecyclerView子项赋值
    public void onBindViewHolder(@NonNull MainHolder holder, int position) {
        IBeaconRecord result = mList.get(position);
        if (result.name == null) {
            holder.tv_name.setText("莫得感情");
        } else {
            holder.tv_name.setText(result.name);
        }
        holder.tv_address.setText(String.valueOf(result.rssi));
    }

    @Override
    public int getItemCount() {
        //用于返回数据项数
        return mList.size();
    }
}


