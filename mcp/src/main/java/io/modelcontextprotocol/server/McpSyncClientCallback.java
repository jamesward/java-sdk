/*
* Copyright 2025 - 2025 the original author or authors.
*/
package io.modelcontextprotocol.server;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * @author Christian Tzolov
 */

class McpSyncClientCallback {

	private final McpAsyncClientCallback delegate;

	McpSyncClientCallback(McpAsyncClientCallback delegate) {
		this.delegate = delegate;
	}

	public McpSchema.CreateMessageResult createMessage(McpSchema.CreateMessageRequest createMessageRequest) {
		return this.delegate.createMessage(createMessageRequest).block();
	}

	public Mono<McpSchema.ListRootsResult> listRoots(String cursor) {
		return this.delegate.listRoots(cursor);
	}

}
