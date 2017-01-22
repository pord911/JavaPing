package com.tcpping.catcher;

import java.io.IOException;
import java.net.Socket;

import com.tcpping.message.MessageInputOutput;
import com.tcpping.time.TimingClass;

public class CatcherClientHandler implements Runnable {
	private Socket connection;
	private MessageInputOutput msgHandler;
	private StringBuilder msgAppend;

	/**
	 * Catcher handler.
	 * @param connection    Socket connection
	 * @throws IOException
	 */
	public CatcherClientHandler(Socket connection) throws IOException {
		this.connection = connection;
		msgHandler = new MessageInputOutput(connection);
		msgAppend = new StringBuilder();
	}

	/**
	 * Message reader. Reads each message, appends the necessary
	 * information and sends it back on the stream.
	 */
	private void startReadingMessages() {
		long catcherTime = 0;
		String msg;

		try {
			while ((msg = msgHandler.readMessage()) != null) {
				catcherTime = TimingClass.getTime();

				/* If client wants to close the connection, close it! */
				if (msg.equals("BYE")) {
					System.out.println("Sending BYE");
					msgHandler.writeMessage("OKBYE");
					break;
				}
				msgHandler.writeMessage(appendTime(msg, catcherTime));
			}
		} catch (IOException e) {
			System.out.print(e.getMessage());
		} finally {
			try {
				if (msgHandler != null)
					msgHandler.closeMessageStream();
				if (connection != null)
					connection.close();
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}
		}
	}

	/**
	 * Append A->B time difference and receival time
	 * to the received message. The newly created message will have
	 * the following syntax: MESSAGE%ABTIME/RECEIVALTIME
	 * @param message      Received message
	 * @param catcherTime  Time when the message was received
	 * @return
	 */
	private String appendTime(String message, long catcherTime) {
		String timeDifference;
		String catcherTimeStr;
		int lastIndex;

		msgAppend.setLength(0);
		timeDifference = getDirectionTime(message, catcherTime);
		catcherTimeStr = Long.toString(catcherTime);
		/* Number 2 is for characters '&' and '/' */
		lastIndex = message.length() - timeDifference.length() - catcherTimeStr.length() - 2;
		msgAppend.append(message.substring(0, lastIndex));
		msgAppend.append("&" + timeDifference)
		         .append("/" + catcherTimeStr);
		return msgAppend.toString();
	}

	/**
	 * Calculate A->B time difference.
	 * @param message      Received message.
	 * @param catcherTime  Time when the message was received
	 * @return
	 */
	private String getDirectionTime(String message, long catcherTime) {
		int firstIndex = message.indexOf("%");
		int lastIndex = message.indexOf("-");
		long directionTime = Long.decode(message.substring(firstIndex + 1, lastIndex));

		return Long.toString(catcherTime - directionTime);
	}

	/**
	 * Start thread.
	 */
	public void run() {
		startReadingMessages();
	}
}
