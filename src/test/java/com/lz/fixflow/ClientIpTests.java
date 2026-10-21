package com.lz.fixflow;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ClientIpTests.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class ClientIpTests {

    @LocalServerPort
    private int port;

    @Test
    void usesClientIpv4FromLocalProxy() throws Exception {
        assertEquals("203.0.113.8", request("203.0.113.8"));
    }

    @Test
    void preservesClientIpv6FromLocalProxy() throws Exception {
        assertEquals("2001:db8::8", request("2001:db8::8"));
    }

    @Test
    void retainsPeerAddressWithoutForwardedHeader() throws Exception {
        assertEquals("127.0.0.1", request(null));
    }

    @Test
    void ignoresSpoofedLeftmostAddress() throws Exception {
        assertEquals("203.0.113.8", request("198.51.100.99, 203.0.113.8"));
    }

    private String request(String forwardedFor) throws Exception {
        return request(port, forwardedFor);
    }

    static String request(int port, String forwardedFor) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/ip")).timeout(Duration.ofSeconds(5));
        if (forwardedFor != null) {
            request.header("X-Forwarded-For", forwardedFor);
        }
        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build()) {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString()).body();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(TomcatServletWebServerAutoConfiguration.class)
    static class TestConfig {
        @Bean
        ServletRegistrationBean<HttpServlet> ipServlet() {
            return new ServletRegistrationBean<>(new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws IOException {
                    response.getWriter().write(request.getRemoteAddr());
                }
            }, "/ip");
        }
    }
}

@SpringBootTest(classes = ClientIpTests.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.tomcat.remoteip.internal-proxies=192\\.168\\.1\\.10")
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class UntrustedClientIpTests {

    @LocalServerPort
    private int port;

    @Test
    void ignoresForwardedHeaderFromUntrustedPeer() throws Exception {
        assertEquals("127.0.0.1", ClientIpTests.request(port, "203.0.113.8"));
    }
}
