package com.epsagon;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Installs Epsagon agent, which is a helper way to do instrumentation.
 */
public class Installer {
    private static final Logger _LOG = LogManager.getLogger(EpsagonRequestHandler.class);

    /**
     * installs the agent.
     */
    public static synchronized void install() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        Patcher.instrumentation = null; // loading Patcher, do not remove this line.

        for (VirtualMachineDescriptor vmd : vms) {
            try {
                VirtualMachine vm = VirtualMachine.attach(vmd.id());
                try {
                    vm.loadAgent("/var/task/agent.jar");
                } finally {
                    vm.detach();
                }
            } catch (Exception e) {
                _LOG.error("Error attaching to VM, skipping instrumentation", e);
                Trace.getInstance().addException(e);
            }
        }
    }
}
