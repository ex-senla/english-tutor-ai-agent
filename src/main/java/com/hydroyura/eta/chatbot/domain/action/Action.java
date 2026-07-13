package com.hydroyura.eta.chatbot.domain.action;

public sealed interface Action permits Action.Command, Action.InputParam, Action.Callback {


    record Command(String command, String userName) implements Action {}

    record InputParam(String text) implements Action {}

    record Callback(String data, int messageId) implements Action {}

}
