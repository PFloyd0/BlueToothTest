package com.example.bluetoothtest.Adapter.Holder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetoothtest.R;


public class MainHolder extends RecyclerView.ViewHolder {

    public TextView tv_name;
    public TextView tv_address;
    public Button bt_connect;

    public MainHolder(@NonNull View itemView) {
        super(itemView);
        tv_name = itemView.findViewById(R.id.tv_main_name);
        tv_address = itemView.findViewById(R.id.tv_main_address);
        bt_connect = itemView.findViewById(R.id.bt_main_connect);
    }
}
