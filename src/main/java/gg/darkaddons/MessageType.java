package gg.darkaddons;

enum MessageType {
    STANDARD_TEXT_MESSAGE((byte) 0),
    SYSTEM_MESSAGE_DISPLAYED_AS_STANDARD_TEXT_MESSAGE((byte) 1),
    STATUS_MESSAGE_DISPLAYED_ABOVE_ACTIONBAR((byte) 2);

    private final byte internalId;

    private MessageType(final byte internalIdIn) {
        this.internalId = internalIdIn;
    }

    final boolean matches(final byte type) {
        return this.internalId == type;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public final String toString() {
        return "MessageType{" +
                "internalId=" + this.internalId +
                '}';
    }
}
