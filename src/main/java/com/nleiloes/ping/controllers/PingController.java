package pt.tml.plannedoffer.controllers;

import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pt.tml.plannedoffer.entities.Webhook;
import pt.tml.plannedoffer.services.AuthService;
import pt.tml.plannedoffer.services.OperatorWebClient;

@Flogger
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

    @Autowired
    OperatorWebClient operatorWebClient;

    @Autowired
    AuthService authService;



    @GetMapping("ping")
    public String ping( /*@RequestHeader(name="Authorization") String token*/ ) {

//        var decoder= new JwtDecoder();
//        var userName = decoder.extractUsernameFromToken(token);
//        var roles = decoder.extractRealAccessRolesFromToken(token);

        return  String.format("Pong from %s version: %s - %s", serviceName, version, serviceDescription);
    }


    @GetMapping("webhookTest")
    public Webhook webhookTest()
    {

        var webhook= operatorWebClient.getOperatorWebhook(41, "Plano de Operações");
        return webhook;
    }

    @GetMapping("tokenTest")
    public String tokenTest()
    {
        var webhookDefs= operatorWebClient.getOperatorWebhook(41, "Ack Plano de Operação");
        log.atInfo().log("Obtained %s as post to Operator endpoint", webhookDefs.getWebhookUri());

        var token= authService.getToken(webhookDefs, false);
        return token;
    }

}
