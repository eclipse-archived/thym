/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
/**
 * Utilities for working with files on the file system and inside bundles.
 * 
 * @author Gorkem Ercan
 *
 */
public final class FileUtils {
	private FileUtils(){
		//No instances
	}	
	
	/**
	 * Copies the contents for source directory to destination directory. 
	 * Source can be a directory on the file system or a jar file.
	 * Destination is a directory on the files system 
	 * 
	 * @param source - directory on the file system or jar file
	 * @param destination - a directory on the file system
	 * @throws IllegalArgumentException 
	 * 		<ul>
	 * 			<li>source or destination is null or not a file</li>
	 *  		<li>destination is not a file URL</li>
	 *  	</ul>
	 */
	public static void directoryCopy (URL source, URL destination ) throws IOException{
		checkCanCopy(source, destination);
		
		source = getFileURL(source);
		destination = getFileURL(destination);
		File dstFile = new File(destination.getFile());
		if(!dstFile.exists() && !dstFile.mkdir() ){
				return;
		}
		
		if("file".equals(source.getProtocol())){
			File srcFile = new File(source.getFile());
			copyFile(srcFile, dstFile);
			
		}else if("jar".equals(source.getProtocol())){
			ZipFile zipFile = getZipFile(source);
			String file = source.getFile();
			int exclamation = file.indexOf('!');
			String jarLocation = file.substring(exclamation + 2); // "/some/path/"
			copyFromZip(zipFile, jarLocation, dstFile);
		}	
		
	}
	
	/**
	 * Copies the contents of source file to the destination file.
	 * Source can be a file on the file system or a jar file.
	 * Destination is a file on the file system.
	 * 
	 * @param source - file on the file system or jar file
	 * @param destination - a file on the file system
	 * @throws IOException
	 * @throws IllegalArgumentException 
	 * 		<ul>
	 * 			<li>source or destination is null or not a file</li>
	 *  		<li>destination is not a file URL</li>
	 *  	</ul>
	 */
	public static void fileCopy(URL source, URL destination) throws IOException {
		checkCanCopy(source, destination);

		source = getFileURL(source);
		destination = getFileURL(destination);
		File dstFile = new File(destination.getFile());
		if( !dstFile.exists() && !dstFile.createNewFile()){
			return;
		}

		if("file".equals(source.getProtocol())){
			File srcFile = new File(source.getFile());
			copyFile(srcFile, dstFile);
			
		}else if("jar".equals(source.getProtocol())){
			ZipFile zipFile = getZipFile(source);
			String file = source.getFile();
			int exclamation = file.indexOf('!');
			String jarLocation = file.substring(exclamation + 2); // remove jar separator !/ 
			copyFromZip(zipFile, jarLocation, dstFile);
		}	

	}
	
	/**
	 * Copies the contents of a source file to the destination file. 
	 * It replaces the value pairs passed on the templatesValues while 
	 * copying. 
	 * 
	 * @param source  file on the file system or jar file
	 * @param destination file on the file system
	 * @param templateValues value pairs to be replaced
	 * @throws IOException
	 * @throws IllegalArgumentException 
	 * 		<ul>
	 * 			<li>source or destination is null or not a file</li>
	 *  		<li>destination is not a file URL</li>
	 *  	</ul>	 
	 */
	public static void templatedFileCopy(URL source, URL destination, Map<String, String> templateValues) throws IOException{
		checkCanCopy(source, destination);
		if (templateValues == null )
			throw new IllegalArgumentException("Template values can not be null");
		
		source = getFileURL(source);
		destination = getFileURL(destination);
		File dstFile = new File(destination.getFile());
		BufferedReader in = null;
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new FileWriter(dstFile));
	
			if("file".equals(source.getProtocol())){
				File srcFile = new File(source.getFile());
				in = new BufferedReader(new FileReader(srcFile));
			}
			else if("jar".equals(source.getProtocol())){
				ZipFile zipFile = getZipFile(source);
				String file = source.getFile();
				int exclamation = file.indexOf('!');
				String jarLocation = file.substring(exclamation + 2); // remove jar separator !/ 
				ZipEntry zipEntry = zipFile.getEntry(jarLocation);
				if(zipEntry == null ){
					throw new IllegalArgumentException(source + " can not be found on the zip file");
				}
				InputStream zipStream = zipFile.getInputStream(zipEntry);
				in = new BufferedReader(new InputStreamReader(zipStream));
			}
			
			 String line;
	         while ((line = in.readLine()) != null) {
	                 for (Map.Entry<String, String> entry : templateValues.entrySet()) {
	                     line = line.replace(entry.getKey(), entry.getValue());
	                 }
	                 out.write(line);
	                 out.newLine();
             }
		}finally{
			if (out != null)
				out.close();
			if (in != null )
				in.close();
		}
		
		
	}
	
	public static boolean isNewer(URL file, URL reference ){
		if(file == null || reference == null )
			throw new IllegalArgumentException("null file value");
		file = getFileURL(file);
		reference = getFileURL(reference);
		if(!isFile(file) && !isFile(reference))
			throw new IllegalArgumentException("destination is not a file URL");
		
		long fileLastModified = 0;
		long referenceLastModified = 0;
		
		if("file".equals(file.getProtocol())){
			File srcFile = new File(file.getFile());
			fileLastModified = srcFile.lastModified();
		}
		if("file".equals(reference.getProtocol())){
			File referenceFile = new File(reference.getFile());
			referenceLastModified = referenceFile.lastModified();
		}
		
		if("jar".equals(file.getProtocol())){
			ZipFile zipFile = getZipFile(file);
			ZipEntry zipEntry = getZipEntry(file, zipFile);
			if(zipEntry == null ){
				throw new IllegalArgumentException(file + " can not be found on the zip file");
			}
			fileLastModified = zipEntry.getTime();
		}
		if("jar".equals(reference.getProtocol())){
			ZipFile zipFile = getZipFile(reference);
			ZipEntry zipEntry = getZipEntry(reference, zipFile);
			if(zipEntry == null ){
				throw new IllegalArgumentException(reference+ " can not be found on the zip file");
			}
			referenceLastModified = zipEntry.getTime();
		}
		
		return fileLastModified >= referenceLastModified;
	}

	/**
	 * Convenience method to turn a file to a URL.
	 * May return null if it can not create a URL from the file passed or file is null.
	 * 
	 * @param file
	 * @return
	 */
	public static URL toURL(File file ){
		if (file == null )
			return null;
		try {
			return file.toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private static void checkCanCopy(URL source, URL destination ){
		if(source == null || destination == null )
			throw new IllegalArgumentException("null source or destination value");
		getFileURL(source);
		destination = getFileURL(destination);
		if(!isFile(destination))
			throw new IllegalArgumentException("destination is not a file URL");
	}
	
	private static boolean isFile(URL url){
		return "file".equals(url.getProtocol());
	}
	
	private static URL getFileURL( URL url ){
		try{
			url = FileLocator.resolve(url);
			return FileLocator.toFileURL(url);
		}
		catch(IOException e){
			return null;
		}
	}
	private static ZipFile getZipFile( URL url ){
		if(!"jar".equals(url.getProtocol()))
			return null;
		String file = url.getFile();
		int exclamation = file.indexOf('!');
		if (exclamation < 0)
			return null;
		URL fileUrl = null;
		try {
			fileUrl = new URL(file.substring(0, exclamation));
		} catch (MalformedURLException mue) {
			return null;
		}
		File pluginJar = new File(fileUrl.getFile());
		if (!pluginJar.exists())
			return null;
		
		try{
			ZipFile zipFile = new ZipFile(pluginJar);
			return zipFile;
		}
		catch(IOException e){
			return null;
		}
	}
	
	private static ZipEntry getZipEntry(URL file, ZipFile zipFile){
		String fileString = file.getFile();
		int exclamation = fileString.indexOf('!');
		String jarLocation = fileString.substring(exclamation + 2); // remove jar separator !/ 
		return zipFile.getEntry(jarLocation);
	}
	
	
	private static void copyFromZip(ZipFile zipFile, String locationInBundle,
			File destination) throws IOException{

	       Enumeration<? extends ZipEntry> entries = zipFile.entries();
	        while (entries.hasMoreElements()) {
	            ZipEntry zipEntry = entries.nextElement();
	            if (zipEntry.getName().startsWith(locationInBundle)) {
	            	
	            	String path = zipEntry.getName().substring(locationInBundle.length());
	            	File file = new File(destination, path);

	            	if (!zipEntry.isDirectory()) {					
	                	createFileFromZipFile(file, zipFile, zipEntry);
					} else {
						if( !file.exists() ){
							file.mkdir();
						}
					}
	            }
	        }
	
		
	}

	private static void createFileFromZipFile(File file, ZipFile zipFile,
			ZipEntry zipEntry) throws IOException {
		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()){
			throw new IOException("Can not create parent directory for " + file.toString() );
		}
		file.createNewFile();
		FileOutputStream fout = null;
		FileChannel out = null;
		InputStream in = null;
		try {
			fout = new FileOutputStream(file);
			out = fout.getChannel();
			in = zipFile.getInputStream(zipEntry);
			out.transferFrom(Channels.newChannel(in), 0, Integer.MAX_VALUE);
		} finally {
			if (out != null)
				out.close();
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	private static void copyFile(File source, File target) throws IOException {
		
	    File file = null;
	    if (source.isDirectory() && source.exists() && target.isDirectory() && target.exists()) {	      
	    	
	    	File[] children = source.listFiles();
	        for (File child : children) {
	        	file = target;
	        	if(child.isDirectory()){
	        		file = new File(target, child.getName());
	        		if(!file.exists())
	        			file.mkdir();
	        	}
	            copyFile(child, file);
	        }
	    } else {// source is a file
	    	if(target.isFile()){
	    		file = target;
	    	}else{
	    		file = new File(target, source.getName());
	    	}
	
	    	FileChannel out = null;
	        FileChannel in = null;
	        try {
	        	if(!file.exists()){
	        		file.createNewFile();
	        	}
	            
	        	 out = new FileOutputStream(file).getChannel();
	        	 in = new FileInputStream(source).getChannel();
	        	in.transferTo(0, in.size(), out);
	        }
	        catch ( IOException e){
	        	e.printStackTrace();
	        	throw e;
	        }
	        finally {
	        	if(out != null )
	        		out.close();
	        	if(in != null )
	        		in.close();
	        }
	    }
	}
	
	public static File[] untarFile(File source, File outputDir) throws IOException, TarException {
		TarFile tarFile = new TarFile(source);
		List<File> untarredFiles = new ArrayList<File>();
		try {
			for (Enumeration<TarEntry> e = tarFile.entries(); e.hasMoreElements();) {
				TarEntry entry = e.nextElement();
				InputStream input = tarFile.getInputStream(entry);
				try {
					File outFile = new File(outputDir, entry.getName());
					outFile = outFile.getCanonicalFile(); //bug 266844
					untarredFiles.add(outFile);
					if (entry.getFileType() == TarEntry.DIRECTORY) {
						outFile.mkdirs();
					} else {
						if (outFile.exists())
							outFile.delete();
						else
							outFile.getParentFile().mkdirs();
						try {
							copyStream(input, false, new FileOutputStream(outFile), true);
						} catch (FileNotFoundException e1) {
							// TEMP: ignore this for now in case we're trying to replace
							// a running eclipse.exe
						}
						outFile.setLastModified(entry.getTime());
					}
				} finally {
					input.close();
				}
			}
		} finally {
			tarFile.close();
		}
		return untarredFiles.toArray(new File[untarredFiles.size()]);
	}
	
	public static int copyStream(InputStream in, boolean closeIn, OutputStream out, boolean closeOut) throws IOException {
		try {
			int written = 0;
			byte[] buffer = new byte[16 * 1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
				written += len;
			}
			return written;
		} finally {
			try {
				if (closeIn) {
					in.close();
				}
			} finally {
				if (closeOut) {
					out.close();
				}
			}
		}
	}
	
}
