package netty.s2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * nio 中基本的 Channel 使用例子:
 * 
 * @author kh
 *
 */
public class TestChannel {

	public static void main(String[] args) {
		// RandomAccessFile是用来访问那些保存数据记录的文件的
		RandomAccessFile aFile;
		try {
			aFile = new RandomAccessFile("/Users/xiongyongshun/settings.xml", "rw");
			FileChannel inChannel = aFile.getChannel();
			// allocate 配置缓冲区大小
			ByteBuffer buf = ByteBuffer.allocate(48);
			// 将数据从channel 读入buffer
			int bytesRead = inChannel.read(buf);
			while (bytesRead != -1) {
				// 调用 Buffer.flip()方法, 将 NIO Buffer 转换为读模式。
				buf.flip();
				while (buf.hasRemaining()) {
					System.out.print((char) buf.get());
				}
				buf.clear();
				bytesRead = inChannel.read(buf);
			}
			aFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
