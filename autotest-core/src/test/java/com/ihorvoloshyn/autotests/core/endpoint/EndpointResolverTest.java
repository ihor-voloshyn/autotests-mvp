package com.ihorvoloshyn.autotests.core.endpoint;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EndpointResolverTest {
    @Test void resolvesUrlWithSchemeAndPath() {
        var e=EndpointResolver.resolve("https://example.com:8443/api/v1");
        assertEquals("https",e.scheme()); assertEquals("example.com",e.host()); assertEquals(8443,e.port()); assertEquals("/api/v1",e.path());
    }
    @Test void resolvesHostnameWithoutPort() {
        var e=EndpointResolver.resolve("example.com");
        assertEquals("example.com",e.host()); assertNull(e.port());
    }
    @Test void resolvesIpv4WithPort() {
        var e=EndpointResolver.resolve("10.20.30.40:8080");
        assertEquals("10.20.30.40",e.host()); assertEquals(8080,e.port());
    }
    @Test void resolvesIpv6WithAndWithoutPort() {
        var a=EndpointResolver.resolve("[2001:db8::10]");
        var b=EndpointResolver.resolve("[2001:db8::10]:8443");
        assertEquals("2001:db8::10",a.host()); assertNull(a.port());
        assertEquals("2001:db8::10",b.host()); assertEquals(8443,b.port());
    }
    @Test void rejectsInvalidPort() {
        assertThrows(IllegalArgumentException.class,()->EndpointResolver.resolve("host:70000"));
        assertThrows(IllegalArgumentException.class,()->EndpointResolver.resolve("[::1]:abc"));
    }
    @Test void uriKeepsIpv6Bracketed() {
        var e=EndpointResolver.resolve("https://[2001:db8::10]:8443/api");
        assertEquals("https://[2001:db8::10]:8443/api",e.toUri().toString());
    }
}