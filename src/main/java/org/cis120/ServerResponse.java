package org.cis120;

/**
 * {@code ServerResponse} is an enumerated type that lists all possible
 * response codes a server can send to a client. Each response has
 * both a string and integer representation.
 *
 * You do not need to modify this file.
 */
public enum ServerResponse {

    /**
     * Response indicating no error has occurred.
     */
    OKAY(200),

    /**
     * Response by the server when a user tries to change his/her
     * nickname to an invalid string.
     */
    INVALID_NAME(401),

    /**
     * Response by the server when the channel specified in a command
     * is not recognized by the server.
     */
    NO_SUCH_CHANNEL(402),

    /**
     * Response by the server when the target specified in the command
     * is not registered on the server.
     */
    NO_SUCH_USER(403),

    /**
     * Response by the server when a client attempts to send a command
     * requiring the sender to be in the specified channel to a
     * channel in which he or she is not a member.
     */
    USER_NOT_IN_CHANNEL(404),

    /**
     * Response by the server when a client which does not own a
     * channel attempts to send a command that is restricted to the
     * owner; currently, the only such command is KICK.
     */
    USER_NOT_OWNER(406),

    /**
     * Response by the server when a client attempts to join a channel
     * to which he or she has not been invited.
     */
    JOIN_PRIVATE_CHANNEL(407),

    /**
     * Response by the server when a client attempts to issue an
     * invite to a channel that is not invite-only.
     */
    INVITE_TO_PUBLIC_CHANNEL(408),

    /**
     * Response by the server when a client attempts to change his or
     * her nick to a nickname that is already in use by another user.
     */
    NAME_ALREADY_IN_USE(500),

    /**
     * Response by the server when a client attempts to create a
     * channel whose name is already used by another channel on the
     * server.
     */
    CHANNEL_ALREADY_EXISTS(501);

    // The integer associated with this enum value
    private final int value;

    ServerResponse(int value) {
        this.value = value;
    }

    /** @return the code associated with this response. */
    public int getCode() {
        return value;
    }

}
