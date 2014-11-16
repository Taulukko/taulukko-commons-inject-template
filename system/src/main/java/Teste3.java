import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;


public class Teste3 {
	public static void main(String... strings) {
		Socket socket = null;
		try {
//			String ip = "72.232.41.41";
			String ip = "127.0.0.1";
			InetAddress addr = InetAddress.getByName(ip);
	//		int port = 80;
			int port = 8080;
			socket = new Socket(addr, port);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
 
			// envia pro servidor a string
			//out.write("GET /index.jsp HTTP/1.1\nHost: www.taulukko.com.br");
			out.write("GET /index.jsp HTTP/1.1\nHost: bb");
			out.flush();
			socket.shutdownOutput();

			// recebe do servidor
			String ret = in.readLine();
			System.out.println("retorno do servico :" + ret);
			in.close();
			out.close();

			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
