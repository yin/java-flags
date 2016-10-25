package com.github.yin.flags;

import com.google.auto.value.AutoValue;

/**
 * @author Matej 'Yin' Gagyi
 */
@AutoValue
public abstract class FlagID implements Comparable<FlagID> {
    static FlagID create(String className, String flagName) {
        return new AutoValue_FlagID(className, flagName);
    }

    public abstract String className();

    public abstract String flagName();

    public final String fqn() {
        return className() + '.' + flagName();
    }

    public int compareTo(FlagID that) {
        return fqn().compareTo(that.fqn());
    }
}
