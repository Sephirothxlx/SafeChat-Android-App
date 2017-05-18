package com.safechat.safechat.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.safechat.safechat.user.Friend;
import com.safechat.safechat.user.ServerUser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.Key;
import java.util.ArrayList;

public class UserDao {
    private static final String USER_INFO_PATH = "res/user.xml";
    private static final String USER_KEY_PATH_PREFIX = "res/key/";
    private static ArrayList<ServerUser> userList;

    public UserDao() {
    }

    public static void init() {
        userList = new ArrayList<>();
        // read user info from file
        File userInfoFile = new File(USER_INFO_PATH);
        if (!userInfoFile.exists()) {
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(USER_INFO_PATH);
            Node root = document.getDocumentElement();
            NodeList userNodeList = root.getChildNodes();
         
            for (int i = 0; i < userNodeList.getLength(); i++) {
                Node userNode = userNodeList.item(i);
                if (userNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String id = "";
                for (Node node = userNode.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if ("id".equals(node.getNodeName())) {
                            id = node.getFirstChild().getNodeValue();
                        }
                    }
                }
                Key userKey = getUserKeyWithID(id);
                userList.add(new ServerUser(id, userKey));
            }

            for (int i = 0; i < userNodeList.getLength(); i++) {
                Node userNode = userNodeList.item(i);
                if (userNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String id = "";
                for (Node node = userNode.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if ("id".equals(node.getNodeName())) {
                            id = node.getFirstChild().getNodeValue();
                        }
                    }
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if ("friendIDList".equals(node.getNodeName())) {
                            if (node.hasChildNodes()) {
                                for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                        if ("friendID".equals(childNode.getNodeName())) {
                                            String friendID = childNode.getFirstChild().getNodeValue();
                                            Key friendKey = getUserKeyWithID(friendID);
                                            getUserWithID(id).addFriend(new Friend(friendID, friendKey));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("can not load user info");
            e.printStackTrace();
        }
    }
    public static Key getUserKeyWithID(String userID) {
        try {
            File userKeyFile = new File(USER_KEY_PATH_PREFIX+userID+".key");
            if (userKeyFile.exists()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(userKeyFile));
                return  (Key) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("can not find user public key file");
            e.printStackTrace();
        }
        return null;
    }
    public static ServerUser getUserWithID(String id) {
        ServerUser ret = null;
        for (ServerUser user: userList) {
            if (user.getID().equals(id)) {
                ret = user;
            }
        }
        return ret;
    }
    public static void addFriendToUserWithID(String id, Friend friend) {
        ServerUser user = getUserWithID(id);
        user.addFriend(friend);
    }
    public static void addUser(ServerUser user) {
        userList.add(user);
        update();
    }

    private static void update() {
        updateXML();
        updateKey();
    }

    private static void updateKey() {
        for (ServerUser user: userList) {
            try {
                File keyFile = new File(USER_KEY_PATH_PREFIX+user.getID()+".key");
                if (!keyFile.exists()) {
                    keyFile.createNewFile();
                }
                ObjectOutputStream privateKeyOutputStream = new ObjectOutputStream(new FileOutputStream(keyFile));
                privateKeyOutputStream.writeObject(user.getPublicKey());
                privateKeyOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootNode = document.createElement("userList");
            for (ServerUser user: userList) {
                Element userNode = document.createElement("user");
                Element idNode = document.createElement("id");
                idNode.setTextContent(user.getID());
                userNode.appendChild(idNode);
                Element friendIDList = document.createElement("friendIDList");
                for (Friend friendId: user.getFriendList()) {
                    Element friendIdNode = document.createElement("friendID");
                    friendIdNode.setTextContent(friendId.getID());
                    friendIDList.appendChild(friendIdNode);
                }
                userNode.appendChild(friendIDList);
                rootNode.appendChild(userNode);
            }
            document.appendChild(rootNode);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter pw = new PrintWriter(new FileOutputStream(USER_INFO_PATH));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | IllegalArgumentException | FileNotFoundException | TransformerException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void addFriendRelation(String aId, String bId) {
        addFriendToUserWithID(aId, new Friend(bId, getUserKeyWithID(bId)));
        addFriendToUserWithID(bId, new Friend(aId, getUserKeyWithID(aId)));
        update();
    }

    public static ArrayList<Friend> getFriendListWithId(String userId) {
        return getUserWithID(userId).getFriendList();
    }
    public static boolean searchUser(String id){
        for (ServerUser user: userList) {
           if(user.getID().equals(id)){
               return true;
           }
        }
        return false;
    }
}
