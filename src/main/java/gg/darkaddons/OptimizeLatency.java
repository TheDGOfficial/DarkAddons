package gg.darkaddons;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelException;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.epoll.EpollSocketChannelConfig;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

//import java.net.Socket;
//import java.net.SocketException;

/**
 * A class to optimize latency of Netty channels.
 */
public final class OptimizeLatency {
    // https://github.com/torvalds/linux/blob/master/include/uapi/linux/ip.h
    private static final int IPTOS_LOWDELAY = 0x10;
    private static final int IPTOS_THROUGHPUT = 0x08;
    private static final int IPTOS_RELIABILITY = 0x04;
    private static final int IPTOS_MINCOST = 0x02;

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private OptimizeLatency() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * Call this method right after creating a new {@link java.net.Socket}.
     * <p>
     * It will configure the socket to optimize latency.
     *
     * @param socket The socket to configure.
     */
    /*public static final void configureSocket(@NotNull final Socket socket) {
        /*try {
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(OptimizeLatency.IPTOS_LOWDELAY);
        } catch (final SocketException ignored) {
            // ignored, target OS likely doesn't support one of the options (i.e., Windows XP doesn't support IP_TOS, for example)
        }*/
    //}

    /**
     * Call this method during {@link io.netty.channel.ChannelInitializer#initChannel(Channel)}.
     * <p>
     * It will configure the channel to optimize latency.
     *
     * @param channel The channel to configure.
     */
    public static final void configureChannel(@NotNull final Channel channel) {
        OptimizeLatency.configureChannelConfig(channel.config(), () -> false);
    }

    /*@NotNull
    private static final Channel getChannel(@NotNull final ChannelConfig config) {
        //noinspection IfCanBeAssertion
        if (!(config instanceof DefaultChannelConfig)) {
            throw new UnsupportedOperationException("getting channel from config of type " + config.getClass().getName());
        }

        try {
            final Field channelField = DefaultChannelConfig.class.getDeclaredField("channel");
            channelField.setAccessible(true);

            final Channel channel = (Channel) channelField.get(config);

            channelField.setAccessible(false);
            return channel;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }*/

    /**
     * Call this method during {@link io.netty.channel.ChannelInitializer#initChannel(Channel)}.
     * <p>
     * It will configure the channel config to optimize latency.
     *
     * @param config The channel config to configure.
     * @param allocatorOnly Whether to only configure allocator or socket-specific options.
     */
    public static final void configureChannelConfig(@NotNull final ChannelConfig config, @NotNull final BooleanSupplier allocatorOnly) {
        try {
            if (Config.isOptimizeLatency()) {
                if (config instanceof final SocketChannelConfig socketChannelConfig && !allocatorOnly.getAsBoolean()) {

                    if (!socketChannelConfig.isTcpNoDelay()) { // In NetworkManager$5 & MC >= 1.8.1, TCP_NODELAY is already set for the client. However, in NetworkManager$6, which is used for integrated server, this is not set even on 1.8.9 so set it if not already set.
                        socketChannelConfig.setTcpNoDelay(true);
                    }

                    // Unlike setTrafficClass, getTrafficClass is not bugged in Native code and gets IP_TOS instead of SO_LINGER correctly. See: https://github.com/netty/netty/blob/netty-4.0.23.Final/transport-native-epoll/src/main/c/io_netty_channel_epoll_Native.c#L1115
                    if (OptimizeLatency.IPTOS_LOWDELAY != socketChannelConfig.getTrafficClass()) {
                        if (config instanceof EpollSocketChannelConfig) {
                            // Native transport IP_TOS option is bugged
                            // Here it says the parameter is tcpNoDelay wrongly: https://github.com/netty/netty/blob/netty-4.0.23.Final/transport-native-epoll/src/main/java/io/netty/channel/epoll/Native.java#L195 (This method is called by https://github.com/netty/netty/blob/netty-4.0.23.Final/transport-native-epoll/src/main/java/io/netty/channel/epoll/EpollSocketChannelConfig.java#L248)
                            // This not harmful, it's like a typo, since the parameter name of a native method (on the Java source code) hardly matters, it's like a parameter name of a method in an interface.
                            // However, it shows that Netty devs were not clear on the head while writing IP_TOS setters for native transport.

                            // And here, in the native code, the method body for SO_LINGER and IP_TOS is reversed: https://github.com/netty/netty/blob/netty-4.0.23.Final/transport-native-epoll/src/main/c/io_netty_channel_epoll_Native.c#L1027

                            // So essentially on EpollSocketChannelConfig, setting SO_LINGER sets IPTOS_LOWDELAY and setting IPTOS_LOWDELAY sets SO_LINGER.

                            // This was a hard to find bug. Nice one Netty developers. I guess this a W for NIO instead of Native Epoll transport, lol.
                            socketChannelConfig.setSoLinger(OptimizeLatency.IPTOS_LOWDELAY);
                        } else {
                            socketChannelConfig.setTrafficClass(OptimizeLatency.IPTOS_LOWDELAY);
                        }
                    }
                }

                // Default in Netty 4.1.x but MC provides 4.0.x. Pooled uses less memory because of fewer duplicates, and it reduces the work of GC.
                //noinspection ObjectEquality
                if (PooledByteBufAllocator.DEFAULT != config.getAllocator()) {
                    config.setAllocator(PooledByteBufAllocator.DEFAULT);
                }

                // TODO Maybe override and provide a newer version of Netty, like how Essential re-launches the game with newer ASM, and set TCP_FASTOPEN_CONNECT as well (although not a high priority since it's only supported on Linux and macOS, apparently)
            }
        } catch (final ChannelException ignored) {
            // ignore, target OS likely doesn't support one of the options (i.e., Windows XP doesn't support IP_TOS, for example)
        }
    }
}
