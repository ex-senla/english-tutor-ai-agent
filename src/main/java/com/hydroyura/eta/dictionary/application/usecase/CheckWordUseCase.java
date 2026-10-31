package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWordCommand;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckWordUseCase implements CheckWord {

    private final WordFactory factory;

    @Override
    public boolean check(CheckWordCommand command) {
        //FIXME сомневаюсь только нормальная ли идея, что здесь происходит "моковое" создание (попытка создания без
        // сохранения результата)
        try {
            factory.create(command.value());
        } catch (WordValidationException e) {
            return false;
        }

        return true;
    }
}
