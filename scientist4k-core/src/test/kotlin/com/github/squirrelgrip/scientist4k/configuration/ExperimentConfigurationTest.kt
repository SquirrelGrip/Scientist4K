package com.github.squirrelgrip.scientist4k.configuration

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.extensions.json.toJson
import com.github.squirrelgrip.scientist4k.model.DefaultExperimentComparator
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class ExperimentConfigurationTest {
    @Test
    fun `serialise and deserialise should return same object`() {
        val experimentConfiguration1 = ExperimentConfiguration(
                "test",
                false,
                "DROPWIZARD",
                emptyMap(),
                "prefix"
        )
        val json = experimentConfiguration1.toJson()
        val experimentConfiguration2 = json.toInstance<ExperimentConfiguration>()
        assertThat(experimentConfiguration2.name).isEqualTo(experimentConfiguration1.name)
        assertThat(experimentConfiguration2.raiseOnMismatch).isEqualTo(experimentConfiguration1.raiseOnMismatch)
        assertThat(experimentConfiguration2.metrics.javaClass).isEqualTo(experimentConfiguration1.metrics.javaClass)
        assertThat(experimentConfiguration2.context).containsAllEntriesOf(experimentConfiguration1.context)
        assertThat(experimentConfiguration2.samplePrefix).isEqualTo(experimentConfiguration1.samplePrefix)
    }
}