package ru.yandex.practicum.analyzer.handler;

public interface HubEventHandler<T> {

    Class<T> getPayloadType();

    void handle(String hubId, T payload);
}