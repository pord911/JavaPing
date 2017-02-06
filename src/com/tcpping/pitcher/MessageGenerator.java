package com.tcpping.pitcher;

import java.io.IOException;
import java.util.TimerTask;

import org.apache.commons.lang3.RandomStringUtils;

import com.tcpping.connection.TCPConnection;
import com.tcpping.message.Message;
import com.tcpping.message.MessageContainer;
import com.tcpping.message.MessageOutput;
import com.tcpping.time.TimingClass;


public class MessageGenerator extends TimerTask {
	private final String CLOSE = "BYE";
	private int PINGS = 5;
	private int size;
	private int msgPerSecond;
	private MessageOutput msgHandler;
	private int messageId = 0;
	private MessageContainer msgContainer;
	private int pingCounter = 0;
	private int sentMessages = 0;
	private StringBuilder buildMessage;

	/**
	 * Create generator object for generating number of messages per second.
	 * @param size            Size of the message.
	 * @param msgPerSecond    Number of messages per second.
	 * @param msgHandler      Reference for message handler.
	 * @param msgContainer    Reference for message container.
	 * @throws IOException
	 */
	public MessageGenerator(int size, int msgPerSecond, TCPConnection connection, MessageContainer msgContainer) throws IOException {
		this.size = size;
		this.msgPerSecond = msgPerSecond;
		this.msgHandler = new MessageOutput(connection.getClientSocket());
		this.msgContainer = msgContainer;
		this.buildMessage = new StringBuilder();
	}

	/**
	 * Send a message and store it in the container/list
	 * @param message    Message to send.
	 */
	private void sendMessages() {
		int i;
		Message message;
		try {
			if (pingCounter < PINGS) {
				for (i = 0; i < msgPerSecond; i++) {
					message = createMessage();
					msgHandler.writeMessage(message.getMessage());
					msgContainer.storeMessage(message);
				}
			} else {
				msgHandler.writeMessage(CLOSE);
			}
			pingCounter++;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Create a message with the following format
	 * MessageId%timeInMillis-randomChars
	 * @return
	 */
	private Message createMessage() {
		Message message = new Message();
		String timeStr = Long.toString(message.getTimeStamp());
		buildMessage.setLength(0);
		buildMessage.append(++messageId)
		            .append("%")
		            .append(Long.toString(message.getTimeStamp()))
		            .append("-")
		            .append(RandomStringUtils.randomAlphabetic(size - timeStr.length() - 3));
		message.setMessage(buildMessage.toString());
		message.setMessageId(messageId);
	    return message;
	}

	/**
	 * Start the generator thread.
	 * Send the configured number of messages 5 times.
	 * After that send BYE indicating end of stream.
	 */
	@Override
	public void run() {
		sendMessages();
	}
}