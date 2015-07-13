package com.gentics.mesh.core.verticle.project;

import static com.gentics.mesh.core.data.relationship.Permission.CREATE_PERM;
import static com.gentics.mesh.core.data.relationship.Permission.DELETE_PERM;
import static com.gentics.mesh.core.data.relationship.Permission.READ_PERM;
import static com.gentics.mesh.core.data.relationship.Permission.UPDATE_PERM;
import static com.gentics.mesh.json.JsonUtil.fromJson;
import static com.gentics.mesh.json.JsonUtil.toJson;
import static com.gentics.mesh.util.RoutingContextHelper.getPagingInfo;
import static com.gentics.mesh.util.RoutingContextHelper.getUser;
import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.core.http.HttpMethod.PUT;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;

import org.apache.commons.lang3.StringUtils;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gentics.mesh.api.common.PagingInfo;
import com.gentics.mesh.core.AbstractProjectRestVerticle;
import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.MeshAuthUser;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Tag;
import com.gentics.mesh.core.data.TagFamily;
import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.error.HttpStatusCodeErrorException;
import com.gentics.mesh.core.rest.tag.TagCreateRequest;
import com.gentics.mesh.core.rest.tag.TagListResponse;
import com.gentics.mesh.core.rest.tag.TagUpdateRequest;
import com.gentics.mesh.error.InvalidPermissionException;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.util.BlueprintTransaction;
import com.gentics.mesh.util.RestModelPagingHelper;

/**
 * The tag verticle provides rest endpoints which allow manipulation and handling of tag related objects.
 * 
 * @author johannes2
 *
 */
@Component
@Scope("singleton")
@SpringVerticle
public class ProjectTagVerticle extends AbstractProjectRestVerticle {

	private static final Logger log = LoggerFactory.getLogger(ProjectTagVerticle.class);

	public ProjectTagVerticle() {
		super("tags");
	}

	@Override
	public void registerEndPoints() throws Exception {
		route("/*").handler(springConfiguration.authHandler());
		addCreateHandler();
		addReadHandler();
		addUpdateHandler();
		addDeleteHandler();

		addTaggedNodesHandler();
	}

	private void addTaggedNodesHandler() {
		Route getRoute = route("/:uuid/nodes").method(GET).produces(APPLICATION_JSON);
		getRoute.handler(rc -> {
			MeshAuthUser requestUser = getUser(rc);

			Project project = getProject(rc);
			loadObject(rc, "uuid", READ_PERM, project.getTagRoot(), rh -> {
				Tag tag = rh.result();
				//				tag.findTaggedNodes(requestUser, projectName, languageTags, pagingInfo);
				});

		});
	}

	// TODO fetch project specific tag
	// TODO update other fields as well?
	// TODO Update user information
	// TODO use schema and only handle those i18n properties that were specified within the schema.
	private void addUpdateHandler() {
		Route route = route("/:uuid").method(PUT).consumes(APPLICATION_JSON).produces(APPLICATION_JSON);
		route.handler(rc -> {
			Project project = getProject(rc);
			if (project == null) {
				rc.fail(new HttpStatusCodeErrorException(400, "Project not found"));
				// TODO i18n error
			} else {
				String uuid = rc.request().params().get("uuid");
				project.getTagRoot().findByUuid(uuid, rh -> {
					if (rh.failed()) {
						rc.fail(rh.cause());
					} else {
						MeshAuthUser requestUser = getUser(rc);
						if (requestUser.hasPermission(rh.result(), UPDATE_PERM)) {
							Tag tag = rh.result();

							TagUpdateRequest requestModel = fromJson(rc, TagUpdateRequest.class);

							if (StringUtils.isEmpty(requestModel.getFields().getName())) {
								rc.fail(new HttpStatusCodeErrorException(400, i18n.get(rc, "tag_name_not_set")));
								return;
							}
							try (BlueprintTransaction tx = new BlueprintTransaction(fg)) {
								tag.setName(requestModel.getFields().getName());
								tx.success();
							}

							tag.transformToRest(requestUser, rh2 -> {
								if (rh2.failed()) {
									rc.fail(rh2.cause());
								}
								rc.response().setStatusCode(200).end(toJson(rh2.result()));
							});
						} else {
							rc.fail(new InvalidPermissionException(i18n.get(rc, "error_missing_perm", uuid)));
						}
					}
				});
			}

		});

	}

	// TODO load project specific root tag
	// TODO handle creator
	// TODO load schema and set the reference to the tag
	// newTag.setSchemaName(request.getSchemaName());
	// TODO maybe projects should not be a set?
	private void addCreateHandler() {
		Route route = route("/").method(POST).consumes(APPLICATION_JSON).produces(APPLICATION_JSON);
		route.handler(rc -> {
			Project project = getProject(rc);
			MeshAuthUser requestUser = getUser(rc);
			Future<Tag> tagCreated = Future.future();
			TagCreateRequest requestModel = fromJson(rc, TagCreateRequest.class);

			if (StringUtils.isEmpty(requestModel.getFields().getName())) {
				rc.fail(new HttpStatusCodeErrorException(400, i18n.get(rc, "tag_name_not_set")));
				return;
			}

			// TODO check tag family reference for null

			loadObjectByUuid(rc, requestModel.getTagFamilyReference().getUuid(), CREATE_PERM, project.getTagFamilyRoot(), rh -> {
				TagFamily tagFamily = rh.result();
				Tag newTag = tagFamily.create(requestModel.getFields().getName());
				project.getTagRoot().addTag(newTag);
				tagCreated.complete(newTag);
				newTag.transformToRest(requestUser, th -> {
					if (hasSucceeded(rc, th)) {
						rc.response().setStatusCode(200).end(toJson(rh.result()));
					}
				});
			});

		});
	}

	// TODO filtering, sorting
	private void addReadHandler() {
		Route route = route("/:uuid").method(GET).produces(APPLICATION_JSON);
		route.handler(rc -> {
			MeshAuthUser requestUser = getUser(rc);
			Project project = getProject(rc);
			loadObject(rc, "uuid", READ_PERM, project.getTagRoot(), trh -> {
				if (hasSucceeded(rc, trh)) {
					Tag tag = trh.result();
					tag.transformToRest(requestUser, th -> {
						if (hasSucceeded(rc, th)) {
							rc.response().setStatusCode(200).end(toJson(th.result()));
						}
					});
				}
			});
		});

		Route readAllRoute = route().method(GET).produces(APPLICATION_JSON);
		readAllRoute.handler(rc -> {
			String projectName = getProjectName(rc);
			MeshAuthUser requestUser = getUser(rc);

			Project project = boot.projectRoot().findByName(projectName);
			if (project == null) {
				rc.fail(new HttpStatusCodeErrorException(400, "Project not found"));
				// TODO i18n error
			} else {
				TagListResponse listResponse = new TagListResponse();
				PagingInfo pagingInfo = getPagingInfo(rc);
				Page<? extends Tag> tagPage;
				try {
					tagPage = project.getTagRoot().findAll(requestUser, pagingInfo);
					RestModelPagingHelper.setPaging(listResponse, tagPage);

					Handler<AsyncResult<Void>> completionHandler = v -> {
						if (v.failed()) {
							rc.fail(v.cause());
						} else {
							rc.response().setStatusCode(200).end(toJson(listResponse));
						}
					};

					if (listResponse.getData().size() == tagPage.getSize()) {
						completionHandler.handle(Future.succeededFuture());
					}
					for (Tag tag : tagPage) {
						tag.transformToRest(requestUser, th -> {
							if (th.failed()) {
								completionHandler.handle(Future.failedFuture(th.cause()));
							} else {
								listResponse.getData().add(th.result());
								if (listResponse.getData().size() == tagPage.getSize()) {
									completionHandler.handle(Future.succeededFuture());
								}
							}
						});

					}

				} catch (Exception e) {
					rc.fail(e);
				}
			}

		});

	}

	// TODO filter by projectName
	private void addDeleteHandler() {
		Route route = route("/:uuid").method(DELETE).produces(APPLICATION_JSON);
		route.handler(rc -> {
			String projectName = getProjectName(rc);

			Project project = boot.projectRoot().findByName(projectName);
			if (project == null) {
				rc.fail(new HttpStatusCodeErrorException(400, "Project not found"));
				// TODO i18n error
			} else {
				String uuid = rc.request().params().get("uuid");
				project.getTagRoot().findByUuid(uuid, rh -> {
					if (rh.failed()) {
						rc.fail(rh.cause());
					} else {
						MeshAuthUser requestUser = getUser(rc);
						if (requestUser.hasPermission(rh.result(), DELETE_PERM)) {
							rh.result().delete();
							rc.response().setStatusCode(200).end(JsonUtil.toJson(new GenericMessageResponse(i18n.get(rc, "tag_deleted", uuid))));
						} else {
							rc.fail(new InvalidPermissionException(i18n.get(rc, "error_missing_perm", uuid)));
						}
					}
				});
			}

		});
	}

}
