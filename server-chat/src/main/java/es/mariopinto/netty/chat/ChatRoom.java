package es.mariopinto.netty.chat;

import java.util.concurrent.ConcurrentLinkedDeque;

import io.netty.channel.group.ChannelGroup;

public class ChatRoom {

	private ChannelGroup channelGroup;
	private String roomName;
	// Personally selected this concurrent collection "Deque" thinking to make use of the remove/add last wich the queue doesn't have
	private ConcurrentLinkedDeque<String> roomHistory;
	private int activeUsersCounter;

	public ChatRoom(ChannelGroup channelGroup, String roomName) {

		this.channelGroup = channelGroup;
		this.roomName = roomName;
		this.roomHistory = new ConcurrentLinkedDeque<>();
		this.activeUsersCounter = 0;
	}

	public void incrementActiveUsersCounter() {
		this.activeUsersCounter++;
	}

	public void decrementActiveUsersCounter() {
		this.activeUsersCounter--;
	}

	// This method always stores the last 5 messages of the channel group
	public void saveMessage(String finalMessage) {
		if (roomHistory.size() < 5) {
			roomHistory.add(finalMessage);
		} else if (roomHistory.size() >= 5) {
			roomHistory.removeFirst();
			roomHistory.add(finalMessage);
		}
	}

	public ChannelGroup getChannelGroup() {
		return channelGroup;
	}

	public void setChannelGroup(ChannelGroup channelGroup) {
		this.channelGroup = channelGroup;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public ConcurrentLinkedDeque<String> getRoomHistory() {
		return roomHistory;
	}

	public void setRoomHistory(ConcurrentLinkedDeque<String> roomHistory) {
		this.roomHistory = roomHistory;
	}

	public int getActiveUsersCounter() {
		return activeUsersCounter;
	}

	public void setActiveUsersCounter(int activeUsersCounter) {
		this.activeUsersCounter = activeUsersCounter;
	}

	@Override
	public String toString() {
		return "Room [Room Name=" + roomName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomName == null) ? 0 : roomName.hashCode());
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
		ChatRoom other = (ChatRoom) obj;
		if (roomName == null) {
			if (other.roomName != null)
				return false;
		} else if (!roomName.equals(other.roomName))
			return false;
		return true;
	}

}
