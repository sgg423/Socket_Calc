import java.io.*;
import java.net.*;
import java.util.*;

public class CalClient {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            socket = new Socket("localhost", 9999);
            System.out.println("서버에 연결되었습니다.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                System.out.print("계산식(빈칸으로 띄어 입력, 예: 24 + 42)>> ");
                String outputMessage = scanner.nextLine();

                if (outputMessage.equalsIgnoreCase("bye")) {
                    out.write("bye\r\n");
                    out.flush();
                    System.out.println("서버 연결 종료");
                    break;
                }

                // ★ 여기서 반드시 \r\n !!
                out.write(outputMessage + "\r\n");
                out.flush();

                String inputMessage = in.readLine(); // 서버 응답 받기
                if (inputMessage == null) {
                    System.out.println("서버 연결이 종료되었습니다.");
                    break;
                }

                System.out.println("계산 결과: " + inputMessage);
            }

        } catch (IOException e) {
            System.out.println("통신 오류: " + e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("자원 해제 중 오류 발생");
            }
        }
    }
}
