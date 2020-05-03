package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.CandidateHandler
import com.github.squirrelgrip.scientist4k.http.test.servlet.ControlServlet
import com.google.common.eventbus.Subscribe
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.awaitility.Awaitility
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.DispatcherType

internal class FilterExperimentTest {

    companion object {
        private val HTTP_CONTROL_URL = "http://localhost:9003"
        private val HTTPS_CONTROL_URL = "https://localhost:9004"

        val filterExperimentConfiguration = File("filter-experiment-config.json").toInstance<FilterExperimentConfiguration>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val chetiConfiguration = Thread.currentThread().contextClassLoader.getResourceAsStream("cheti.json")
            val cheti = Cheti(chetiConfiguration)
            cheti.execute()

            val holder: FilterHolder = FilterHolder(FilterExperiment::class.java)
            holder.name = "Experiment Filter"
            holder.initParameters = mapOf(
                    "config" to "filter-experiment-config.json"
            )

            val context = ServletContextHandler(ServletContextHandler.SESSIONS).apply {
                contextPath = "/"
                addServlet(ControlServlet::class.java, "/")
                addFilter(holder, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST))
            }
            val controlServer = SecuredServer(ControlServlet.serverConfiguration, context)
            controlServer.start()

            val candidateServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
            candidateServer.start()
        }

        fun isRunning(url: String): Boolean {
            val sslConfiguration = filterExperimentConfiguration.candidate.sslConfiguration
            val httpClient = HttpClients.custom().setSSLSocketFactory(
                    SSLConnectionSocketFactory(
                            sslConfiguration!!.sslContext(),
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                    )
            ).build()
            val request = RequestBuilder.get(url).build()
            val response = httpClient.execute(request)
            return response.statusLine.statusCode == 200
        }
    }

    init {
        AbstractExperiment.DEFAULT_EVENT_BUS.register(this)
    }

    var actualExperimentResult: MutableList<ExperimentResult<ExperimentResponse>> = mutableListOf()

    @Subscribe
    fun receiveResult(experimentResult: ExperimentResult<ExperimentResponse>) {
        println(experimentResult.sample.notes["request"])
        actualExperimentResult.add(experimentResult)
    }


    @BeforeEach
    fun beforeEach() {
        actualExperimentResult.clear()
    }

    private fun getResult(uri: String): ExperimentResult<ExperimentResponse>? {
        return actualExperimentResult.firstOrNull {
            it.sample.notes["uri"] == uri
        }
    }

    private fun awaitResult(url: String): ExperimentResult<ExperimentResponse> {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
            getResult(url) != null
        }
        val result = getResult(url)
        Assertions.assertNotNull(result)
        return result!!
    }

    @Test
    fun filter() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/ok")).isTrue()

        val result = awaitResult("/ok")
        AssertionsForInterfaceTypes.assertThat(result.match.matches).isTrue()
        AssertionsForInterfaceTypes.assertThat(result.match.failureReasons).isEmpty()
    }

}