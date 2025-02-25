/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.server;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.junit.Assert.assertThat;

/**
 * Tests for {@link McpAsyncServer} using {@link WebFluxSseServerTransport}.
 *
 * @author Christian Tzolov
 */
@Timeout(15000) // Giving extra time beyond the client timeout
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

		var callResponse = new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("CALL RESPONSE")), null);
		McpServerFeatures.SyncToolRegistrationInterface<?> tool1 = new McpServerFeatures.SyncToolRegistration2(
				new McpSchema.Tool("tool1", "tool1 description", """
						{
							"": "http://json-schema.org/draft-07/schema#",
							"type": "object",
							"properties": {}
						}
						"""),
				input -> {

					var messages = List
							.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
									new McpSchema.TextContent("Test message")));
					var modelPrefs = new McpSchema.ModelPreferences(List.of(), 1.0, 1.0, 1.0);

					var request = new McpSchema.CreateMessageRequest(messages, modelPrefs, null,
							McpSchema.CreateMessageRequest.ContextInclusionStrategy.NONE, null, 100, List.of(),
							Map.of());

					CreateMessageResult llmResponse = input.clientCallback().createMessage(request);

					input.data();

					String response = RestClient.create()
							.get()
							.uri("https://github.com/modelcontextprotocol/specification/blob/main/README.md")
							.retrieve()
							.body(String.class);
					return callResponse;
				});

		Prompt prompt = new Prompt("TEST_PROMPT_NAME", "Test Prompt", List.of());
		McpServerFeatures.SyncPromptRegistration registration = new McpServerFeatures.SyncPromptRegistration(prompt,
				req -> new GetPromptResult("Test prompt description", List
						.of(new PromptMessage(McpSchema.Role.ASSISTANT, new McpSchema.TextContent("Test content")))));

		var mcpServer = McpServer.sync(createMcpTransport())
				.serverInfo("test-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().prompts(true).tools(true).build())
				.prompts(registration)
				.tools(tool1)
				.build();

		mcpServer.createMessage(null);

		var mcpClient = McpClient
				.sync(new WebFluxSseClientTransport(WebClient.builder().baseUrl("http://localhost:" + PORT)))
				.requestTimeout(Duration.ofSeconds(60))
				.build();

		var initResult = mcpClient.initialize();

		// ListPromptsResult prompts = mcpClient.listPrompts();

		CallToolResult response = mcpClient.callTool(new McpSchema.CallToolRequest("tool1", Map.of()));

		mcpClient.close();

		mcpServer.close();
	}

}
