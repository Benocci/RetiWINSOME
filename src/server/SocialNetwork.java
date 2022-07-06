package server;

import exception.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta il socialnetwork dall'interno, mantiene le seguenti strutture dati:
 *               - users: associa ad ogni username lo user corrispondente
 *               - followersMap: associa ad ogni username la lista di tutti gli username degli utenti che lo seguono
 *               - followingMap: associa ad ogni username la lista di tutti gli username che lui segue
 *               - postMap: associa ad ogni ID univoco un post all'interno del socialnetwork
 */
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

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna true se username appartiene a users, false altrimenti
     */
    public Boolean userExist(String username){
        return users.containsKey(username);
    }

    /*
     * REQUIRES: to_add != null
     * MODIFIES: this
     * EFFECTS: aggiunge to_add agli utenti del social
     * THROWS:
     */
    public void addUser(User to_add){
        users.putIfAbsent(to_add.getUsername(), to_add);
        followersMap.putIfAbsent(to_add.getUsername(), new ArrayList<>());
        followingMap.putIfAbsent(to_add.getUsername(), new ArrayList<>());
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna l'utente con nome username
     * THROWS:
     */
    public User getUser(String username){
        return users.get(username);
    }

    /*
     * REQUIRES: user != null
     * EFFECTS: ritorna un arraylist di stringe con gli username degli utenti che hanno almeno un tag in comune con user
     * THROWS:
     */
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

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna l'intera lista di utenti mappata
     * THROWS:
     */
    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getFollowersMap() {
        return followersMap;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getFollowingMap() {
        return followingMap;
    }

    public void setUsers(ConcurrentHashMap<String, User> users) {
        if(users != null){
            this.users = users;
        }
    }

    public void setPostMap(ConcurrentHashMap<Integer, Post> postMap) {
        if(postMap != null){
            this.postMap = postMap;
            this.post_id.addAndGet(postMap.keySet().size());
        }
    }

    public void setFollowersMap(ConcurrentHashMap<String, ArrayList<String>> followersMap) {
        if(followersMap != null){
            this.followersMap = followersMap;
        }
    }

    public void setFollowingMap(ConcurrentHashMap<String, ArrayList<String>> followingMap) {
        if(followingMap != null){
            this.followingMap = followingMap;
        }
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna un arraylist di stringe con gli username che seguono username
     * THROWS:
     */
    public ArrayList<String> getFollowers(String username) {
        return followersMap.get(username);
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna un arraylist di stringe con gli username che sono seguiti da username
     * THROWS:
     */
    public ArrayList<String> getFollowing(String username) {
        return followingMap.get(username);
    }

    /*
     * REQUIRES: username != null ∧ to_follow != null
     * MODIFIES: this
     * EFFECTS: aggiunge username ai follower di to_follow e to_follow ai seguiti di username
     * THROWS: UserNotExistException se gli utenti non sono presenti nel socialnewtok
     */
    public void followUser(String username, String to_follow) throws UserNotExistException {
        if(!users.containsKey(username) || !users.containsKey(to_follow)){
            throw new UserNotExistException();
        }

        followersMap.get(to_follow).add(username);
        followingMap.get(username).add(to_follow);
    }

    /*
     * REQUIRES: username != null ∧ to_unfollow != null
     * MODIFIES: this
     * EFFECTS: rimuove username ai follower di to_unfollow e to_unfollow ai seguiti di username
     * THROWS: UserNotExistException se gli utenti non sono presenti nel socialnewtok
     */
    public void unfollowUser(String username, String to_unfollow) throws UserNotExistException {
        if (!users.containsKey(username) || !users.containsKey(to_unfollow)) {
            throw new UserNotExistException();
        }

        followersMap.get(to_unfollow).remove(username);
        followingMap.get(username).remove(to_unfollow);
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna un arraylist con tutti i post di cui username è autore
     * THROWS: UserNotExistException se username non è presente nel socialnewtok
     */
    public ArrayList<Post> viewBlog(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        ArrayList<Post> to_return = new ArrayList();

        for (Post p: postMap.values()) {
            if(p.getAuthor().equals(username)){
                to_return.add(p);
            }
            else if(p.getRewinUsers().contains(username)){
                to_return.add(p);
            }
        }

        return to_return;
    }

    /*
     * REQUIRES: username != null ∧ title != null ∧ content != null
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge un nuovo post
     * THROWS: UserNotExistException se username non è presente nel socialnewtok
     */
    public void addPost(String username, String title, String content) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        int id = post_id.getAndIncrement();
        postMap.putIfAbsent(id, new Post(id, username, title, content));
    }

    /*
     * REQUIRES: username != null ∧ id_post >= 0 ∧ vote_value = -1/+1
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge il voto "vote_value" al post con ID id_post
     * THROWS:  UserNotExistException se username non è presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          VoteNotValidException se il voto non è -1/+1
     *          SameUserException se username è uguale all'autore del post
     */
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

    /*
     * REQUIRES: username != null ∧ id_post >= 0 ∧ content != null
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge il commento al post con ID id_post
     * THROWS:  UserNotExistException se username non è presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          SameUserException se username è uguale all'autore del post
     */
    public void commentPost(int id_post, String username, String content) throws SameUserException, UserNotExistException, PostNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        postMap.get(id_post).addComment(username, content);
    }

    /*
     * REQUIRES: username != null ∧ id_post >= 0
     * MODIFIES: this.postMap
     * EFFECTS: rimuove il post con ID id_post dal social
     * THROWS:  NoAutorityException se username non è l'autore del post
     *          PostNotExistException se il post indicato ta id_post non esiste
     */
    public void removePost(int id_post,String username) throws PostNotExistException, NoAuthorizationException {
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        if(!postMap.get(id_post).getAuthor().equals(username)){
            throw new NoAuthorizationException();
        }

        postMap.remove(id_post);
    }

    /*
     * REQUIRES: username != null ∧ id_post >= 0
     * MODIFIES: this.postMap
     * EFFECTS: fa il rewin del post con ID id_post da parte di username
     * THROWS:  UserNotExistException se username non è presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          SameUserException se username è uguale all'autore del post
     */
    public void rewinPost(int id_post, String username) throws PostNotExistException, SameUserException, UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        postMap.get(id_post).addRewin(username);
    }

    /*
     * REQUIRES: id_post >= 0
     * EFFECTS: ritorna il post con ID id_post
     * THROWS: PostNotExistException se il post indicato ta id_post non esiste
     */
    public Post getPost(int id_post) throws PostNotExistException {
        if(!postMap.containsKey(id_post)) {
            throw new PostNotExistException();
        }

        return postMap.get(id_post);
    }

    /*
     * EFFECTS: ritorna postMap
     */
    public ConcurrentHashMap<Integer, Post> getPostMap() {
        return postMap;
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna il wallet di username
     * THROWS: UserNotExistException se username non è presente nel socialnewtok
     */
    public Wallet getWallet(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        return users.get(username).getWallet();
    }
}
