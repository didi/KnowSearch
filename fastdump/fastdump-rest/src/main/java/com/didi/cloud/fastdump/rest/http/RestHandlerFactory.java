package com.didi.cloud.fastdump.rest.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import com.didi.cloud.fastdump.common.content.http.PathTrie;
import com.didi.cloud.fastdump.rest.rest.RestHandler;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.support.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2022/8/4
 */
@Component
public class RestHandlerFactory {
    protected static final Logger          LOGGER       = LoggerFactory.getLogger(RestHandlerFactory.class);

    public static final PathTrie.Decoder   REST_DECODER = RestUtils::decodeComponent;

    private final PathTrie<MethodHandlers> handlers     = new PathTrie<>(REST_DECODER);

    public void registerHandler(Method method, String path, RestHandler handler) {
        handlers.insertOrUpdate(path, new MethodHandlers(path, handler, method),
            (mHandlers, newMHandler) -> mHandlers.addMethods(handler, method));
    }

    public RestHandler tryAllHandlers(final RestRequest request) {
        final String rawPath = request.rawPath();
        final String uri = request.uri();
        final Method requestMethod;
        try {
            // Resolves the HTTP method and fails if the method is invalid
            requestMethod = request.method();
            // Loop through all possible handlers, attempting to dispatch the request
            Iterator<MethodHandlers> allHandlers = getAllHandlers(request.params(), rawPath);
            while (allHandlers.hasNext()) {
                final RestHandler handler;
                final MethodHandlers methodHandlers = allHandlers.next();
                if (methodHandlers == null) {
                    handler = null;
                } else {
                    handler = methodHandlers.getHandler(requestMethod);
                }
                if (handler != null) {
                    return handler;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("unable to find uri={}, rawPath={} handler", uri, rawPath);
        }
        return null;
    }

    Iterator<MethodHandlers> getAllHandlers(@Nullable Map<String, String> requestParamsRef, String rawPath) {
        final Supplier<Map<String, String>> paramsSupplier;
        if (requestParamsRef == null) {
            paramsSupplier = () -> null;
        } else {
            // Between retrieving the correct path, we need to reset the parameters,
            // otherwise parameters are parsed out of the URI that aren't actually handled.
            final Map<String, String> originalParams = new HashMap<>(requestParamsRef);
            paramsSupplier = () -> {
                // PathTrie modifies the request, so reset the params between each iteration
                requestParamsRef.clear();
                requestParamsRef.putAll(originalParams);
                return requestParamsRef;
            };
        }
        // we use rawPath since we don't want to decode it while processing the path resolution
        // so we can handle things like:
        // my_index/my_type/http%3A%2F%2Fwww.google.com
        return handlers.retrieveAll(rawPath, paramsSupplier);
    }

}
