import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;


public class ProcessQuery implements Runnable {
	StringBuffer stringBuffer;
	String hostName;
	String path;

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(10001);
			while (true) {
				Socket socket = serverSocket.accept();
				stringBuffer = new StringBuffer();
				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				QueryObject queryObject = (QueryObject) objectInputStream.readObject();
				System.out.println("Query object received");
				hostName = socket.getLocalAddress().getHostName();
				path = "/home/stu4/s1/vkm7895/" + hostName + "/";
				if (queryObject.IdVal == null && queryObject.lessThan == null && queryObject.greaterThan == null
						&& queryObject.serachByColumnName != null && queryObject.valueOfColumnName != null) {
					queryOnColumnName(queryObject);
				} else if (queryObject.IdVal == null && queryObject.lessThan == null
						&& queryObject.greaterThan == null) {
					queryOnColumnsWithoutConditions(queryObject);
				} else if (queryObject.IdVal == null
						&& queryObject.lessThan != null & queryObject.greaterThan != null) {
					System.out.println("greater than and less than condition");
					queryOnColumnsWithGreaterLessConditions(queryObject);
				} else {
					System.out.println("has conditions");
					queryOnColumnsWithEqualConditions(queryObject);
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				objectOutputStream.writeObject(stringBuffer);
				objectInputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void queryOnColumnName(QueryObject queryObject) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path + queryObject.fileName)));
			String line = null;

			ArrayList<Integer> c = findIfColumnRequired(queryObject);
			Integer id = queryObject.IdVal;
			int columnOfID = getColumnIndex(queryObject.fileName, queryObject.serachByColumnName);
			System.out.println("ID" + id);
			if (c.size() == Process.fileHeaders.get(queryObject.fileName).size()) {
				System.out.println("All columns are asked..");
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					if (spl[columnOfID].equals(queryObject.valueOfColumnName)) {
						stringBuffer.append(line + "\n");
						System.out.println(line);
					}
				}
			} else {
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					String temp = new String();
					if (Integer.parseInt(spl[columnOfID]) == id) {
						for (int i = 0; i < c.size(); i++) {
							temp = temp + spl[c.get(i)] + ",";
						}
						temp = temp.substring(0, temp.length() - 1);
						stringBuffer.append(temp + "\n");
					}
				}
			}
			System.out.println("Query processed on this node..");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void queryOnColumnsWithGreaterLessConditions(QueryObject queryObject) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path + queryObject.fileName)));
			String line = null;

			ArrayList<Integer> c = findIfColumnRequired(queryObject);
			Integer greaterThan = queryObject.greaterThan;
			Integer lessThan = queryObject.lessThan;
			int columnOfID = getColumnOfID(queryObject.fileName);
			// System.out.println("ID" + id);
			if (c.size() == Process.fileHeaders.get(queryObject.fileName).size()) {
				System.out.println("All columns are asked..");
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					if (Integer.parseInt(spl[columnOfID]) >= greaterThan
							&& Integer.parseInt(spl[columnOfID]) <= lessThan) {
						stringBuffer.append(line + "\n");
						System.out.println(line);
						// break;
					}
				}
			} else {
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					String temp = new String();
					if (Integer.parseInt(spl[columnOfID]) >= greaterThan
							&& Integer.parseInt(spl[columnOfID]) <= lessThan) {
						for (int i = 0; i < c.size(); i++) {
							temp = temp + spl[c.get(i)] + ",";
						}
						temp = temp.substring(0, temp.length() - 1);
						stringBuffer.append(temp + "\n");
					}
				}
			}
			System.out.println("Query processed on this node..");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void queryOnColumnsWithEqualConditions(QueryObject queryObject) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path + queryObject.fileName)));
			String line = null;

			ArrayList<Integer> c = findIfColumnRequired(queryObject);
			Integer id = queryObject.IdVal;
			int columnOfID = getColumnOfID(queryObject.fileName);
			System.out.println("ID" + id);
			if (c.size() == Process.fileHeaders.get(queryObject.fileName).size()) {
				System.out.println("All columns are asked..");
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					if (Integer.parseInt(spl[columnOfID]) == id) {
						stringBuffer.append(line + "\n");
						System.out.println(line);
						break;
					}
				}
			} else {
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					String temp = new String();
					if (Integer.parseInt(spl[columnOfID]) == id) {
						for (int i = 0; i < c.size(); i++) {
							temp = temp + spl[c.get(i)] + ",";
						}
						temp = temp.substring(0, temp.length() - 1);
						stringBuffer.append(temp + "\n");
					}
				}
			}
			System.out.println("Query processed on this node..");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void queryOnColumnsWithoutConditions(QueryObject queryObject) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path + queryObject.fileName)));
			String line = null;

			ArrayList<Integer> c = findIfColumnRequired(queryObject);
			if (c.size() == Process.fileHeaders.get(queryObject.fileName).size()) {
				System.out.println("All columns are asked..");
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					stringBuffer.append(line + "\n");
				}
			} else {
				int co = 0;
				while ((line = bufferedReader.readLine()) != null) {
					if (co == 0) {
						co = 1;
						continue;
					}
					String spl[] = line.split(",");
					String temp = new String();
					for (int i = 0; i < c.size(); i++) {
						temp = temp + spl[c.get(i)] + ",";
					}
					temp = temp.substring(0, temp.length() - 1);
					stringBuffer.append(temp + "\n");
				}
			}
			System.out.println("Query processed on this node..");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<Integer> findIfColumnRequired(QueryObject queryObject) {
		Socket socket = null;
		try {
			socket = new Socket("newyork.cs.rit.edu", 8000);
			System.out.println("Connected to controller");
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			Process.fileHeaders = (Map<String, ArrayList<String>>) objectInputStream.readObject();
			System.out.println("Header received");

			Process.primaryKeys = (Map<String, String>) objectInputStream.readObject();
			System.out.println("Primary keys received");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<String> h = Process.fileHeaders.get(queryObject.fileName);
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<String> queryOn = queryObject.columns;

		for (int i = 0; i < h.size(); i++) {
			if (queryOn.contains(h.get(i))) {
				result.add(i);
			}
		}
		return result;
	}

	public int getColumnIndex(String fileName, String columnName) {
		int columnOfID = 0;
		ArrayList<String> headers = Process.fileHeaders.get(fileName);
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).equals(columnName)) {
				columnOfID = i;
			}
		}
		System.out.println("Column id is" + columnOfID);
		return columnOfID;
	}

	public int getColumnOfID(String fileName) {
		int columnOfID = 0;
		ArrayList<String> headers = Process.fileHeaders.get(fileName);
		String pk = Process.primaryKeys.get(fileName);
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).equals(pk)) {
				columnOfID = i;
			}
		}
		System.out.println("Column id is" + columnOfID);
		return columnOfID;
	}
}
