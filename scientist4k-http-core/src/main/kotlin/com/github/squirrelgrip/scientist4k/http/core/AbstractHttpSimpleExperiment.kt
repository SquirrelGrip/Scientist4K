package com.github.squirrelgrip.scientist4k.http.core

import com.github.squirrelgrip.scientist4k.core.comparator.NoopComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.simple.SimpleExperiment
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import java.util.*
import javax.servlet.http.HttpServletRequest

open class AbstractHttpSimpleExperiment(
    name: String,
    metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    sampleFactory: SampleFactory = SampleFactory(),
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT,
    val mappingConfiguration: List<MappingConfiguration> = emptyList()
) : SimpleExperiment<ExperimentResponse>(
    name,
    metrics,
    NoopComparator(),
    sampleFactory,
    eventBus,
    experimentOptions
) {
    override fun publish(
        result: Any,
        runOptions: EnumSet<ExperimentOption>
    ) {
        if (isPublishable(runOptions)) {
            if (result is SimpleExperimentResult<*> && result.control.value is ExperimentResponse) {
                eventBus.post((result as SimpleExperimentResult<ExperimentResponse>).toHttpExperimentResult())
            } else {
                super.publish(result, runOptions)
            }
        }
    }

    fun getRunOptions(inboundRequest: HttpServletRequest): EnumSet<ExperimentOption> {
        return mappingConfiguration.firstOrNull {
            it.matches(inboundRequest.pathInfo)
        }?.options ?: ExperimentOption.DEFAULT
    }

}