# Contributing to the plugin

## Legal
The license is GPL3. New significant source files (usually *.java) should carry the GPL3 license header.

## Known limitations
Similar to the pipeline plugin, not all plugins are out of the box compatible with this plugin.
There are 1000s of Jenkins plugins; majority of which are designed for Freestyle jobs in mind; thus this plugin cannot convert all of them readily.

Refer to the COMPATIBILITY.md file to know what the plugin supports currently.

## How the plugin works
The plugin uses the basic concept that Jenkins configurations rely on; the `xml` file.

Every job in Jenkins has a corresponding `config.xml` file.

The plugin will retrieve this `xml` file and parse it.

The `transformers` package in handles the various forms of transformation of configurations.
The xml tags are run through these transformer classes that in turn create native pipeline scripts if support is available; or create corresponding `batch` or `shell` scripts.

This orchestration is handled by the `transformer` class.

Once the configuration has transformed to pipeline compatible; the generated script is written in-line or committed to a Jenkinsfile depending on the options selected in the UI.

The script / Jenkinsfile CPSFlow declaration is written to a new `config.xml` file and a new pipeline job is created. 

## Contributions
To add new features / bug fixes / improvements, please create a PR; provide relevant comments for other developers to easily understand.

Naturally, ensure existing functionality does not break.

For easy structural understanding; provide relevant transformations in corresponding transformer classes e.g. Build step transformations go in `BuilderTransformer` while post-build transformations go in `PublisherTransformer`.

Support for each plugin's functionality is catered for separately in the `plugins` package. 
It is recommended to create a corresponding class to map a plugin and extend the `Plugins` class.
Override the corresponding methods that the plugin handles.

If required, you can make an entry in PluginClass enum if the XML tag of the has an arbitrary name compared to the plugin e.g. as in the case of TestNG.

The plugin classes are mapped first by the enum; if not found in enum list, then based on Reflections and class name found in XML tag.

If some plugin classes need to be ignored, create a corresponding entry in the PluginIgnoredClass enum. These plugins will not undergo any form of transformation. 

Use `Utils` classes to provide common and backend functionality. A good example of this is shown in use of `SCMTransformer` vs `SCMUtil`.
