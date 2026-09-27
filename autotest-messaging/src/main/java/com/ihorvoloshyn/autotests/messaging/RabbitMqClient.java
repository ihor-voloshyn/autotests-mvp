package com.ihorvoloshyn.autotests.messaging;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import com.rabbitmq.client.*;

public final class RabbitMqClient implements AutoCloseable {
    private final ConnectionFactory factory;
    private Connection connection;

    public RabbitMqClient(String endpoint, String username, String password, String virtualHost) {
        this(EndpointResolver.resolve(endpoint), username, password, virtualHost);
    }
    public RabbitMqClient(ConnectionEndpoint endpoint, String username, String password, String virtualHost) {
        if(endpoint==null) throw new IllegalArgumentException("endpoint must not be null");
        factory=new ConnectionFactory();
        factory.setHost(endpoint.host());
        factory.setPort(endpoint.portOr("amqps".equalsIgnoreCase(endpoint.scheme())?5671:5672));
        if("amqps".equalsIgnoreCase(endpoint.scheme())) factory.useSslProtocol();
        if(username!=null&&!username.isBlank()) factory.setUsername(username);
        if(password!=null) factory.setPassword(password);
        if(virtualHost!=null&&!virtualHost.isBlank()) factory.setVirtualHost(virtualHost);
    }
    public synchronized Connection connect(){
        try{if(connection==null||!connection.isOpen())connection=factory.newConnection();return connection;}
        catch(Exception e){throw new IllegalStateException("Cannot connect to RabbitMQ",e);}
    }
    public boolean isConnected(){return connection!=null&&connection.isOpen();}
    @Override public synchronized void close(){if(connection!=null)try{connection.close();}catch(Exception ignored){}}
}