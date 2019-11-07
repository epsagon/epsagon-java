package com.epsagon.instrumentation;

import com.epsagon.EpsagonRequestHandler;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

/**
 * A base class for Epsagon instrumentation.
 */
public abstract class EpsagonInstrumentation {
    /**
     * @return An {@link ElementMatcher} matching only classes the instrumentation should
     * be activated on.
     */
    protected abstract ElementMatcher<? super TypeDescription> getTypeMatcher();

    /**
     * @return a map of {@link ElementMatcher} which match functions the instrumentation should
     * tranform to Transformers class names.
     */
    protected abstract Map<ElementMatcher, String> getTransformers();

    /**
     * Activates the instrumentation.
     * @param parent The current agentBuilder.
     * @return agentBuilder with the instrumentation activated.
     */
    public AgentBuilder instrument(final AgentBuilder parent) {
        AgentBuilder agentBuilder = runSpecificTransformers(parent);
        return agentBuilder;
    }

    /**
     * Runs all the transformers this instrumentation.
     * @param builder The current builder.
     * @return agentBuilder with this instrumentation activated.
     */
    private AgentBuilder runSpecificTransformers(
            AgentBuilder builder
    ) {
        try {
            for (
                final Map.Entry<? extends ElementMatcher, String> entry :
                getTransformers().entrySet()
            ) {
                builder = builder.type(getTypeMatcher())
                    .transform(
                        new AgentBuilder.Transformer.ForAdvice()
                            .include(EpsagonRequestHandler.class.getClassLoader())
                            .advice(entry.getKey(), entry.getValue())
                    );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("unable to update classloader");
        }

        return builder;
    }

}
