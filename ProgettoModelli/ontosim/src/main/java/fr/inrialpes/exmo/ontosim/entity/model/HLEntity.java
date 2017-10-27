package fr.inrialpes.exmo.ontosim.entity.model;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;

/**
 *
 * @param <E>
 */
public interface HLEntity<E> extends Entity<E> {
    public HeavyLoadedOntology<E> getOntology();
    
}
