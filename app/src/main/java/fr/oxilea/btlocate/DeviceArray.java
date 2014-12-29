package fr.oxilea.btlocate;


import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.sql.Timestamp;

public class DeviceArray {

    // save up to 50 devices
    static final int MAX_DEVICES = 10; // should be also modified in MainActivity and DeviceList
    static final int DEVICE_DESC_FIELD = 5;
    static final int DEVICE_ADD = 0;
    static final int DEVICE_NAME = 1;
    static final int DEVICE_UUID = 2;
    static final int DEVICE_RSSI = 3;
    static final int DEVICE_LAST_TIMESTAMP = 4;

    String currentDeviceList [][] = new String[MAX_DEVICES][DEVICE_DESC_FIELD];

    /*
       ** Clear current device Array
       */
    public void clearAllDevices(){

        for (int i = 0; i < MAX_DEVICES; i++) {
            currentDeviceList[i][DEVICE_ADD] = null;
        }
    }

    /*
    ** Clear too old devices in the device Array
    */
    public void clearOldDevices(){

    }

    /*
    ** RSSI sort
    */
    public void rssiSortDevices(){

    }

    /*
    * ** get Device Mac Address
    */
    public String getDeviceAdd(int pos){

        return currentDeviceList[pos][DEVICE_ADD];
    }

    /*
    ** Check if device already in the current devices array
     */
    private int checkDeviceInRange(String address)
    {
        for (int i = 0; i < MAX_DEVICES; i++) {
            if (currentDeviceList[i][DEVICE_ADD] != null)
            {
                if(currentDeviceList[i][DEVICE_ADD].equals(address)) {
                    // found the same object already in list
                    return i;
                }
            }
            else
            {
                if (i==0) return -1;
            }
        }
        return -1;
    }

    /*
    ** Get the first free record in the array
     */
    private int getFirstAvailableSpace()
    {
        for (int i = 0; i < MAX_DEVICES; i++) {
            if (currentDeviceList[i][DEVICE_ADD] == null)
            {
                // find the first free record
                return i;
            }
        }

        // no space left
        return -1;
    }


    /*
    ** Get the string definition of the pos device
     */
    public String getDevice(int pos)
    {
        String myString;
        String dName;

        if ( pos < MAX_DEVICES) {
            if (((currentDeviceList[pos][DEVICE_ADD]) != null) && ((currentDeviceList[pos][DEVICE_ADD]) != "")) {
                if (currentDeviceList[pos][DEVICE_NAME] == null)
                {
                    dName="...";
                }
                else
                {
                    dName=currentDeviceList[pos][DEVICE_NAME];
                }
                myString = currentDeviceList[pos][DEVICE_ADD] + "    | " + currentDeviceList[pos][DEVICE_RSSI] + " dbm   |    " + dName;
            } else {
                myString = null;
            }
        }else {
            myString = null;
        }

        return myString;
    }

    /*
        ** Get the number of devices in the device array
         */
    public int countDevice(){

        int i;

        for (i = 0; i < MAX_DEVICES; i++) {
            if (currentDeviceList[i][DEVICE_ADD] == null)
            {
                // find the first free record
                return i+1;
            }
        }
        return i+1;
    }

    /*
        ** Add a device in the device array
         */
    public void addDevice(BluetoothDevice device, int rssi){

        // get current timestamp
        int time = (int) (System.currentTimeMillis());
        Timestamp tsTemp = new Timestamp(time);
        String ts =  tsTemp.toString();

        if (device != null) {
            String deviceAddress = device.getAddress();
            String deviceName = device.getName();
            ParcelUuid[] UUID = device.getUuids();
            String deviceUUID;
            if (UUID!=null) {
                deviceUUID = UUID.toString();
            }
            else
            {
                deviceUUID = "";
            }
            String deviceRssi = String.valueOf(rssi);

            int indexDevice = checkDeviceInRange(device.getAddress());

            if (indexDevice != -1){
                // already in range, update Rssi and Timestamp
                currentDeviceList[indexDevice][DEVICE_RSSI] = deviceRssi;
                currentDeviceList[indexDevice][DEVICE_LAST_TIMESTAMP] = ts;
            }
            else{
                // suppress too old devices
                this.clearOldDevices();

                // add the device in the array
                indexDevice = this.getFirstAvailableSpace();
                if (indexDevice != -1){
                    // free space found
                    currentDeviceList[indexDevice][DEVICE_ADD] = deviceAddress;
                    currentDeviceList[indexDevice][DEVICE_NAME] = deviceName;
                    currentDeviceList[indexDevice][DEVICE_UUID] = deviceUUID;
                    currentDeviceList[indexDevice][DEVICE_RSSI] = deviceRssi;
                    currentDeviceList[indexDevice][DEVICE_LAST_TIMESTAMP] = ts;
                }
                else{
                    // too much devices in range

                }

            }
        }
    }

}
