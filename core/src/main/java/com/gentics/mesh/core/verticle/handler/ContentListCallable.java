package com.gentics.mesh.core.verticle.handler;

import java.util.List;

import org.springframework.data.domain.Page;

import com.gentics.mesh.core.data.model.Content;
import com.gentics.mesh.core.data.model.Tag;
import com.gentics.mesh.paging.PagingInfo;

@FunctionalInterface
public interface ContentListCallable {

	Page<Content> findContents(String projectName, Tag rootTag, List<String> languageTags, PagingInfo pagingInfo);

}
