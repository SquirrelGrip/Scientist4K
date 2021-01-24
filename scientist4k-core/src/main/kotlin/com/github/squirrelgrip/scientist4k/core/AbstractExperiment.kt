package com.github.squirrelgrip.scientist4k.core

import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservationStatus
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

abstract class AbstractExperiment<T>(
    val name: String,
    val metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    val comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    val sampleFactory: SampleFactory = SampleFactory(),
    val eventBus: EventBus = DEFAULT_EVENT_BUS,
    val experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
) {
    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val controlTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "control")
    private val candidateTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "candidate")
    private val mismatchCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "mismatch")
    private val candidateExceptionCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "candidate.exception")
    private val totalCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "total")

    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AbstractExperiment::class.java)
        const val NAMESPACE_PREFIX = "scientist"
        val DEFAULT_EVENT_BUS: EventBus = EventBus()
    }

    open fun isSync(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.SYNC) || experimentOptions.contains(ExperimentOption.SYNC)
    open fun isReturnCandidate(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.RETURN_CANDIDATE) || experimentOptions.contains(ExperimentOption.RETURN_CANDIDATE)
    open fun isRaiseOnMismatch(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.RAISE_ON_MISMATCH) || experimentOptions.contains(ExperimentOption.RAISE_ON_MISMATCH)
    open fun isDisabled(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.DISABLED) || experimentOptions.contains(ExperimentOption.DISABLED)
    open fun isWithholdPublication(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.WITHHOLD_PUBLICATION) || experimentOptions.contains(ExperimentOption.WITHHOLD_PUBLICATION)

    open fun isPublishable(runOptions: EnumSet<ExperimentOption>) = !isWithholdPublication(runOptions)

    protected fun executeCandidate(
        candidate: () -> T?,
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): ExperimentObservation<T> =
        if (isDisabled(runOptions) && !isReturnCandidate(runOptions)) {
            scrap("candidate")
        } else {
            execute(
                "candidate",
                candidateTimer,
                candidate,
                isReturnCandidate(runOptions)
            )
        }

    protected fun executeControl(
        control: () -> T?,
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): ExperimentObservation<T> =
        if (isDisabled(runOptions) && isReturnCandidate(runOptions)) {
            scrap("control")
        } else {
            execute(
                "control",
                controlTimer,
                control,
                !isReturnCandidate(runOptions)
            )
        }

    private fun countExceptions(experimentObservation: ExperimentObservation<T>, exceptions: Counter) {
        if (experimentObservation.exception != null) {
            exceptions.increment()
        }
    }

    protected fun execute(
        name: String,
        timer: Timer,
        function: () -> T?,
        shouldThrow: Boolean
    ): ExperimentObservation<T> {
        val observation = ExperimentObservation<T>(name, timer)
        observation.time(function)
        val exception = observation.exception
        if (exception != null && shouldThrow) {
            throw exception
        }
        return observation
    }

    protected fun scrap(name: String): ExperimentObservation<T> {
        val experimentObservation: ExperimentObservation<T> = ExperimentObservation(name)
        experimentObservation.status = ExperimentObservationStatus.SCRAPPED
        return experimentObservation
    }

    open fun publish(result: Any, runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT) {
        if (isPublishable(runOptions)) {
            eventBus.post(result)
        }
    }

    fun compare(control: ExperimentObservation<T>, candidate: ExperimentObservation<T>): ComparisonResult {
        countExceptions(candidate, candidateExceptionCount)
        return if (candidate.exception != null) {
            ComparisonResult("Candidate threw an exception.")
        } else {
            LOGGER.trace("Comparing\n{}\n{}", control.value, candidate.value)
            comparator.invoke(control.value, candidate.value)
        }.apply {
            LOGGER.debug("Compared...match={}", this.matches)
            totalCount.increment()
            if (!matches) {
                mismatchCount.increment()
            }
        }
    }

}