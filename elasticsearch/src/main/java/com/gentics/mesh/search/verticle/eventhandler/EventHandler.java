package com.gentics.mesh.search.verticle.eventhandler;

import com.gentics.mesh.core.rest.MeshEvent;
import com.gentics.mesh.search.verticle.MessageEvent;
import com.gentics.mesh.search.verticle.request.ElasticsearchRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface EventHandler {

	/**
	 * Handles an event from mesh. Creates elasticsearch document from the data of the graph and creates a list
	 * of requests that represent the data that was changed during the event.
	 *
	 * @param messageEvent
	 * @return
	 */
	List<ElasticsearchRequest> handle(MessageEvent messageEvent);

	/**
	 * Gets collection of all events that can be handled by this class.
	 * @return
	 */
	Collection<MeshEvent> handledEvents();

	/**
	 * Creates an event handler that handles a single event.
	 * @param event The event to handle
	 * @param transformer The implementation for {@link #handle(MessageEvent)}
	 * @return
	 */
	static EventHandler forEvent(MeshEvent event, Function<MessageEvent, List<ElasticsearchRequest>> transformer) {
		return new EventHandler() {
			@Override
			public List<ElasticsearchRequest> handle(MessageEvent messageEvent) {
				return transformer.apply(messageEvent);
			}

			@Override
			public Collection<MeshEvent> handledEvents() {
				return Collections.singleton(event);
			}
		};
	}
}
