package com.github.yin.flags;

import com.google.common.collect.*;

import java.util.Map;

/**
 * Stores value objects indexed by <code>FlagID</code> in a <code>Multimap</code> fashion
 * and allows for easy access by any FlagID attribute.
 * @author Matej 'Yin' Gagyi
 */
public final class FlagIndex<T> {
    private final Multimap<String, T> byName = HashMultimap.create();
    private final Multimap<String, T> byClass = HashMultimap.create();
    private final Map<String, T> byFQN = Maps.newTreeMap();
    private ImmutableMultimap<String, T> _byName;
    private ImmutableMultimap<String, T> _byClass;
    private ImmutableMap<String, T> _byFQN;

    public void add(FlagID flagID, T flag) {
        String clazz = flagID.className();
        String name = flagID.flagName();
        String fqn = flagID.fqn();
        byName.put(name, flag);
        byClass.put(clazz, flag);
        byFQN.put(fqn, flag);
        _byName = null;
        _byClass = null;
        _byFQN = null;
    }

    public Multimap<String, T> byName() {
        return _byName != null ? _byName : (_byName = ImmutableMultimap.copyOf(byName));
    }

    public ImmutableMultimap<String, T> byClass() {
        return _byClass != null ? _byClass : (_byClass = ImmutableMultimap.copyOf(byClass));
    }

    public Map<String, T> byFQN() {
        return _byFQN != null ? _byFQN : (_byFQN = ImmutableMap.copyOf(byFQN));
    }
}
