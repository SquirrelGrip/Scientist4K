package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class HttpExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var sampleFactory: SampleFactory = SampleFactory()
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var enabled: Boolean = true
    private var async: Boolean = true
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
        controlConfig = httpExperimentConfiguration.control
        candidateConfig = httpExperimentConfiguration.candidate
        mappings = httpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): HttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): HttpExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): HttpExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): HttpExperimentBuilder {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withControlConfig(controlConfiguration: EndPointConfiguration): HttpExperimentBuilder {
        this.controlConfig = controlConfiguration
        return this
    }

    fun withCandidateConfig(candidateConfiguration: EndPointConfiguration): HttpExperimentBuilder {
        this.candidateConfig = candidateConfiguration
        return this
    }

    fun withEventBus(eventBus: EventBus): HttpExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): HttpExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun withEnabled(enabled: Boolean): HttpExperimentBuilder {
        this.enabled = enabled
        return this
    }

    fun withAsync(async: Boolean): HttpExperimentBuilder {
        this.async = async
        return this
    }

    fun build(): HttpSimpleExperiment {
        if (controlConfig != null && candidateConfig != null) {
            return HttpSimpleExperiment(name, metrics, sampleFactory, eventBus, mappings, enabled, async, controlConfig!!, candidateConfig!!)
        }
        throw LaboratoryException("Both control and candidate configurations must be set")
    }

}