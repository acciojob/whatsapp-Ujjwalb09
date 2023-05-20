package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile; //db that will store mobile no.
    private int customGroupCount;
    private int messageId;


    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception {

        //if user mobNo alraedy exist in database thor exception
        if(userMobile.contains(mobile)){
            throw new Exception("User already exist");
        }

        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {

        //if There are 2 users in the list
        if(users.size()==2){
            Group group = new Group();
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(users.size());
            adminMap.put(group, users.get(0));
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        }

        else{ //if there are more than 2 users in list
            String grpNo = "Group" + " "+ customGroupCount;

            Group group = new Group();
            group.setName(grpNo);
            group.setNumberOfParticipants(users.size());
            adminMap.put(group, users.get(0));
            groupUserMap.put(group, users);
            customGroupCount++;
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        }
    }

    public int createMessage(String content) {
        Message message = new Message(messageId, content);
        messageId++;
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }

        else if (!groupUserMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }

        senderMap.put(message, sender);

        List<Message> messages = groupMessageMap.get(group);
        messages.add(message);
        groupMessageMap.put(group, messages);
        return messages.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }

        List<User> list = groupUserMap.get(group);
        if(list.get(0)!=approver){
            throw new Exception("Approver does not have rights");
        }

        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }

        adminMap.put(group, user);
        return "SUCCESS";
    }
}
