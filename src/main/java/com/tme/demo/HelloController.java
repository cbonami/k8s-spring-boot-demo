package com.tme.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloController {

//    @Autowired
//    private DiscoveryClient discoveryClient;

    @RequestMapping("/")
    public String hello() {
        return "Hello World";
    }

//    @RequestMapping("/services")
//    public List<String> services() {
//        return this.discoveryClient.getServices();
//    }
}
