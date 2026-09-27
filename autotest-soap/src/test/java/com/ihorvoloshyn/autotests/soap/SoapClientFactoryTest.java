package com.ihorvoloshyn.autotests.soap;

import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import jakarta.jws.WebService;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoapClientFactoryTest {

    @WebService(targetNamespace = "urn:test", name = "TestService")
    public interface TestService {
        String ping(String value);
    }

    @Test
    void createsProxyFromEndpoint() {
        TestService proxy = SoapClientFactory.create(
                TestService.class,
                "http://localhost:8080/service",
                null,
                null);

        assertNotNull(proxy);
        Client client = ClientProxy.getClient(proxy);
        assertEquals("http://localhost:8080/service", client.getEndpoint().getEndpointInfo().getAddress());
    }

    @Test
    void configuresBasicAuthentication() {
        TestService proxy = SoapClientFactory.create(
                TestService.class,
                EndpointResolver.resolve("https://example.com:8443/service"),
                "user",
                "password");

        Client client = ClientProxy.getClient(proxy);
        AuthorizationPolicy policy = (AuthorizationPolicy) client.getRequestContext()
                .get(AuthorizationPolicy.class.getName());

        assertNotNull(policy);
        assertEquals("user", policy.getUserName());
        assertEquals("password", policy.getPassword());
        assertEquals("Basic", policy.getAuthorizationType());
    }

    @Test
    void rejectsEndpointWithoutScheme() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SoapClientFactory.create(
                        TestService.class,
                        "example.com:8080",
                        null,
                        null));
    }

    @Test
    void rejectsNullServiceClass() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SoapClientFactory.create(
                        null,
                        "http://localhost:8080/service",
                        null,
                        null));
    }
}
