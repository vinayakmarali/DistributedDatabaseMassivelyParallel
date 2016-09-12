import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ReceiveAdjustedFiles implements Runnable {
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(5010);
			while (true) {
				Socket socket = serverSocket.accept();
				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				String requestType = (String) objectInputStream.readObject();

				if (requestType.equals("add")) {
					NameAndBufferObjects nameAndBufferObjects = (NameAndBufferObjects) objectInputStream.readObject();
					System.out.println("File received || request type: add");

					String hostName = socket.getLocalAddress().getHostName();
					String path = "/home/stu4/s1/vkm7895/" + hostName + "/";
					ArrayList<String> fileNames = nameAndBufferObjects.names;
					ArrayList<StringBuffer> buffers = nameAndBufferObjects.stringBuffers;
					for (int i = 0; i < fileNames.size(); i++) {
						String p = path + fileNames.get(i);
						StringBuffer stringBuffer = buffers.get(i);
						BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(p)));
						bufferedWriter.write(stringBuffer.toString());
						bufferedWriter.close();
					}
				} else if (requestType.equals("remove")) {
					NameAndBufferObjects nameAndBufferObjects = (NameAndBufferObjects) objectInputStream.readObject();
					System.out.println("File received || request type: remove");

					String hostName = socket.getLocalAddress().getHostName();
					String path = "/home/stu4/s1/vkm7895/" + hostName + "/";

					ArrayList<String> fileNames = nameAndBufferObjects.names;
					ArrayList<StringBuffer> stringBuffers = nameAndBufferObjects.stringBuffers;

					for (int i = 0; i < fileNames.size(); i++) {
						System.out.println("Adding file content to : " + fileNames.get(i));
						boolean exists = new File(path + fileNames.get(i)).exists();
						if (exists) {
							System.out.println(fileNames.get(i) + " file already exist...Overwriting contents...");
							appendToFile(path + fileNames.get(i), stringBuffers.get(i));
						} else {
							System.out.println("Creating new file");
							createNewFile(path + fileNames.get(i), stringBuffers.get(i));
						}
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createNewFile(String s, StringBuffer stringBuffer) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(s)));
			bufferedWriter.write(stringBuffer.toString());
			bufferedWriter.close();
			System.out.println("File created: " + s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void appendToFile(String s, StringBuffer stringBuffer) {
		BufferedReader bufferedReader = null;
		StringBuffer stringBuffer1 = new StringBuffer();
		try {
			bufferedReader = new BufferedReader(new FileReader(s));
			String line = null;
			int c = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (c == 0) {
					c = 1;
					continue;
				}
				stringBuffer1.append(line + "\n");
			}
			// new File(s).delete();

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(s)));
			stringBuffer1.append(stringBuffer.toString());
			bufferedWriter.write(stringBuffer1.toString());
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
