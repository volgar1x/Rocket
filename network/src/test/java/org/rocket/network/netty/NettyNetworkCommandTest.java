package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.fungsi.concurrent.Timer;
import org.fungsi.concurrent.Timers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class NettyNetworkCommandTest {
    
    private Supplier<Timer> timer;

    @Before
    public void setUp() throws Exception {
        timer = () -> Timers.wrap(Executors.newSingleThreadScheduledExecutor());

    }

    @Test
    public void testEmptyTransaction() throws Exception {
        Channel channel = mock(Channel.class);
        TransactionCommand cmd = new TransactionCommand(channel, tx -> {}, timer);

        cmd.async();

        InOrder o = inOrder(channel);
        o.verify(channel).flush();

        o.verifyNoMoreInteractions();
    }

    @Test
    public void testTransaction() throws Exception {
        Channel channel = mock(Channel.class);
        ChannelFuture holaFut = mock(ChannelFuture.class);
        ChannelFuture helloFut = mock(ChannelFuture.class);

        TransactionCommand cmd = new TransactionCommand(channel, tx -> {
            tx.write("hola");
            tx.write("hello");
        }, timer);

        when(channel.write("hola")).thenReturn(holaFut);
        when(channel.write("hello")).thenReturn(helloFut);

        cmd.async();

        InOrder o = inOrder(channel, holaFut, helloFut);
        o.verify(channel).write("hola");
        o.verify(holaFut).addListener(any());
        o.verify(channel).write("hello");
        o.verify(helloFut).addListener(any());
        o.verify(channel).flush();

        o.verifyNoMoreInteractions();
    }

    @Test
    public void testWrite() throws Exception {
        Channel channel = mock(Channel.class);
        ChannelFuture fut = mock(ChannelFuture.class);

        WriteCommand cmd = new WriteCommand(channel, "hello", timer);

        when(channel.writeAndFlush("hello")).thenReturn(fut);

        cmd.async();

        InOrder o = inOrder(channel, fut);
        o.verify(channel).writeAndFlush("hello");
        o.verify(fut).addListener(any());

        o.verifyNoMoreInteractions();
    }

    @Test
    public void testClose() throws Exception {
        Channel channel = mock(Channel.class);
        ChannelFuture fut = mock(ChannelFuture.class);

        CloseCommand cmd = new CloseCommand(channel, timer);

        when(channel.close()).thenReturn(fut);

        cmd.async();

        InOrder o = inOrder(channel, fut);
        o.verify(channel).close();
        o.verify(fut).addListener(any());

        o.verifyNoMoreInteractions();
    }

    @Test
    public void testBroadcast() throws Exception {
        Channel channel1 = mock(Channel.class);
        Channel channel2 = mock(Channel.class);
        ChannelFuture channelFut1 = mock(ChannelFuture.class);
        ChannelFuture channelFut2 = mock(ChannelFuture.class);

        BroadcastCommand cmd = new BroadcastCommand(Stream.of(channel1, channel2), "hello", timer);

        when(channel1.writeAndFlush("hello")).thenReturn(channelFut1);
        when(channel2.writeAndFlush("hello")).thenReturn(channelFut2);

        cmd.async();

        InOrder o = inOrder(channel1, channel2, channelFut1, channelFut2);
        o.verify(channel1).writeAndFlush("hello");
        o.verify(channelFut1).addListener(any());
        o.verify(channel2).writeAndFlush("hello");
        o.verify(channelFut2).addListener(any());

        o.verifyNoMoreInteractions();
    }
}