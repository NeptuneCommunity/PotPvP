package net.frozenorb.potpvp.util.command.data;

import java.beans.ConstructorProperties;
import java.util.Set;

public class ParameterData
        implements Data {
    private final String name;
    private final String defaultValue;
    private final Class<?> type;
    private final boolean wildcard;
    private final int methodIndex;
    private final Set<String> tabCompleteFlags;
    private Class<? extends ParameterType> parameterType = null;

    @ConstructorProperties(value = {"name", "defaultValue", "type", "wildcard", "methodIndex", "tabCompleteFlags", "parameterType"})
    public ParameterData(String name, String defaultValue, Class<?> type, boolean wildcard, int methodIndex, Set<String> tabCompleteFlags, Class<? extends ParameterType> parameterType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
        this.wildcard = wildcard;
        this.methodIndex = methodIndex;
        this.tabCompleteFlags = tabCompleteFlags;
        this.parameterType = parameterType;
    }

    public String getName() {
        return this.name;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public Class<?> getType() {
        return this.type;
    }

    public boolean isWildcard() {
        return this.wildcard;
    }

    public int getMethodIndex() {
        return this.methodIndex;
    }

    public Set<String> getTabCompleteFlags() {
        return this.tabCompleteFlags;
    }

    public Class<? extends ParameterType> getParameterType() {
        return this.parameterType;
    }
}