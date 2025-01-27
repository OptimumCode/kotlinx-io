package kotlinx.io.benchmarks

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.io.Buffer
import kotlinx.io.readCodePointValue
import kotlinx.io.readString
import kotlinx.io.readTo
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader

@State(Scope.Benchmark)
open class ReadCodepointBenchmarks {
    @Param("10", "100", "1000", "10000")
    var size = 0

    lateinit var buffer: Buffer
    lateinit var jvmBuffer: Reader
    lateinit var dest: ByteArray
    lateinit var jvmDest: CharArray
    lateinit var codepoints: IntArray

    @Setup
    fun setup() {
        buffer = Buffer()
        repeat(size) {
            buffer.writeByte(('a'..'z').random().code.toByte())
        }
        jvmBuffer = BufferedReader(InputStreamReader(ByteArrayInputStream(buffer.copy().readString().toByteArray())))

        dest = ByteArray(size)
        jvmDest = CharArray(size)
        codepoints = IntArray(size)
    }


    @Benchmark
    fun readStringFromBuffer(blackhole: Blackhole) {
        blackhole.consume(buffer.peek().readString(size.toLong()))
    }

    @Benchmark
    fun readBytesFromBuffer(blackhole: Blackhole) {
        buffer.peek().readTo(dest)
        blackhole.consume(dest)
    }

    @Benchmark
    fun readCharsFromReader(blackhole: Blackhole) {
        val peek = buffer.peek()
        jvmBuffer.read(jvmDest)
        blackhole.consume(peek)
        blackhole.consume(jvmDest)
    }

    @Benchmark
    fun readCodepointsFromBuffer(blackhole: Blackhole) {
        val cp = buffer.peek()
        repeat(size) {
            codepoints[it] = cp.readCodePointValue()
        }
        blackhole.consume(codepoints)
    }
}