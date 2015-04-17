package bluetooth.aglogica.com.bluetoothtool;

/**
 * Created by James.Shi on 12/26/14.
 */
public class DeviceCellItem {
    private String deviceName;
    private String deviceAddress;
    private boolean isSelected;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
