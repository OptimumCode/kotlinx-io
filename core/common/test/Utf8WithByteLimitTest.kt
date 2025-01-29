package kotlinx.io

import kotlin.test.Test
import kotlin.test.assertEquals


class Utf8WithByteLimitTest {
    @Test
    fun `reads empty string from empty buffer`() {
        val buffer = Buffer()
        assertEquals("", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads ascii string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        assertEquals("abc", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        assertEquals("абв", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        assertEquals("੨੩੪", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads ascii string from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("abcdefghijklmnop")
        assertEquals("abc", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit`() {
        // each cyrillic letter is 2 bytes long
        // 5 bytes is a half of the 3rd letter
        val buffer = Buffer()
        buffer.writeString("абвгдежзиклмн")
        assertEquals("аб", buffer.readUtf8WithLimit(5))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        val buffer = Buffer()
        buffer.writeString("абвгдежзиклмн")
        assertEquals("", buffer.readUtf8WithLimit(1))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪੨੩੪")
        assertEquals("੨", buffer.readUtf8WithLimit(4))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪੨੩੪")
        assertEquals("", buffer.readUtf8WithLimit(2))
        assertEquals("", buffer.readUtf8WithLimit(1))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00", buffer.readUtf8WithLimit(5))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit less than size of one codepoint`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("", buffer.readUtf8WithLimit(3))
        assertEquals("", buffer.readUtf8WithLimit(2))
        assertEquals("", buffer.readUtf8WithLimit(1))
    }

    @Test
    fun `reads ascii string from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("abcdefgh")
        assertEquals("abc", buffer.readUtf8WithLimit(3))
        assertEquals("def", buffer.readUtf8WithLimit(3))
        assertEquals("gh", buffer.readUtf8WithLimit(3))
        assertEquals("", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `reads 2 bytes unicode string from buffer up to the limit continuously`() {
        // each letter is 2 bytes long
        // 5 bytes is a half of the 3rd letter
        val buffer = Buffer()
        buffer.writeString("абвгд")
        assertEquals("аб", buffer.readUtf8WithLimit(5))
        assertEquals("вг", buffer.readUtf8WithLimit(5))
        assertEquals("д", buffer.readUtf8WithLimit(5))
        assertEquals("", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `reads 3 bytes unicode string from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        assertEquals("੨", buffer.readUtf8WithLimit(4))
        assertEquals("੩", buffer.readUtf8WithLimit(4))
        assertEquals("੪", buffer.readUtf8WithLimit(4))
        assertEquals("", buffer.readUtf8WithLimit(4))
    }

    @Test
    fun `reads 4 bytes unicode string from buffer up to the limit continuously`() {
        // https://www.compart.com/en/unicode/U+10300
        // https://www.compart.com/en/unicode/U+10301
        // https://www.compart.com/en/unicode/U+10302
        val buffer = Buffer()
        buffer.writeString("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
        assertEquals("\uD800\uDF00", buffer.readUtf8WithLimit(4))
        assertEquals("\uD800\uDF01", buffer.readUtf8WithLimit(4))
        assertEquals("\uD800\uDF02", buffer.readUtf8WithLimit(4))
        assertEquals("", buffer.readUtf8WithLimit(4))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer`() {
        val buffer = Buffer()
        buffer.writeString("abcабв")
        assertEquals("abcабв", buffer.readUtf8WithLimit(1024))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer up to the limit`() {
        val buffer = Buffer()
        buffer.writeString("aяbюcэ")
        assertEquals("aя", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `reads mix of unicode and ascii from buffer up to the limit continuously`() {
        val buffer = Buffer()
        buffer.writeString("aяbюcэ")
        assertEquals("aя", buffer.readUtf8WithLimit(3))
        assertEquals("bю", buffer.readUtf8WithLimit(3))
        assertEquals("cэ", buffer.readUtf8WithLimit(3))
        assertEquals("", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `reads a long unicode string from buffer with smaller step`() {
        val originalString = "я".repeat(10_000)
        val buffer = Buffer()
        buffer.writeString(originalString)
        val result = buildString {
            while (true) {
                // the odd limit to read half of the original bytes
                val read = buffer.readUtf8WithLimit(1021)
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

        assertEquals("abc", buffer.readUtf8WithLimit(3))
        assertEquals("�de", buffer.readUtf8WithLimit(3))
        assertEquals("f", buffer.readUtf8WithLimit(3))
        assertEquals("", buffer.readUtf8WithLimit(3))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining ascii bytes`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("def")

        assertEquals("abc�", buffer.readUtf8WithLimit(4))
        assertEquals("def", buffer.readUtf8WithLimit(4))
        assertEquals("", buffer.readUtf8WithLimit(4))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining ascii bytes`() {
        val buffer = Buffer()
        buffer.writeString("abc")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("def")

        assertEquals("abc�", buffer.readUtf8WithLimit(4))
        assertEquals("�def", buffer.readUtf8WithLimit(4))
        assertEquals("", buffer.readUtf8WithLimit(4))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining 2 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("где")

        assertEquals("абв�", buffer.readUtf8WithLimit(7))
        assertEquals("где", buffer.readUtf8WithLimit(7))
        assertEquals("", buffer.readUtf8WithLimit(7))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining 2 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("абв")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("где")

        assertEquals("абв�", buffer.readUtf8WithLimit(7))
        assertEquals("�где", buffer.readUtf8WithLimit(7))
        assertEquals("", buffer.readUtf8WithLimit(7))
    }

    @Test
    fun `ill-formed byte at the boundary of request does not affect remaining 3 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("੨੩੪")

        assertEquals("੨੩੪�", buffer.readUtf8WithLimit(10))
        assertEquals("੨੩੪", buffer.readUtf8WithLimit(10))
        assertEquals("", buffer.readUtf8WithLimit(7))
    }

    @Test
    fun `ill-formed bytes at the boundary of request does not affect remaining 3 bytes unicode`() {
        val buffer = Buffer()
        buffer.writeString("੨੩੪")
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeString("੨੩੪")

        assertEquals("੨੩੪�", buffer.readUtf8WithLimit(10))
        assertEquals("�੨੩੪", buffer.readUtf8WithLimit(10))
        assertEquals("", buffer.readUtf8WithLimit(7))
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

        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02�", buffer.readUtf8WithLimit(13))
        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readUtf8WithLimit(13))
        assertEquals("", buffer.readUtf8WithLimit(7))
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

        assertEquals("\uD800\uDF00\uD800\uDF01\uD800\uDF02�", buffer.readUtf8WithLimit(13))
        assertEquals("�\uD800\uDF00\uD800\uDF01\uD800\uDF02", buffer.readUtf8WithLimit(13))
        assertEquals("", buffer.readUtf8WithLimit(7))
    }

    @Test
    fun `reads all requested bytes in buffer ends before a valid codepoint found`() {
        val buffer = Buffer()
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())
        buffer.writeByte(0xBB.toByte())

        assertEquals("��", buffer.readUtf8WithLimit(2))
        assertEquals("��", buffer.readUtf8WithLimit(2))
        assertEquals("", buffer.readUtf8WithLimit(2))
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

        assertEquals("�����", buffer.readUtf8WithLimit(5))
        assertEquals("�", buffer.readUtf8WithLimit(2))
        assertEquals("", buffer.readUtf8WithLimit(2))
    }
}