import java.util.*;
import java.text.SimpleDateFormat;

// This class is a VersionControlSimulator that users can use to build and maintain repositories.
// It could do this by storing and keeping track of multiple commit versions. Addtionally,
// it also has the function to merge two different repositories into a single one.
public class Repository {
    private String name;
    private Commit commit;

    // This is the contstructor for Repository which accepts a name from the user as
    // a paramenter and assigns it to the Repository object when it is created.
    // An IllegalArgumentException() gets thrown if the name given to the repo is 
    // either null or empty. 
    public Repository (String name){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Need a name for repository");
        }
        this.name = name;
    }

    // This method processes and returns the current head commit for the respository it was
    // called on as a string. It will return null if the current head commit is null.
    public String getRepoHead(){
        if(commit == null){
            return null;
        }
        return commit.id;
    }

    // This method processes and returns the total number of commits for the respository that
    // it was called on as an integar. 
    public int getRepoSize(){
        Commit curr = commit;
        int size = 0;
        while (curr != null){
            curr = curr.past;
            size++;
        }
        return size;
    }

    // This method returns a string consisting information about the current head commit  
    // including the repo's name, the head commit's unique indetifier, the head commit's
    // timestamp, and the head commit's message. 
    // If the head commit is null, then this would return a string saying there are
    // currently no commits.
    public String toString(){
        if (commit == null){
            return this.name + " - " + "No commits";
        }
        return this.name + " - " + "Current head: " + commit.toString();
    }

    // This method accepts a target ID as a parameter and searches through the repository to
    // check if any commits contain the target ID.
    // target ID. 
    // The method will return a boolean value. True: Commit in the repo contians the target ID
    // False: Commit in the repo does not contian the target ID.
    // It will through a IllegalArgumentException() if the target ID is null. 
    public boolean contains (String targetId){
        if(targetId == null){
            throw new IllegalArgumentException("Id cannot be null");
        }
        Commit curr = commit;
        while (curr != null){
            if (curr.id.equals(targetId)){
                return true;
            }
            curr = curr.past;
        }
        return false;
    }

    // This method collects and returns a string that consists of a history of a given 
    // amount of recent commits to the repository that it was called on. It accepts an integar  
    // which is the amount of recent commits in the repository that the method will return. 
    // If given amount is greater than the total amount of commits in the repositories then it 
    // will return all commits for that repository. 
    // The method will throw an IllegalArgumentException if the given amount is either 0
    // or a negative number. 
    // If the head commit is null, then an empty string will be return instead. 
    public String getHistory(int n){
        if (n <= 0){
            throw new IllegalArgumentException("count cannot be negative");
        }
        String history = "";
        if(commit == null){
            return "";
        }
        Commit curr = commit;
        history += curr.toString();
        curr = curr.past;
        for(int i = 1; i < n; i++){
            if(curr != null){
               history += "\n" + curr.toString();
               curr = curr.past; 
            }
        }
        return history;
    }

    // This method creates a new commit for a repository with a given message accepted 
    // as a string through the parameter. 
    // It will also return the ID of the new commit as a string.
    // The method will return a IllegalArgumentException() if the given message is null.
    public String commit(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        commit = new Commit(message, commit);
        return commit.id;
    }

    // This method removes the particular commit with the specific target ID taken in 
    // as String through the parameter. 
    // It return a boolean. True: If commit was dropped successfully. 
    // False: If there was no commit found with the target id.
    // The method will also return an IllegalArgumentException() if the given
    // targetID is null. 
    public boolean drop(String targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("target id cannot be null");
        }
        if (commit != null) {
            if (commit.id.equals(targetId)) {
                commit = commit.past;
                return true;
            }
            Commit curr = commit;
            while (curr.past != null) {
                if (curr.past.id.equals(targetId)) {
                    curr.past = curr.past.past;
                    return true;
                }
                curr = curr.past;
            }
        }
        return false;
    }

    // This method synchronizes the repository it was called on with a 
    // different repository that was passed into it as a parameter. 
    // It does this while maintianing the chronological order of all commits 
    // at the end regarless of their order when they were seperate. 
    // The other respository that was combined with the one the method was called on
    // will be left empty at the end. 
    // The method will throw a IllegalArgumentException() if the other 
    // repository that was passed in as parameter is null. 
    public void synchronize (Repository other){
        if(other == null){
            throw new IllegalArgumentException("Inputed Repository is null");
        }
        if(this.commit == null){
            commit = other.commit;
            other.commit = null;
        }
        if(commit != null && other.commit != null){
            if(commit.timeStamp < other.commit.timeStamp){
                Commit temp = commit;
                commit = other.commit;
                other.commit = other.commit.past;
                commit.past = temp;
                
            }
            Commit curr = this.commit;
            while (curr.past != null && other.commit != null){
                if (curr.past.timeStamp < other.commit.timeStamp){
                    Commit temp2 = other.commit;
                    other.commit = other.commit.past;
                    temp2.past = curr.past;
                    curr.past = temp2;
                    
                }
                curr = curr.past;
            }
            if (other.commit != null){
                curr.past = other.commit;
            }
            other.commit = null;
        }

    }
    //  The commit class represents a single commit in the repository.
    //  Commits are characterized by an identifier, a commit message,
    //  and the time that the commit was made. A commit also stores
    //  a reference to the immediately previous commit if it exists.
    public static class Commit {

        private static int currentCommitID;

        
        // The time, in milliseconds, at which this commit was created.         
        public final long timeStamp;

        
        // A unique identifier for this commit.
        public final String id;

        
        // A message describing the changes made in this commit.       
        public final String message;

        
        // A reference to the previous commit, if it exists. Otherwise, null.
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        
        // Resets the IDs of the commit nodes such that they reset to 0.
        // Primarily for testing purposes.
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}