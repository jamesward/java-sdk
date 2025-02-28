/*
 * Copyright 2024-2024 the original author or authors.
 */
package io.modelcontextprotocol.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Root;
import io.modelcontextprotocol.util.Assert;

/**
 * @author Christian Tzolov
 */
class ClientSession {

	final String sessionId;

	private final ClientCapabilities clientCapabilities;

	private final McpSchema.Implementation clientInfo;

	private boolean initialized;

	private CopyOnWriteArrayList<Root> roots = new CopyOnWriteArrayList<>();

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

	public List<Root> getRoots() {
		return this.roots;
	}

}