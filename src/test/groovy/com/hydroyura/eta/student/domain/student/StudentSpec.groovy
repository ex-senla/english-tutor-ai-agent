package com.hydroyura.eta.student.domain.student

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId
import com.hydroyura.eta.student.api.student.CefrLevel
import com.hydroyura.eta.student.api.student.StudentId
import spock.lang.Specification

class StudentSpec extends Specification {

    def "создание ученика с уровнем"() {
        when:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.A2)

        then:
        student.id != null
        student.name == 'Иван'
        student.dictionaryId != null
        student.level == CefrLevel.A2
    }

    def "создание с null именем бросает NPE"() {
        when:
        Student.create(StudentId.generate(), DictionaryId.generate(), null, CefrLevel.A2)

        then:
        thrown(NullPointerException)
    }

    def "создание с null уровнем бросает NPE"() {
        when:
        Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', null)

        then:
        thrown(NullPointerException)
    }

    def "changeLevel меняет уровень"() {
        given:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.A2)

        when:
        student.changeLevel(CefrLevel.B1)

        then:
        student.level == CefrLevel.B1
    }

    def "changeLevel с null бросает NPE"() {
        given:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.A2)

        when:
        student.changeLevel(null)

        then:
        thrown(NullPointerException)
    }
}
