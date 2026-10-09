package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import java.util.List;
import java.util.Set;

public final class DemoWordCatalog {

    private static final List<DemoWord> WORDS = List.of(
            new DemoWord("apple", Set.of("яблоко"), PartOfSpeech.NOUN),
            new DemoWord("book", Set.of("книга"), PartOfSpeech.NOUN),
            new DemoWord("house", Set.of("дом"), PartOfSpeech.NOUN),
            new DemoWord("water", Set.of("вода"), PartOfSpeech.NOUN),
            new DemoWord("friend", Set.of("друг"), PartOfSpeech.NOUN),
            new DemoWord("time", Set.of("время"), PartOfSpeech.NOUN),
            new DemoWord("city", Set.of("город"), PartOfSpeech.NOUN),
            new DemoWord("day", Set.of("день"), PartOfSpeech.NOUN),
            new DemoWord("table", Set.of("стол"), PartOfSpeech.NOUN),
            new DemoWord("chair", Set.of("стул"), PartOfSpeech.NOUN),
            new DemoWord("door", Set.of("дверь"), PartOfSpeech.NOUN),
            new DemoWord("window", Set.of("окно"), PartOfSpeech.NOUN),
            new DemoWord("car", Set.of("машина"), PartOfSpeech.NOUN),
            new DemoWord("tree", Set.of("дерево"), PartOfSpeech.NOUN),
            new DemoWord("school", Set.of("школа"), PartOfSpeech.NOUN),
            new DemoWord("family", Set.of("семья"), PartOfSpeech.NOUN),
            new DemoWord("money", Set.of("деньги"), PartOfSpeech.NOUN),
            new DemoWord("morning", Set.of("утро"), PartOfSpeech.NOUN),
            new DemoWord("night", Set.of("ночь"), PartOfSpeech.NOUN),
            new DemoWord("world", Set.of("мир"), PartOfSpeech.NOUN),

            new DemoWord("run", Set.of("бежать"), PartOfSpeech.VERB),
            new DemoWord("read", Set.of("читать"), PartOfSpeech.VERB),
            new DemoWord("write", Set.of("писать"), PartOfSpeech.VERB),
            new DemoWord("speak", Set.of("говорить"), PartOfSpeech.VERB),
            new DemoWord("go", Set.of("идти"), PartOfSpeech.VERB),
            new DemoWord("see", Set.of("видеть"), PartOfSpeech.VERB),
            new DemoWord("eat", Set.of("есть"), PartOfSpeech.VERB),
            new DemoWord("drink", Set.of("пить"), PartOfSpeech.VERB),
            new DemoWord("sleep", Set.of("спать"), PartOfSpeech.VERB),
            new DemoWord("play", Set.of("играть"), PartOfSpeech.VERB),
            new DemoWord("study", Set.of("учиться"), PartOfSpeech.VERB),
            new DemoWord("help", Set.of("помогать"), PartOfSpeech.VERB),
            new DemoWord("love", Set.of("любить"), PartOfSpeech.VERB),
            new DemoWord("know", Set.of("знать"), PartOfSpeech.VERB),
            new DemoWord("think", Set.of("думать"), PartOfSpeech.VERB),

            new DemoWord("big", Set.of("большой"), PartOfSpeech.ADJECTIVE),
            new DemoWord("small", Set.of("маленький"), PartOfSpeech.ADJECTIVE),
            new DemoWord("good", Set.of("хороший"), PartOfSpeech.ADJECTIVE),
            new DemoWord("new", Set.of("новый"), PartOfSpeech.ADJECTIVE),
            new DemoWord("old", Set.of("старый"), PartOfSpeech.ADJECTIVE),
            new DemoWord("long", Set.of("длинный"), PartOfSpeech.ADJECTIVE),
            new DemoWord("happy", Set.of("счастливый"), PartOfSpeech.ADJECTIVE),
            new DemoWord("sad", Set.of("грустный"), PartOfSpeech.ADJECTIVE),
            new DemoWord("fast", Set.of("быстрый"), PartOfSpeech.ADJECTIVE),
            new DemoWord("slow", Set.of("медленный"), PartOfSpeech.ADJECTIVE),
            new DemoWord("hot", Set.of("горячий"), PartOfSpeech.ADJECTIVE),
            new DemoWord("cold", Set.of("холодный"), PartOfSpeech.ADJECTIVE),
            new DemoWord("young", Set.of("молодой"), PartOfSpeech.ADJECTIVE),
            new DemoWord("beautiful", Set.of("красивый"), PartOfSpeech.ADJECTIVE),
            new DemoWord("important", Set.of("важный"), PartOfSpeech.ADJECTIVE)
    );

    private DemoWordCatalog() {
    }

    public static List<DemoWord> words() {
        return WORDS;
    }
}
