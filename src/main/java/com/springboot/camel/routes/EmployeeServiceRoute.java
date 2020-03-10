package com.springboot.camel.routes;

import com.springboot.camel.StoredCookie;
import com.springboot.camel.model.AvailabilityByEmployeeInputDto;
import com.springboot.camel.model.EmployeeDto;
import com.springboot.camel.model.TaskDto;
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
public class EmployeeServiceRoute extends RouteBuilder {

    private final String URI_EMPLOYEE_SERVICE = "http://localhost:8080/employees";
    @Value("${server.port}")
    String serverPort;
    @Value("${springboot.api.path}")
    String contextPath;

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jsonDataFormat = new JacksonDataFormat(EmployeeDto.class);


        // http://localhost:8082/camel/api-doc
        restConfiguration()
                .contextPath(contextPath) //
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Employee Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .dataFormatProperty("disableFeatures", "FAIL_ON_EMPTY_BEANS")
                .dataFormatProperty("prettyPrint", "true");


        //EMPLOYEES
        rest("/api/employees").description("Employee Service").id("employees-route")
                //GET http://localhost:8083/camel/api/employees
                .get("/all").produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:employees")

                .get("/?employeeId={employeeId}").param().name("employeeId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:employeeById")

                //POST http://localhost:8083/camel/api/employees
                .post("/?admin={admin}").param().name("admin").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(EmployeeDto.class).enableCORS(true).outType(String.class).to("direct:postEmployee")

                .put().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(EmployeeDto.class).enableCORS(true).outType(String.class).to("direct:putEmployee")

                .delete("/?employeeId={employeeId}").param().name("employeeId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .outType(String.class).to("direct:deleteEmployee")

                .get("/employeesByTeamAndTask/?teamId={teamId}").param().name("teamId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON))
                .bindingMode(RestBindingMode.auto).type(TaskDto.class).enableCORS(true).outType(String.class)
                .to("direct:employeesByTeamAndTask")

                .get("/availability").produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON))
                .bindingMode(RestBindingMode.auto).type(AvailabilityByEmployeeInputDto.class).enableCORS(true).outType(String.class)
                .to("direct:availability");


        //EMPLOYEES
        from("direct:employees")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("StoredCookie.cookie della get: " + exchange.getIn().getHeader("cookie"));
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_EMPLOYEE_SERVICE);

        from("direct:employeeById")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE + "/${header.employeeId}"))
                .setHeader(Exchange.HTTP_QUERY, constant(""))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_EMPLOYEE_SERVICE + "/${header.employeeId}");

        from("direct:postEmployee")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE + "/${header.admin}"))
                .convertBodyTo(EmployeeDto.class)
                .marshal(jsonDataFormat)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_EMPLOYEE_SERVICE + "/${header.admin}")
                .unmarshal().json(JsonLibrary.Jackson, EmployeeDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Post empl " + exchange.getIn().getBody(EmployeeDto.class));
                    }
                })
        ;

        from("direct:putEmployee")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE))
                .convertBodyTo(EmployeeDto.class)
                .marshal(jsonDataFormat)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_EMPLOYEE_SERVICE)
                .unmarshal().json(JsonLibrary.Jackson, EmployeeDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Put empl " + exchange.getIn().getBody(EmployeeDto.class));
                    }
                })
        ;

        from("direct:deleteEmployee")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE + "/${header.employeeId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_EMPLOYEE_SERVICE + "/${header.employeeId}");

        from("direct:employeesByTeamAndTask")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE + "/employeesByTeamAndTask/${header.teamId}"))
                .setHeader("Accept", constant("application/json"))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_EMPLOYEE_SERVICE + "/employeesByTeamAndTask/${header.teamId}");

        from("direct:availability")
                .setHeader(Exchange.HTTP_URI, simple(URI_EMPLOYEE_SERVICE + "/availability"))
                .convertBodyTo(AvailabilityByEmployeeInputDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_EMPLOYEE_SERVICE + "/availability")
        ;


    }
}
