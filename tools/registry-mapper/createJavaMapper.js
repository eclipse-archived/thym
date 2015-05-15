#!/usr/bin/env node
'use strict';

var mapper = require('cordova-registry-mapper'),
    path   = require('path'),
    shell  = require('shelljs');

if(process.argv.length < 3 ){
	throw Error('Missing file argument. ')
}
var filePath = path.join('.',process.argv[2]);
console.log(filePath);

var javaInit = "//___\n"
var map = mapper.oldToNew;
for(var property in map){
	if(mapper.oldToNew.hasOwnProperty(property)){
		javaInit = javaInit.concat('        map.put("'+map[property]+'","'+property+'");\n');
	}
}
javaInit = javaInit.concat('        //___\n');

shell.sed('-i',/\/\/___[\s\S]*___\n/i,javaInit, filePath);
