package com.hydroyura.eta.student.application.usecase

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats
import com.hydroyura.eta.dictionary.api.dictionary.FindWords
import com.hydroyura.eta.student.api.lesson.FindActiveLesson
import com.hydroyura.eta.student.api.student.CefrLevel
import com.hydroyura.eta.student.api.student.StudentId
import com.hydroyura.eta.student.domain.student.Student
import com.hydroyura.eta.student.domain.student.StudentRepository
import spock.lang.Specification

class StudentQueryServiceSpec extends Specification {

    def repository = Mock(StudentRepository)
    def findWords = Mock(FindWords)
    def findActiveLesson = Mock(FindActiveLesson)
    def service = new StudentQueryService(repository, findWords, findActiveLesson)

    def "getCefrLevel возвращает уровень ученика"() {
        given:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.B1)

        when:
        def result = service.getCefrLevel(student.id)

        then:
        1 * repository.findById(student.id) >> Optional.of(student)
        result.get() == CefrLevel.B1
    }

    def "getCefrLevel возвращает empty если ученика нет"() {
        when:
        def result = service.getCefrLevel(StudentId.generate())

        then:
        1 * repository.findById(_) >> Optional.empty()
        result.isEmpty()
    }

    def "findStudentDetails включает уровень"() {
        given:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.C1)

        when:
        def details = service.findStudentDetails(student.id)

        then:
        1 * repository.findById(student.id) >> Optional.of(student)
        1 * findWords.getStats(student.dictionaryId) >> new DictionaryStats(3, 1, 1, 1)
        1 * findActiveLesson.findByStudentId(student.id) >> Optional.empty()
        details.get().name == 'Иван'
        details.get().level == CefrLevel.C1
    }
}
