package org.cis120;

import java.util.List;
import java.util.LinkedList;

/**
 * The {@code CommandParser} class includes a single static method that can
 * convert a
 * string into the appropriate {@link Command} object that it represents.
 *
 * You do not need to modify this file.
 */
public final class CommandParser {

    /**
     * Parses a string command received from a client into its component parts, and
     * creates a
     * {@link Command} object representing it.
     *
     * @param senderId      The backend-generated ID for the sender of the command
     * @param sender        The current username of the sender
     * @param commandString The command string to parse
     * @return a subclass of {@link Command} corresponding to the string
     * @throws IllegalArgumentException if the commandString is syntactically
     *                                  invalid, meaning that
     *                                  it is of an unrecognized type or its
     *                                  components do not match its type.
     *
     */
    public static Command parse(int senderId, String sender, String commandString) {
        CommandType commandType = null;
        List<String> parameters = new LinkedList<>();
        String payload = null;
        int index;

        while ((index = commandString.indexOf(' ')) > 0) {
            if (commandString.startsWith(":")) {
                payload = commandString.substring(1);
                commandString = "";
                break;
            } else {
                String token = commandString.substring(0, index);
                if (commandType == null) {
                    commandType = CommandType.valueOf(token);
                } else {
                    parameters.add(token);
                }
                commandString = commandString.substring(index + 1);
            }
        }

        if (!commandString.isEmpty()) {
            if (commandString.startsWith(":")) {
                payload = commandString.substring(1);
            } else if (commandType == null) {
                commandType = CommandType.valueOf(commandString);
            } else {
                parameters.add(commandString);
            }
        }

        if (commandType == null) {
            throw new IllegalArgumentException("Unknown command type");
        } else if (parameters.size() > 2) {
            throw new IllegalArgumentException("Too many parameters");
        }

        String param0 = parameters.size() >= 1 ? parameters.get(0) : null;
        String param1 = parameters.size() >= 2 ? parameters.get(1) : null;

        switch (commandType) {
            case CREATE:
                boolean isInviteOnly;
                if ("1".equals(param1)) {
                    isInviteOnly = true;
                } else if ("0".equals(param1)) {
                    isInviteOnly = false;
                } else {
                    return null;
                }
                return new CreateCommand(senderId, sender, param0, isInviteOnly);
            case INVITE:
                return new InviteCommand(senderId, sender, param0, param1);
            case JOIN:
                return new JoinCommand(senderId, sender, param0);
            case KICK:
                return new KickCommand(senderId, sender, param0, param1);
            case LEAVE:
                return new LeaveCommand(senderId, sender, param0);
            case MESG:
                return new MessageCommand(senderId, sender, param0, payload);
            case NICK:
                return new NicknameCommand(senderId, sender, param0);
            default:
                return null;
        }
    }

    private enum CommandType {
        CREATE, INVITE, JOIN, KICK, LEAVE, MESG, NICK
    }

    // Prevents the instantiation of any CommandParser objects,
    // which would be nonsensical.
    private CommandParser() {
    }
}
