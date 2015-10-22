/*******************************************************************************
 * @(#)NioService.java 2008-10-24
 *
 * Copyright 2008 Neusoft Group Ltd. All rights reserved.
 * Neusoft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *******************************************************************************/
package com.neusoft.smsplatform.nio.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neusoft.clw.log.LogFormatter;
/**
 * 
 * @author Administrator
 *
 */
public abstract class SmsTcpNioService implements ISmsTcpService {

    private Logger log = LoggerFactory.getLogger(SmsTcpNioService.class);
    
    private final static String NAME = "Sms TcpNioService";

    private String ip;

    private int port;

    protected int processorNum;

    protected int threadPoolSize;

    protected int reconnectInterval;

    private boolean isAvailable;

    protected SmsProtocolCodec decoder;

    private IoSession session;

    protected SmsAbstractNioHandler<? extends SmsTcpNioService> nioHandler;

    private static final int MAX_PORT_VALUE = 65535;

    private static final int SIXTY_SECONDS = 60;

    private static final int MSEL = 1000;

    protected abstract void setProcessorNum();

    protected abstract void setThreadPoolSize();

    protected abstract void setReconnectInterval();

    protected abstract void setNioHandler();

    protected abstract void setDecoder();

    private Executor socketConnectorThreadPool = Executors.newCachedThreadPool();

    private Executor downMsgThreadPool;

	public SmsTcpNioService(String ip, int port) throws Exception {
        this.ip = ip;
        this.port = port;
        setDecoder();
        setNioHandler();
        setProcessorNum();
        setThreadPoolSize();
        setReconnectInterval();
        validate();
        downMsgThreadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    private void validate() throws Exception {
        checkIp(ip);
        checkPort();
        chekcDecoder();
        checkNioHandler();
        checkProcessorNum();
        checkThreadPoolSize();
        checkReconnectInterval();
    }

    private void checkThreadPoolSize() throws Exception {
        if (threadPoolSize <= 0) {
            throw new Exception("threadPoolSize is invalid.");
        }
    }

    private void checkReconnectInterval() throws Exception {
        if (reconnectInterval <= 0) {
            throw new Exception("reconnectInterval is invalid.");
        }
    }

    private void checkProcessorNum() throws Exception {
        if (processorNum <= 0) {
            throw new Exception("processorNum is invalid.");
        }
    }

    private void checkNioHandler() throws Exception {
        if (nioHandler == null) {
            throw new Exception("nioHandler is null.");
        }
    }

    private void chekcDecoder() throws Exception {
        if (decoder == null) {
            throw new Exception("decoder is null.");
        }
    }

    private void checkPort() throws Exception {
        if (port <= 0 || port > MAX_PORT_VALUE) {
            throw new Exception("port:" + port + " is null.");
        }
    }

    private void checkIp(String address) throws Exception {
        if (address == null) {
            throw new Exception("ip is null.");
        }
        final int min = 0;
        final int max = 255;
        final int part = 4;
        String[] values = address.split("\\.");
        if (values.length != part) {
            throw new Exception("ip:" + address + " format is invalid.");
        }
        for (String str : values) {
            int value = Integer.parseInt(str);
            if (value < min || value >= max) {
                throw new Exception("ip:" + address + " is invalid.");
            }
        }
    }

    public boolean connect() throws Exception {
	    	SocketConnector connector = new SocketConnector(processorNum, socketConnectorThreadPool);
	        SocketConnectorConfig cfg = new SocketConnectorConfig();
	            ((ExecutorThreadModel) cfg.getThreadModel()).setExecutor(downMsgThreadPool);
	        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(decoder));
	        ConnectFuture future = connector.connect(new InetSocketAddress(ip, port), nioHandler
	                    .getAdapter(), cfg);
	        log.info(LogFormatter.formatMsg(NAME, "connecting " + ip + ":" + port));
	        future.join();
	        return future.isConnected();
    }

	public boolean reconnect() throws Exception {
		while (true) {
            log.info(LogFormatter.formatMsg(NAME, "it will reconnect sms platform in the "
                    + reconnectInterval + " minutes later."));
            Thread.sleep(reconnectInterval * SIXTY_SECONDS * MSEL);
            if (!connect()) {
                log.info(LogFormatter.formatMsg(NAME,
                        "reconnect failed, it will reconnect sms platform in the " + reconnectInterval
                                + " minutes later."));
            }else{
            	break;
            }
        }
        return true;
    }

    public void send(byte[] buf) throws Exception {
        if (!isAvailable) {
            throw new Exception("connection is not available, the message cann't be send.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(buf.length, false);
        buffer.put(buf);
        buffer.flip();
        session.write(buffer);
    }

    public void close() throws Exception {
        session.close();
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public IoSession getSession() {
        return session;
    }

    public String getLocalAddress() {
        InetSocketAddress localAddress = (InetSocketAddress) session.getLocalAddress();
        return localAddress.getAddress().getHostAddress() + ":" + localAddress.getPort();
    }

    public String getRemoteAddress() {
        InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}