package com.ihorvoloshyn.autotests.soap;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public final class SoapClientFactory {

    private SoapClientFactory() {
    }

    public static <T> T create(
            Class<T> serviceClass,
            String endpoint,
            String username,
            String password) {

        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass must not be null");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint must not be blank");
        }

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(endpoint);

        T proxy = factory.create();

        if (username != null && !username.isBlank()) {
            Client client = ClientProxy.getClient(proxy);
            AuthorizationPolicy policy = new AuthorizationPolicy();
            policy.setUserName(username);
            policy.setPassword(password == null ? "" : password);
            policy.setAuthorizationType("Basic");
            client.getRequestContext().put(AuthorizationPolicy.class.getName(), policy);
        }

        return proxy;
    }
}