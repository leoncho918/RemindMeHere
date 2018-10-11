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

/**
 * An adapter that provides binding for Reminders and sets it to a custom view which are displayed in a recyclerview
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    //Variables to store data
    ArrayList<Reminder> mReminders;
    Context mContext;
    Activity mActivity;

    //Constants
    public static final String LAT = "com.mad.remindmehere.ReminderAdapter.LAT";
    public static final String LNG = "com.mad.remindmehere.ReminderAdapter.LNG";

    /**
     * Constructor that takes a arraylist of reminders, context and the activity that called it as parameters
     * @param context
     * @param trains
     * @param activity
     */
    public ReminderAdapter(Context context, ArrayList<Reminder> trains, Activity activity) {
        this.mReminders = trains;
        this.mContext = context;
        this.mActivity = activity;
    }

    /**
     * Called when the recyclerview needs a new viewholder of reminder
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ReminderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Create new view to store custom layout for recyclerview
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);

        //Create new viewholder to hold custom view
        ViewHolder mViewHolder = new ViewHolder(mView);

        //Return viewholder
        return  mViewHolder;
    }

    /**
     * Called by the RecyclerView to display data to display data at the given position
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.ViewHolder holder, final int position) {
        //Setting contents of itemview to display infor for each reminder object
        holder.mReminderName.setText(mReminders.get(position).getName());
        holder.mReminderAddress.setText(AddReminderActivity.getAddress(new LatLng(mReminders.get(position).getLat(), mReminders.get(position).getLng()), mContext));
        holder.mReminderRadius.setText(mContext.getResources().getString(R.string.reminder_item_radius) + mReminders.get(position).getRadius());
        //Setting onclicklistener for each itemview
        holder.mReminderLayout.setOnClickListener(new View.OnClickListener() {
            //Called when the itemview is clicked
            @Override
            public void onClick(View v) {
                //Create new intent to send data back to previous activity
                Intent resultIntent = new Intent();
                //Store the reminder lat and lng coordinates for selected reminder item
                double lat = mReminders.get(position).getLat();
                double lng = mReminders.get(position).getLng();
                //Put lat and lng coordinates into intent
                resultIntent.putExtra(LAT, lat);
                resultIntent.putExtra(LNG, lng);
                //Set result code and intent
                mActivity.setResult(RemindersMapsActivity.LIST_REMINDER, resultIntent);
                //Close activity
                mActivity.finish();
            }
        });
    }

    /**
     * Returns the total number of reminders in the data set being held by the adapter
     * @return
     */
    @Override
    public int getItemCount() {
        return mReminders.size();
    }

    /**
     * Deletes the reminder at the specified postion from the adapter's dataset
     * @param position
     */
    public void removeReminder(int position) {
        mReminders.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mReminders.size());
    }

    /**
     * Class to for recyclerview viewholder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        //Variables to store ui widgets
        private TextView mReminderName;
        private TextView mReminderAddress;
        private TextView mReminderRadius;
        private RelativeLayout mReminderLayout;

        /**
         * Constructor for viewholder
         * @param itemView
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //Get view for each ui widget
            mReminderName = itemView.findViewById(R.id.reminder_name_tv);
            mReminderAddress = itemView.findViewById(R.id.reminder_address_tv);
            mReminderRadius = itemView.findViewById(R.id.reminder_radius_tv);
            mReminderLayout = itemView.findViewById(R.id.reminder_layout);
        }
    }

    /**
     * Called by recyclerview when it starts observing the adapter
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
