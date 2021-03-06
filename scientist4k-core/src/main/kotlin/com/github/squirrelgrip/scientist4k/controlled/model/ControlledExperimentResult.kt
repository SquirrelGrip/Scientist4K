package com.github.squirrelgrip.scientist4k.controlled.model

import com.github.squirrelgrip.scientist4k.controlled.ControlledExperiment
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample

class ControlledExperimentResult<T>(
        experiment: ControlledExperiment<T>,
        control: ExperimentObservation<T>,
        val reference: ExperimentObservation<T>?,
        candidate: ExperimentObservation<T>?,
        sample: Sample
): ExperimentResult<T>(
        experiment,
        control,
        candidate,
        sample
)