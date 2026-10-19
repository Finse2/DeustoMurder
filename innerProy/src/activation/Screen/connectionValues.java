package activation.Screen;

public class connectionValues {
    public static String ipAddress;
    public static int port;
    public static String hostName;

    public static void setConnectionValues(String ip, int portNumber, String host) {
        ipAddress = ip;
        port = portNumber;
        hostName = host;
    }

    public static String getIpAddress() {
        return ipAddress;
    }
    public static int getPort() {
        return port;
    }
    public static String getHostName() {
        return hostName;
    }

    public static void setIpAddress(String ip) {
        ipAddress = ip;
    }
    public static void setPort(int portNumber) {
        port = portNumber;
    }
    public static void setHostName(String host) {
        hostName = host;
    }

    @Override
    public String toString() {
        return "connectionValues{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", hostName='" + hostName + '\'' +
                '}';
    }
}
