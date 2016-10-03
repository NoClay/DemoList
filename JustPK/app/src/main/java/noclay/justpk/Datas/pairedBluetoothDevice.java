package noclay.justpk.Datas;

/**
 * Created by 82661 on 2016/9/13.
 */
public class pairedBluetoothDevice {
    //用于对应每个蓝牙设备
    private String userImagePath;
    private String userName;
    private String bluetoothDeviceName;
    private String bluetoothDeviceAddress;

    public pairedBluetoothDevice(String userImagePath, String userName, String bluetoothDeviceName,
                                 String bluetoothDeviceAddress) {
        this.userImagePath = userImagePath;
        this.userName = userName;
        this.bluetoothDeviceName = bluetoothDeviceName;
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
    }

    public String getUserImagePath() {
        return userImagePath;
    }

    public void setUserImagePath(String userImagePath) {
        this.userImagePath = userImagePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBluetoothDeviceName() {
        return bluetoothDeviceName;
    }

    public void setBluetoothDeviceName(String bluetoothDeviceName) {
        this.bluetoothDeviceName = bluetoothDeviceName;
    }

    public String getBluetoothDeviceAddress() {
        return bluetoothDeviceAddress;
    }

    public void setBluetoothDeviceAddress(String bluetoothDeviceAddress) {
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
    }
}
