package server;

import exception.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SocialNetwork {
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, ArrayList<String>> followersMap;
    private ConcurrentHashMap<String, ArrayList<String>> followingMap;
    private ConcurrentHashMap<Integer, Post> postMap;

    private AtomicInteger post_id;

    public SocialNetwork(){
        users = new ConcurrentHashMap<>();
        followersMap = new ConcurrentHashMap<>();
        followingMap = new ConcurrentHashMap<>();
        postMap = new ConcurrentHashMap<>();
        post_id = new AtomicInteger();
    }

    public Boolean userExist(String username){
        return users.containsKey(username);
    }

    public void addUser(User to_add){
        users.putIfAbsent(to_add.getUsername(), to_add);
        followersMap.putIfAbsent(to_add.getUsername(), new ArrayList<>());
        followingMap.putIfAbsent(to_add.getUsername(), new ArrayList<>());
    }

    public User getUser(String username){
        return users.get(username);
    }

    public ArrayList<String> listUsers(User user){
        ArrayList<String> to_return = new ArrayList();

        for (User u: users.values()) {
            for (String s: user.getTags()) {
                if(u.getTags().contains(s) && !u.getUsername().equals(user.getUsername())){
                    if(!to_return.contains(u.getUsername())){
                        to_return.add(u.getUsername());
                    }
                }
            }
        }

        return to_return;
    }

    public ArrayList<String> getFollowers(String username) {
        return followersMap.get(username);
    }

    public ArrayList<String> getFollowing(String username) {
        return followingMap.get(username);
    }

    public void followUser(String username, String to_follow) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        followersMap.get(username).add(to_follow);
        followingMap.get(to_follow).add(username);
    }

    public void unfollowUser(String username, String to_unfollow) throws UserNotExistException {
        if (!users.containsKey(username)) {
            throw new UserNotExistException();
        }

        followersMap.get(username).remove(to_unfollow);
        followingMap.get(to_unfollow).remove(username);
    }

    public ArrayList<Post> viewBlog(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        ArrayList<Post> to_return = new ArrayList();

        for (Post p: postMap.values()) {
            if(p.getAuthor().equals(username)){
                to_return.add(p);
            }
        }

        return to_return;
    }

    public void addPost(String username, String title, String content) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        int id = post_id.getAndIncrement();
        postMap.putIfAbsent(id, new Post(id, username, title, content));
    }

    public void ratePost(int id_post, String username, int vote_value) throws UserNotExistException, PostNotExistException, VoteNotValidException, SameUserException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }
        if(vote_value != -1 && vote_value != 1){
            throw new VoteNotValidException();
        }

        postMap.get(id_post).addVote(username, vote_value);
    }

    public void commentPost(int id_post, String username, String content) throws SameUserException, UserNotExistException, PostNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        postMap.get(id_post).addComment(username, content);
    }

    public void removePost(int id_post) throws PostNotExistException {
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        postMap.remove(id_post);
    }

    public void rewinPost(int id_post, String username) throws PostNotExistException, SameUserException {
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        postMap.get(id_post).addRewin(username);
    }

    public Post getPost(int id_post) throws PostNotExistException {
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        return postMap.get(id_post);
    }


    public Wallet getWallet(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        return users.get(username).getWallet();
    }
}
