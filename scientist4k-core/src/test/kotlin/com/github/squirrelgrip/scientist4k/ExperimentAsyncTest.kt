package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.exceptions.MismatchException
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class ExperimentAsyncTest {
    private fun exceptionThrowingFunction(): Int {
        throw RuntimeException("throw an exception")
    }

    private fun sleepFunction(): Int {
        Thread.sleep(1001)
        return 3
    }

    private fun shortSleepFunction(): Int {
        Thread.sleep(101)
        return 3
    }

    private fun safeFunction(): Int {
        return 3
    }

    private fun safeFunctionWithDifferentResult(): Int {
        return 4
    }

    @Test
    fun itThrowsAnExceptionWhenControlFails() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        assertThrows(RuntimeException::class.java) {
            experiment.runAsync({ exceptionThrowingFunction() }, { exceptionThrowingFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runAsync({ safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        assertThrows(MismatchException::class.java) {
            experiment.runAsync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        val value = experiment.runAsync({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        val value = experiment.run({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun asyncRunsFaster() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        val date1 = Date()
        val value = experiment.runAsync({ sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        val difference = date2.time - date1.time
        assertThat(difference).isLessThan(2000)
        assertThat(difference).isGreaterThanOrEqualTo(1000)
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun raiseOnMismatchRunsSlower() {
        val raisesOnMismatch = Experiment<Int>("raise", true, NoopMetricsProvider())
        val doesNotRaiseOnMismatch = Experiment<Int>("does not raise", NoopMetricsProvider())
        val raisesExecutionTime = timeExperiment(raisesOnMismatch)
        val doesNotRaiseExecutionTime = timeExperiment(doesNotRaiseOnMismatch)
        assertThat(raisesExecutionTime).isGreaterThan(doesNotRaiseExecutionTime)
        assertThat(raisesExecutionTime).isGreaterThan(1000)
        assertThat(doesNotRaiseExecutionTime).isLessThan(200)
    }

    private fun timeExperiment(experiment: Experiment<Int>): Long {
        val date1 = Date()
        experiment.runAsync({ shortSleepFunction() }, { sleepFunction() })
        val date2 = Date()
        return date2.time - date1.time
    }
}