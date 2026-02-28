package com.advancedjava.demo;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class AttachMain {

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
