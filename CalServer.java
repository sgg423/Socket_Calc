import java.io.*;
import java.net.*;
import java.util.*;


public class CalServer {
    public static void main(String[] args) {
        int port = 9999;
        ServerSocket listener = null;
        System.out.println("Calculation Server running on port " + port + "...");

        try {
            listener = new ServerSocket(port); // Create a server socket on the given port

            while (true) { // Wait for clients continuously
                Socket socket = listener.accept(); // Accept a client connection
                System.out.println("Client connected: " + socket.getInetAddress());

                // Start a new thread for each connected client
                new CalcThread(socket).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            try {
                if (listener != null)
                    listener.close(); // Close the server socket when shutting down
            } catch (IOException e) {
                System.out.println("Error closing the server socket.");
            }
        }
    }
}


class CalcThread extends Thread {
    private Socket socket;

    public CalcThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            while (true) { // Keep reading requests until "bye" or disconnect
                String inputMessage = in.readLine(); // Read a line from client
                if (inputMessage == null) break; // Client disconnected

                inputMessage = inputMessage.trim();
                System.out.println("Received: " + inputMessage);

                // Check if client requested to close connection
                if (inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("Client requested to terminate.");
                    break;
                }

                // Validate request format: must be 3 tokens (operand1, operator, operand2)
                StringTokenizer st = new StringTokenizer(inputMessage, " ");
                if (st.countTokens() != 3) { // Incorrect input format
                    out.write("Input error! Format: <number> <operator> <number>\r\n");
                    out.flush();
                    continue; // Go back to wait for next request
                }

                try {
                    double op1 = Double.parseDouble(st.nextToken()); // Parse first number
                    String operator = st.nextToken(); // Get operator
                    double op2 = Double.parseDouble(st.nextToken()); // Parse second number
                    double result = 0.0;

                    // Perform arithmetic operation based on operator
                    switch (operator) {
                        case "+": 
                            result = op1 + op2;
                            break;
                        case "-": 
                            result = op1 - op2;
                            break;
                        case "*": 
                            result = op1 * op2;
                            break;
                        case "/": 
                            if (op2 == 0) { // Prevent division by zero
                                out.write("Division by zero is not allowed.\r\n");
                                out.flush();
                                continue;
                            }
                            result = op1 / op2;
                            break;
                        default: // Invalid operator
                            out.write("Unsupported operator.\r\n");
                            out.flush();
                            continue;
                    }

                    // Send calculation result back to client
                    out.write("Result: " + result + "\r\n");
                    out.flush();

                } catch (NumberFormatException e) { // Handle invalid number input
                    out.write("Invalid number format.\r\n");
                    out.flush();
                }
            }

            System.out.println("Client connection closed.");
            socket.close(); // Close client socket

        } catch (IOException e) { // Handle communication error
            System.out.println("Error handling client: " + e.getMessage());
        }
    }
}
