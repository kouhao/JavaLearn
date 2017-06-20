package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceTest {

	public static void main(String[] args) {
		BufferedReader in;
		ServerSocket server;
		try {
			// 初始化服务器的soket服务，端口为9998
			server = new ServerSocket(9998);
			// 获取服务端的socket
			Socket income = server.accept();
			// 获取输入流，输出流
			in = new BufferedReader(new InputStreamReader(income.getInputStream()));
			String newLine = "";
			String results = "";
			while ((newLine = in.readLine()) != null) {
				results += newLine + "\n";
			}
			System.out.print(results);
			in.close();
			PrintWriter out = new PrintWriter(income.getOutputStream(), true);
			out.append("1234");
			out.flush();
			out.print("4567");
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
