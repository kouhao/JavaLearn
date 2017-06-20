1、服务器端
(1)Sokect服务器的类：ServerSocket;端口为9998
   	ServerSocket server = new ServerSocket(9998);
(2)获取服务端socket:
   	Socket incoming = server.accept();
(3)得到输入流和输出流，并进行封装
	