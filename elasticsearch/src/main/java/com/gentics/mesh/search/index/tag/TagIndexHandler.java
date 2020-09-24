package com.gentics.mesh.search.index.tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Tag;
import com.gentics.mesh.core.data.dao.ProjectDaoWrapper;
import com.gentics.mesh.core.data.project.HibProject;
import com.gentics.mesh.core.data.root.ProjectRoot;
import com.gentics.mesh.core.data.search.UpdateDocumentEntry;
import com.gentics.mesh.core.data.search.bulk.IndexBulkEntry;
import com.gentics.mesh.core.data.search.index.IndexInfo;
import com.gentics.mesh.core.data.search.request.SearchRequest;
import com.gentics.mesh.core.data.tag.HibTag;
import com.gentics.mesh.core.db.Tx;
import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.search.SearchProvider;
import com.gentics.mesh.search.index.BucketManager;
import com.gentics.mesh.search.index.entry.AbstractIndexHandler;
import com.gentics.mesh.search.index.metric.SyncMetersFactory;
import com.gentics.mesh.search.verticle.eventhandler.MeshHelper;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Handler for the tag specific search index.
 */
@Singleton
public class TagIndexHandler extends AbstractIndexHandler<HibTag> {

	/**
	 * Name of the custom property of SearchQueueEntry containing the project uuid.
	 */
	public final static String CUSTOM_PROJECT_UUID = "projectUuid";

	@Inject
	TagTransformer transforer;

	@Inject
	TagMappingProvider mappingProvider;

	@Inject
	public TagIndexHandler(SearchProvider searchProvider, Database db, BootstrapInitializer boot, MeshHelper helper, MeshOptions options,
		SyncMetersFactory syncMetricsFactory, BucketManager bucketManager) {
		super(searchProvider, db, boot, helper, options, syncMetricsFactory, bucketManager);
	}

	@Override
	public String getType() {
		return "tag";
	}

	@Override
	public Class<Tag> getElementClass() {
		return Tag.class;
	}

	@Override
	protected TagTransformer getTransformer() {
		return transforer;
	}

	@Override
	public TagMappingProvider getMappingProvider() {
		return mappingProvider;
	}

	@Override
	protected String composeDocumentIdFromEntry(UpdateDocumentEntry entry) {
		return Tag.composeDocumentId(entry.getElementUuid());
	}

	@Override
	protected String composeIndexNameFromEntry(UpdateDocumentEntry entry) {
		return Tag.composeIndexName(entry.getContext().getProjectUuid());
	}

	@Override
	public Completable store(HibTag tag, UpdateDocumentEntry entry) {
		entry.getContext().setProjectUuid(tag.getProject().getUuid());
		return super.store(tag, entry);
	}

	@Override
	public Observable<IndexBulkEntry> storeForBulk(HibTag tag, UpdateDocumentEntry entry) {
		entry.getContext().setProjectUuid(tag.getProject().getUuid());
		return super.storeForBulk(tag, entry);
	}

	@Override
	public Map<String, IndexInfo> getIndices() {
		return db.tx(() -> {
			Map<String, IndexInfo> indexInfo = new HashMap<>();
			ProjectRoot projectRoot = boot.meshRoot().getProjectRoot();
			for (Project project : projectRoot.findAll()) {
				IndexInfo info = getIndex(project.getUuid());
				indexInfo.put(info.getIndexName(), info);
			}
			return indexInfo;
		});
	}

	public IndexInfo getIndex(String projectUuid) {
		return new IndexInfo(Tag.composeIndexName(projectUuid), null, getMappingProvider().getMapping(), "tag");
	}

	@Override
	public Flowable<SearchRequest> syncIndices() {
		return Flowable.defer(() -> db.tx(() -> {
			return boot.meshRoot().getProjectRoot().findAll().stream()
				.map(project -> {
					String uuid = project.getUuid();
					return diffAndSync(Tag.composeIndexName(uuid), uuid);
				}).collect(Collectors.collectingAndThen(Collectors.toList(), Flowable::merge));
		}));
	}

	@Override
	public Set<String> filterUnknownIndices(Set<String> indices) {
		return db.tx(tx -> {
			Set<String> activeIndices = new HashSet<>();
			ProjectDaoWrapper projectDao = tx.data().projectDao();
			for (HibProject project : projectDao.findAll()) {
				activeIndices.add(Tag.composeIndexName(project.getUuid()));
			}
			return indices.stream()
				.filter(i -> i.startsWith(getType() + "-"))
				.filter(i -> !activeIndices.contains(i))
				.collect(Collectors.toSet());
		});
	}

	@Override
	public Set<String> getIndicesForSearch(InternalActionContext ac) {
		return db.tx(() -> {
			HibProject project = ac.getProject();
			if (project != null) {
				return Collections.singleton(Tag.composeIndexName(project.getUuid()));
			} else {
				return getIndices().keySet();
			}
		});
	}

	@Override
	public Function<String, HibTag> elementLoader() {
		return uuid -> boot.meshRoot().getTagRoot().findByUuid(uuid);
	}

	@Override
	public Stream<? extends HibTag> loadAllElements(Tx tx) {
		return tx.data().tagDao().findAllGlobal().stream();
	}

}
