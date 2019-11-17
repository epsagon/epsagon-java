package com.epsagon;

import com.epsagon.instrumentation.EpsagonInstrumentation;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * A class for making all the patching required for instrumentation.
 */
public class Patcher {
    public static Instrumentation instrumentation;

    /**
     * Runs all the existing instrumentations.
     * @param inst The instrumentation object the agentmain received.
     */
    public static void instrumentAll(Instrumentation inst) {
        instrumentation = inst;
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(
                        not(nameStartsWith("com.amazonaws"))
                );

        for (final EpsagonInstrumentation instrumenter : ServiceLoader.load(EpsagonInstrumentation.class)) {
            agentBuilder = instrumenter.instrument(agentBuilder);
        }
        agentBuilder.installOn(inst);
    }
}
