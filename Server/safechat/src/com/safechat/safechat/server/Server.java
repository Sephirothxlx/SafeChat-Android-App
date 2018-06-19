package com.safechat.safechat.server;

import com.safechat.safechat.dao.ServerDao;
import com.safechat.safechat.dao.UserDao;
import com.safechat.safechat.encryptor.AES;
import com.safechat.safechat.encryptor.RSA;
import com.safechat.safechat.message.*;
import com.safechat.safechat.user.Friend;
import com.safechat.safechat.user.ServerUser;

import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class Server implements Runnable {
	private Socket socket;
	private Socket chatsocket;
	private ServerUser threadUser;
	private Key kcs;
	private Key chatkcs;

	public Server(Socket socket) {
		this.socket = socket;
	}

	public ServerUser getThreadUser() {
		return threadUser;
	}

	public void setChatSocket(Socket chatsocket) {
		this.chatsocket = chatsocket;
	}

	public Socket getSocket() {
		return socket;
	}

	public Socket getChatSocket() {
		return chatsocket;
	}

	public void setKcs(Key kcs) {
		this.kcs = kcs;
	}

	public Key getKcs() {
		return kcs;
	}

	public Key getChatKcs() {
		return chatkcs;
	}

	public void setChatKcs(Key chatkcs) {
		this.chatkcs = chatkcs;
	}

	@Override
	public void run() {
		quit: while (true) {
			Datagram temp = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				temp = (Datagram) ois.readObject();
			} catch (Exception e) {
				continue;
			}
			// decrypt massage
			AbstractMessage message = null;
			if (temp.getMessageEncryptType() == Datagram.MessageEncryptType.AES && kcs != null) {
				try {
					message = AES.decryptMessage(temp.getCipherMessageBytes(), kcs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					message = (AbstractMessage) RSA.decryptMessage(temp.getCipherMessageBytes(),
							ServerDao.getPrivateKey());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (message.getType()) {
			case REGISTER_REQUEST:
				handleRegisterRequest(message);
				break quit;

			case LOGIN_REQUEST:
				handleLoginRequest(message);
				break;

			case FRIEND_LIST_REQUEST:
				handleFriendListRequest(message);
				break;

			case FRIEND_REQUEST:
				handleFriendRequest(message);
				break;

			case ACCEPT_FRIEND_RESPONSE:
				handleAcceptFriendResponse(message);
				break;

			case REJECT_FRIEND_RESPONSE:
				handleRejectFriendResponse(message);
				break;

			case ESTABLISH_CHAT_REQUEST:
				handleEstablishChatRequest(message);
				break;

			case FORWARD_MESSAGE:
				handleForwardRequest(message);
				break;

			case USER_CLOSE:
				ServerDao.removeOnlineUserThread(this);
				break quit;
			default:
				break;
			}
		}
	}

	public void sendMessage(Socket socket, AbstractMessage message, Key key,
			Datagram.MessageEncryptType messageEncryptType) {
		byte[] cipherMessageBytes;
		if (messageEncryptType == Datagram.MessageEncryptType.RSA) {
			cipherMessageBytes = RSA.encryptMessage(message, key);
		} else {
			cipherMessageBytes = AES.encryptMessage(message, key);
		}
		Datagram datagram = new Datagram(cipherMessageBytes, messageEncryptType);
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(datagram);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleRegisterRequest(AbstractMessage message) {
		RegisterRequest temp = (RegisterRequest) message;
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(temp.getSenderPublicKey());
		Key userKey = null;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			userKey = keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (RSA.checkTimeStamp(temp.getCipherTimeStamp(), userKey)) {
			if (UserDao.getUserWithID(temp.getSenderID()) == null) {
				UserDao.addUser(new ServerUser(temp.getSenderID(), userKey));
				AbstractMessage reply = new AcceptRegisterResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()),
						temp.getSenderID());
				sendMessage(socket, reply, userKey, Datagram.MessageEncryptType.RSA);
			} else {
				AbstractMessage reply = new RejectRegisterResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()),
						temp.getSenderID());
				sendMessage(socket, reply, userKey, Datagram.MessageEncryptType.RSA);
			}
		} else {
			AbstractMessage reply = new RejectRegisterResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()),
					temp.getSenderID());
			sendMessage(socket, reply, userKey, Datagram.MessageEncryptType.RSA);
		}
	}

	private void handleLoginRequest(AbstractMessage message) {
		String userId = ((LoginRequest) message).getSenderID();
		ServerUser user = UserDao.getUserWithID(userId);
		if (user != null) {
			ServerDao.addOnlineUserThread(this);
			Key userKey = user.getPublicKey();
			if (RSA.checkTimeStamp(message.getCipherTimeStamp(), userKey)) {
				setKcs(AES.geneAESKey());
				AbstractMessage reply = new AcceptLoginResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()), kcs);
				sendMessage(socket, reply, userKey, Datagram.MessageEncryptType.RSA);
				threadUser = new ServerUser(userId, userKey);
			} else {
				AbstractMessage reply = new RejectLoginResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()));
				sendMessage(socket, reply, userKey, Datagram.MessageEncryptType.RSA);
			}
		}
	}

	private void handleFriendListRequest(AbstractMessage message) {
		String senderId = ((FriendListRequest) message).getSenderID();
		ArrayList<Friend> friendList = UserDao.getFriendListWithId(senderId);
		AbstractMessage reply = new FriendListResponse(AES.getCipherTimeStamp(kcs), friendList);
		sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
	}

	private void handleFriendRequest(AbstractMessage message) {
		String senderId = ((FriendRequest) message).getSenderID();
		String receiverId = ((FriendRequest) message).getReceiverID();
		Server receiverThread = ServerDao.getOnlineUserThreadWithId(receiverId);
		if (!UserDao.searchUser(receiverId)) {
			AbstractMessage reply = new UserNotExistResponse(AES.getCipherTimeStamp(kcs), receiverId);
			sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
		} else {
			if (receiverThread == null) {
				AbstractMessage reply = new UserOfflineResponse(AES.getCipherTimeStamp(kcs), receiverId);
				sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
			} else {
				ArrayList<Friend> al = UserDao.getFriendListWithId(senderId);
				for (Friend f : al) {
					if (f.getID().equals(receiverId))
						return;
				}

				AbstractMessage forwardMessage = new FriendRequest(AES.getCipherTimeStamp(receiverThread.getKcs()),
						senderId, receiverId);
				sendMessage(receiverThread.getSocket(), forwardMessage, receiverThread.getKcs(),
						Datagram.MessageEncryptType.AES);
			}
		}
	}

	private void handleRejectFriendResponse(AbstractMessage message) {
		String senderId = ((RejectFriendResponse) message).getSenderID();
		String receiverId = ((RejectFriendResponse) message).getReceiverID();
		Server receiverThread = ServerDao.getOnlineUserThreadWithId(receiverId);
		if (receiverThread == null) {
			AbstractMessage reply = new UserOfflineResponse(AES.getCipherTimeStamp(kcs), receiverId);
			sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
		} else {
			AbstractMessage forwardMessage = new RejectFriendResponse(AES.getCipherTimeStamp(receiverThread.getKcs()),
					senderId, receiverId);
			sendMessage(receiverThread.getSocket(), forwardMessage, receiverThread.getKcs(),
					Datagram.MessageEncryptType.AES);
		}
	}

	private void handleAcceptFriendResponse(AbstractMessage message) {
		String senderId = ((AcceptFriendResponse) message).getSenderID();
		String receiverId = ((AcceptFriendResponse) message).getReceiverID();
		Server receiverThread = ServerDao.getOnlineUserThreadWithId(receiverId);
		if (receiverThread == null) {
			AbstractMessage reply = new UserOfflineResponse(AES.getCipherTimeStamp(kcs), receiverId);
			sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
		} else {
			UserDao.addFriendRelation(senderId, receiverId);
			AbstractMessage forwardMessage = new AcceptFriendResponse(AES.getCipherTimeStamp(receiverThread.getKcs()),
					senderId, receiverId);
			sendMessage(receiverThread.getSocket(), forwardMessage, receiverThread.getKcs(),
					Datagram.MessageEncryptType.AES);
		}
	}

	private void handleEstablishChatRequest(AbstractMessage message) {
		String senderId = ((EstablishChatRequest) message).getSenderID();
		Server senderThread = ServerDao.getOnlineUserThreadWithId(senderId);
		if (senderThread != null) {
			kcs = AES.geneAESKey();
			senderThread.setChatKcs(kcs);
			senderThread.setChatSocket(this.getSocket());
			AbstractMessage m = new AcceptEstablishChatResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()),
					senderId, kcs);
			sendMessage(senderThread.getChatSocket(), m, UserDao.getUserKeyWithID(senderId),
					Datagram.MessageEncryptType.RSA);
		} else {
			AbstractMessage m = new RejectEstablishChatResponse(RSA.getCipherTimeStamp(ServerDao.getPrivateKey()),
					senderId);
			sendMessage(senderThread.getChatSocket(), m, UserDao.getUserKeyWithID(senderId),
					Datagram.MessageEncryptType.RSA);
		}
	}

	private void handleForwardRequest(AbstractMessage message) {
		String senderId = ((ForwardMessage) message).getSenderID();
		String receiverId = ((ForwardMessage) message).getReceiverID();
		Datagram datagram = ((ForwardMessage) message).getDatagram();
		int mode = ((ForwardMessage) message).getMode();
		if (mode == 0) {
			Server receiverThread = ServerDao.getOnlineUserThreadWithId(receiverId);
			if (receiverThread != null) {
				AbstractMessage forwardMessage = new ForwardMessage(AES.getCipherTimeStamp(receiverThread.getKcs()),
						senderId, receiverId, datagram, 0);
				sendMessage(receiverThread.getSocket(), forwardMessage, receiverThread.getKcs(),
						Datagram.MessageEncryptType.AES);
			} else {
				AbstractMessage reply = new UserOfflineResponse(AES.getCipherTimeStamp(kcs), receiverId);
				sendMessage(socket, reply, kcs, Datagram.MessageEncryptType.AES);
			}
		} else {
			Server receiverThread = ServerDao.getOnlineUserThreadWithId(receiverId);
			if (receiverThread.getChatSocket() != null) {
				AbstractMessage forwardMessage = new ForwardMessage(AES.getCipherTimeStamp(receiverThread.getChatKcs()),
						senderId, receiverId, datagram, 1);
				sendMessage(receiverThread.getChatSocket(), forwardMessage, receiverThread.getChatKcs(),
						Datagram.MessageEncryptType.AES);
			} else {
				AbstractMessage reply = new UserOfflineResponse(AES.getCipherTimeStamp(chatkcs), receiverId);
				sendMessage(socket, reply, chatkcs, Datagram.MessageEncryptType.AES);
			}
		}
	}
}
