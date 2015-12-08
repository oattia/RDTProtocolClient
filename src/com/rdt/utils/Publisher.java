package com.rdt.utils;

public interface Publisher {

    void publish(Event e);

    void subscribe(Subscriber s);

    void unsubscribe(Subscriber s);
}
