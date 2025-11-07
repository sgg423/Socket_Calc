import java.io.*;
import java.net.*;
import java.util.*;

public class CalServer {
    public static void main(String[] args) {
        int port = 9999;
        ServerSocket listener = null;
        System.out.println("계산 서버 실행 중 (port " + port + ")...");

        try {
            listener = new ServerSocket(port);

            while (true) {
                Socket socket = listener.accept();
                System.out.println("클라이언트 연결됨: " + socket.getInetAddress());
                new CalcThread(socket).start();
            }

        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        } finally {
            try {
                if (listener != null)
                    listener.close();
            } catch (IOException e) {
                System.out.println("서버 소켓 종료 오류");
            }
        }
    }
}


class CalcThread extends Thread {
    private Socket socket;

    public CalcThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                String inputMessage = in.readLine(); // 한 줄 읽기
                if (inputMessage == null) break;
                inputMessage = inputMessage.trim();

                System.out.println("입력받음: " + inputMessage); // ★ 디버그 출력

                if (inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("클라이언트 종료 요청");
                    break;
                }

                StringTokenizer st = new StringTokenizer(inputMessage, " ");
                if (st.countTokens() != 3) {
                    out.write("입력 오류! 형식: <숫자> <연산자> <숫자>\r\n");
                    out.flush();
                    continue;
                }

                try {
                    double op1 = Double.parseDouble(st.nextToken());
                    String operator = st.nextToken();
                    double op2 = Double.parseDouble(st.nextToken());
                    double result = 0.0;

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
                            if (op2 == 0) {
                                out.write("0으로 나눌 수 없습니다.\r\n");
                                out.flush();
                                continue;
                            }
                            result = op1 / op2;
                            break;
                        default:
                            out.write("지원하지 않는 연산자입니다.\r\n");
                            out.flush();
                            continue;
                    }

                    out.write("결과: " + result + "\r\n");
                    out.flush();

                } catch (NumberFormatException e) {
                    out.write("숫자 형식 오류입니다.\r\n");
                    out.flush();
                }
            }

            System.out.println("클라이언트 연결 종료.");
            socket.close();

        } catch (IOException e) {
            System.out.println("클라이언트 처리 중 오류: " + e.getMessage());
        }
    }
}
