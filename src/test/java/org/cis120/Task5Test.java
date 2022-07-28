package org.cis120;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.TreeSet;

/**
 * These tests are provided for testing the server's handling of invite-only
 * channels and kicking
 * users. You can and should use these tests as a model for your own testing,
 * but write your tests
 * in ServerModelTest.java.
 * 
 * Note that "assert" commands used for testing can have a String as their first
 * argument, denoting
 * what the "assert" command is testing.
 * 
 * Remember to check http://junit.sourceforge.net/javadoc/org/junit/Assert.html
 * for assert method
 * documention!
 */
public class Task5Test {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);
    }

    @Test
    public void testInviteByOwner() {
        Command invite = new InviteCommand(0, "User0", "java", "User1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.names(invite, recipients, "User0");
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }

    @Test
    public void testInviteByNonOwner() {
        model.registerUser(2);
        Command inviteValid = new InviteCommand(0, "User0", "java", "User1");
        inviteValid.updateServerModel(model);

        Command inviteInvalid = new InviteCommand(1, "User1", "java", "User2");
        Broadcast expected = Broadcast.error(inviteInvalid, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, inviteInvalid.updateServerModel(model), "error");

        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User2"), "User2 not in channel");
    }

    @Test
    public void testKickOneChannel() {
        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(kick, recipients);
        assertEquals(expected, kick.updateServerModel(model));

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 still in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 still in channel");
    }
}
