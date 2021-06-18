package com.ahmedorabi.bluetoothapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmedorabi.bluetoothapp.R;
import com.ahmedorabi.bluetoothapp.data.Device;
import com.ahmedorabi.bluetoothapp.databinding.DeviceItemBinding;

public class DeviceAdapter extends ListAdapter<Device, DeviceAdapter.MyViewHolder> {

    private DeviceCallback mCallback;


    private static final DiffUtil.ItemCallback<Device> DIFF_CALLBACK = new DiffUtil.ItemCallback<Device>() {
        @Override
        public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return oldItem.getName() == newItem.getName();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getAddress().equals(newItem.getAddress());
        }
    };


    public DeviceAdapter(DeviceCallback callback) {
        super(DIFF_CALLBACK);
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        DeviceItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.device_item, parent, false);

        binding.setCallback(mCallback);

        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Device device = getItem(position);

        holder.binding.setDevice(device);

        holder.binding.executePendingBindings();


    }

    class MyViewHolder extends RecyclerView.ViewHolder {


        final DeviceItemBinding binding;

        MyViewHolder(DeviceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}