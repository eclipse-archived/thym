/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Maps the old cordova plugin ids to the new ones on npm.
 * See the <a href="https://github.com/stevengill/cordova-registry-mapper">
 * cordova-registry-mapper</a> library for details
 * 
 * @author Gorkem Ercan
 *
 */
public final class CordovaPluginRegistryMapper {

	private static final Map<String, String>  map = new HashMap<String, String>();
	
	//Do not remove the ___ it is used by the tools to regenerate values from cordova-registry-mapper.
	static{
		//___
        map.put("cordova-plugin-battery-status","org.apache.cordova.battery-status");
        map.put("cordova-plugin-camera","org.apache.cordova.camera");
        map.put("cordova-plugin-console","org.apache.cordova.console");
        map.put("cordova-plugin-contacts","org.apache.cordova.contacts");
        map.put("cordova-plugin-device","org.apache.cordova.device");
        map.put("cordova-plugin-device-motion","org.apache.cordova.device-motion");
        map.put("cordova-plugin-device-orientation","org.apache.cordova.device-orientation");
        map.put("cordova-plugin-dialogs","org.apache.cordova.dialogs");
        map.put("cordova-plugin-file","org.apache.cordova.file");
        map.put("cordova-plugin-file-transfer","org.apache.cordova.file-transfer");
        map.put("cordova-plugin-geolocation","org.apache.cordova.geolocation");
        map.put("cordova-plugin-globalization","org.apache.cordova.globalization");
        map.put("cordova-plugin-inappbrowser","org.apache.cordova.inappbrowser");
        map.put("cordova-plugin-media","org.apache.cordova.media");
        map.put("cordova-plugin-media-capture","org.apache.cordova.media-capture");
        map.put("cordova-plugin-network-information","org.apache.cordova.network-information");
        map.put("cordova-plugin-splashscreen","org.apache.cordova.splashscreen");
        map.put("cordova-plugin-statusbar","org.apache.cordova.statusbar");
        map.put("cordova-plugin-vibration","org.apache.cordova.vibration");
        map.put("cordova-plugin-test-framework","org.apache.cordova.test-framework");
        map.put("cordova-plugin-websql","com.msopentech.websql");
        map.put("cordova-plugin-indexeddb","com.msopentech.indexeddb");
        map.put("cordova-plugin-ms-adal","com.microsoft.aad.adal");
        map.put("capptain-cordova","com.microsoft.capptain");
        map.put("cordova-plugin-ms-aad-graph","com.microsoft.services.aadgraph");
        map.put("cordova-plugin-ms-files","com.microsoft.services.files");
        map.put("cordova-plugin-ms-outlook","om.microsoft.services.outlook");
        map.put("cordova-plugin-sim","com.pbakondy.sim");
        map.put("cordova-plugin-android-support-v4","android.support.v4");
        map.put("cordova-plugin-android-support-v7-appcompat","android.support.v7-appcompat");
        map.put("cordova-plugin-googleplayservices","com.google.playservices");
        map.put("cordova-plugin-admobpro","com.google.cordova.admob");
        map.put("cordova-plugin-extension","com.rjfun.cordova.extension");
        map.put("cordova-plugin-admob","com.rjfun.cordova.plugin.admob");
        map.put("cordova-plugin-flurry","com.rjfun.cordova.flurryads");
        map.put("cordova-plugin-facebookads","com.rjfun.cordova.facebookads");
        map.put("cordova-plugin-httpd","com.rjfun.cordova.httpd");
        map.put("cordova-plugin-iad","com.rjfun.cordova.iad");
        map.put("cordova-plugin-iflyspeech","com.rjfun.cordova.iflyspeech");
        map.put("cordova-plugin-lianlianpay","com.rjfun.cordova.lianlianpay");
        map.put("cordova-plugin-mobfox","com.rjfun.cordova.mobfox");
        map.put("cordova-plugin-mopub","com.rjfun.cordova.mopub");
        map.put("cordova-plugin-mmedia","com.rjfun.cordova.mmedia");
        map.put("cordova-plugin-nativeaudio","com.rjfun.cordova.nativeaudio");
        map.put("cordova-plugin-paypalmpl","com.rjfun.cordova.plugin.paypalmpl");
        map.put("cordova-plugin-smartadserver","com.rjfun.cordova.smartadserver");
        map.put("cordova-plugin-sms","com.rjfun.cordova.sms");
        map.put("cordova-plugin-wifi","com.rjfun.cordova.wifi");
        map.put("cordova-plugin-appavailability","com.ohh2ahh.plugins.appavailability");
        map.put("cordova-plugin-fonts","org.adapt-it.cordova.fonts");
        map.put("cordova-plugin-barcodescanner","de.martinreinhardt.cordova.plugins.barcodeScanner");
        map.put("cordova-plugin-urlhandler","de.martinreinhardt.cordova.plugins.urlhandler");
        map.put("cordova-plugin-email","de.martinreinhardt.cordova.plugins.email");
        map.put("cordova-plugin-certificates","de.martinreinhardt.cordova.plugins.certificates");
        map.put("cordova-plugin-sqlite","de.martinreinhardt.cordova.plugins.sqlite");
        map.put("cordova-plugin-fileopener","fr.smile.cordova.fileopener");
        map.put("cordova-plugin-websqldatabase-initializer","org.smile.websqldatabase.initializer");
        map.put("cordova-plugin-websqldatabase","org.smile.websqldatabase.wpdb");
        map.put("aerogear-cordova-push","org.jboss.aerogear.cordova.push");
        map.put("aerogear-cordova-oauth2","org.jboss.aerogear.cordova.oauth2");
        map.put("aerogear-cordova-geo","org.jboss.aerogear.cordova.geo");
        map.put("aerogear-cordova-crypto","org.jboss.aerogear.cordova.crypto");
        map.put("aerogear-cordova-otp","org.jboss.aerogaer.cordova.otp");
        map.put("cordova-plugin-apple-watch","uk.co.ilee.applewatch");
        map.put("cordova-plugin-directions","uk.co.ilee.directions");
        map.put("cordova-plugin-game-center","uk.co.ilee.gamecenter");
        map.put("cordova-plugin-jailbreak-detection","uk.co.ilee.jailbreakdetection");
        map.put("cordova-plugin-native-transitions","uk.co.ilee.nativetransitions");
        map.put("cordova-plugin-pedometer","uk.co.ilee.pedometer");
        map.put("cordova-plugin-shake","uk.co.ilee.shake");
        map.put("cordova-plugin-touchid","uk.co.ilee.touchid");
        //___
	}
	
	/**
	 * Finds the old plug-in ID for the given new ID.
	 * @param newID
	 * @return old ID or null if there is no mapping
	 */
	public static String toOld(String newId){
		if(newId == null ) return null;
		return map.get(newId);
	}
	
	/**
	 * Finds the new plug-in ID for the given old ID.
	 * @param oldId
	 * @return new ID or null if there is no mapping
	 */
	public static String toNew(String oldId){
		if(oldId == null ) return null;
		for (Entry<String, String> entry : map.entrySet()) {
			if(entry.getValue().equals(oldId)){
				return entry.getKey();
			}
		}
		return null;
	}
}
