package muck.client;

import io.helidon.http.Method;

/**
 * Represents an API operation from the OpenAPI specification
 *
 * @param operationId Unique identifier for the operation (e.g.,
 *                    "listPipelines")
 * @param path        URL path (e.g., "/pipelines")
 * @param method      HTTP method (e.g., "GET", "POST")
 */
public record ApiOperation(String operationId, String path, Method method) {
}
