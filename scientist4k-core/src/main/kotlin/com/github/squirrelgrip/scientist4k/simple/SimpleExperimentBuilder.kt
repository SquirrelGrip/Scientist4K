package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class SimpleExperimentBuilder<T>(
        private var name: String = "Test",
        private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        private var raiseOnMismatch: Boolean = false,
        private var sampleFactory: SampleFactory = SampleFactory(),
        private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
) {
    constructor(experimentConfiguration: ExperimentConfiguration): this(
        experimentConfiguration.name,
        experimentConfiguration.metrics,
        experimentConfiguration.raiseOnMismatch,
        experimentConfiguration.sampleFactory
    )

    fun withName(name: String): SimpleExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): SimpleExperimentBuilder<T> {
        this.metricsProvider = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): SimpleExperimentBuilder<T> {
        this.metricsProvider = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T?>): SimpleExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): SimpleExperimentBuilder<T> {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): SimpleExperimentBuilder<T> {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withEventBus(eventBus: EventBus): SimpleExperimentBuilder<T> {
        this.eventBus = eventBus
        return this
    }

    fun build(): SimpleExperiment<T> {
        return SimpleExperiment(name, raiseOnMismatch, metricsProvider, comparator, sampleFactory, eventBus)
    }

}