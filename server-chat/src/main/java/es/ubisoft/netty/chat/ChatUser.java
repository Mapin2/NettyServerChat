package es.ubisoft.netty.chat;

import java.time.Duration;
import java.time.LocalTime;

import io.netty.channel.Channel;
import io.netty.util.internal.MathUtil;

public class ChatUser {

	private Channel channel;
	private String nickName;
	private ChatRoom chatRoom;
	private int messageCounter;
	@SuppressWarnings("unused")
	public static final int MESSAGE_LIMIT = 30;
	private LocalTime firstMessageCounterToLimit;
	
	public ChatUser(Channel channel, String nickName, ChatRoom chatRoom) {

		this.channel = channel;
		this.nickName = nickName;
		this.chatRoom = chatRoom;
		this.messageCounter = 0;
	}

	// XXX TEST - Point 1.5 Messages limit (Only 30 per minute)
	public boolean checkMessageLimit() {
		boolean result = false;
		
		// First we check if more than one minute has passed since our first message to reset counterss
		if (this.firstMessageCounterToLimit != null && Duration.between(this.firstMessageCounterToLimit, LocalTime.now()).toMinutes() >= 1) {
			this.messageCounter = 0;
		}
		
		// If messageCounter is less than the MESSAGE_LIMIT we can message
		if (MathUtil.compare(this.messageCounter, MESSAGE_LIMIT) == -1) {
			result = true;
			this.incrementMessageCounter();
		}
		
		return result;
	}
	
	private void incrementMessageCounter() {
		// We set the time when messageCounter is 0
		if (this.messageCounter == 0) {
			this.firstMessageCounterToLimit = LocalTime.now();
		}
		this.messageCounter++;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public ChatRoom getChatRoom() {
		return chatRoom;
	}

	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	public int getMessageCounter() {
		return messageCounter;
	}

	public void setMessageCounter(int messageCounter) {
		this.messageCounter = messageCounter;
	}

	@Override
	public String toString() {
		return "User [NickName=" + nickName + ", Room Name=" + chatRoom.getRoomName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nickName == null) ? 0 : nickName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatUser other = (ChatUser) obj;
		if (nickName == null) {
			if (other.nickName != null)
				return false;
		} else if (!nickName.equals(other.nickName))
			return false;
		return true;
	}

}