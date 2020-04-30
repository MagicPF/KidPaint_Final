import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

public class KidPaint {
	ServerSocket srvSocket;
	ArrayList<Socket> list = new ArrayList<Socket>();
	int[][] mydata = new int[50][50];
	int[] change = new int[10];
	String msg;
	String Cname;
	UI ui = UI.getInstance(); // get the instance of UI
	boolean sent = false;

	String msgtmp = "";

	// client
	public void Sloaddata(DataInputStream in) throws IOException {
		for (int i = 1; i <= 8; i++)
			change[i] = in.readInt();
		int len = in.readInt();
		if (len != 0) {
			byte[] buffer = new byte[1024];
			in.read(buffer, 0, len);
			msg = new String(buffer, 0, len);
		}
	}

	public void Cloaddata(DataInputStream in) throws IOException {
		for (int i = 0; i < 50; i++)
			for (int j = 0; j < 50; j++)
				mydata[i][j] = in.readInt();
		int len = in.readInt();
		if (len != 0) {
			byte[] buffer = new byte[1024];
			in.read(buffer, 0, len);
			msg = new String(buffer, 0, len);
			// if (msg != null && !msg.isEmpty() && !msg.equals(" "))
			// 	System.out.println("received:" + msg);
		} else
			msg = "";
	}

	public void Csenddata(DataOutputStream out) throws IOException {
		for (int i = 1; i <= 8; i++)
			out.writeInt(change[i]);
		// System.out.println(sent);
		if (msg != null && sent == false) {
			sent = true;
			out.writeInt(msg.length());
			out.write(msg.getBytes(), 0, msg.length());
		} else
			out.writeInt(0);
	}

	public void Ssenddata(DataOutputStream out) throws IOException {
		for (int i = 0; i < 50; i++)
			for (int j = 0; j < 50; j++)
				out.writeInt(mydata[i][j]);
		if (msg == null) {
			out.writeInt(0);
			return;
		}
		out.writeInt(msg.length());
		if (msg.length() != 0)
			out.write(msg.getBytes(), 0, msg.length());
	}

	public KidPaint(String server, int port, String Cname) throws IOException {
		Socket socket = new Socket(server, port);
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		ui.setData(mydata, 20);
		// ui.pushdata(mydata);
		ui.setVisible(true);
		Thread t = new Thread(() -> {
			byte[] buffer = new byte[1024];
			try {
				while (true) {
					Cloaddata(in);
					// System.out.println(msg);
					ui.pushdata(mydata);
					if (msg != null)
						if(!msg.equals(msgtmp) && msg.length() > 0){
							ui.pushmsg(msg);
							// System.out.println(msgtmp+"vs"+msg.length());
							msgtmp = msg;
						}
					// if(msg.length() >= 1)
				}
			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();
		int[] tmp = { -1, -1, -1, -1, -1, -1, -1, -1, -1 };
		while (true) {
			change = ui.getchange();
			msg = ui.getmsg();
			if(msg != null){
				sent = false;
				if(!msg.isEmpty())
				System.out.println(msg);
			}
			// if (msg != null)
			// System.out.println(msg);
			// ui.cleandata();
			if (tmp[1] != change[1] || tmp[2] != change[2] || tmp[3] != change[3] || tmp[4] != change[4]
					|| tmp[5] != change[5] || tmp[6] != change[6] || tmp[7] != change[7] || tmp[8] != change[8]) {
				// System.out.println(change[1]+" "+change[2]+" "+change[3]);
				Csenddata(out);
				tmp[1] = change[1];
				tmp[2] = change[2];
				tmp[3] = change[3];
				tmp[4] = change[4];
				tmp[5] = change[5];
				tmp[6] = change[6];
				tmp[7] = change[7];
				tmp[8] = change[8];
			}
			else{
				if(msg!=null){
					if(!msg.isEmpty()){
						System.out.println(msg.length());
						
						Csenddata(out);
						msgtmp = msg;
					}
				}
			}

		}
	}// server

	public KidPaint(int port) throws IOException {
		srvSocket = new ServerSocket(port);
		File fin = new File("KidPaint.txt");
		if (fin.exists()) {
			FileInputStream inFileStream = new FileInputStream(fin);
			BufferedReader bufferReader = new BufferedReader(new FileReader(fin));
			for (int i = 0; i < 50; i++) {
				for (int j = 0; j < 50; j++) {
					String str = bufferReader.readLine();
					mydata[i][j] = Integer.parseInt(str);
				}
			}
		}
		ui.setData(mydata, 20);

		ui.setVisible(true);
		while (true) {
			System.out.printf("Listening at port %d...\n", port);
			Socket cSocket = srvSocket.accept();

			synchronized (list) {
				list.add(cSocket);
				System.out.printf("Total %d clients are connected.\n", list.size());
			}

			Thread t = new Thread(() -> {
				try {
					serve(cSocket);
				} catch (IOException e) {
					System.err.println("connection dropped.");
				}
				synchronized (list) {
					list.remove(cSocket);
				}
			});
			t.start();
		}
	}

	private void serve(Socket clientSocket) throws IOException {
		byte[] buffer = new byte[1024];
		System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
				clientSocket.getPort());

		DataInputStream in = new DataInputStream(clientSocket.getInputStream());

		while (true) {
			Sloaddata(in);
			ui.changeit(change);
			if (msg != null)
				ui.pushmsg(msg);
			mydata = ui.getdata();
			forward(clientSocket);
		}
	}

	// �hello" 5
	private void forward(Socket CSocket) {
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket socket = list.get(i);
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					if (socket.getInetAddress() == CSocket.getInetAddress() && socket.getPort() == CSocket.getPort())
						continue;
					mydata = ui.getdata();
					Ssenddata(out);
				} catch (IOException e) {
					// the connection is dropped but the socket is not yet removed.
				}
			}
		}
	}

	public static void main(String[] args) {

		try {
			System.out.println("Please input Name:");
			Scanner scanner = new Scanner(System.in);
			String Cname = scanner.nextLine();
			String s = "127.0.0.1";
			int p = 12345;
			new KidPaint(s, p, Cname);
		} catch (IOException e) {
			try {
				new KidPaint(12345);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
}