package com.github.jsonvalidatorgradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin that validates JSON files against schema.
 *
 * Registers "validateJson" task of type {@link JsonValidatorTask}.
 */
class JsonValidatorPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        target.tasks.register('validateJson', JsonValidatorTask)
    }
}
