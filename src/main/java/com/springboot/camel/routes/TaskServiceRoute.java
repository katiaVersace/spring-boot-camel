package com.springboot.camel.routes;

import com.springboot.camel.StoredCookie;
import com.springboot.camel.model.TaskDto;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceRoute extends RouteBuilder {

    private final String URI_TASK_SERVICE = "http://localhost:8080/tasks";

    @Value("${server.port}")
    String serverPort;
    @Value("${springboot.api.path}")
    String contextPath;

    @Override
    public void configure() {

        JacksonDataFormat jsonDataFormat = new JacksonDataFormat(TaskDto.class);


        // http://localhost:8082/camel/api-doc
        restConfiguration()
                .contextPath(contextPath) //
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Task Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .dataFormatProperty("disableFeatures", "FAIL_ON_EMPTY_BEANS")
                .dataFormatProperty("prettyPrint", "true");


        //TASKS
        rest("/api/tasks").description("Task Service").id("tasks-route")
                //GET http://localhost:8083/camel/api/tasks
                .get("/all").produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:tasks")

                .get("/?taskId={taskId}").param().name("taskId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto).outType(String.class).to("direct:taskById")

                //POST http://localhost:8083/camel/api/tasks
                .post().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TaskDto.class).enableCORS(true).outType(String.class).to("direct:postTask")

                .put().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TaskDto.class).enableCORS(true).outType(String.class).to("direct:putTask")

                .patch().produces(String.valueOf(MediaType.APPLICATION_JSON)).consumes(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .type(TaskDto.class).enableCORS(true).outType(String.class).to("direct:patchTask")

                .delete("/?taskId={taskId}").param().name("taskId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .outType(String.class).to("direct:deleteTask")

                .get("/tasksByEmployee/?employeeId={employeeId}").param().name("employeeId").type(RestParamType.header).endParam()
                .produces(String.valueOf(MediaType.APPLICATION_JSON)).bindingMode(RestBindingMode.auto)
                .outType(String.class).to("direct:tasksByEmployee")
        ;

        //TASKS

        from("direct:tasks")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("StoredCookie.cookie della get: " + exchange.getIn().getHeader("cookie"));
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TASK_SERVICE);

        from("direct:taskById")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE + "/${header.taskId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_TASK_SERVICE + "/${header.taskId}");

        from("direct:postTask")
                .setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TASK_SERVICE)
                .unmarshal().json(JsonLibrary.Jackson, TaskDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Post task " + exchange.getIn().getBody(TaskDto.class));
                    }
                })

        ;

        from("direct:putTask")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TASK_SERVICE)
                .unmarshal().json(JsonLibrary.Jackson, TaskDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Put task " + exchange.getIn().getBody(TaskDto.class));
                    }
                })
        ;

        from("direct:patchTask")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE))
                .convertBodyTo(TaskDto.class)
                .marshal(jsonDataFormat)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);

                    }
                })
                .to(URI_TASK_SERVICE)
                .unmarshal().json(JsonLibrary.Jackson, TaskDto.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Patch task " + exchange.getIn().getBody(TaskDto.class));
                    }
                })
        //
        ;

        from("direct:deleteTask")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE + "/${header.taskId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_TASK_SERVICE + "/${header.taskId}");

        from("direct:tasksByEmployee")
                .setHeader(Exchange.HTTP_URI, simple(URI_TASK_SERVICE + "/tasksByEmployee/${header.employeeId}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
                    }
                })
                .to(URI_TASK_SERVICE + "/tasksByEmployee/${header.employeeId}");


    }


}
