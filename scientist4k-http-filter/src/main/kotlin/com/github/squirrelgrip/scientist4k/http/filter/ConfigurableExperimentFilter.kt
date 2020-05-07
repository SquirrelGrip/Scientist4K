package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.extension.json.toInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.*

class ConfigurableExperimentFilter : Filter {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ConfigurableExperimentFilter::class.java)
    }

    lateinit var experiment: FilterExperiment

    override fun init(filterConfig: FilterConfig) {
        val config = filterConfig.getInitParameter("config")
        val filterExperimentConfiguration = File(config).toInstance<FilterExperimentConfiguration>()
        experiment = FilterExperimentBuilder(filterExperimentConfiguration).build()
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        experiment.run(request, response, chain)
    }

}