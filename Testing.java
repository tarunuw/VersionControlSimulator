import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;
import java.util.*;

public class Testing {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    @Test 
    @Timeout(1)
    @DisplayName("Sync Test - moving data from a repo to another empty repo")
    public void syncEmpty() throws InterruptedException{
        commitAll(repo2, new String[]{"Tarun", "Tamilselvan"});
        assertEquals(0, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        repo1.synchronize(repo2);

        assertEquals(2, repo1.getRepoSize(), "Repo2 should be added to Repo1");
        assertEquals(0, repo2.getRepoSize(), "Repo2 should become empty");
        testHistory(repo1, 2, new String[]{"Tarun", "Tamilselvan"});
    }

    @Test
    @Timeout(1)
    @DisplayName("Sync Test - merging two filled repos")
    public void syncMerge() throws InterruptedException{
        commitAll(repo2, new String[]{"Jan", "Feb"});
        commitAll(repo1, new String[]{"Mar", "Apr"});

        repo1.synchronize(repo2);

        assertEquals(4, repo1.getRepoSize(), "should have the sum of the sizes");
        assertEquals(0, repo2.getRepoSize(), "should become empty");

        String[] merge = new String[]{"Jan", "Feb", "Mar", "Apr"};
        testHistory(repo1, 4, merge);
    }   

    @Test
    @Timeout(1)
    @DisplayName("Sync Test - new commits adds to the front")
    public void syncNewFrontCommits() throws InterruptedException{
        commitAll(repo1, new String[]{"Jan 24", "Feb 24"});
        commitAll(repo2, new String[]{"Jan 25", "Feb 25", "Mar 25"});

        repo1.synchronize(repo2);

        assertEquals(5, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        String[] merge = new String[]{"Jan 24", "Feb 24", "Jan 25", "Feb 25", "Mar 25"};
        testHistory(repo1, 5, merge);
    }

    @Test
    @Timeout(1)
    @DisplayName("Sync Test - order of commits")
    public void syncOrderCommits() throws InterruptedException{
        commitAll(repo1, new String[]{"Monday", "Tuesday"});
        commitAll(repo2, new String[]{"Wednesday", "Thursday"});

        repo1.synchronize(repo2);

        assertEquals(4, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        String[] merge = new String[]{"Monday","Tuesday","Wednesday","Thursday"};
        testHistory(repo1, 4, merge);
    }      



    /////////////////////////////////////////////////////////////////////////////////
    // PROVIDED HELPER METHODS (You don't have to use these if you don't want to!) //
    /////////////////////////////////////////////////////////////////////////////////

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. 
    // repo and messages should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. repo and allCommits should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
