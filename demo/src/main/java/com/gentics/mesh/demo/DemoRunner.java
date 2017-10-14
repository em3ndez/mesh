package com.gentics.mesh.demo;

import static com.gentics.mesh.demo.DemoZipHelper.unzip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.gentics.mesh.Mesh;
import com.gentics.mesh.OptionsLoader;
import com.gentics.mesh.context.impl.LoggingConfigurator;
import com.gentics.mesh.dagger.MeshComponent;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.demo.verticle.DemoVerticle;
import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.graphdb.MissingOrientCredentialFixer;
import com.gentics.mesh.search.verticle.ElasticsearchHeadVerticle;
import com.gentics.mesh.util.DeploymentUtil;
import com.gentics.mesh.verticle.admin.AdminGUIVerticle;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.lingala.zip4j.exception.ZipException;

/**
 * Main runner that is used to deploy a preconfigured set of verticles.
 */
public class DemoRunner {

	private static Logger log;

	static {
		System.setProperty("vertx.httpServiceFactory.cacheDir", "data" + File.separator + "tmp");
		System.setProperty("vertx.cacheDirBase", "data" + File.separator + "tmp");
		System.setProperty("storage.trackChangedRecordsInWAL", "true");
	}

	public static void main(String[] args) throws Exception {
		LoggingConfigurator.init();
		log = LoggerFactory.getLogger(DemoRunner.class);
		// Extract dump file on first time startup to speedup startup
		setupDemo();

		MeshOptions options = OptionsLoader.createOrloadOptions(args);

		MissingOrientCredentialFixer.fix(options);

		options.getHttpServerOptions().setEnableCors(true);
		options.getHttpServerOptions().setCorsAllowCredentials(false);
		options.getHttpServerOptions().setCorsAllowedOriginPattern("*");
		// For Mesh UI Dev
		// options.getHttpServerOptions().setCorsAllowCredentials(true);
		// options.getHttpServerOptions().setCorsAllowedOriginPattern("http://localhost:5000");
		// options.getSearchOptions().setHttpEnabled(true);
		// options.getStorageOptions().setStartServer(true);
		// options.getSearchOptions().setHttpEnabled(true);
		// options.getStorageOptions().setDirectory(null);
		// options.setClusterMode(true);

		Mesh mesh = Mesh.mesh(options);
		mesh.setCustomLoader((vertx) -> {
			JsonObject config = new JsonObject();
			config.put("port", options.getHttpServerOptions().getPort());

			// Add demo content provider
			MeshComponent meshInternal = MeshInternal.get();
			DemoVerticle demoVerticle = new DemoVerticle(
					new DemoDataProvider(meshInternal.database(), meshInternal.meshLocalClientImpl(), meshInternal.boot()),
					MeshInternal.get().routerStorage());
			DeploymentUtil.deployAndWait(vertx, config, demoVerticle, false);

			// Add admin ui
			AdminGUIVerticle adminVerticle = new AdminGUIVerticle(MeshInternal.get().routerStorage());
			DeploymentUtil.deployAndWait(vertx, config, adminVerticle, false);

			// Add elastichead
			if (options.getSearchOptions().isHttpEnabled()) {
				ElasticsearchHeadVerticle headVerticle = new ElasticsearchHeadVerticle(MeshInternal.get().routerStorage());
				DeploymentUtil.deployAndWait(vertx, config, headVerticle, false);
			}
		});
		mesh.run();
	}

	private static void setupDemo() throws FileNotFoundException, IOException, ZipException {
		File dataDir = new File("data");
		if (!dataDir.exists() || dataDir.list().length == 0) {
			log.info("Extracting demo data since this is the first time you start mesh...");
			unzip("/mesh-dump.zip", "data");
			log.info("Demo data extracted to {" + dataDir.getAbsolutePath() + "}");
		}
	}

}
