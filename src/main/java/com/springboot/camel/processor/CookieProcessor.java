package com.springboot.camel.processor;

import com.springboot.camel.StoredCookie;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


public class CookieProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader("cookie", StoredCookie.cookie);
    }
}
