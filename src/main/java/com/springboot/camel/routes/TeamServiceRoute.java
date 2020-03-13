package com.springboot.camel.routes;

import com.springboot.camel.model.RandomPopulationInputDto;
import com.springboot.camel.model.TaskDto;
import com.springboot.camel.model.TeamDto;
import com.springboot.camel.processor.CookieProcessor;
import com.springboot.camel.processor.PrintBodyResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public class TeamServiceRoute extends RouteBuilder {

    private final String URI_TEAM_SERVICE = "http4://team-service:8300/teams";

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


        rest("/api/teams").description("Team Service").id("teams-route")

                //GET http://localhost:8400/camel/api/teams/all
                .get("/all").produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .enableCORS(true).outType(String.class).to("direct:teams")

                //GET http://localhost:8400/camel/api/teams/?teamId={teamId}
                .get("/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:teamById")

                //POST http://localhost:8400/camel/api/teams
                .post().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TeamDto.class).enableCORS(true).outType(String.class).to("direct:postTeam")

                //PUT http://localhost:8400/camel/api/teams
                .put().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TeamDto.class).enableCORS(true).outType(String.class).to("direct:putTeam")

                //DELETE http://localhost:8400/camel/api/teams/?teamId={teamId}
                .delete("/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .outType(String.class).to("direct:deleteTeam")

                //POST http://localhost:8400/camel/api/teams/randomPopulation
                .post("/randomPopulation").produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(RandomPopulationInputDto.class).enableCORS(true).outType(String.class).to("direct:randomPopulation")

                //POST http://localhost:8400/camel/api/teams/assignTaskToTeam/?teamId={teamId}
                .post("/assignTaskToTeam/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TaskDto.class).enableCORS(true).outType(TaskDto.class).to("direct:assignTaskToTeam")

                //POST http://localhost:8400/camel/api/teams/employeesByTeamAndTask/?teamId={teamId}
                .post("/employeesByTeamAndTask/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON))
                .bindingMode(RestBindingMode.auto).skipBindingOnErrorCode(false).type(TaskDto.class).enableCORS(true).outType(String.class)
                .to("direct:employeesByTeamAndTask");


        from("direct:postTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .convertBodyTo(TeamDto.class)
                .marshal(jsonDataFormat)
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE);

        from("direct:putTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .convertBodyTo(TeamDto.class)
                .marshal(jsonDataFormat)
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE);

        from("direct:deleteTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/${header.teamId}"))
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE + "/${header.teamId}");


        from("direct:randomPopulation")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/randomPopulation"))
                .convertBodyTo(RandomPopulationInputDto.class)
                .marshal(jsonDataFormat)
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE + "/randomPopulation")

        ;
        from("direct:assignTaskToTeam")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/assignTaskToTeam/${header.teamId}"))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE + "/assignTaskToTeam/${header.teamId}")
                .process(new PrintBodyResponseProcessor());


        from("direct:teams")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE))
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE);

        from("direct:teamById")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/${header.teamId}"))
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE + "/${header.teamId}");

        from("direct:employeesByTeamAndTask")
                .setHeader(Exchange.HTTP_URI, simple(URI_TEAM_SERVICE + "/employeesByTeamAndTask/${header.teamId}"))
                .removeHeader(Exchange.HTTP_QUERY)
                .convertBodyTo(TaskDto.class)
                .marshal(new JacksonDataFormat(TaskDto.class))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new CookieProcessor())
                .to(URI_TEAM_SERVICE + "/employeesByTeamAndTask/${header.teamId}");


    }


}
