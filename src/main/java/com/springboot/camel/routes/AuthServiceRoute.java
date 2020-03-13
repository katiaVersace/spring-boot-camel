package com.springboot.camel.routes;

import com.springboot.camel.StoredCookie;
import com.springboot.camel.model.EmployeeDto;
import com.springboot.camel.processor.CookieProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceRoute extends RouteBuilder {

    private final String URI_LOGIN_SERVICE = "http4://localhost:8080/auth";


    @Value("${server.port}")
    String serverPort;
    @Value("${springboot.api.path}")
    String contextPath;

    @Override
    public void configure() {

        // http://localhost:8082/camel/api-doc
        restConfiguration()
                .contextPath(contextPath) //
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Authentication Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .dataFormatProperty("disableFeatures", "FAIL_ON_EMPTY_BEANS")
                .dataFormatProperty("prettyPrint", "true");

        //LOGIN
        rest("/api/auth").description("Authentication Service").id("auth-route")
                //POST http://localhost:8400/camel/auth/login
                .post("/login").produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .bindingMode(RestBindingMode.auto)
                .enableCORS(true).outType(String.class).to("direct:login")

                //GET http://localhost:8400/camel/auth/logout
                .get("/logout").produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:logout");

        from("direct:login")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, "username=" + exchange.getIn().getHeader("username", String.class) + "&password=" + exchange.getIn().getHeader("password", String.class));
                    }
                })
                .setHeader(Exchange.HTTP_URI, simple(URI_LOGIN_SERVICE + "/login"))
                .to(URI_LOGIN_SERVICE + "/login")
                .doTry()
                .unmarshal().json(JsonLibrary.Jackson, EmployeeDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Save cookie " + exchange.getIn().getHeader("cookie"));
                        StoredCookie.cookie = (String) exchange.getIn().getHeader("cookie");

                    }
                })
                .doCatch(Exception.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        final Throwable ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                        exchange.getIn().setBody(ex.getMessage());
                        StoredCookie.cookie = null;
                    }
                })
                .end();


        from("direct:logout")
                .setHeader(Exchange.HTTP_URI, simple(URI_LOGIN_SERVICE + "/logout"))
                .process(new CookieProcessor())
                .to(URI_LOGIN_SERVICE + "/logout")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        StoredCookie.cookie = null;
                    }
                });
    }
}
