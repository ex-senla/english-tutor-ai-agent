package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CheckLanguage;
import com.hydroyura.eta.dictionary.api.dictionary.CheckLanguageCommand;

public class CheckLanguageUseCase implements CheckLanguage {

    @Override
    public boolean check(CheckLanguageCommand command) {
        //TODO использовать WordSpecifications
        return false;
    }
}
