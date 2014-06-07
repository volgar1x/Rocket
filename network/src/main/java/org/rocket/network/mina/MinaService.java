package org.rocket.network.mina;

import com.github.blackrush.acara.EventBus;
import com.google.common.collect.ImmutableSet;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.fungsi.concurrent.Timer;
import org.fungsi.concurrent.Timers;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.NetworkCommand;
import org.rocket.network.NetworkService;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.DisconnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;

public class MinaService<C extends MinaClient> implements NetworkService<C> {
    private final EventBus eventBus;
    private final ScheduledExecutorService scheduler;
    private final BiFunction<IoSession, MinaService<C>, C> clientFactory;
    private final Logger logger;

    private final IoAcceptor acceptor;
    private final Set<C> clients = new HashSet<>();

    public MinaService(EventBus eventBus, ScheduledExecutorService scheduler, SocketAddress addr, BiFunction<IoSession, MinaService<C>, C> clientFactory, Logger logger, Consumer<IoAcceptor> config) {
        this.eventBus = eventBus;
        this.scheduler = scheduler;
        this.clientFactory = clientFactory;
        this.logger = logger;

        this.acceptor = new NioSocketAcceptor();
        this.acceptor.setDefaultLocalAddress(addr);
        this.acceptor.setHandler(new Handler());
        config.accept(this.acceptor);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public Timer newTimer() {
        return Timers.wrap(getScheduler());
    }

    @Override
    public void start(ServiceContext ctx) {
        try {
            logger.debug("starting...");
            acceptor.bind();
            logger.info("started");
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    @Override
    public void stop(ServiceContext ctx) {
        logger.debug("stopping...");
        acceptor.unbind();
        acceptor.dispose();
        logger.info("stopped");
    }

    @Override
    public NetworkCommand broadcast(Stream<C> clients, Object o) {
        return new BroadcastCommand(clients.map(s -> s.session), o, this::newTimer);
    }

    @Override
    public int getActualConnectedClients() {
        return acceptor.getManagedSessionCount();
    }

    @Override
    public int getMaxConnectedClients() {
        return acceptor.getStatistics().getLargestManagedSessionCount();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public ImmutableSet<C> getClients() {
        return ImmutableSet.copyOf(clients);
    }

    @Override
    public Optional<Class<? extends Service>> dependsOn() {
        return Optional.empty();
    }

    public static final Object CLIENT_KEY = MinaService.Handler.class.getName() + ".CLIENT_KEY";

    class Handler implements IoHandler {

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            C client = clientFactory.apply(session, MinaService.this);
            clients.add(client);
            session.setAttribute(CLIENT_KEY, client);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            @SuppressWarnings("unchecked")
            C client = (C) session.getAttribute(CLIENT_KEY);

            eventBus.publishSync(new ConnectEvent<>(client));
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            @SuppressWarnings("unchecked")
            C client = (C) session.removeAttribute(CLIENT_KEY);
            clients.remove(client);

            eventBus.publishSync(new DisconnectEvent<>(client));
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            if (cause instanceof Error) {
                throw (Error) cause;
            }

            @SuppressWarnings("unchecked")
            C client = (C) session.getAttribute(CLIENT_KEY);

            eventBus.publishSync(new RecoverEvent<>(client, cause));
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            @SuppressWarnings("unchecked")
            C client = (C) session.getAttribute(CLIENT_KEY);

            eventBus.publishSync(new ReceiveEvent<>(client, message));
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {

        }
    }
}
