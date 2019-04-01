package com.github.json_validator_gradle_plugin

import groovy.transform.CompileStatic
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Task that validates specified JSON files according to specified JSON Schema.
 */
@CompileStatic
class JsonValidatorTask extends DefaultTask {

    /**
     * Location of schema file.
     */
    @InputFile
    File schemaFile

    /**
     * Location of files to validate.
     *
     * Can be {@link File} or {@link FileCollection}.
     *
     * @see org.gradle.api.Project#file
     * @see org.gradle.api.Project#files
     * @see org.gradle.api.Project#fileTree
     */
    @InputFiles
    Object jsonFiles

    /**
     * Creates an instance.
     */
    JsonValidatorTask() {
        group = "verification"
        description = "Validates JSON files against schema"
    }

    /**
     * Validates all specified files.
     */
    @TaskAction
    void validate() {
        Map<File, List<String>> errors = [:]
        schemaFile.withInputStream { schemaStream ->
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream))
            Schema schema = SchemaLoader.load(rawSchema)
            Set<File> files
            switch (jsonFiles) {
                case File:
                    files = [jsonFiles] as Set<File>
                    break
                case FileCollection:
                    files = (jsonFiles as FileCollection).files
                    break
                default:
                    throw new GradleException("jsonFiles should be File or FileCollection but was ${jsonFiles.class}")
            }
            files.each { jsonFile ->
                try {
                    jsonFile.withInputStream { jsonFileStream ->
                        schema.validate(new JSONObject(new JSONTokener(jsonFileStream)))
                    }
                } catch (ValidationException e) {
                    if (!errors[jsonFile]) {
                        errors[jsonFile] = []
                    }
                    errors[jsonFile].addAll(e.getAllMessages())
                }
            }
        }
        if (errors) {
            throw new GradleException("There are JSON validation errors:\n" + errors.collect { k, v ->
                "- $k\n${v.collect({ e -> "\t- $e" }).join("\n")}"
            }.join("\n"))
        }
    }

}
