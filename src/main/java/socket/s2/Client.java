package socket.s2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	static Socket server;

	public static void main(String[] args) throws UnknownHostException, IOException {
		server = new Socket(InetAddress.getLocalHost(), 9910);
		BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		PrintWriter out = new PrintWriter(server.getOutputStream());
		BufferedReader wt = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String str = wt.readLine();
			out.println(str);
			out.flush();
			if (str.equals("end")) {
				break;
			}
			System.out.println(in.readLine());
		}
		server.close();

	}

}
