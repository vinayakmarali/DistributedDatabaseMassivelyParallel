import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ControllerQueryParse implements Runnable {
	public String query;
	public boolean doesQueryContainWhere = false;
	public boolean doesConditionHasPrimaryKey = false;
	public boolean doesHaveBetween = false;
	public Socket client;
	public boolean doesHaveEqualPK = false;
	public boolean doesHaveBetweenPK = false;
	public StringBuffer finalB;
	public int pkBetweenStarts, pkBetweenEnds;
	public int pkEqualVal;

	public String columnName;
	public boolean doesHaveEqualColumn = false;
	public String columnEqualVal;
	public boolean doesHaveBetweenColumn = false;

	@Override
	public void run() {
		synchronized (ControllerToHandleQuery.sync) {
			finalB = new StringBuffer();
			System.out.println("Starting to process query");
			System.out.println("Query is:");
			System.out.println(query);

			if (query.contains("where")) {
				doesQueryContainWhere = true;
			}
			if (query.contains("between")) {
				doesHaveBetween = true;
			}

			String[] queryPartitions = query.split(" ");
			boolean doesHaveAnyError = errorHandling(queryPartitions);

			if (!doesHaveAnyError) {
				ArrayList<String> columns = columnsToBeQueried(queryPartitions[1], queryPartitions[3]);
				System.out.println("Query does not have any error so filtering data");
				System.out.println(doesQueryContainWhere);

				if (doesQueryContainWhere) {
					doesConditionHasPrimaryKey = doesConditionHasPrimaryKey(query, queryPartitions[3]);
					if (doesConditionHasPrimaryKey) {
						findIDEqualOrBetweenForPk(queryPartitions);
						System.out.println("B" + doesHaveBetweenPK);
						System.out.println("=" + doesHaveEqualPK);
						if (doesConditionHasPrimaryKey) {
							if (doesHaveEqualPK) {
								System.out.println("Redirecting via primary key '='");
								ArrayList<String> sendTo = redirectToViaEqual(queryPartitions);
								sendRequestToServersWithPkEqualityAtTheEnd(sendTo, queryPartitions, columns);
							} else if (doesHaveBetweenPK) {
								System.out.println("Redirecting via primary key 'between'");
								ArrayList<String> sendTo = redirectToViaBetween(pkBetweenStarts, pkBetweenEnds);
								sendRequestToServersWithPkBetweenAtTheEnd(sendTo, queryPartitions, columns);
							}
						}
					} else if (doesConditionHasPrimaryKey == false) {
						System.out.println("Does not contain primary key!!!!!!");

						findIDEqualOrBetweenForColumn(queryPartitions);
						System.out.println("B" + doesHaveBetweenColumn);
						System.out.println("=" + doesHaveEqualColumn);
						if (doesHaveEqualColumn) {
							System.out.println("Redirecting via column '='");
							//ArrayList<String> sendTo = redirectToViaEqualForNonPK(queryPartitions);
							sendRequestToAllServersColumn(queryPartitions[3], columns, columnName,
									columnEqualVal);
						}
					}
				} else {
					sendRequestToAllServers(queryPartitions[3], columns);
				}
				System.out.println("sent to client");
			}
		}
	}

	private ArrayList<String> redirectToViaEqualForNonPK(String[] queryPartitions) {
		ArrayList<String> sendTo = new ArrayList<String>();
		for (int i = 0; i < Controller.nodes.size(); i++) {
			sendTo.add(Controller.nodes.get(i).nodeName);
		}
		return sendTo;
	}

	private void sendRequestToServersWithPkBetweenAtTheEnd(ArrayList<String> sendTo, String[] queryPartitions,
			ArrayList<String> columns) {
		for (int i = 0; i < sendTo.size(); i++) {
			try {
				Socket socket = new Socket(sendTo.get(i), 10001);
				System.out.println("sending query to between: " + sendTo.get(i));
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

				QueryObject queryObject = new QueryObject();
				queryObject.columns = columns;
				queryObject.fileName = queryPartitions[3];
				queryObject.greaterThan = pkBetweenStarts;
				queryObject.lessThan = pkBetweenEnds;
				System.out.println("Object prepared to send");

				objectOutputStream.writeObject(queryObject);
				objectOutputStream.flush();
				System.out.println("Query object sent");

				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();

				/*
				 * while (Controller.othersAreWriting) {
				 * 
				 * }
				 */
				finalB.append(stringBuffer.toString());
				// System.out.println(stringBuffer.toString());
				System.out.println("Output written");

				// Controller.queryParsed.remove(0);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(client.getOutputStream());
			objectOutputStream.writeObject(finalB);
			objectOutputStream.flush();
			Controller.finalOut = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> redirectToViaBetween(int pkBetweenStarts, int pkBetweenEnds) {
		ArrayList<String> sendTo = new ArrayList<String>();
		int hashStart = hashOfVal(String.valueOf(pkBetweenStarts));
		int hashEnd = hashOfVal(String.valueOf(pkBetweenEnds));

		for (int i = 0; i < Controller.nodes.size(); i++) {
			if (Controller.nodes.get(i).range.minRange > hashStart
					&& Controller.nodes.get(i).range.maxRange < hashEnd) {
				sendTo.add(Controller.nodes.get(i).nodeName);
			} else if (hashStart > Controller.nodes.get(i).range.minRange
					&& hashStart < Controller.nodes.get(i).range.maxRange) {
				sendTo.add(Controller.nodes.get(i).nodeName);
			} else if (hashEnd > Controller.nodes.get(i).range.minRange
					&& hashEnd < Controller.nodes.get(i).range.maxRange) {
				sendTo.add(Controller.nodes.get(i).nodeName);
			}
		}
		return sendTo;
	}

	private void findIDEqualOrBetweenForPk(String[] queryPartitions) {
		for (int i = 0; i < queryPartitions.length; i++) {
			if (queryPartitions[i].equals(Controller.primaryKey.get(queryPartitions[3]))) {
				String t = queryPartitions[i + 1];
				if (t.equals("=")) {
					doesHaveEqualPK = true;
					pkEqualVal = Integer.parseInt(queryPartitions[i + 2]);
				} else if (t.equals("between")) {
					doesHaveBetweenPK = true;
					pkBetweenStarts = Integer.parseInt(queryPartitions[i + 2]);
					pkBetweenEnds = Integer.parseInt(queryPartitions[i + 4]);
				}
			}
		}
	}

	private void findIDEqualOrBetweenForColumn(String[] queryPartitions) {
		for (int i = 4; i < queryPartitions.length; i++) {
			if (!queryPartitions[i].equals(Controller.primaryKey.get(queryPartitions[3]))) {
				String t = queryPartitions[i];
				if (t.equals("=")) {
					doesHaveEqualColumn = true;
					columnName = queryPartitions[i - 1];
					columnEqualVal = queryPartitions[i + 1];
				}
			}
		}
	}

	private void sendRequestToServersWithPkEqualityAtTheEnd(ArrayList<String> sendTo, String[] queryPartitions,
			ArrayList<String> columns) {
		for (int i = 0; i < sendTo.size(); i++) {
			try {
				Socket socket = new Socket(sendTo.get(i), 10001);
				System.out.println("sending query to : " + sendTo.get(i));
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

				QueryObject queryObject = new QueryObject();
				queryObject.columns = columns;
				queryObject.fileName = queryPartitions[3];
				queryObject.IdVal = pkEqualVal;
				System.out.println("Object prepared to send");

				objectOutputStream.writeObject(queryObject);
				objectOutputStream.flush();
				System.out.println("Query object sent");

				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();

				/*
				 * while (Controller.othersAreWriting) {
				 * 
				 * }
				 */
				finalB.append(stringBuffer.toString());
				// System.out.println(stringBuffer.toString());
				System.out.println("Output written");

				// Controller.queryParsed.remove(0);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			ObjectOutputStream objectOutputStream = null;
			try {
				objectOutputStream = new ObjectOutputStream(client.getOutputStream());
				objectOutputStream.writeObject(finalB);
				objectOutputStream.flush();
				Controller.finalOut = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*
			 * System.out.println("Send to : " + sendTo); ControllerSendQuery
			 * controllerSendQuery = new ControllerSendQuery();
			 * controllerSendQuery.doesHaveConditions = false;
			 * controllerSendQuery.columns = columns;
			 * controllerSendQuery.fileName = queryPartitions[3];
			 * controllerSendQuery.sendQueryTo = sendTo;
			 * controllerSendQuery.IdVal = Integer.parseInt(queryPartitions[7]);
			 * Controller.queryParsed.add(1); Thread sendQueryThread = new
			 * Thread(controllerSendQuery); sendQueryThread.start();
			 */
		}
	}

	private ArrayList<String> redirectToViaEqual(String[] queryPartitions) {
		ArrayList<String> sendTo = new ArrayList<String>();
		Integer val = Integer.parseInt(queryPartitions[7]);
		Integer hasOfVal = hashOfVal(queryPartitions[queryPartitions.length - 1]);
		for (int i = 0; i < Controller.nodes.size(); i++) {
			if (Controller.nodes.get(i).range.minRange <= hasOfVal
					&& Controller.nodes.get(i).range.maxRange >= hasOfVal) {
				sendTo.add(Controller.nodes.get(i).nodeName);
			}
		}
		return sendTo;
	}

	private int hashOfVal(String val) {
		int hash = val.hashCode();
		hash = hash % Controller.maxLimit;
		return hash;
	}

	private boolean doesConditionHasPrimaryKey(String query, String fileName) {
		String[] a = query.split(" ");
		ArrayList<String> queryList = new ArrayList<>();
		boolean isPk = false;
		for (int i = 0; i < a.length; i++) {
			queryList.add(a[i]);
		}
		System.out.println("PK:" + Controller.primaryKey.get(fileName));
		for (int i = 4; i < queryList.size(); i++) {
			if (queryList.get(i).toLowerCase().equals(Controller.primaryKey.get(fileName))) {
				isPk = true;
			}
		}
		return isPk;
	}

	private void sendRequestToAllServersColumn(String fileName, ArrayList<String> columns,
			String columnName, String columnValue) {
		ArrayList<NodeDetails> nodes = Controller.nodes;
		for (int i = 0; i < nodes.size(); i++) {
			try {
				Socket socket = new Socket(nodes.get(i).nodeName, 10001);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				QueryObject queryObject = new QueryObject();
				queryObject.columns = columns;
				queryObject.fileName = fileName;
				queryObject.IdVal = null;
				queryObject.lessThan = null;
				queryObject.greaterThan = null;
				queryObject.valueOfColumnName = columnValue;
				queryObject.serachByColumnName = columnName;
				objectOutputStream.writeObject(queryObject);
				objectOutputStream.flush();
				System.out.println("Query object sent");

				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();

				/*
				 * while (Controller.othersAreWriting) {
				 * 
				 * }
				 */
				finalB.append(stringBuffer.toString());
				// System.out.println(stringBuffer.toString());
				System.out.println("Output written");

				// Controller.queryParsed.remove(0);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			/*
			 * ControllerSendQuery controllerSendQuery = new
			 * ControllerSendQuery(); controllerSendQuery.doesHaveConditions =
			 * false; controllerSendQuery.columns = columns;
			 * controllerSendQuery.fileName = fileName;
			 * controllerSendQuery.sendQueryTo = nodes.get(i).nodeName;
			 * Controller.queryParsed.add(1); Thread sendQueryThread = new
			 * Thread(controllerSendQuery); sendQueryThread.start();
			 */
		}

		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(client.getOutputStream());
			objectOutputStream.writeObject(finalB);
			objectOutputStream.flush();
			Controller.finalOut = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendRequestToAllServers(String fileName, ArrayList<String> columns) {
		ArrayList<NodeDetails> nodes = Controller.nodes;
		for (int i = 0; i < nodes.size(); i++) {

			try {
				Socket socket = new Socket(nodes.get(i).nodeName, 10001);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				QueryObject queryObject = new QueryObject();
				queryObject.columns = columns;
				queryObject.fileName = fileName;
				queryObject.IdVal = null;
				objectOutputStream.writeObject(queryObject);
				objectOutputStream.flush();
				System.out.println("Query object sent");

				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				StringBuffer stringBuffer = (StringBuffer) objectInputStream.readObject();

				/*
				 * while (Controller.othersAreWriting) {
				 * 
				 * }
				 */
				finalB.append(stringBuffer.toString());
				// System.out.println(stringBuffer.toString());
				System.out.println("Output written");

				// Controller.queryParsed.remove(0);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			/*
			 * ControllerSendQuery controllerSendQuery = new
			 * ControllerSendQuery(); controllerSendQuery.doesHaveConditions =
			 * false; controllerSendQuery.columns = columns;
			 * controllerSendQuery.fileName = fileName;
			 * controllerSendQuery.sendQueryTo = nodes.get(i).nodeName;
			 * Controller.queryParsed.add(1); Thread sendQueryThread = new
			 * Thread(controllerSendQuery); sendQueryThread.start();
			 */
		}

		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(client.getOutputStream());
			objectOutputStream.writeObject(finalB);
			objectOutputStream.flush();
			Controller.finalOut = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<String> columnsToBeQueried(String queryPartition, String fileName) {
		ArrayList<String> columns = new ArrayList<String>();
		if (queryPartition.equals("*")) {
			ArrayList<String> header = Controller.fileHeaders.get(fileName);
			columns = header;
		} else {
			String[] col = queryPartition.split(",");
			for (int i = 0; i < col.length; i++) {
				columns.add(col[i]);
			}
		}
		return columns;
	}

	private boolean errorHandling(String[] q) {
		if (!q[0].equals("select")) {
			System.out.println("select not written correctly");
			return true;
		} else if (!q[2].equals("from")) {
			System.out.println("from clause written incorrectly");
			return true;
		} else if (fileDoesNotExists(q[3])) {
			System.out.println("Queried file does not exist");
			return true;
		}
		return false;
	}

	private boolean fileDoesNotExists(String s) {
		if (Controller.files.contains(s)) {
			return false;
		} else {
			return true;
		}
	}
}
