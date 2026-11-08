package activation.Screen;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
/**
 * The ServerCode class provides methods to encode and decode server information into a compact string format.
 * It allows encoding of an IP address, port number, and hostname into a Base64 string, and decoding it back into its components.
 * This class is useful for generating and interpreting the server code that can be shared with other clients to connect to a specific server.
 */
public class ServerCode {
    public record ServerInfo(
            String ip,
            int port,
            String hostname
    ){}
/**
     * The main method demonstrates the encoding and decoding of server information.
     * It encodes a sample IP address, port number, and hostname into a server code,
     * then decodes the server code back into its components and prints them to the console.
     *
     * @param args Command-line arguments (not used).
     * @throws Exception If an error occurs during encoding or decoding.
     */
    public static void main(String[] args) throws Exception {
        String code = ServerCode.Encode(
                "192.168.1.32",
                25565,
                "Luka"
        );
        System.out.println(code);

        ServerCode.ServerInfo server = ServerCode.decode(code);

        System.out.println(server.ip);
        System.out.println(server.port);
        System.out.println(server.hostname);
    }
/**
     * Encodes the given IP address, port number, and hostname into a Base64 string format.
     *
     * @param ip       The IP address to encode.
     * @param port     The port number to encode.
     * @param hostname The hostname to encode.
     * @return A Base64 encoded string representing the server information.
     * @throws Exception If an error occurs during encoding or if the input values are invalid.
     */
    public static String Encode(String ip, int port, String hostname)
            throws Exception {

        byte[] ipBytes = InetAddress.getByName(ip).getAddress();

        if (ipBytes.length != 4) {
            throw new IllegalArgumentException("IPV4 required");
        }
        if (port < 1 || port > 65535){
            throw new IllegalArgumentException("Invalid Port");
        }
        byte[] nameBytes = hostname.getBytes(StandardCharsets.UTF_8);

        if (nameBytes.length > 255){
            throw new IllegalArgumentException("Hostname too long");
        }
        ByteBuffer buffer = ByteBuffer.allocate(4+2+1+nameBytes.length);

        buffer.put(ipBytes);
        buffer.putShort((short) port);
        buffer.put((byte) nameBytes.length);
        buffer.put(nameBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }
/**
     * Decodes a Base64 encoded server code into its components: IP address, port number, and hostname.
     *
     * @param code The Base64 encoded server code to decode.
     * @return A ServerInfo record containing the decoded IP address, port number, and hostname.
     * @throws Exception If an error occurs during decoding or if the input code is invalid.
     */
    public static ServerInfo decode(String code) throws Exception {

        byte[] data = Base64.getUrlDecoder().decode(code);

        if (data.length < 7) {
            throw new IllegalArgumentException("Invalid Code");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] ipBytes = new byte[4];
        buffer.get(ipBytes);

        String ip = InetAddress.getByAddress(ipBytes).getHostAddress();

        int port = buffer.getShort() & 0XFFFF;

        int nameLength = Byte.toUnsignedInt(buffer.get());

        if (buffer.remaining() != nameLength) {
            throw new IllegalArgumentException("Invalid hostname length");
        }

        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);

        String hostname = new String(nameBytes, StandardCharsets.UTF_8);

        return new ServerInfo(ip, port, hostname);
    }
}
