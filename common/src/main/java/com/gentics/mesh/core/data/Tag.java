package com.gentics.mesh.core.data;

import java.util.List;
import java.util.Objects;

import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.rest.tag.TagReference;
import com.gentics.mesh.core.rest.tag.TagResponse;
import com.gentics.mesh.error.InvalidArgumentException;
import com.gentics.mesh.parameter.PagingParameters;

/**
 * Graph domain model interface for a tag.
 * 
 * Tags can currently only hold a single string value. Tags are not localizable. A tag can only be assigned to a single tag family.
 */
public interface Tag extends MeshCoreVertex<TagResponse, Tag>, ReferenceableElement<TagReference>, UserTrackingVertex, IndexableElement {

	/**
	 * Type Value: {@value #TYPE}
	 */
	static final String TYPE = "tag";

	/**
	 * Compose the index name for tags. Use the projectUuid in order to create a project specific index.
	 * 
	 * @param projectUuid
	 * @return
	 */
	static String composeIndexName(String projectUuid) {
		Objects.requireNonNull(projectUuid, "A projectUuid must be provided.");
		StringBuilder indexName = new StringBuilder();
		indexName.append(TYPE.toLowerCase());
		indexName.append("-").append(projectUuid);
		return indexName.toString();
	}

	static String composeDocumentId(String elementUuid) {
		Objects.requireNonNull(elementUuid, "A elementUuid must be provided.");
		return elementUuid;
	}

	static String composeIndexType() {
		return TYPE.toLowerCase();
	}

	/**
	 * Compose the type name for tags.
	 * 
	 * @return
	 */
	static String composeTypeName() {
		return Tag.TYPE.toLowerCase();
	}

	@Override
	default String getType() {
		return TYPE;
	}

	/**
	 * Return the tag family to which the tag belongs.
	 * 
	 * @return
	 */
	TagFamily getTagFamily();

	/**
	 * Unassign the the node from the tag.
	 * 
	 * @param node
	 */
	void removeNode(Node node);

	/**
	 * Return a list of nodes that were tagged by this tag in the given release
	 * 
	 * @param release
	 *            release
	 * 
	 * @return
	 */
	List<? extends Node> getNodes(Release release);

	/**
	 * Return a page of nodes that are visible to the user and which are tagged by this tag. Use the paging and language information provided.
	 * 
	 * @param requestUser
	 * @param release
	 * @param languageTags
	 * @param type
	 * @param pagingInfo
	 * @return
	 * @throws InvalidArgumentException
	 */
	Page<? extends Node> findTaggedNodes(MeshAuthUser requestUser, Release release, List<String> languageTags, ContainerType type,
			PagingParameters pagingInfo) throws InvalidArgumentException;

	/**
	 * Set the tag family of this tag.
	 * 
	 * @param tagFamily
	 */
	void setTagFamily(TagFamily tagFamily);

	/**
	 * Set the project to which tag is assigned to.
	 * 
	 * @param project
	 */
	void setProject(Project project);

	/**
	 * Return the project to which the tag was assigned to
	 * 
	 * @return
	 */
	Project getProject();

}
