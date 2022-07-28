package org.cis120;

import java.util.*;

/**
 * A {@code Broadcast} stores a mapping from users to a list of responses.
 *
 * The server backend uses a broadcast to send messages to clients to inform
 * them of relevant events in their channels. For instance, many chat services
 * inform you when someone has been removed from a chat that you're in.
 *
 * You do not need to modify this file.
 */
public final class Broadcast {

    private final Map<String, List<String>> responses;

    // Hide constructor so Broadcasts can only be created via one of the
    // static factory methods below.
    private Broadcast() {
        responses = new TreeMap<>();
    }

    /**
     * Enqueue a response to be sent to the given user nickname.
     *
     * @param nick     The nickname of the user to whom the response should be sent
     * @param response A string encoding of the response
     */
    private void addResponse(String nick, String response) {
        if (!responses.containsKey(nick)) {
            responses.put(nick, new LinkedList<String>());
        }
        List<String> userResponses = responses.get(nick);
        if (!userResponses.contains(response)) {
            userResponses.add(response);
        }
    }

    // ==========================================================================
    // Factory methods
    // ==========================================================================

    /**
     * Creates a {@code Broadcast} for the general case where a client's
     * {@link Command} is accepted
     * by the server and should be relayed to the appropriate clients.
     *
     * @param command    The {@link Command} whose effects to broadcast
     * @param recipients The set of user names of clients who should receive the
     *                   broadcast from the
     *                   server
     * @return A {@code Broadcast} representing a set of responses to send
     */
    public static Broadcast okay(Command command, Collection<String> recipients) {
        Broadcast broadcast = new Broadcast();
        for (String recipient : recipients) {
            broadcast.addResponse(recipient, command.toString());
        }

        // Need to send response to user ID associated with *new* nick
        if (command instanceof NicknameCommand) {
            broadcast.responses.remove(command.getSender());
            NicknameCommand nickCommand = (NicknameCommand) command;
            broadcast.addResponse(nickCommand.getNewNickname(), command.toString());
        }
        return broadcast;
    }

    /**
     * Creates a {@code Broadcast} for the case where a client's {@link Command} is
     * invalid, and the
     * client should be informed.
     *
     * @param command The command which caused the error
     * @param error   The {@link ServerResponse} that the command caused
     * @return A {@code Broadcast} representing the response to send
     * @throws IllegalArgumentException if error value is {@code OKAY}
     */
    public static Broadcast error(Command command, ServerResponse error) {
        if (error == ServerResponse.OKAY) {
            throw new IllegalArgumentException("Invalid error type");
        }
        Broadcast broadcast = new Broadcast();
        String recipient = command.getSender();
        int errorCode = error.getCode();
        String response = String.format(":%s ERROR %d", recipient, errorCode);
        broadcast.addResponse(recipient, response);
        return broadcast;
    }

    /**
     * Creates a {@code Broadcast} for the case when a user first connects to the
     * server and should
     * be informed of their new nickname
     *
     * @param recipient The automatically generated nickname for the client
     * @return A {@code Broadcast} to the new client
     */
    public static Broadcast connected(String recipient) {
        Broadcast broadcast = new Broadcast();
        String response = String.format(":%s CONNECT", recipient);
        broadcast.addResponse(recipient, response);
        return broadcast;
    }

    /**
     * Creates a {@code Broadcast} for the case when a user disconnects from the
     * server and other
     * clients should be informed of this fact.
     * 
     * @param user       The nickname of the disconnected user
     * @param recipients A set of nicknames of clients who should be informed of the
     *                   user's
     *                   disconnection. Should not include the user who
     *                   disconnected.
     * @return A {@code Broadcast} representing the response to send
     */
    public static Broadcast disconnected(String user, Collection<String> recipients) {
        if (recipients.contains(user)) {
            throw new IllegalArgumentException("Disconnected user in broadcast");
        }
        Broadcast broadcast = new Broadcast();
        String response = String.format(":%s QUIT", user);
        for (String recipient : recipients) {
            broadcast.addResponse(recipient, response);
        }
        return broadcast;
    }

    /**
     * A specialized method for creating a {@code Broadcast} in the event that a
     * user is added to a
     * channel as the result of a {@link JoinCommand} or {@link InviteCommand}. The
     * resulting
     * Broadcast informs the relevant client of the nicknames of all other users in
     * the channel.
     *
     * @param command    The command issued by the client (Invite or Join)
     * @param recipients A set of nicknames of the other users in the channel which
     *                   the user is
     *                   joining, and to whom the command should be relayed.
     * @param owner      The nickname of the channel's owner
     * @return A {@code Broadcast} representing the responses to send
     * @throws IllegalArgumentException if {@code command} is not an instanceof
     *                                  {@link JoinCommand}
     *                                  or {@link InviteCommand}
     */
    public static Broadcast names(Command command, Collection<String> recipients, String owner) {
        // Relay JOIN or INVITE normally
        Broadcast broadcast = Broadcast.okay(command, recipients);

        // Also relay NAMES to user who joins channel
        String channelName, userToAdd;
        if (command instanceof JoinCommand) {
            JoinCommand joinCommand = (JoinCommand) command;
            channelName = joinCommand.getChannel();
            userToAdd = joinCommand.getSender();
        } else if (command instanceof InviteCommand) {
            InviteCommand inviteCommand = (InviteCommand) command;
            channelName = inviteCommand.getChannel();
            userToAdd = inviteCommand.getUserToInvite();
        } else {
            throw new IllegalArgumentException("Invalid command type");
        }
        String namesPayload = createNamesPayload(owner, recipients);
        String namesResponse = String
                .format(":%s NAMES %s :%s", userToAdd, channelName, namesPayload);
        broadcast.addResponse(userToAdd, namesResponse);
        return broadcast;
    }

    // ==========================================================================
    // Response dispatch
    // ==========================================================================

    /**
     * You should not call this method yourself. Associates the stored responses
     * with the user IDs
     * of the recipients. This * function will be called by the
     * {@link ServerBackend} before
     * dispatching the {@code Broadcast}.
     * 
     * @param model A data structure representing the state of the server
     * @return a mapping from user ID to a list of response strings that should be
     *         delivered by the
     *         {@link ServerBackend}
     */
    public Map<Integer, List<String>> getResponses(ServerModel model) {
        Map<Integer, List<String>> userIdResponses = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : responses.entrySet()) {
            int userId = model.getUserId(entry.getKey());
            userIdResponses.put(userId, entry.getValue());
        }
        return userIdResponses;
    }

    // ==========================================================================
    // Private utility methods
    // ==========================================================================

    /**
     * Generates a payload for a {@code NAMES} response for some channel, given the
     * nickname of the
     * channel's owner, and a collection of the nicknames of the users in the
     * channel (including the
     * owner).
     * 
     * @param owner the channel owner's nickname
     * @param nicks the nicknames of the channel's users, including the owner
     * @return a string payload containing the nicks specified
     * @throws IllegalArgumentException if any argument is {@code null}, or if
     *                                  {@code nicks} does
     *                                  not contain {@code owner}
     */
    private static String createNamesPayload(String owner, Collection<String> nicks) {
        if (owner == null || nicks == null || !nicks.contains(owner)) {
            throw new IllegalArgumentException();
        }

        List<String> nicksList = new LinkedList<>(nicks);
        Collections.sort(nicksList);
        StringBuilder payload = new StringBuilder();
        for (String nick : nicksList) {
            if (nick.equals(owner)) {
                payload.append('@');
            }
            payload.append(nick);
            payload.append(' ');
        }
        return payload.toString().trim();
    }

    // ==========================================================================
    // Overrides from Object
    // ==========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Broadcast)) {
            return false;
        }
        Broadcast that = (Broadcast) o;
        return this.responses.equals(that.responses);
    }

    @Override
    public int hashCode() {
        return responses.hashCode();
    }

    @Override
    public String toString() {
        return responses.toString();
    }

}
