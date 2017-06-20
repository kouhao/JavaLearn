package socket.s5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

public class URLReader {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// 声明抛出所有例外
		URL tirc = new URL("http://www.sina.com");
		File writeFile = new File("d:\\style_1.html");
		// 构建一URL对象14100
		BufferedReader in = new BufferedReader(new InputStreamReader(tirc.openStream()));
		BufferedWriter bos = new BufferedWriter(new FileWriter(writeFile));
		// byte[] b=new byte[4096];
		String inputLine;
		// 使用openStream得到一输入流并由此构造一个BufferedReader对象
		while ((inputLine = in.readLine()) != null) {
			bos.write(inputLine);
			System.out.println(inputLine);
		}
		bos.flush();
		in.close();
		bos.close();
	}
}
