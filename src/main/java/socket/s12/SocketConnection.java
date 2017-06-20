package socket.s12;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * 
 * @title: socket通信包装类
 * @Description:
 * @CopyRight: CopyRight (c) 2009
 * @Company: * 99bill.com
 * @Create date: 2009-10-14
 * @author
 * @desc:对命令收发逻辑及收发线程互斥机制进行了优化， 处理命令速度由原来8~16个/秒提高到25~32个/秒
 */
public class SocketConnection {
	private volatile Socket socket;
	private int timeout = 1000 * 10; // 超时时间，初始值10秒
	private boolean isLaunchHeartcheck = false;// 是否已启动心跳检测
	private boolean isNetworkConnect = false; // 网络是否已连接
	private static String host = "";
	private static int port;
	static InputStream inStream = null;
	static OutputStream outStream = null;
	private static Logger log = Logger.getLogger("SocketConnection");
	private static SocketConnection socketConnection = null;
	private static java.util.Timer heartTimer = null;

	private final ConcurrentHashMap<String, Object> recMsgMap = new ConcurrentHashMap<String, Object>();
	private static Thread receiveThread = null;
	private final ReentrantLock lock = new ReentrantLock();

	private SocketConnection() {
		Properties conf = new Properties();
		try {
			conf.load(SocketConnection.class.getResourceAsStream("test.conf"));
			this.timeout = Integer.valueOf(conf.getProperty("timeout"));
			init(conf.getProperty("ip"), Integer.valueOf(conf.getProperty("port")));
		} catch (IOException e) {
			// log.fatal("socket初始化异常!", e);
			throw new RuntimeException("socket初始化异常,请检查配置参数");
		}
	}

	/**
	 * 单态模式
	 */
	public static SocketConnection getInstance() {
		if (socketConnection == null) {
			synchronized (SocketConnection.class) {
				if (socketConnection == null) {
					socketConnection = new SocketConnection();
					return socketConnection;
				}
			}
		}
		return socketConnection;
	}

	private void init(String host, int port) throws IOException {
		InetSocketAddress addr = new InetSocketAddress(host, port);
		socket = new Socket();
		synchronized (this) {
			socket.connect(addr, timeout);
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
			socket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
			socket.setSoLinger(true, 0);// socket关闭时，立即释放资源
			socket.setKeepAlive(true);
			socket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
			isNetworkConnect = true;
			receiveThread = new Thread(new ReceiveWorker());
			receiveThread.start();
			SocketConnection.host = host;
			SocketConnection.port = port;
			if (!isLaunchHeartcheck)
				launchHeartcheck();
		}
	}

	/**
	 * 心跳包检测
	 */
	private void launchHeartcheck() {
		if (socket == null)
			throw new IllegalStateException("socket is not established!");
		heartTimer = new Timer();
		isLaunchHeartcheck = true;
		heartTimer.schedule(new TimerTask() {
			public void run() {
				String msgStreamNo = "kq";// StreamNoGenerator.getStreamNo("kq");
				int mstType = 9999;// 999-心跳包请求
				SimpleDateFormat dateformate = new SimpleDateFormat("yyyyMMddHHmmss");
				String msgDateTime = dateformate.format(new Date());
				int msgLength = 38;// 消息头长度
				String commandstr = "00" + msgLength + mstType + msgStreamNo;
				log.info("心跳检测包 -> IVR " + commandstr);
				int reconnCounter = 1;
				while (true) {
					String responseMsg = null;
					try {
						responseMsg = readReqMsg(commandstr);
					} catch (IOException e) {
						log.info("IO流异常");
						reconnCounter++;
					}
					if (responseMsg != null) {
						log.info("心跳响应包 <- IVR " + responseMsg);
						reconnCounter = 1;
						break;
					} else {
						reconnCounter++;
					}
					if (reconnCounter > 3) {// 重连次数已达三次，判定网络连接中断，重新建立连接。连接未被建立时不释放锁
						reConnectToCTCC();
						break;
					}
				}
			}
		}, 1000 * 60 * 1, 1000 * 60 * 2);
	}

	/**
	 * 重连与目标IP建立重连
	 */
	private void reConnectToCTCC() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				log.info("重新建立与" + host + ":" + port + "的连接");
				// 清理工作，中断计时器，中断接收线程，恢复初始变量
				heartTimer.cancel();
				isLaunchHeartcheck = false;
				isNetworkConnect = false;
				receiveThread.interrupt();
				try {
					socket.close();
				} catch (IOException e1) {
					log.info("重连时，关闭socket连接发生IO流异常");
				}
				synchronized (this) {
					for (;;) {
						try {
							Thread.currentThread();
							Thread.sleep(1000 * 1);
							init(host, port);
							this.notifyAll();
							break;
						} catch (IOException e) {
							log.info("重新建立连接未成功");
						} catch (InterruptedException e) {
							log.info("重连线程中断");
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 发送命令并接受响应
	 * 
	 * @param requestMsg
	 * @return
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public String readReqMsg(String requestMsg) throws IOException {
		if (requestMsg == null) {
			return null;
		}
		if (!isNetworkConnect) {
			synchronized (this) {
				try {
					this.wait(1000 * 5); // 等待5秒，如果网络还没有恢复，抛出IO流异常
					if (!isNetworkConnect) {
						throw new IOException("网络连接中断！");
					}
				} catch (InterruptedException e) {
					log.info("发送线程中断");
				}
			}
		}
		String msgNo = requestMsg.substring(8, 8 + 24);// 读取流水号
		outStream = socket.getOutputStream();
		outStream.write(requestMsg.getBytes());
		outStream.flush();
		Condition msglock = lock.newCondition(); // 消息锁
		// 注册等待接收消息
		recMsgMap.put(msgNo, msglock);
		try {
			lock.lock();
			msglock.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.info("发送线程中断");
		} finally {
			lock.unlock();
		}
		Object respMsg = recMsgMap.remove(msgNo); // 响应信息
		if (respMsg != null && (respMsg != msglock)) {
			// 已经接收到消息，注销等待，成功返回消息
			return (String) respMsg;
		} else {
			log.info(msgNo + " 超时，未收到响应消息");
			throw new SocketTimeoutException(msgNo + " 超时，未收到响应消息");
		}
	}

	public void finalize() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 消息接收线程
	private class ReceiveWorker implements Runnable {
		String intStr = null;

		public void run() {
			while (!Thread.interrupted()) {
				try {
					byte[] headBytes = new byte[4];
					if (inStream.read(headBytes) == -1) {
						log.info("读到流未尾，对方已关闭流!");
						reConnectToCTCC();// 读到流未尾，对方已关闭流
						return;
					}
					byte[] tmp = new byte[4];
					tmp = headBytes;
					String tempStr = new String(tmp).trim();
					if (tempStr == null || tempStr.equals("")) {
						log.info("received message is null");
						continue;
					}
					intStr = new String(tmp);
					int totalLength = Integer.parseInt(intStr);
					byte[] msgBytes = new byte[totalLength - 4];
					inStream.read(msgBytes);
					String resultMsg = new String(headBytes) + new String(msgBytes);
					// 抽出消息ID
					String msgNo = resultMsg.substring(8, 8 + 24);
					Condition msglock = (Condition) recMsgMap.get(msgNo);
					if (msglock == null) {
						log.info(msgNo + "序号可能已被注销！响应消息丢弃");
						recMsgMap.remove(msgNo);
						continue;
					}
					recMsgMap.put(msgNo, resultMsg);
					try {
						lock.lock();
						msglock.signalAll();
					} finally {
						lock.unlock();
					}
				} catch (SocketException e) {
					log.info("服务端关闭socket");
					reConnectToCTCC();
				} catch (IOException e) {
					log.info("接收线程读取响应数据时发生IO流异常");
				} catch (NumberFormatException e) {
					log.info("收到没良心包，String转int异常，异常字符:" + intStr);
				}
			}
		}
	}
}
