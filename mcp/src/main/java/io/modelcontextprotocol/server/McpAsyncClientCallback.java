/*
* Copyright 2025 - 2025 the original author or authors.
*/
package io.modelcontextprotocol.server;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSession;
import reactor.core.publisher.Mono;

/**
 * @author Christian Tzolov
 */

class McpAsyncClientCallback {

	private static final TypeReference<McpSchema.CreateMessageResult> CREATE_MESSAGE_RESULT_TYPE_REF = new TypeReference<>() {
	};

	private static final TypeReference<McpSchema.ListRootsResult> LIST_ROOTS_RESULT_TYPE_REF = new TypeReference<>() {
	};

	private final McpSession mcpSession;

	private final ClientSession clientSession;

	McpAsyncClientCallback(McpSession mcpSession, ClientSession clientSession) {
		this.mcpSession = mcpSession;
		this.clientSession = clientSession;
	}

	public Mono<McpSchema.CreateMessageResult> createMessage(McpSchema.CreateMessageRequest createMessageRequest) {

		String sessionId = (createMessageRequest.metadata() != null)
				? (String) createMessageRequest.metadata().get("sessionId") : null;

		if (sessionId == null) {
			return Mono.error(new McpError("Session ID must be provided in the metadata"));
		}

		if (this.clientSession.isInitialized() == false) {
			return Mono.error(new McpError("Client session not initialized for session ID: " + sessionId));
		}
		if (this.clientSession.getClientCapabilities().sampling() == null) {
			return Mono.error(new McpError("Client must be configured with sampling capabilities"));
		}

		return this.mcpSession.sendRequest(McpSchema.METHOD_SAMPLING_CREATE_MESSAGE, createMessageRequest,
				CREATE_MESSAGE_RESULT_TYPE_REF, Map.of("sessionId", sessionId));
	}

	public Mono<McpSchema.ListRootsResult> listRoots(String cursor) {
		return this.mcpSession.sendRequest(McpSchema.METHOD_ROOTS_LIST, new McpSchema.PaginatedRequest(cursor),
				LIST_ROOTS_RESULT_TYPE_REF, Map.of("sessionId", this.clientSession.sessionId));
	}

}
