/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;


import java.util.logging.Logger;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableBinding(Source.class)
public class SourceApplication {
	
	protected Logger logger = Logger.getLogger(SourceApplication.class.getName());

	
	public static void main(String[] args) {
		SpringApplication.run(SourceApplication.class, args);
	}
	
	final static String queueName = "test-queue";
	
	@Bean
    Queue queue() {
        return new Queue(queueName, true);
    }
	
//	@Bean
//	TopicExchange exchange() {
//		return new TopicExchange("spring-boot-exchange");
//	}
//
//	@Bean
//	Binding binding(Queue queue, TopicExchange exchange) {
//		return BindingBuilder.bind(queue).to(exchange).with(queueName);
//	}
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	Tracer tracer;
	
	@RequestMapping(value="/a/{reqMsg}")
	public void sourceApi_A(@PathVariable("reqMsg") String reqMsg) {
		
		logger.info("================== source ===================");
		logger.info("reqMsg   >>>>>>>>>>>>>> " + reqMsg);
		
		logger.info("TaraceId >>>>>>>>>>>>>  " + Span.idToHex(tracer.getCurrentSpan().getTraceId()));
		logger.info("SpanId   >>>>>>>>>>>>>  " + Span.idToHex(tracer.getCurrentSpan().getSpanId()));
		
		rabbitTemplate.convertAndSend(queueName, reqMsg, m -> {
		    m.getMessageProperties().getHeaders().put("spanTraceId", Span.idToHex(tracer.getCurrentSpan().getTraceId()));
		    m.getMessageProperties().getHeaders().put("spanId", Span.idToHex(tracer.getCurrentSpan().getSpanId()));
//		    m.getMessageProperties().setPriority(priority);        
		    return m;
		});
		
		//rabbitTemplate.convertAndSend(queueName, reqMsg);
		
	}
	
	
	@Autowired
	Source source;
	
	@RequestMapping(value="/b/{reqMsg}")
	public void sourceApi_B(@PathVariable("reqMsg") String reqMsg) {
		
		logger.info("================== source ===================");
		logger.info("reqMsg   >>>>>>>>>>>>>> " + reqMsg);
		
		logger.info("TaraceId >>>>>>>>>>>>>  " + Span.idToHex(tracer.getCurrentSpan().getTraceId()));
		logger.info("SpanId   >>>>>>>>>>>>>  " + Span.idToHex(tracer.getCurrentSpan().getSpanId()));
		
		
		
		source.output().send(MessageBuilder.withPayload(reqMsg).build());
		
	}
	
	

}
