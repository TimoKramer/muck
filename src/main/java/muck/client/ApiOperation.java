package muck.client;

/**
 * Represents an API operation from the OpenAPI specification
 *
 * @param operationId  Unique identifier for the operation (e.g., "listPipelines")
 * @param path         URL path (e.g., "/pipelines")
 * @param method       HTTP method (e.g., "GET", "POST")
 */
public record ApiOperation(String operationId, String path, String method) {
}
