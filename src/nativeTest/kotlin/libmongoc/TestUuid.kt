package libmongoc

import kotlin.test.Test

class TestUuid {

    @ExperimentalUnsignedTypes
    @Test
    fun testNew() {

        for (i in 1..10) {
            println(Uuid.getNewUuid())
        }
    }
}