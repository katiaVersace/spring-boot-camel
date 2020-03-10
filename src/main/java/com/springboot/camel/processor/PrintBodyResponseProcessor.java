package com.springboot.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PrintBodyResponseProcessor implements Processor {


    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody(String.class));
    }

}
