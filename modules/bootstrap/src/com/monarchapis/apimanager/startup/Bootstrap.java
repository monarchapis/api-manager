/*
 * Copyright (C) 2015 CapTech Ventures, Inc.
 * (http://www.captechconsulting.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monarchapis.apimanager.startup;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Bootstrap {
	private static final String CLASSPATH_DIR = "lib";
	private static final String LIB_EXT = ".jar";
	private static final String APP_CLASS = "com.monarchapis.apimanager.cli.StandaloneServer";

	private String home;
	private Object serverInstance;

	public Bootstrap(String home) {
		this.home = home;
	}

	public static void main(String[] args) throws Exception {
		String home = System.getProperty("monarch.home");

		if (home == null) {
			System.err.println("monarch.home was not set");
			System.exit(-1);
		}

		try {
			Bootstrap bootstrap = new Bootstrap(home);
			bootstrap.init();

			String command = "start";
			if (args.length > 0) {
				command = args[args.length - 1];
			}

			if (command.equals("start")) {
				bootstrap.start();
			} else if (command.equals("stop")) {
				bootstrap.stop();
			} else if (command.equals("version")) {
				bootstrap.version();
			} else {
				System.err.println("Bootstrap: command \"" + command + "\" does not exist.");
			}
		} catch (Throwable t) {
			// Unwrap the Exception for clearer error reporting
			if (t instanceof InvocationTargetException && t.getCause() != null) {
				t = t.getCause();
			}
			t.printStackTrace();
			System.exit(-1);
		}
	}

	public void init() throws Exception {
		serverInstance = getServer();
	}

	public void start() throws Exception {
		Method method = serverInstance.getClass().getMethod("start", (Class[]) null);
		method.invoke(serverInstance, (Object[]) null);
	}

	public void stop() throws Exception {
		Method method = serverInstance.getClass().getMethod("stopServer", (Class[]) null);
		method.invoke(serverInstance, (Object[]) null);
	}

	public void version() throws Exception {
		Method method = serverInstance.getClass().getMethod("version", (Class[]) null);
		method.invoke(serverInstance, (Object[]) null);
	}

	private Object getServer() throws Exception {
		ClassLoader cl = getClassLoaderFromPath(new File(home, CLASSPATH_DIR), Thread.currentThread()
				.getContextClassLoader());
		Thread.currentThread().setContextClassLoader(cl);

		Class<?> clazz = Class.forName(APP_CLASS, true, cl);
		Object instance = clazz.newInstance();
		return instance;
	}

	// Returns a ClassLoader that for the provided path.
	private static ClassLoader getClassLoaderFromPath(File path, ClassLoader parent) throws Exception {
		// Get jar files from libraries path
		File[] jarFiles = path.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(LIB_EXT);
			}
		});

		URL[] classpath = new URL[jarFiles.length];

		for (int j = 0; j < jarFiles.length; j++) {
			classpath[j] = jarFiles[j].toURI().toURL();
		}

		return new URLClassLoader(classpath, parent);
	}
}
