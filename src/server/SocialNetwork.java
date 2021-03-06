package server;

import exception.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> followersMap;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> followingMap;
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
     * RETURN: true se va a buon fine false altrimenti
     */
    public boolean addUser(User to_add){
        if(users.putIfAbsent(to_add.getUsername(), to_add) == null){
            followersMap.putIfAbsent(to_add.getUsername(), new ConcurrentLinkedQueue<>());
            followingMap.putIfAbsent(to_add.getUsername(), new ConcurrentLinkedQueue<>());
            return true;
        }

        return false;
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna l'utente con nome username
     */
    public User getUser(String username){
        return users.get(username);
    }


    /*
     * REQUIRES: user != null
     * EFFECTS: ritorna una coda di stringe con gli username degli utenti che hanno almeno un tag in comune con user
     * RETURNS: ConcurrentLinkedQueue<String>
     */
    public ConcurrentLinkedQueue<String> listUsers(User user){
        ConcurrentLinkedQueue<String> to_return = new ConcurrentLinkedQueue<>();

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
     * EFFECTS: metodo getter della mappa di utenti
     * RETURNS: ConcurrentHashMap<String, User>
     */
    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    /*
     * EFFECTS: metodo getter della mappa dei followers
     * RETURNS: ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>
     */
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getFollowersMap() {
        return followersMap;
    }

    /*
     * EFFECTS: metodo getter della mappa dei seguiti
     * RETURNS: ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>
     */
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getFollowingMap() {
        return followingMap;
    }

    /*
     * EFFECTS: metodo getter della mappa dei post
     * RETURNS: ConcurrentHashMap<String, Post>
     */
    public ConcurrentHashMap<Integer, Post> getPostMap() {
        return postMap;
    }


    /*
     * REQUIRES: users != null
     * MODIFIES: this.users
     * EFFECTS: metodo setter della mappa
     */
    public void setUsers(ConcurrentHashMap<String, User> users) {
        if(users != null){
            this.users = users;
        }
    }

    /*
     * REQUIRES: postMap != null
     * MODIFIES: this.postMap
     * EFFECTS: metodo setter della mappa
     */
    public void setPostMap(ConcurrentHashMap<Integer, Post> postMap) {
        if(postMap != null){
            this.postMap = postMap;
            this.post_id.addAndGet(getLastIdPost(postMap)+1);
        }
    }

    /*
     * REQUIRES: postMap != null
     * RETURNS: intero con l'ultimo id_post della mappa
     */
    private int getLastIdPost(ConcurrentHashMap<Integer, Post> postMap){
        int to_ret = 0;

        for (Integer i:  postMap.keySet()) {
            if(i > to_ret){
                to_ret = i;
            }
        }

        return to_ret;
    }

    /*
     * REQUIRES: followersMap != null
     * MODIFIES: this.followersMap
     * EFFECTS: metodo setter della mappa
     */
    public void setFollowersMap(ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> followersMap) {
        if(followersMap != null){
            this.followersMap = followersMap;
        }
    }

    /*
     * REQUIRES: followingMap != null
     * MODIFIES: this.followingMap
     * EFFECTS: metodo setter della mappa
     */
    public void setFollowingMap(ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> followingMap) {
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
        return new ArrayList<>(followersMap.get(username));
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna un arraylist di stringe con gli username che sono seguiti da username
     * THROWS:
     */
    public ArrayList<String> getFollowing(String username) {
        return new ArrayList<>(followingMap.get(username));
    }

    /*
     * REQUIRES: username != null ??? to_follow != null
     * MODIFIES: this
     * EFFECTS: aggiunge username ai follower di to_follow e to_follow ai seguiti di username
     * THROWS: UserNotExistException se gli utenti non sono presenti nel socialnewtok
     */
    public void followUser(String username, String to_follow) throws UserNotExistException, AlreadyFollowerException {
        if(!users.containsKey(username) || !users.containsKey(to_follow)){
            throw new UserNotExistException();
        }

        if(followingMap.get(username).contains(to_follow)){
            throw new AlreadyFollowerException();
        }

        followersMap.get(to_follow).add(username);
        followingMap.get(username).add(to_follow);

    }

    /*
     * REQUIRES: username != null ??? to_unfollow != null
     * MODIFIES: this
     * EFFECTS: rimuove username ai follower di to_unfollow e to_unfollow ai seguiti di username
     * THROWS: UserNotExistException se gli utenti non sono presenti nel socialnewtok
     */
    public void unfollowUser(String username, String to_unfollow) throws UserNotExistException, AlreadyFollowerException {
        if (!users.containsKey(username) || !users.containsKey(to_unfollow)) {
            throw new UserNotExistException();
        }

        if(!followingMap.get(username).contains(to_unfollow)){
            throw new AlreadyFollowerException();
        }

        followersMap.get(to_unfollow).remove(username);
        followingMap.get(username).remove(to_unfollow);
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna una coda con tutti i post di cui username ?? autore
     * THROWS: UserNotExistException se username non ?? presente nel socialnewtok
     */
    public ConcurrentLinkedQueue<Post> viewBlog(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        ConcurrentLinkedQueue<Post> to_return = new ConcurrentLinkedQueue<>();

        for (Post p: postMap.values()) {
            if(p.getAuthor().equals(username)){
                to_return.add(p);
            }
        }

        return to_return;
    }

    /*
     * REQUIRES: username != null ??? title != null ??? content != null
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge un nuovo post
     * THROWS: UserNotExistException se username non ?? presente nel socialnewtok
     */
    public void addPost(String username, String title, String content, String rewinAuthor, int id_rewin) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        int id = post_id.getAndIncrement();
        postMap.putIfAbsent(id, new Post(id, username, title, content, rewinAuthor, id_rewin));
    }

    /*
     * REQUIRES: id_post >= 0 && username != null
     * EFFECTS: controlla se il post id_post ?? nel feed di username
     * THROWS: UserNotExistException se sollevata da viewBlog
     * RETURNS: true se il post appartiene al feed, false altrimenti
     */
    public boolean postInFeed(int id_post, String username) throws UserNotExistException {
        ArrayList<String> followingList = new ArrayList<>(followingMap.get(username));

        for (String s: followingList) {
            ConcurrentLinkedQueue<Post> authorPost = viewBlog(s);

            for (Post p: authorPost) {
                if(p.getId() == id_post){
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * REQUIRES: username != null ??? id_post >= 0 ??? vote_value = -1/+1
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge il voto "vote_value" al post con ID id_post
     * THROWS:  UserNotExistException se username non ?? presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          VoteNotValidException se il voto non ?? -1/+1
     *          SameUserException se username ?? uguale all'autore del post
     */
    public boolean ratePost(int id_post, String username, int vote_value) throws UserNotExistException, PostNotExistException, VoteNotValidException, SameUserException {
        Post post = postMap.get(id_post);
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(post == null) {
            throw new PostNotExistException();
        }
        if(vote_value != -1 && vote_value != 1){
            throw new VoteNotValidException();
        }

        if(post.getVotes().containsKey(username)){
            return false;
        }
        else{
            post.addVote(username, vote_value);
            return true;
        }
    }

    /*
     * REQUIRES: username != null ??? id_post >= 0 ??? content != null
     * MODIFIES: this.postMap
     * EFFECTS: aggiunge il commento al post con ID id_post
     * THROWS:  UserNotExistException se username non ?? presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          SameUserException se username ?? uguale all'autore del post
     */
    public void commentPost(int id_post, String username, String content) throws SameUserException, UserNotExistException, PostNotExistException {
        Post post = postMap.get(id_post);
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(post == null) {
            throw new PostNotExistException();
        }

        post.addComment(username, content);
    }

    /*
     * REQUIRES: username != null ??? id_post >= 0
     * MODIFIES: this.postMap
     * EFFECTS: rimuove il post con ID id_post dal social
     * THROWS:  NoAutorityException se username non ?? l'autore del post
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
     * REQUIRES: username != null ??? id_post >= 0
     * MODIFIES: this.postMap
     * EFFECTS: fa il rewin del post con ID id_post da parte di username
     * THROWS:  UserNotExistException se username non ?? presente nel socialnewtok
     *          PostNotExistException se il post indicato ta id_post non esiste
     *          SameUserException se username ?? uguale all'autore del post
     */
    public void rewinPost(int id_post, String username) throws PostNotExistException, SameUserException, UserNotExistException {
        Post post = postMap.get(id_post);
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }
        if(post == null) {
            throw new PostNotExistException();
        }

        AtomicBoolean ex = new AtomicBoolean(false);
        postMap.computeIfPresent(id_post, (id, post1) -> {
            try {
                post.addRewin(username);
                addPost(username, post.getTitle(), post.getContent(), post.getAuthor(), id_post);
            } catch (SameUserException e) {
                ex.set(true);
            } catch (UserNotExistException ignore){
                ;
            }

            return post1;
        });

        if(ex.get()){
            throw new SameUserException();
        }

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
     * REQUIRES: username != null
     * EFFECTS: ritorna il wallet di username
     * THROWS: UserNotExistException se username non ?? presente nel socialnewtok
     */
    public Wallet getWallet(String username) throws UserNotExistException {
        if(!users.containsKey(username)){
            throw new UserNotExistException();
        }

        return users.get(username).getWallet();
    }
}
