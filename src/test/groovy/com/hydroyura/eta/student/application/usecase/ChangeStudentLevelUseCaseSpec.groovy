package com.hydroyura.eta.student.application.usecase

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId
import com.hydroyura.eta.student.api.student.CefrLevel
import com.hydroyura.eta.student.api.student.ChangeStudentLevelCommand
import com.hydroyura.eta.student.api.student.StudentId
import com.hydroyura.eta.student.domain.student.Student
import com.hydroyura.eta.student.domain.student.StudentRepository
import spock.lang.Specification

class ChangeStudentLevelUseCaseSpec extends Specification {

    def "меняет уровень ученика"() {
        given:
        def student = Student.create(StudentId.generate(), DictionaryId.generate(), 'Иван', CefrLevel.A2)
        def repository = Mock(StudentRepository) {
            findById(student.id) >> Optional.of(student)
        }
        def useCase = new ChangeStudentLevelUseCase(repository)

        when:
        useCase.execute(new ChangeStudentLevelCommand(student.id, CefrLevel.B2))
        then:
        student.level == CefrLevel.B2
        1 * repository.save(student)
    }

    def "бросает исключение если ученик не найден"() {
        given:
        def repository = Mock(StudentRepository) {
            findById(_) >> Optional.empty()
        }
        def useCase = new ChangeStudentLevelUseCase(repository)

        when:
        useCase.execute(new ChangeStudentLevelCommand(StudentId.generate(), CefrLevel.A2))

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('Student not found')
    }
}
