/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.server;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ListPromptsResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.ServerMcpTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * Tests for {@link McpAsyncServer} using {@link WebFluxSseServerTransport}.
 *
 * @author Christian Tzolov
 */
@Timeout(150) // Giving extra time beyond the client timeout
class WebFluxSseMcpAsyncServer2Tests {

	private static final int PORT = 8181;

	private static final String MESSAGE_ENDPOINT = "/mcp/message";

	private DisposableServer httpServer;

	protected ServerMcpTransport createMcpTransport() {
		var transport = new WebFluxSseServerTransport(new ObjectMapper(), MESSAGE_ENDPOINT);

		HttpHandler httpHandler = RouterFunctions.toHttpHandler(transport.getRouterFunction());
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
		httpServer = HttpServer.create().port(PORT).handle(adapter).bindNow();

		return transport;
	}

	protected void onStart() {
	}

	protected void onClose() {
		if (httpServer != null) {
			httpServer.disposeNow();
		}
	}

	@Test
	void testImmediateClose() {

		Prompt prompt = new Prompt("TEST_PROMPT_NAME", "Test Prompt", List.of());
		McpServerFeatures.SyncPromptRegistration registration = new McpServerFeatures.SyncPromptRegistration(prompt,
				req -> new GetPromptResult("Test prompt description", List
					.of(new PromptMessage(McpSchema.Role.ASSISTANT, new McpSchema.TextContent("Test content")))));

		var mcpServer = McpServer.sync(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().prompts(true).build())
			.prompts(registration)
			.build();

		var mcpClient = McpClient
			.sync(new WebFluxSseClientTransport(WebClient.builder().baseUrl("http://localhost:" + PORT)))
			.build();

		var initResult = mcpClient.initialize();

		ListPromptsResult prompts = mcpClient.listPrompts();

		mcpClient.close();

		mcpServer.close();
	}

}
