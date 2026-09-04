package com.hydroyura.eta.chatbot.domain.action;

public sealed interface Action permits Action.Command, Action.Input, Action.Callback, Action.Button {


    record Command(String command) implements Action {}

    record Input(String text) implements Action {}

    record Callback(String prefix, String payload, int messageId) implements Action {}

    record Button(String command) implements Action {}
}
