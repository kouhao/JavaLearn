package socket.s2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 适用于多客户端;问题：不能并行处理
 * 
 * @author kh
 *
 */
public class MyServer2 {

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(9910);
		while (true) {
			Socket client = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream());
			while (true) {
				String str = in.readLine();
				System.out.println(str);
				out.println("has receive....");
				out.flush();
				if (str.equals("end"))
					break;
			}
			client.close();
		}
	}

}
