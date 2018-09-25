package com.mad.remindmehere.adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mad.remindmehere.activity.AddReminderActivity;
import com.mad.remindmehere.activity.RemindersMapsActivity;
import com.mad.remindmehere.model.Reminder;
import com.mad.remindmehere.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    ArrayList<Reminder> mReminders;
    Context mContext;
    Activity mActivity;
    public static final String LAT = "com.mad.remindmehere.ReminderAdapter.LAT";
    public static final String LNG = "com.mad.remindmehere.ReminderAdapter.LNG";

    public ReminderAdapter(Context context, ArrayList<Reminder> trains, Activity activity) {
        this.mReminders = trains;
        this.mContext = context;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ReminderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);

        ViewHolder mViewHolder = new ViewHolder(mView);

        return  mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.ViewHolder holder, final int position) {
        holder.mReminderName.setText(mReminders.get(position).getName());
        holder.mReminderAddress.setText(AddReminderActivity.getAddress(new LatLng(mReminders.get(position).getLat(), mReminders.get(position).getLng()), mContext));
        holder.mReminderRadius.setText(mContext.getResources().getString(R.string.reminder_item_radius) + mReminders.get(position).getRadius());

        holder.mReminderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                double lat = mReminders.get(position).getLat();
                double lng = mReminders.get(position).getLng();
                resultIntent.putExtra(LAT, lat);
                resultIntent.putExtra(LNG, lng);
                mActivity.setResult(RemindersMapsActivity.LIST_REMINDER, resultIntent);
                mActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReminders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mReminderName;
        private TextView mReminderAddress;
        private TextView mReminderRadius;
        private RelativeLayout mReminderLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mReminderName = itemView.findViewById(R.id.reminder_name_tv);
            mReminderAddress = itemView.findViewById(R.id.reminder_address_tv);
            mReminderRadius = itemView.findViewById(R.id.reminder_radius_tv);
            mReminderLayout = itemView.findViewById(R.id.reminder_layout);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
