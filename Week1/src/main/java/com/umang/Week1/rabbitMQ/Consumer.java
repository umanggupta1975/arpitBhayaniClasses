package com.umang.Week1.rabbitMQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.umang.Week1.rabbitMQ.RabbitMqConfig.QUEUE_NAME;


@Slf4j
@Service
public class Consumer {

    @RabbitListener(queues = {QUEUE_NAME})
    public void consume(Message message) {
        System.out.println("Received message: " + message);
    }

}