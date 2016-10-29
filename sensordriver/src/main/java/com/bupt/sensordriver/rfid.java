package com.bupt.sensordriver;

public class rfid {
    static {
        System.loadLibrary("sensor-driver");
    }

    private static rfid sRfid;

    public static rfid instance() {
        if (sRfid == null) {
            sRfid = new rfid();
        }
        return sRfid;
    }

    public rfid() {
        Open();
    }

    public long getRfidId(boolean isBigEndian) {
        int[] bytes = Read();
        if (bytes == null) return 0;
        long id;
        if (isBigEndian) {
            id = (bytes[0] << 24) + (bytes[1] << 16) + (bytes[2] << 8) + bytes[3];
        } else {
            id = (bytes[3] << 24) + (bytes[2] << 16) + (bytes[1] << 8) + bytes[0];
        }
        return id;
    }

    public native int Open();

    public native int Close();

    public native int Ioctl(int num, int en);

    // 4 int number are used as 4 byte number, represent the id card number of 4 byte
    public native int[] Read();

    public native byte[] ReadCardNum();
}
