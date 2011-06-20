package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

import play.modules.rabbitmq.producer.*;
import sample.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void publish(String q) {
    	RabbitMQPublisher.publish("myQueue", new SampleMessage(q, q));
    	render(q);
    }

}
