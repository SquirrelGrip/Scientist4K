package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.comparator.DefaultExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.configuration.ControlledHttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class ControlledHttpExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var raiseOnMismatch: Boolean = false
    private var sampleFactory: SampleFactory = SampleFactory()
    private var comparator: ExperimentComparator<ExperimentResponse?> = DefaultExperimentResponseComparator()
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var enabled: Boolean = true
    private var async: Boolean = true
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null

    constructor(controlledHttpExperimentConfiguration: ControlledHttpExperimentConfiguration) : this() {
        name = controlledHttpExperimentConfiguration.experiment.name
        metrics = controlledHttpExperimentConfiguration.experiment.metrics
        raiseOnMismatch = controlledHttpExperimentConfiguration.experiment.raiseOnMismatch
        sampleFactory = controlledHttpExperimentConfiguration.experiment.sampleFactory
        controlConfig = controlledHttpExperimentConfiguration.control
        candidateConfig = controlledHttpExperimentConfiguration.candidate
        referenceConfig = controlledHttpExperimentConfiguration.reference
        mappings = controlledHttpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): ControlledHttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): ControlledHttpExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ControlledHttpExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<ExperimentResponse?>): ControlledHttpExperimentBuilder {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ControlledHttpExperimentBuilder {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): ControlledHttpExperimentBuilder {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withControlConfig(controlConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.controlConfig = controlConfiguration
        return this
    }

    fun withCandidateConfig(candidateConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.candidateConfig = candidateConfiguration
        return this
    }

    fun withReferenceConfig(referenceConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.referenceConfig = referenceConfiguration
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledHttpExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withEnabled(enabled: Boolean): ControlledHttpExperimentBuilder {
        this.enabled = enabled
        return this
    }

    fun withAsync(async: Boolean): ControlledHttpExperimentBuilder {
        this.async = async
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): ControlledHttpExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun build(): ControlledHttpExperiment {
        if (controlConfig != null && referenceConfig != null && candidateConfig != null) {
            return ControlledHttpExperiment(name, raiseOnMismatch, metrics, comparator, sampleFactory, eventBus, enabled, async, mappings, controlConfig!!, referenceConfig!!, candidateConfig!!)
        }
        throw LaboratoryException("primaryControl, secondaryControl and candidate configurations must be set")
    }

}