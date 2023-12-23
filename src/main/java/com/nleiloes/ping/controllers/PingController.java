package com.nleiloes.ping.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin("*")
@RequestMapping
public class PingController
{
    @Value("${info.app.version:unknown}")
    String version;
    @Value("${info.app.name:unknown}")
    String serviceName;
    @Value("${info.app.description:unknown}")
    String serviceDescription;

    @GetMapping("public/ping")
    public String ping() {
        return  String.format("pong from %s version: %s - %s", serviceName, version, serviceDescription);
    }

    @GetMapping("private/ping")
    public String pong(HttpServletRequest request) {
        var x = request.getHeader("Authorization");
        return  String.format("I'm a secured route: ");
    }
}
