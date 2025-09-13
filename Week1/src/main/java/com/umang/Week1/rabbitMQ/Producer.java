package com.umang.Week1.rabbitMQ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.umang.Week1.rabbitMQ.RabbitMqConfig.EXCHANGE_NAME;
import static com.umang.Week1.rabbitMQ.RabbitMqConfig.QUEUE_NAME;


@Slf4j
@Service
@RequiredArgsConstructor
public class Producer {
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 30000)
    public void sendMessage() {
        Message msg = new Message();
        msg.setMsg("Message from " + LocalDateTime.now());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, QUEUE_NAME, msg);
        System.out.println(String.format("Message with id [%s] sent to [%s] exchange with routing key [%s]", msg.getId(), EXCHANGE_NAME, QUEUE_NAME));
    }

}