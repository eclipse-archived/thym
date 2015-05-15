Registry mapper generator
=============================
This is a small utility for regenerating the mapping from 
old cordova IDs to new ones. It is used to generate the 
map on the *org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryMapper*

## Usage
Requires node.js to run.
1. Install dependencies by running```npm install```
2. Run the generator script
     

    node createJavaMapper.js ../../plugins/org.eclipse.thym.core/src/org/eclipse/thym/core/plugin/registry/CordovaPluginRegistryMapper.java