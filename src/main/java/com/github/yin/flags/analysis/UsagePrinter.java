package com.github.yin.flags.analysis;

import com.github.yin.flags.ClassMetadataIndex;
import com.github.yin.flags.FlagIndex;
import com.github.yin.flags.FlagMetadata;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Matej 'Yin' Gagyi
 */
public class UsagePrinter {
    public void printUsage(FlagIndex<FlagMetadata> flagMetaIndex, ClassMetadataIndex classMetaIndex, PrintStream out) {
        ImmutableMultimap<String, FlagMetadata> links = flagMetaIndex.byClass();
        for (String className : links.keySet()) {
            Set<FlagMetadata> classFlags = Sets.newTreeSet(links.get(className));
            out.println(className + ':');
            if (classMetaIndex.classes().containsKey(className)) {
                String desc = classMetaIndex.classes().get(className).desc();
                if (desc != null && !desc.isEmpty()) {
                    out.println(desc);
                    out.println();
                }
            }
            for (FlagMetadata link : classFlags) {
                out.println("\t" + link.flagID().flagName() + "\t" + link.desc());
            }
            out.println();
        }
    }

}
