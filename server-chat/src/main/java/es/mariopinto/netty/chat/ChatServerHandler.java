package es.mariopinto.netty.chat;

import java.util.HashMap;
import java.util.Map;

import es.mariopinto.netty.chat.utils.Constants;
import es.mariopinto.netty.chat.utils.NameUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;

// Handles a server-side channel.
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

	private static Map<Channel, ChatUser> chatUsers = new HashMap<>();
	private static Map<String, ChatRoom> chatRooms = new HashMap<>();
	private static NameUtils nameUtils = NameUtils.geSingletonInstance();

	/**
	 * Keep track of our active channels.
	 * 
	 * This method will be called when a new client connects to the server.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// First of all we create and add the general ChannelGroup to the chatRooms Map
		if (!chatRooms.containsKey(Constants.GENERAL_ROOM_NAME)) {
			ChatRoom newChatRoom = new ChatRoom(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
					Constants.GENERAL_ROOM_NAME);
			chatRooms.put(Constants.GENERAL_ROOM_NAME, newChatRoom);
		}

		Channel inconming = ctx.channel();
		// XXX TEST - Point 1.1 First connection welcome message / Nickname assignation
		// to channel
		String incomingAutoName = nameUtils.generateRandomNickName();
		inconming.writeAndFlush("Welcome " + incomingAutoName + "!\n");
		// Every new conecction enters the default channelGroup, here we construct the
		// chatUser and stores it
		ChatUser newUser = new ChatUser(inconming, incomingAutoName, chatRooms.get(Constants.GENERAL_ROOM_NAME));
		chatUsers.put(newUser.getChannel(), newUser);

		// Here we can notify the others than a new client has joined
		for (Channel channel : newUser.getChatRoom().getChannelGroup()) {
			channel.writeAndFlush("[SERVER] - " + newUser.getNickName() + " has joined!\n");
		}
		// We add it to the ChannelGroup
		newUser.getChatRoom().getChannelGroup().add(newUser.getChannel());
		newUser.getChatRoom().incrementActiveUsersCounter();

		// Shows history if there's any
		if (!newUser.getChatRoom().getRoomHistory().isEmpty()) {
			for (String messageHistory : newUser.getChatRoom().getRoomHistory()) {
				newUser.getChannel().writeAndFlush(messageHistory);
			}
		}
	}

	/**
	 * Keep track of when a client disconnect from the server.
	 * 
	 * This method will be called when a new client disconnect from the server
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		ChatUser incoming = chatUsers.get(ctx.channel());
		ChatRoom incomingChannelGroup = incoming.getChatRoom();
		// Here we can notify the others in the same chatRoom than a client has
		// disconnected
		for (Channel channel : incomingChannelGroup.getChannelGroup()) {
			channel.writeAndFlush("[SERVER] - " + incoming.getNickName() + " has left the server!\n");
		}
		// We remove it from the channel group and the chatUsers
		incomingChannelGroup.getChannelGroup().remove(incoming.getChannel());
		chatUsers.remove(incoming.getChannel());
		incomingChannelGroup.decrementActiveUsersCounter();
		// If active users counter is 0 delete the room (If it's not general room)
		if (incomingChannelGroup.getActiveUsersCounter() == 0
				&& !incomingChannelGroup.getRoomName().equals(Constants.GENERAL_ROOM_NAME)) {
			chatRooms.remove(incomingChannelGroup.getRoomName());
		}
	}

	/**
	 * This method will be renamed to messageReceived(ChannelHandlerContext, String)
	 * in v5.0.
	 * 
	 * This method is called with the received message, whenever new data is
	 * received from a client.
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		// In order to identify who send the message we ask for Channel Object
		ChatUser incoming = chatUsers.get(ctx.channel());
		ChatRoom incomingChannelGroup = incoming.getChatRoom();
		msg = msg.trim();
		if (!StringUtil.isNullOrEmpty(msg)) {
			// Obtain the command if there's any
			String command = this.messageHasCommand(msg);
			switch (command != null ? command : msg) {
				// XXX TEST - Point 1.6 Exit
				case Constants.COMMAND_EXIT:
					incoming.getChannel().close();
	
				break;
	
				case Constants.COMMAND_NICK:
					// XXX TEST - Point 1.2 Nickname Change
					// Delete the command from the string
					String newNickName = msg.replaceFirst(command, "").trim().toLowerCase();
	
					if (!StringUtil.isNullOrEmpty(newNickName) && !nameUtils.getNickNames().contains(newNickName)) {
						// The NickName will be 20 characters max
						if (newNickName.length() > 20) {
							newNickName = newNickName.substring(0, 20);
						}
	
						nameUtils.getNickNames().remove(incoming.getNickName());
						nameUtils.getNickNames().add(newNickName);
						incoming.setNickName(newNickName);
						incoming.getChannel().writeAndFlush(
								"[SERVER] - You've changed your name successfully <" + incoming.getNickName() + "> :).\n");
					} else {
						if (StringUtil.isNullOrEmpty(newNickName)) {
							incoming.getChannel().writeAndFlush(
									"[SERVER] - ERROR, empty new name, the command format is '/nick <newname>'.\n");
						} else {
							incoming.getChannel()
									.writeAndFlush("[SERVER] - ERROR, the name is already in use, try another :(.\n");
						}
					}
	
				break;
	
				case Constants.COMMAND_LIST:
					// XXX TEST - Point 1.3 List Rooms
					incoming.getChannel().writeAndFlush("[SERVER] - List of active chat Rooms ('*' is where you are):\n");
					for (String chatRoomName : chatRooms.keySet()) {
						incoming.getChannel().writeAndFlush("- " + chatRoomName
								+ (incoming.getChatRoom().getRoomName().equals(chatRoomName) ? " *" : "") + "\n");
					}
	
				break;
	
				case Constants.COMMAND_JOIN:
					// XXX TEST - Point 1.4 Join Rooms
					// Delete the command from the string
					String newRoomName = msg.replaceFirst(command, "").trim().toLowerCase();
					if (!StringUtil.isNullOrEmpty(newRoomName) && !incomingChannelGroup.getRoomName().equals(newRoomName)) {
						// The Room Name will be 20 characters max
						if (newRoomName.length() > 20) {
							newRoomName = newRoomName.substring(0, 20);
						}
	
						// If the rooms already exists join
						if (chatRooms.containsKey(newRoomName)) {
	
							this.joinGroupHandler(incomingChannelGroup, incoming, newRoomName);
	
							// Shows history if there's any
							if (!incoming.getChatRoom().getRoomHistory().isEmpty()) {
								for (String messageHistory : incoming.getChatRoom().getRoomHistory()) {
									incoming.getChannel().writeAndFlush(messageHistory);
								}
							}
						} else {
							// Create new room
							ChatRoom newChatRoom = new ChatRoom(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
									newRoomName);
							chatRooms.put(newRoomName, newChatRoom);
	
							this.joinGroupHandler(incomingChannelGroup, incoming, newRoomName);
	
						}
					} else {
						if (StringUtil.isNullOrEmpty(newRoomName)) {
							incoming.getChannel().writeAndFlush(
									"[SERVER] - ERROR, empty room name, the command format is '/join <roomname>'.\n");
						}
	
						if (incomingChannelGroup.getRoomName().equals(newRoomName)) {
							incoming.getChannel().writeAndFlush("[SERVER] - You are already in this room.\n");
						}
					}
	
				break;
	
				default:
					// XXX TEST - Point 1.5 Sending & Receiving messages / Message limit control
					// (Only 30 per minute)
					// Send the message to everybody else (not yourself) in the same channelGroup,
					// we do this by iterating over all known
					if (incoming.checkMessageLimit()) {
						String finalMessage = "[" + incoming.getNickName() + "]: " + msg + "\n";
						for (Channel channel : incomingChannelGroup.getChannelGroup()) {
							if (channel != incoming.getChannel()) {
								channel.writeAndFlush(finalMessage);
							}
						}
						// We store the message on the history
						incomingChannelGroup.saveMessage(finalMessage);
					} else {
						incoming.getChannel().writeAndFlush(
								"[SERVER] - You have exceeded the limit of 30 messages. Wait a minute :)\n");
					}
	
				break;
			}
		}
	}

	// Operations to join new group
	public void joinGroupHandler(ChatRoom incomingChannelGroup, ChatUser incoming, String newRoomName) {
		// Delete incoming channel from the channelgroup that is leaving
		incomingChannelGroup.getChannelGroup().remove(incoming.getChannel());
		incomingChannelGroup.decrementActiveUsersCounter();
		// Leaving message for other users
		for (Channel channel : incomingChannelGroup.getChannelGroup()) {
			channel.writeAndFlush("[SERVER] - " + incoming.getNickName() + " has left the channel!\n");
		}
		// If active users counter is 0 delete the room (If it's not general room)
		if (incomingChannelGroup.getActiveUsersCounter() == 0
				&& !incomingChannelGroup.getRoomName().equals(Constants.GENERAL_ROOM_NAME)) {
			chatRooms.remove(incomingChannelGroup.getRoomName());
		}
		// Assign the new chatRoom to the chatUser
		incoming.setChatRoom((chatRooms.get(newRoomName)));
		// Add the chatUser channel to the new chatRoom channelgroup
		incoming.getChatRoom().getChannelGroup().add(incoming.getChannel());
		incoming.getChatRoom().incrementActiveUsersCounter();
		incoming.getChannel().writeAndFlush("[SERVER] - Welcome to <" + incoming.getChatRoom().getRoomName()
				+ ">, there are " + incoming.getChatRoom().getActiveUsersCounter() + " users connected.\n");
	}

	private String messageHasCommand(String msg) {
		String command = null;

		if (msg.startsWith(Constants.COMMAND_EXIT)) {
			command = Constants.COMMAND_EXIT;
		}

		if (msg.startsWith(Constants.COMMAND_NICK)) {
			command = Constants.COMMAND_NICK;
		}

		if (msg.startsWith(Constants.COMMAND_JOIN)) {
			command = Constants.COMMAND_JOIN;
		}

		if (msg.startsWith(Constants.COMMAND_LIST)) {
			command = Constants.COMMAND_LIST;
		}

		return command;
	}

	/**
	 * Method is called with a Throwable when an exception was raised by Netty due
	 * to an I/O error or by a handler implementation due to the exception thrown
	 * while processing events.
	 * 
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
