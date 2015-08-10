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
package org.eclipse.thym.core.test;

import static org.eclipse.thym.core.internal.util.FileUtils.directoryCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.fileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.templatedFileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.toURL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.hybrid.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("restriction")
public class FileUtilsTest {
	private static File tempDir;
	private static File plainFile;
	private static File jarFile;	
	@BeforeClass
	public static void setFiles() throws IOException{
		tempDir = TestUtils.getTempDirectory();
		plainFile = TestUtils.createTempFile("plain.file");
		jarFile = TestUtils.createTempFile("fileTest.jar");
	}

	private boolean deleteDirRecursively(File dir) 
	{ 
		if (dir.isDirectory()) 
		{ 
			String[] children = dir.list(); 
			for (int i=0; i<children.length; i++)
			{ 
				boolean success = deleteDirRecursively(new File(dir, children[i])); 
				if (!success) 
				{  
					return false; 
				} 
			} 
		}
	  return dir.delete(); 
	} 
		
	@Test
	public void testDirectoryCopy() throws IOException{
		File resultFile = new File(tempDir, "dirCopyTest");
		if(resultFile.exists()){
			deleteDirRecursively(resultFile);
			assertFalse("Leftover file: "+resultFile.toString()+" from earlier runs could not be deleted",resultFile.exists());
		}
		
		URL url = new URL("jar:"+toURL(jarFile)+"!/dir/");
		directoryCopy(url, toURL(resultFile) );
		
		assertTrue(resultFile.exists());
		File fileIn = new File(resultFile,"dummy2.file");
		assertTrue(fileIn.exists());
		File nestedDir = new File(resultFile, "nesteddir");
		assertTrue(nestedDir.exists());
		File nestedFile = new File(nestedDir,"nested.file");
		nestedFile.delete();
		nestedDir.delete();
		fileIn.delete();
		resultFile.delete();
	}
	
	@Test
	public void testFileCopy() throws IOException{
		File resultFile = new File(tempDir, "fileCopy.file");
		if(resultFile.exists()){
			deleteDirRecursively(resultFile);
			assertFalse("Leftover file: "+resultFile.toString()+" from earlier runs could not be deleted",resultFile.exists());
		}
		
		URL url = new URL("jar:"+toURL(jarFile)+"!/dir/dummy2.file");
		
		fileCopy(url, toURL(resultFile) );
		assertTrue(resultFile.exists());
		resultFile.delete();
		assertFalse(resultFile.exists());
		fileCopy(toURL(plainFile), toURL(resultFile));
		assertTrue(resultFile.exists());
		resultFile.delete();
	}
	
	@Test
	public void testNoParentFileCopy() throws IOException{
		File resultFile = new File(tempDir, Long.toHexString(System.currentTimeMillis())+ "/fileCopy.file");
		if(resultFile.exists()){
			deleteDirRecursively(resultFile);
			assertFalse("Leftover file: "+resultFile.toString()+" from earlier runs could not be deleted",resultFile.exists());
		}
		
		URL url = new URL("jar:"+toURL(jarFile)+"!/dir/dummy2.file");
		
		fileCopy(url, toURL(resultFile) );
		assertTrue(resultFile.exists());
		resultFile.delete();
		assertFalse(resultFile.exists());
		fileCopy(toURL(plainFile), toURL(resultFile));
		assertTrue(resultFile.exists());
		resultFile.delete();
	}
	

	@Test
	public void testTemplateCopy() throws IOException{
		File resultFile = new File(tempDir, "templatedCopy.file");
		if(resultFile.exists()){
			deleteDirRecursively(resultFile);
			assertFalse("Leftover file: "+resultFile.toString()+" from earlier runs could not be deleted",resultFile.exists());
		}
		
		URL url = new URL("jar:"+toURL(jarFile)+"!/templated.file");
		HashMap<String, String> values = new HashMap<String, String>();
		String timeStamp = Long.toString(System.currentTimeMillis());
		values.put("__VALUE__", timeStamp);
		templatedFileCopy(url, toURL(resultFile),values);
		assertTrue(resultFile.exists());
		BufferedReader reader =null;
		boolean found = false;
		try{
		    reader = new BufferedReader(new FileReader(resultFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if(found = line.contains(timeStamp))
					break;
			}
		}finally{
			if(reader != null)
				reader.close();
			assertTrue("Changed template value not found",found);
		}
		resultFile.delete();
	}
	
	
	@Test
	public void testToUrl() throws MalformedURLException{
		File f = new File("/directory/file.my");
		assertEquals(f.toURL(), toURL(f));
	}
	
	@Test
	public void testFileCopyNullValues() throws IOException{
		try{
			fileCopy(null, null);	
		}
		catch(IllegalArgumentException e){
			return;
		}
		fail("Proper exception missing for null values");
	}
	
	@Test
	public void testDirectoryCopyNullValues() throws IOException{
		try{
			directoryCopy(null, null);	
		}
		catch(IllegalArgumentException e){
			return;
		}
		fail("Proper exception missing for null values");
	}	
	
	@Test
	public void testTemplatedFielCopyNullValues() throws IOException{
		try{
			templatedFileCopy(null, null,null);	
		}
		catch(IllegalArgumentException e){
			return;
		}
		fail("Proper exception missing for null values");
	}
	
	@Test
	public void testToUrlNullValues() throws IOException{
		assertNull(toURL(null));	
	}
	
	@Test
	public void testResolveFile(){
		File[] files = FileUtils.resolveFile(TestUtils.getTempDirectory(), "*.file");
		assertTrue(files.length == 1);
		assertEquals(plainFile, files[0]);
	}
	
}
