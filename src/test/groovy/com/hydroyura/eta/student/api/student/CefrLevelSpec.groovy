package com.hydroyura.eta.student.api.student

import spock.lang.Specification
import spock.lang.Unroll

class CefrLevelSpec extends Specification {

    @Unroll
    def "код уровня #level равен '#code'"() {
        expect:
        level.code() == code

        where:
        level        | code
        CefrLevel.A1 | 'A1'
        CefrLevel.A2 | 'A2'
        CefrLevel.B1 | 'B1'
        CefrLevel.B2 | 'B2'
        CefrLevel.C1 | 'C1'
        CefrLevel.C2 | 'C2'
    }

    def "fromCode возвращает уровень для валидного кода"() {
        expect:
        CefrLevel.fromCode('A2').get() == CefrLevel.A2
    }

    @Unroll
    def "fromCode возвращает empty для невалидного кода: #code"() {
        expect:
        CefrLevel.fromCode(code).isEmpty()

        where:
        code << ['ZZ', null, '  ']
    }
}
