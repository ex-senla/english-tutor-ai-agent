package com.hydroyura.eta.student.application.usecase

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId
import com.hydroyura.eta.student.api.student.CefrLevel
import com.hydroyura.eta.student.api.student.CreateStudentCommand
import com.hydroyura.eta.student.domain.student.Student
import com.hydroyura.eta.student.domain.student.StudentRepository
import spock.lang.Specification

class CreateStudentUseCaseSpec extends Specification {

    def "создаёт ученика с уровнем и сохраняет в репозиторий"() {
        given:
        def repository = Mock(StudentRepository)
        def useCase = new CreateStudentUseCase(repository)
        def dictionaryId = DictionaryId.generate()

        when:
        def studentId = useCase.execute(new CreateStudentCommand('Иван', dictionaryId, CefrLevel.B1))

        then:
        1 * repository.save({ it.name == 'Иван' && it.level == CefrLevel.B1 && it.dictionaryId == dictionaryId }) >>
                { Student s -> s }
        studentId != null
    }

    def "отклоняет пустое имя"() {
        given:
        def repository = Mock(StudentRepository)
        def useCase = new CreateStudentUseCase(repository)

        when:
        useCase.execute(new CreateStudentCommand('  ', DictionaryId.generate(), CefrLevel.A2))

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('blank')
        0 * repository.save(_)
    }
}
