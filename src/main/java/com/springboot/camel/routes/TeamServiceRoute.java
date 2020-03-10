package com.springboot.camel.routes;

import com.springboot.camel.StoredCookie;
import com.springboot.camel.model.RandomPopulationInputDto;
import com.springboot.camel.model.TaskDto;
import com.springboot.camel.model.TeamDto;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public class TeamServiceRoute extends RouteBuilder {

    private final String URI_TEAM_SERVICE = "http://localhost:8080/teams";

    @Value("${server.port}")
    String serverPort;
    @Value("${springboot.api.path}")
    String contextPath;

    @Override
    public void configure() {

        JacksonDataFormat jsonDataFormat = new JacksonDataFormat(TeamDto.class);
        // http://localhost:8083/camel/api-doc
        restConfiguration()
                .contextPath(contextPath) //
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Team Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .dataFormatProperty("disableFeatures", "FAIL_ON_EMPTY_BEANS")
                .dataFormatProperty("prettyPrint", "true");


        //TEAMS
        rest("/api/teams").description("Team Service").id("teams-route")

                //GET http://localhost:8083/camel/api/teams
                .get("/all").produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .enableCORS(true).outType(String.class).to("direct:teams")

                //GET http://localhost:8083/camel/api/teams/?teamId={teamId}
                .get("/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:teamById")

                //POST http://localhost:8083/camel/api/teams
                .post().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TeamDto.class).enableCORS(true).outType(String.class).to("direct:postTeam")

                .put().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TeamDto.class).enableCORS(true).outType(String.class).to("direct:putTeam")

                .delete("/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .outType(String.class).to("direct:deleteTeam")

                .post("/randomPopulation").produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(RandomPopulationInputDto.class).enableCORS(true).outType(String.class).to("direct:randomPopulation")

                .post("/assignTaskToTeam/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TaskDto.class).enableCORS(true).outType(TaskDto.class).to("direct:assignTaskToTeam")
        ;


//        interceptFrom()
//                .routeId("login-route")
//                .setHeader(Exchange.HTTP_URI, simple("http://localhost:8080/auth/login"))
//                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
//                .setHeader(Exchange.CONTENT_TYPE, constant("application/x-www-form-urlencoded"))
//                .setHeader(Exchange.HTTP_QUERY, constant("username=admin&password=admin"))
//                .to("http://localhost:8080/auth/login")
//        ;


        from("direct:postTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .convertBodyTo(TeamDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TEAM_SERVICE)
        ;

        from("direct:putTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .convertBodyTo(TeamDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TEAM_SERVICE)
        ;

        from("direct:deleteTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/${header.teamId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_TEAM_SERVICE + "/${header.teamId}");


        from("direct:randomPopulation")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/randomPopulation"))
                .setHeader("Accept", constant("application/json"))
                .convertBodyTo(RandomPopulationInputDto.class)
                .marshal(jsonDataFormat)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TEAM_SERVICE + "/randomPopulation")

        ;
        from("direct:assignTaskToTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/assignTaskToTeam/${header.teamId}"))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TEAM_SERVICE + "/assignTaskToTeam/${header.teamId}")
                .unmarshal().json(JsonLibrary.Jackson, TaskDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("assignTaskToTeam " + exchange.getIn().getBody(TaskDto.class));
                    }
                })

        ;


        // from("timer:mytimer?repeatCount=1")
        from("direct:teams")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TEAM_SERVICE)
        ;

        from("direct:teamById")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/${header.teamId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                        System.out.println("StoredCookie.cookie della get: " + exchange.getIn().getHeader("cookie"));

                    }
                })
                .to(URI_TEAM_SERVICE + "/${header.teamId}");


    }


}
