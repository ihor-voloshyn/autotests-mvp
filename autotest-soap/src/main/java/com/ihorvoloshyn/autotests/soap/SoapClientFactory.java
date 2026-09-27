package com.ihorvoloshyn.autotests.soap;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public final class SoapClientFactory {
    private SoapClientFactory() {}

    public static <T> T create(Class<T> serviceClass, String endpoint, String username, String password) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass must not be null");
        }
        var resolved = EndpointResolver.resolve(endpoint);
        if (resolved.scheme() == null) {
            throw new IllegalArgumentException("SOAP endpoint must include a URL scheme");
        }
        return create(serviceClass, resolved, username, password);
    }

    public static <T> T create(
            Class<T> serviceClass,
            ConnectionEndpoint endpoint,
            String username,
            String password) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass must not be null");
        }
        if (endpoint == null || endpoint.scheme() == null) {
            throw new IllegalArgumentException("SOAP endpoint must include a URL scheme");
        }

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(endpoint.toUri().toString());

        @SuppressWarnings("unchecked")
        T proxy = (T) factory.create();

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
