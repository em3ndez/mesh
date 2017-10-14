package com.gentics.mesh.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gentics.mesh.core.AbstractEndpoint;
import com.gentics.mesh.core.verticle.admin.AdminEndpoint;
import com.gentics.mesh.core.verticle.admin.RestInfoEndpoint;
import com.gentics.mesh.core.verticle.auth.AuthenticationEndpoint;
import com.gentics.mesh.core.verticle.eventbus.EventbusEndpoint;
import com.gentics.mesh.core.verticle.group.GroupEndpoint;
import com.gentics.mesh.core.verticle.microschema.MicroschemaEndpoint;
import com.gentics.mesh.core.verticle.microschema.ProjectMicroschemaEndpoint;
import com.gentics.mesh.core.verticle.navroot.NavRootEndpoint;
import com.gentics.mesh.core.verticle.node.NodeEndpoint;
import com.gentics.mesh.core.verticle.project.ProjectEndpoint;
import com.gentics.mesh.core.verticle.project.ProjectInfoEndpoint;
import com.gentics.mesh.core.verticle.release.ReleaseEndpoint;
import com.gentics.mesh.core.verticle.role.RoleEndpoint;
import com.gentics.mesh.core.verticle.schema.ProjectSchemaEndpoint;
import com.gentics.mesh.core.verticle.schema.SchemaEndpoint;
import com.gentics.mesh.core.verticle.tagfamily.TagFamilyEndpoint;
import com.gentics.mesh.core.verticle.user.UserEndpoint;
import com.gentics.mesh.core.verticle.utility.UtilityEndpoint;
import com.gentics.mesh.core.verticle.webroot.WebRootEndpoint;
import com.gentics.mesh.etc.RouterStorage;
import com.gentics.mesh.graphql.GraphQLEndpoint;
import com.gentics.mesh.search.ProjectSearchEndpoint;
import com.gentics.mesh.search.SearchEndpoint;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

/**
 * Central REST API Verticle which will provide all core REST API Endpoints
 */
public class RestAPIVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AbstractEndpoint.class);

	protected HttpServer server;

	@Inject
	public RouterStorage routerStorage;

	@Inject
	public UserEndpoint userEndpoint;

	@Inject
	public RoleEndpoint roleEndpoint;

	@Inject
	public GroupEndpoint groupEndpoint;

	@Inject
	public ProjectEndpoint projectEndpoint;

	@Inject
	public NodeEndpoint nodeEndpoint;

	@Inject
	public TagFamilyEndpoint tagFamilyEndpoint;

	@Inject
	public ReleaseEndpoint releaseEndpoint;

	@Inject
	public SchemaEndpoint schemaEndpoint;

	@Inject
	public ProjectSearchEndpoint projectSearchEndpoint;

	@Inject
	public ProjectSchemaEndpoint projectSchemaEndpoint;

	@Inject
	public ProjectInfoEndpoint projectInfoEndpoint;

	@Inject
	public ProjectMicroschemaEndpoint projectMicroschemaEndpoint;

	@Inject
	public WebRootEndpoint webrootEndpoint;

	@Inject
	public RestInfoEndpoint restInfoEndpoint;

	@Inject
	public UtilityEndpoint utilityEndpoint;

	@Inject
	public MicroschemaEndpoint microschemaEndpoint;

	@Inject
	public EventbusEndpoint eventbusEndpoint;

	@Inject
	public NavRootEndpoint navrootEndpoint;

	@Inject
	public AuthenticationEndpoint authenticationEndpoint;

	@Inject
	public SearchEndpoint searchEndpoint;

	@Inject
	public GraphQLEndpoint graphqlEndpoint;

	@Inject
	public AdminEndpoint adminEndpoint;

	@Inject
	public RestAPIVerticle() {
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// this.localRouter = setupLocalRouter();
		// if (localRouter == null) {
		// throw new MeshConfigurationException("The local router was not setup correctly. Startup failed.");
		// }
		int port = config().getInteger("port");
		String host = config().getString("host");
		if (log.isInfoEnabled()) {
			log.info("Starting http server on {" + host + ":" + port + "}..");
		}
		HttpServerOptions options = new HttpServerOptions();
		options.setPort(port);
		options.setHost(host);
		options.setCompressionSupported(true);
		// MeshOptions meshOptions = Mesh.mesh().getOptions();
		// HttpServerConfig httpServerOptions = meshOptions.getHttpServerOptions();
		// if (httpServerOptions.isSsl()) {
		// if (log.isErrorEnabled()) {
		// log.debug("Setting ssl server options");
		// }
		// options.setSsl(true);
		// PemKeyCertOptions keyOptions = new PemKeyCertOptions();
		// if (isEmpty(httpServerOptions.getCertPath()) || isEmpty(httpServerOptions.getKeyPath())) {
		// throw new MeshConfigurationException("SSL is enabled but either the server key or the cert path was not specified.");
		// }
		// keyOptions.setKeyPath(httpServerOptions.getKeyPath());
		// keyOptions.setCertPath(httpServerOptions.getCertPath());
		// options.setPemKeyCertOptions(keyOptions);
		// }

		log.info("Starting http server in verticle {" + getClass().getName() + "} on port {" + options.getPort() + "}");
		server = vertx.createHttpServer(options);
		server.requestHandler(routerStorage.getRootRouter()::accept);
		server.listen(rh -> {
			if (rh.failed()) {
				startFuture.fail(rh.cause());
			} else {
				if (log.isInfoEnabled()) {
					log.info("Started http server.. Port: " + config().getInteger("port"));
				}
				try {
					registerEndPoints();
					startFuture.complete();
				} catch (Exception e) {
					e.printStackTrace();
					startFuture.fail(e);
				}
			}
		});

	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		server.close(rh -> {
			if (rh.failed()) {
				stopFuture.fail(rh.cause());
			} else {
				stopFuture.complete();
			}
		});
	}

	private void registerEndPoints() throws Exception {

		List<AbstractEndpoint> endpoints = new ArrayList<>();
		endpoints.add(restInfoEndpoint);
		// verticles.add(projectInfoVerticle());

		// User Group Role verticles
		endpoints.add(userEndpoint);
		endpoints.add(groupEndpoint);
		endpoints.add(roleEndpoint);

		// Project specific verticles
		endpoints.add(nodeEndpoint);
		endpoints.add(tagFamilyEndpoint);
		endpoints.add(projectSchemaEndpoint);
		endpoints.add(projectMicroschemaEndpoint);
		endpoints.add(releaseEndpoint);
		endpoints.add(graphqlEndpoint);

		// Global verticles
		endpoints.add(webrootEndpoint);
		endpoints.add(navrootEndpoint);
		endpoints.add(projectEndpoint);
		endpoints.add(schemaEndpoint);
		endpoints.add(microschemaEndpoint);
		endpoints.add(searchEndpoint);
		endpoints.add(projectSearchEndpoint);
		endpoints.add(authenticationEndpoint);
		endpoints.add(adminEndpoint);
		endpoints.add(eventbusEndpoint);
		endpoints.add(utilityEndpoint);
		endpoints.add(projectInfoEndpoint);

		for (AbstractEndpoint endpoint : endpoints) {
			endpoint.registerEndPoints();
		}
	}

	public HttpServer getServer() {
		return server;
	}

}
