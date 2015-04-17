package bluetooth.aglogica.com.bluetoothtool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James.Shi on 12/26/14.
 */
public class DeviceAdapter extends BaseAdapter {
    private Context mContext;
    private List<DeviceCellItem> mDeviceList = new ArrayList<DeviceCellItem>();

    public DeviceAdapter(List<DeviceCellItem> deviceList, Context context) {
        mDeviceList = deviceList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listviewrow_device, null);
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            holder.textAddress = (TextView) convertView.findViewById(R.id.textAddress);
            holder.radioButton = (RadioButton) convertView.findViewById(R.id.rbtnSelect);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DeviceCellItem deviceItem = mDeviceList.get(position);
        holder.textName.setText(deviceItem.getDeviceName());
        holder.textAddress.setText(deviceItem.getDeviceAddress());
        holder.radioButton.setChecked(deviceItem.isSelected());

        return convertView;
    }
}
