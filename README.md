# Introduction

[JSON Schema](https://json-schema.org/) can be used to define a
structure of data exchanged between two systems in JSON format.
When creating such a schema you often want to create a number
of samples to be both used as reference examples and to
verify the schema itself. The plugin allows to integrate
JSON samples verification by schema into a build process.

There is also [gradle-json-validator](https://github.com/alenkacz/gradle-json-validator)
plugin. The difference is that *gradle-json-validator* lets you
either select single JSON file or a directory with JSON files.
The problem is if you want your schema in the same directory
and having the same .json extension.

Instead, *json-validator-gradle-plugin* allows to configure
files to be validated as Gradle's *FileCollection*.
The latter can be constructed from a list of files or from a
directory tree including support for ant-style includes and excludes.
That's much more flexible. You can name and place your
schema and samples whatever and wherever you like.

# Usage

Plugin as well as its dependencies are available via 
[jitpack](https://jitpack.io).

```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.nikolay-martynov:json-validator-gradle-plugin:1.0.1'
    }
}

apply plugin: com.github.jsonvalidatorgradleplugin.JsonValidatorPlugin
```

Plugin registers default task *validateJson* that has to be configured:

Property name|Type|Description
------------------------------
schemaFile|File|JSON Schema
jsonFiles|File or FileCollection|JSON files to be validated

To validate a single file, use *file*:

```groovy
validateJson {
    schemaFile = file("src/main/resources/schema.json")
    jsonFiles = file("src/main/resources/data.json")
}
```

To validate multiple files, use *files* or *fileTree*:

```groovy
validateJson {
    schemaFile = file("src/main/resources/schema.json")
    jsonFiles = fileTree("src/main/resources/").with {
        include "*.json"
        exclude "schema.json"
        it
    }
}
```

If you have multiple schema or multiple data sets then add custom tasks:

```groovy
validateJson {
    schemaFile = file("src/main/resources/schema.json")
    jsonFiles = file("src/main/resources/data.json")
}
task validateAnotherJson(type: com.github.jsonvalidatorgradleplugin.JsonValidatorTask) {
    schemaFile = file("src/main/resources/another-schema.json")
    jsonFiles = file("src/main/resources/another-data.json")
}
```

If data does not match the schema then the task will throw an exception
with description of the issues:

```
> Task :validateJson FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':validateJson'.
> There are JSON validation errors:
  - src/main/resources/data.json
  	- #/name: string [A green door.] does not match pattern ^[\w\d\s]+$
```
