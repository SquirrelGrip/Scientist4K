package com.github.squirrelgrip.report.model.file

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.report.exception.ExperimentResultNotFoundException
import com.github.squirrelgrip.report.model.Experiment
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import java.io.File

data class FileExperiment(
        private val experimentDirectory: File
): Experiment {
    override val name: String = experimentDirectory.name
    private var lastUpdate: Long = 0
    private val resultsMap = mutableMapOf<String, File>()

    fun update() {
        experimentDirectory.listFiles { file, _ ->
            file.lastModified() > lastUpdate
        }.forEach {
            if (it.lastModified() > lastUpdate) {
                lastUpdate = it.lastModified()
            }
            val httpExperimentResult = it.toInstance<HttpExperimentResult>()
            resultsMap[httpExperimentResult.id] = it
        }
    }

    override val results: List<String>
        get() {
            update()
            return resultsMap.keys.sorted().toList()
        }

    override fun getResult(id: String): HttpExperimentResult {
        update()
        return resultsMap[id]?.toInstance<HttpExperimentResult>() ?: throw ExperimentResultNotFoundException(id)
    }

}
