package activation.Screen;
/**
 * The connectionValues class is a utility class that holds the connection values for a network connection.
 * It provides static methods to set and retrieve the IP address, port number, and host name.
 * This class is designed to be used as a singleton-like structure, where the connection values are shared across the application.
 */
public class connectionValues {
    public static String ipAddress;
    public static int port;
    public static String hostName;


    /**
     * Sets the connection values for the network connection.
     *
     * @param ip The IP address to set.
     * @param portNumber The port number to set.
     * @param host The host name to set.
     */
    public static void setConnectionValues(String ip, int portNumber, String host) {
        ipAddress = ip;
        port = portNumber;
        hostName = host;
    }

    /**
     * Retrieves the IP address.
     *
     * @return The IP address.
     */
    public static String getIpAddress() {
        return ipAddress;
    }

    /**
     * Retrieves the port number.
     *
     * @return The port number.
     */
    public static int getPort() {
        return port;
    }
    /**
     * Retrieves the host name.
     *
     * @return The host name.
     */
    public static String getHostName() {
        return hostName;
    }
/**
     * Sets the IP address.
     *
     * @param ip The IP address to set.
     */
    public static void setIpAddress(String ip) {
        ipAddress = ip;
    }
    /**
     * Sets the port number.
     *
     * @param portNumber The port number to set.
     */
    public static void setPort(int portNumber) {
        port = portNumber;
    }
    /**
     * Sets the host name.
     *
     * @param host The host name to set.
     */
    public static void setHostName(String host) {
        hostName = host;
    }
/**
     * Returns a string representation of the connection values.
     *
     * @return A string containing the IP address, port number, and host name.
     */
    @Override
    public String toString() {
        return "connectionValues{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", hostName='" + hostName + '\'' +
                '}';
    }
}
