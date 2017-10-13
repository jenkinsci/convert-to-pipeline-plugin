# Convert to Pipeline Plugin

## About the plugin
This plugin converts FreeStyle jobs to scripted Pipeline jobs.

## Target problem
If you are using Jenkins to orchestrate application releases, at any point in time, there would be a series of freestyle jobs chained together based on specific criteria to perform builds.

To convert these freestyle jobs to pipeline, DevOps engineers will have to manually drill down to each of the 100s or 1000s of jobs, understand the tools, configurations, URLs, and parameters etc. and re-write those in pipeline syntax. This manual effort not only involves converting individual job logic but also requires to ensure that the chain is converted to a single pipeline while keeping a base-rule of 1 chain = 1 pipeline. However, as the number of jobs increases, it becomes extremely difficult to convert the freestyle jobs to coded pipelines.

This plugin drastically reduces the effort behind this manual process. Now, DevOps engineers can auto-generate the script for this conversion. The plugin can be further modified to create scripts that adhere to each organization's coding standards, separates complex business logic and standard declaration from execution flow declaration and accelerates the transition process of any new set of applications being on-boarded to pipeline.

## Compatibility
Refer to the COMPATIBILITY.md file to know the current compatibility / features of the plugin.

## Contributing
Refer to the CONTRIBUTING.md file to know how to contribute to this plugin's development.

See the [Plugin Wiki](https://wiki.jenkins.io/display/JENKINS/Convert+To+Pipeline+Plugin) for other information and usage documentation.