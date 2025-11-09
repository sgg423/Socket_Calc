import java.io.*;
import java.net.*;
import java.util.*;


public class CalClient {

    private static InetSocketAddress loadServerInfo() {
        String host = "localhost";
        int port = 9999; // default aligned with your server
        File cfg = new File("server_info.dat");

        if (cfg.exists()) { // config file available
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(cfg)) {
                props.load(fis);
                String h = props.getProperty("host");
                String p = props.getProperty("port");
                if (h != null && !h.isBlank()) host = h.trim(); // host provided
                if (p != null) {
                    try {
                        port = Integer.parseInt(p.trim()); // valid integer port
                    } catch (NumberFormatException ignore) {
                        System.out.println("Invalid port in server_info.dat; using default " + port);
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to read server_info.dat; using defaults.");
            }
        } else {
            System.out.println("server_info.dat not found; using defaults (localhost:" + port + ").");
        }
        return new InetSocketAddress(host, port);
    }

    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            InetSocketAddress addr = loadServerInfo();
            socket = new Socket(addr.getHostName(), addr.getPort()); // connect using config/defaults
            System.out.println("Connected to server (" + addr.getHostName() + ":" + addr.getPort() + ").");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) { // keep interacting until user types 'exit' or server closes
                System.out.print("Enter expression >> ");
                String outputMessage = scanner.nextLine();

                if (outputMessage.equalsIgnoreCase("exit")) { // graceful client termination
                    out.write("exit\r\n"); // send command line
                    out.flush();
                    System.out.println("Closing connection.");
                    break;
                }

                out.write(outputMessage + "\r\n"); // request line
                out.flush();

                String inputMessage = in.readLine(); // blocking read for one response line
                if (inputMessage == null) { // server closed connection
                    System.out.println("Server closed the connection.");
                    break;
                }

                System.out.println(inputMessage);
            }

        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null) socket.close(); // release socket if it was opened
            } catch (IOException e) {
                System.out.println("Error while releasing resources.");
            }
        }
    }
}
