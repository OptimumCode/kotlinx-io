package kotlinx.io

import kotlin.test.Test
import kotlin.test.assertEquals


class Utf8WithByteLimitTest {
    @Test
    fun `reads empty string from empty buffer`() {
        val buffer = Buffer()
        assertEquals("", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads ascii string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        assertEquals("abc", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        assertEquals("абв", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        assertEquals("੨੩੪", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads ascii string from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("abcdefghijklmnop")
        assertEquals("abc", buffer.readStringWithLimit(3))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit`() {
        // each cyrillic letter is 2 bytes long
        // 5 bytes is a half of the 3rd letter
        val buffer = Buffer()
        buffer.writeString("абвгдежзиклмн")
        assertEquals("аб", buffer.readStringWithLimit(5))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        val buffer = Buffer()
        buffer.writeString("абвгдежзиклмн")
        assertEquals("", buffer.readStringWithLimit(1))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪੨੩੪")
        assertEquals("੨", buffer.readStringWithLimit(4))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪੨੩੪")
        assertEquals("", buffer.readStringWithLimit(2))
        assertEquals("", buffer.readStringWithLimit(1))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00", buffer.readStringWithLimit(5))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("", buffer.readStringWithLimit(3))
        assertEquals("", buffer.readStringWithLimit(2))
        assertEquals("", buffer.readStringWithLimit(1))
    }

    @Test
    fun `reads ascii string from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("abcdefgh")
        assertEquals("abc", buffer.readStringWithLimit(3))
        assertEquals("def", buffer.readStringWithLimit(3))
        assertEquals("gh", buffer.readStringWithLimit(3))
        assertEquals("", buffer.readStringWithLimit(3))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit continuously`() {
        // each letter is 2 bytes long
        // 5 bytes is a half of the 3rd letter
        val buffer = Buffer()
        buffer.writeString("абвгд")
        assertEquals("аб", buffer.readStringWithLimit(5))
        assertEquals("вг", buffer.readStringWithLimit(5))
        assertEquals("д", buffer.readStringWithLimit(5))
        assertEquals("", buffer.readStringWithLimit(3))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        assertEquals("੨", buffer.readStringWithLimit(4))
        assertEquals("੩", buffer.readStringWithLimit(4))
        assertEquals("੪", buffer.readStringWithLimit(4))
        assertEquals("", buffer.readStringWithLimit(4))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit continuously`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00", buffer.readStringWithLimit(4))
        assertEquals("\uD800\uDF01", buffer.readStringWithLimit(4))
        assertEquals("\uD800\uDF02", buffer.readStringWithLimit(4))
        assertEquals("", buffer.readStringWithLimit(4))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer`() {
        val buffer = Buffer()
        buffer.writeString("abcабв")
        assertEquals("abcабв", buffer.readStringWithLimit(1024))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("aяbюcэ")
        assertEquals("aя", buffer.readStringWithLimit(3))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("aяbюcэ")
        assertEquals("aя", buffer.readStringWithLimit(3))
        assertEquals("bю", buffer.readStringWithLimit(3))
        assertEquals("cэ", buffer.readStringWithLimit(3))
        assertEquals("", buffer.readStringWithLimit(3))
    }

    @Test
    fun `reads a long unicode string from buffer with smaller step`() {
        val originalString = "я".repeat(10_000)
        val buffer = Buffer()
        buffer.writeString(originalString)
        val result = buildString {
            while (true) {
                // the odd limit to read half of the original bytes
                val read = buffer.readStringWithLimit(1021)
                if (read.isEmpty()) break
                append(read)
            }
        }

        assertEquals(originalString, result)
    }

    @Test
    fun `ill-formed bytes in the middle does not affect remaining bytes`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("def")

        assertEquals("abc", buffer.readStringWithLimit(3))
        assertEquals("�de", buffer.readStringWithLimit(3))
        assertEquals("f", buffer.readStringWithLimit(3))
        assertEquals("", buffer.readStringWithLimit(3))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining ascii bytes`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("def")

        assertEquals("abc�", buffer.readStringWithLimit(4))
        assertEquals("def", buffer.readStringWithLimit(4))
        assertEquals("", buffer.readStringWithLimit(4))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining ascii bytes`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("def")

        assertEquals("abc�", buffer.readStringWithLimit(4))
        assertEquals("�def", buffer.readStringWithLimit(4))
        assertEquals("", buffer.readStringWithLimit(4))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining 2 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("где")

        assertEquals("абв�", buffer.readStringWithLimit(7))
        assertEquals("где", buffer.readStringWithLimit(7))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining 2 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("где")

        assertEquals("абв�", buffer.readStringWithLimit(7))
        assertEquals("�где", buffer.readStringWithLimit(7))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining 3 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("੨੩੪")

        assertEquals("੨੩੪�", buffer.readStringWithLimit(10))
        assertEquals("੨੩੪", buffer.readStringWithLimit(10))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining 3 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("੨੩੪")

        assertEquals("੨੩੪�", buffer.readStringWithLimit(10))
        assertEquals("�੨੩੪", buffer.readStringWithLimit(10))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining 4 bytes unicode`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")

        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02�", buffer.readStringWithLimit(13))
        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readStringWithLimit(13))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining 4 bytes unicode`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")

        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02�", buffer.readStringWithLimit(13))
        assertEquals("�\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readStringWithLimit(13))
        assertEquals("", buffer.readStringWithLimit(7))
    }

    @Test
    fun `reads all requested bytes in buffer ends before a valid codepoint found`() {
        val buffer = Buffer()
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())

        assertEquals("��", buffer.readStringWithLimit(2))
        assertEquals("��", buffer.readStringWithLimit(2))
        assertEquals("", buffer.readStringWithLimit(2))
    }

    @Test
    fun `reads all requested bytes if no valid codepoint found within 4 bytes`() {
        val buffer = Buffer()
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())

        assertEquals("�����", buffer.readStringWithLimit(5))
        assertEquals("�", buffer.readStringWithLimit(2))
        assertEquals("", buffer.readStringWithLimit(2))
    }
}