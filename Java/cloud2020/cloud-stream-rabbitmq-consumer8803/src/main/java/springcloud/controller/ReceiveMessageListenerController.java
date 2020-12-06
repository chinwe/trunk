package springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableBinding(Sink.class)
public class ReceiveMessageListenerController {

    @Value("${server.port}")
    private String port;

    @StreamListener(Sink.INPUT)
    public void input(Message<String> message) {
        System.out.println(port + " Consumer 1. Receive message :" + message.getPayload());
    }
}
