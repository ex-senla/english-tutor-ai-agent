package com.hydroyura.eta.chatbot.domain.inputchain;

import com.hydroyura.eta.chatbot.domain.statemachine.Context;

public interface InputProcessor {

    boolean checkInput(Context context, String message);

    String getFailureMessage();

}
