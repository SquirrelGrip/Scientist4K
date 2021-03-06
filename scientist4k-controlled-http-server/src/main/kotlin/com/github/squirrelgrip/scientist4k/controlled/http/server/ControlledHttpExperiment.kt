package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.controlled.AbstractControlledHttpExperiment
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.CANDIDATE_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.CONTROL_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.REFERENCE_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.createRequest
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.processResponse
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.eventbus.EventBus
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlledHttpExperiment(
    experimentConfiguration: ExperimentConfiguration,
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    private val mappingsConfiguration: MappingsConfiguration = MappingsConfiguration(),
    controlConfig: EndPointConfiguration,
    referenceConfig: EndPointConfiguration,
    private val candidateConfig: EndPointConfiguration
) : AbstractControlledHttpExperiment(
    experimentConfiguration,
    eventBus
) {
    private val controlRequestFactory = RequestFactory(controlConfig, CONTROL_COOKIE_STORE)
    private val referenceRequestFactory = RequestFactory(referenceConfig, REFERENCE_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, CANDIDATE_COOKIE_STORE, mappingsConfiguration)

    fun run(
        inboundRequest: HttpServletRequest,
        inboundResponse: HttpServletResponse,
        sample: Sample = sampleFactory.create(mappingsConfiguration.getRunOptions(inboundRequest))
    ) {
        val experimentRequest = createRequest(inboundRequest, sample)
        val controlResponse =
            if (candidateConfig.allowedMethods.contains("*") or candidateConfig.allowedMethods.contains(inboundRequest.method)) {
                run(
                    createControlRequest(experimentRequest),
                    createReferenceRequest(experimentRequest),
                    createCandidateRequest(experimentRequest),
                    sample
                )
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


