package com.github.json_validator_gradle_plugin


import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class JsonValidatorTaskIntegrationTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'com.github.json-validator-gradle-plugin'
}
"""
    }

    def "can complete successfully"() {
        buildFile << """
validateJson {
    schemaFile = file("${new File("src/test/resources/schema.json").absolutePath}")
    jsonFiles = file("${new File("src/test/resources/valid.json").absolutePath}")
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("validateJson")
                .build()

        then:
        result.task(":validateJson").outcome == TaskOutcome.SUCCESS
        !result.output.contains("valid.json")
    }

    def "parameters are mandatory"() {
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("validateJson")
                .buildAndFail()

        then:
        result.task(":validateJson").outcome == TaskOutcome.FAILED
        result.output.contains("schemaFile")
        result.output.contains("jsonFiles")
    }

    def "prints file name and all errors"() {
        buildFile << """
validateJson {
    schemaFile = file("${new File("src/test/resources/schema.json").absolutePath}")
    jsonFiles = file("${new File("src/test/resources/malformed.json").absolutePath}")
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("validateJson")
                .buildAndFail()

        then:
        result.task(":validateJson").outcome == TaskOutcome.FAILED
        result.output.contains("malformed.json")
        result.output.contains("price")
        result.output.contains("tags")
    }

    def "can validate multiple files and use exclusions"() {
        buildFile << """
validateJson {
    schemaFile = file("${new File("src/test/resources/schema.json").absolutePath}")
    jsonFiles = fileTree("${new File("src/test/resources/").absolutePath}").with {
        include "*.json"
        exclude "schema.json"
        it
    }
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("validateJson")
                .buildAndFail()

        then:
        result.task(":validateJson").outcome == TaskOutcome.FAILED
        !result.output.contains("valid.json")
        result.output.contains("malformed.json")
    }

    def "can use with multiple schemas"() {
        buildFile << """
validateJson {
    schemaFile = file("${new File("src/test/resources/schema.json").absolutePath}")
    jsonFiles = file("${new File("src/test/resources/valid.json").absolutePath}")
}
task validateAnotherJson(type: ${JsonValidatorTask.class.name}) {
    schemaFile = file("${new File("src/test/resources/anotherSchema.json").absolutePath}")
    jsonFiles = file("${new File("src/test/resources/malformed.json").absolutePath}")
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("validateJson", "validateAnotherJson")
                .build()

        then:
        result.task(":validateJson").outcome == TaskOutcome.SUCCESS
        result.task(":validateAnotherJson").outcome == TaskOutcome.SUCCESS
    }
}
