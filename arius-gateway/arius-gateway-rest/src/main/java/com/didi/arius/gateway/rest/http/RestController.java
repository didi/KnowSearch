package com.didi.arius.gateway.rest.http;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.utils.PathTrie;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.support.RestUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
* @author weizijun
* @date：2016年8月16日
* 
*/
@Component("restController")
public class RestController {

    protected static final ILog bootLogger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);

    public static final PathTrie.Decoder REST_DECODER = value -> RestUtils.decodeComponent(value);

    private final PathTrie<MethodHandlers> handlers = new PathTrie<>(REST_DECODER);
    
    public void registerHandler(Method method, String path, IRestHandler handler) {
        handlers.insertOrUpdate(path, new MethodHandlers(path, handler, method),
                (mHandlers, newMHandler) -> mHandlers.addMethods(handler, method));
    }

    public IRestHandler tryAllHandlers(final RestRequest request) {
        final String rawPath = request.rawPath();
        final String uri = request.uri();
        final Method requestMethod;
        try {
            // Resolves the HTTP method and fails if the method is invalid
            requestMethod = request.method();
            // Loop through all possible handlers, attempting to dispatch the request
            Iterator<MethodHandlers> allHandlers = getAllHandlers(request.params(), rawPath);
            while (allHandlers.hasNext()) {
                final IRestHandler handler;
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
            bootLogger.warn("find uri={}, rawPath={} handler throws exception: ", uri, rawPath);
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
