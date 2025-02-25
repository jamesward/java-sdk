package io.modelcontextprotocol.server;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.util.Assert;

class ClientSession {

	final String sessionId;

	private final ClientCapabilities clientCapabilities;

	private final McpSchema.Implementation clientInfo;

	private boolean initialized;

	ClientSession(String sessionId, ClientCapabilities clientCapabilities, McpSchema.Implementation clientInfo) {

		Assert.hasText(sessionId, "Session ID must not be empty");
		Assert.notNull(clientInfo, "Client info must not be null");
		Assert.notNull(clientCapabilities, "Client capabilities must not be null");

		this.sessionId = sessionId;
		this.clientCapabilities = clientCapabilities;
		this.clientInfo = clientInfo;
		this.initialized = false;
	}

	public ClientCapabilities getClientCapabilities() {
		return this.clientCapabilities;
	}

	public McpSchema.Implementation getClientInfo() {
		return this.clientInfo;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}