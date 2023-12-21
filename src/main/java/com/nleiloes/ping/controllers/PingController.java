package com.nleiloes.ping.controllers;

import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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

    @GetMapping("")
    public String ping() {
        return  String.format("pong from %s version: %s - %s", serviceName, version, serviceDescription);
    }

    @GetMapping("secured")
    public String pong(Principal principal) {
        return  String.format("I'm a secured route: "+ principal.getName().toString());
    }
}
