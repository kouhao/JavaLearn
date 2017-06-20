package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTest {

	public static void main(String[] args) {
		// 建立客户端socket
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 9998);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
