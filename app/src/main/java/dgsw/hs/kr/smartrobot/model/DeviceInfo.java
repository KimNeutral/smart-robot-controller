package dgsw.hs.kr.smartrobot.model;

import java.util.Objects;

public class DeviceInfo {
    private String name;
    private String address;
    private boolean isConnected;

    public DeviceInfo(String name, String address, boolean isConnected) {
        this.name = name;
        this.address = address;
        this.isConnected = isConnected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getTitle() {
        if (name == null || name.equals("")) {
            return address;
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }
}
