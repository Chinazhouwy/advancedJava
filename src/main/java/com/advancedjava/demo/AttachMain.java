package com.advancedjava.demo;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

/**
 * 动态 attach Java Agent 的示例入口。
 *
 * <p>运行该类后，会枚举当前 JVM 列表，查找目标演示进程，
 * 然后把包含 {@code agentmain} 的 Agent JAR 动态加载到目标 JVM 中。
 */
public class AttachMain {

    /**
     * 查找目标 JVM，并把 Agent JAR 动态加载进去。
     */
    public static void main(String[] args) throws Exception {
        List<VirtualMachineDescriptor> listBefore = VirtualMachine.list();
        // agentmain()方法所在jar包
        String jar = "/Users/chinazhouwy/doc/code/advancedJava/target/advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar";

        for (VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
            // 针对指定名称的JVM实例
            if (virtualMachineDescriptor.displayName().equals("src.main.java.com.advancedjava.demo.AgentTest")) {
                System.out.println("将对该进程的vm进行增强：testDemo.AgentTest的vm进程, pid=" + virtualMachineDescriptor.id());
                // attach到新JVM
                VirtualMachine vm = VirtualMachine.attach(virtualMachineDescriptor);
                // 加载agentmain所在的jar包
                vm.loadAgent(jar);
                // detach
                vm.detach();
            }
        }
    }
}
