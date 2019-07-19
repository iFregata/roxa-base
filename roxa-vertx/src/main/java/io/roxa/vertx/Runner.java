/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @author Steven Chen
 *
 */
public class Runner {

	private static final String JAVA_SRC_DIR = "/src/main/java/";
	private static final String JAVA_RES_DIR = "/src/main/resources/";

	public static void runClustered(Class<?> clazz) {
		run(getCurrentJavaSourceDir(), clazz, true, null);
	}

	public static void run(Class<?> clazz) {
		run(getCurrentJavaSourceDir(), clazz, false, null);
	}

	public static void run(String exampleDir, Class<?> clazz, boolean clustered, DeploymentOptions deploymentOptions) {
		VertxOptions options = new VertxOptions();
		if (clustered)
			options.getEventBusOptions().setClustered(clustered);
		run(exampleDir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions);
	}

	public static void runScript(String prefix, String scriptName, VertxOptions options) {
		File file = new File(scriptName);
		String dirPart = file.getParent();
		String scriptDir = prefix + dirPart;
		run(scriptDir, scriptDir + "/" + file.getName(), options, null);
	}

	public static void run(String exampleDir, String verticleID, VertxOptions options,
			DeploymentOptions deploymentOptions) {
		if (options == null) {
			// Default parameter
			options = new VertxOptions();
		}
		// Smart cwd detection

		// Based on the current directory (.) and the desired directory (exampleDir), we
		// try to compute the vertx.cwd
		// directory:
		try {
			// We need to use the canonical file. Without the file name is .
			File current = new File(".").getCanonicalFile();
			if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
				exampleDir = exampleDir.substring(current.getName().length() + 1);
			}
		} catch (IOException e) {
			// Ignore it.
		}

		System.setProperty("vertx.cwd", exampleDir);
		Consumer<Vertx> runner = vertx -> {
			try {
				if (deploymentOptions != null) {
					vertx.deployVerticle(verticleID, deploymentOptions);
				} else {
					vertx.deployVerticle(verticleID);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		};
		if (options.getEventBusOptions().isClustered()) {
			Vertx.clusteredVertx(options, res -> {
				if (res.succeeded()) {
					Vertx vertx = res.result();
					runner.accept(vertx);
				} else {
					res.cause().printStackTrace();
				}
			});
		} else {
			Vertx vertx = Vertx.vertx(options);
			runner.accept(vertx);
		}
	}

	public static String getCurrentJavaSourceDir() {
		Path p = Paths.get(System.getProperty("user.dir"));
		return p.getFileName().toString() + JAVA_SRC_DIR;
	}

	public static String getCurrentJavaResourceDir() {
		Path p = Paths.get(System.getProperty("user.dir"));
		return p.getFileName().toString() + JAVA_RES_DIR;
	}

}
