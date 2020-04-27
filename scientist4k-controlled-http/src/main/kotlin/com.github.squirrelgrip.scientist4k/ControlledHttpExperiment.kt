package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.HttpExperimentUtil.CANDIDATE_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.HttpExperimentUtil.CONTROL_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.HttpExperimentUtil.REFERENCE_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.HttpExperimentUtil.createRequest
import com.github.squirrelgrip.scientist4k.HttpExperimentUtil.processResponse
import com.github.squirrelgrip.scientist4k.comparator.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlledHttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<ExperimentResponse?> = ExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        eventBus: EventBus = EventBus(),
        mappings: List<MappingConfiguration> = emptyList(),
        controlConfig: EndPointConfiguration,
        referenceConfig: EndPointConfiguration,
        private val candidateConfig: EndPointConfiguration
) : ControlledExperiment<ExperimentResponse>(
        name,
        raiseOnMismatch,
        metrics,
        context,
        comparator,
        sampleFactory,
        eventBus
) {
    private val controlRequestFactory = RequestFactory(controlConfig, CONTROL_COOKIE_STORE)
    private val referenceRequestFactory = RequestFactory(referenceConfig, REFERENCE_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, CANDIDATE_COOKIE_STORE, mappings)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlledHttpExperiment::class.java)
    }

    fun run(
            inboundRequest: HttpServletRequest,
            inboundResponse: HttpServletResponse,
            sample: Sample = sampleFactory.create()
    ) {
        val experimentRequest = createRequest(inboundRequest, sample)
        val controlResponse = if (candidateConfig.allowedMethods.contains("*") or candidateConfig.allowedMethods.contains(inboundRequest.method)) {
            run(createControlRequest(experimentRequest), createReferenceRequest(experimentRequest), createCandidateRequest(experimentRequest), sample)
        } else {
            createControlRequest(experimentRequest).invoke()
        }
        processResponse(inboundResponse, controlResponse)
    }

    private fun createControlRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return controlRequestFactory.create(request)
    }

    private fun createReferenceRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return referenceRequestFactory.create(request)
    }

    private fun createCandidateRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }
}


