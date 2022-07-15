package net.frozenorb.potpvp.util.command.data;

@FunctionalInterface
public interface Processor<T, R> {
    R process(T var1);
}