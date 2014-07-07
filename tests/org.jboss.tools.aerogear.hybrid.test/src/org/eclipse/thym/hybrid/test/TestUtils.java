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
package org.eclipse.thym.hybrid.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class TestUtils {
	
	public static final String FILE_PLAIN = "plain.file";
	
	public static File createTempFile(String fileName) throws IOException, FileNotFoundException {
		File f = new File(getTempDirectory(), fileName);
		f.createNewFile();
		f.deleteOnExit();
		FileOutputStream fout = null;
		FileChannel out = null;
		InputStream in = TestUtils.class.getResourceAsStream("/"+fileName);
		try {
			fout = new FileOutputStream(f);
			out = fout.getChannel();
			
			out.transferFrom(Channels.newChannel(in), 0, Integer.MAX_VALUE);
			return f;
		} finally {
			if (out != null)
				out.close();
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	public static File getTempDirectory() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

}
