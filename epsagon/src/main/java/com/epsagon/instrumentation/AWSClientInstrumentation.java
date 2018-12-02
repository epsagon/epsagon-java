package com.epsagon.instrumentation;

import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.handlers.RequestHandler2;

import com.epsagon.Trace;
import com.epsagon.events.operations.aws.Factory;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instrumentation for the {@link com.amazonaws.AmazonWebServiceClient} class.
 */
public class AWSClientInstrumentation extends EpsagonInstrumentation {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<TypeDescription> getTypeMatcher() {
        return ElementMatchers.named("com.amazonaws.AmazonWebServiceClient")
                .and(ElementMatchers.declaresField(ElementMatchers.named("requestHandler2s")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<ElementMatcher, String> getTransformers() {
        final Map<ElementMatcher, String> transformers = new HashMap<>();
        transformers.put(ElementMatchers.isConstructor(), AWSClientAdvice.class.getName());
        return transformers;
    }

    /**
     * A class defining a transformer for {@link com.amazonaws.AmazonWebServiceClient}
     */
    public static class AWSClientAdvice {
        /**
         * Adds code to the end of the {@link com.amazonaws.AmazonWebServiceClient} constructors
         * to add another request handler.
         * we are ignoring Throwables cause it is a constructor.
         * @param handlers The handlers member of the {@link com.amazonaws.AmazonWebServiceClient}
         *                 instance whose creation we are instrumenting.
         */
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void addEpsagonHandler(
            @Advice.FieldValue("requestHandler2s") final List<RequestHandler2> handlers,
            @Advice.FieldValue("client") final AmazonHttpClient client
        ) {
            for (final RequestHandler2 handler : handlers) {
                if (handler instanceof EpsagonAWSRequestHandler) {
                    return;
                }
            }
            try {
                handlers.add(new AWSClientInstrumentation.EpsagonAWSRequestHandler(client));
            } catch (Throwable e) {
                Trace.getInstance().addException(e);
            }
        }
    }

    /**
     * A RequestHandler that adds the event to Epsagon's Trace.
     */
    public static class EpsagonAWSRequestHandler extends RequestHandler2 {

        private Trace _trace = Trace.getInstance();
        private AmazonHttpClient _client;

        public EpsagonAWSRequestHandler(AmazonHttpClient client) {
            _client = client;
        }
        /**
        * {@inheritDoc}
        */
        @Override
        public void beforeRequest(Request<?> request) {
            // TODO: if problem of async event occures, add "promise" here
            // TODO: should be done via the get/setHandlerContext methods.
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public void afterResponse(Request<?> request, Response<?> response) {
            try {
                _trace.addEvent(
                        Factory.newBuilder(
                                request,
                                response,
                                _client
                        )
                );
            } catch (Exception error) {
                _trace.addException(error);
            }
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public void afterError(Request<?> request, Response<?> response, Exception e) {
            try {
                _trace.addEvent(
                        Factory.newBuilder(
                                request,
                                response,
                                _client,
                                e
                        )
                );
            } catch (Exception error) {
                _trace.addException(error);
            }
        }


    }

}
