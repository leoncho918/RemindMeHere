package com.mad.remindmehere.adapter;

import android.app.FragmentManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mad.remindmehere.model.Reminder;
import com.mad.remindmehere.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    ArrayList<Reminder> mReminders;
    Context mContext;

    public ReminderAdapter(Context context, ArrayList<Reminder> trains) {
        this.mReminders = trains;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ReminderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);

        ViewHolder mViewHolder = new ViewHolder(mView);

        return  mViewHolder;
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(mContext);
        String lastAddress = "Couldn't get Address";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                lastAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastAddress;
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.ViewHolder holder, int position) {
        holder.mReminderName.setText(mReminders.get(position).getName());
        holder.mReminderAddress.setText(getAddress(mReminders.get(position).getLatLng()));
        holder.mReminderRadius.setText(mContext.getResources().getString(R.string.reminder_item_radius) + mReminders.get(position).getRadius());
    }

    @Override
    public int getItemCount() {
        return mReminders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mReminderName;
        private TextView mReminderAddress;
        private TextView mReminderRadius;

        public ViewHolder(View itemView) {
            super(itemView);
            mReminderName = itemView.findViewById(R.id.reminder_name_tv);
            mReminderAddress = itemView.findViewById(R.id.reminder_address_tv);
            mReminderRadius = itemView.findViewById(R.id.reminder_radius_tv);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
