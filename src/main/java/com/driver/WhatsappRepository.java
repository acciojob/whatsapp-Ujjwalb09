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

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        Group group  = null;
        int count_msg = 0;
        int res = 0;
        for(Group g : groupUserMap.keySet()){
            List<User> users = groupUserMap.get(g);
            if(users.contains(user)){
                group = g;

                break;
            }
        }
        if(group == null){
            throw new Exception("User not found");
        }
        else if(adminMap.get(group).equals(user)){
            throw new Exception("Cannot remove admin");
        }
        else{
            for(Message m : senderMap.keySet()){
                if(senderMap.get(m).equals(user)){
                    for(Group g : groupMessageMap.keySet()){
                        List<Message> msg  = groupMessageMap.get(g);
                        if(msg.contains(m)){ msg.remove(m);
                            count_msg++;
                        }
                    }

                    senderMap.remove(m);
                }
            }
            groupUserMap.get(group).remove(user);

            res+=groupUserMap.get(group).size();
            res+=groupMessageMap.get(group).size();
            res = res + (this.messageId-count_msg);
            return res;
        }

    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> messages = new ArrayList<>(senderMap.keySet());
        for(Message m : messages){
            if(m.getTimestamp().compareTo(start) <= 0 || m.getTimestamp().compareTo(end) >= 0){
                messages.remove(m);
            }
        }
        if(messages.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(messages ,(a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        return messages.get(messages.size()-K).getContent();
    }
}
