/*
 * $Id: NetworkCorrespondenceDropper.java 1659 2011-12-28 10:50:46Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2011
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.gen;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.OntologyNetwork;

import java.net.URI;
import java.util.*;

/**
 * NetworkCorrespondenceDropper
 *
 * suppress n% of the correspondences at random in all alignments (globally)
 * n is a number between 0. and 1.
 * Returns a brand new BasicOntologyNetwork (with new alignments and cells)
 * the @threshold parameter tells if the corrrespondences are suppressed at random (false) of by suppressing the n% of lower confidence (true)
 */

public class NetworkCorrespondenceDropper implements OntologyNetworkWeakener {

    public OntologyNetwork weaken( OntologyNetwork on, int n, Properties p ) throws AlignmentException {
	throw new AlignmentException( "Cannot weaken alignments by fixed number of correspondences" );
    }

    public OntologyNetwork weaken( OntologyNetwork on, double n, Properties p ) throws AlignmentException {
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	boolean threshold = (p != null && p.getProperty("threshold") != null);
	OntologyNetwork newon = new BasicOntologyNetwork();

	this.weakenInnerA(on, newon);

	// Put all the cell/alignment in a array
	Collection<LCell> corresp = null;

		this.weakenInnerB(threshold, corresp);


	this.weakenInnerC(on, corresp, newon);

	// Select these correspondences to delete: either shuffle or order
	//System.err.println( n+" * "+corresp.size()+" = "+n*(double)(corresp.size()) );
	int q = (int)(n*(double)(corresp.size()));
	if ( !threshold ) Collections.shuffle( (ArrayList)corresp );
	// Suppress the n*size() last ones or those which are under threshold

		this.weakenInnerD(corresp, q);

	// Cut
	return newon;
    }

	/**
	 *
	 * @param on
	 * @param newon
	 */
	private void weakenInnerA(OntologyNetwork on, OntologyNetwork newon){
		for ( URI ontouri : on.getOntologies() ){
			newon.addOntology( ontouri );
		}
	}

	/**
	 *
	 * @param threshold
	 * @param corresp
	 */
	private void weakenInnerB(boolean threshold, Collection<LCell> corresp){
		if ( threshold ) {
			corresp = new TreeSet<LCell>();
		} else {
			corresp = new ArrayList<LCell>();
		}
	}

	/**
	 *
	 * @param on
	 * @param corresp
	 * @param newon
	 * @throws AlignmentException
	 */
	private void weakenInnerC(OntologyNetwork on, Collection<LCell> corresp, OntologyNetwork newon) throws AlignmentException {
		for ( Alignment al : on.getAlignments() ) {
			Alignment newal = (Alignment)al.clone();
			for ( Cell c : newal ) {
				corresp.add( new LCell( c, newal ) );
			}
			newon.addAlignment( newal );
		}
	}

	/**
	 *
	 * @param corresp
	 * @param q
	 * @throws AlignmentException
	 */
	private void weakenInnerD(Collection<LCell> corresp, int q) throws AlignmentException {
		for ( LCell c : corresp ) {
			if ( q == 0 ) break;
			q--;
			//System.err.println( "Cell ["+c.getCell().getStrength()+"] : "+c );
			c.getAlignment().remCell( c.getCell() );
		}
	}

}

class LCell implements Comparable<LCell> {
    Alignment alignment = null;
    Cell cell = null;

    LCell( Cell c, Alignment al ) {
	alignment = al;
	cell = c;
    }

    /**
     * This is not the standard required definition of compareTo
     * But this is the one which works exactly here (we want the structure ordered, 
     * we do not want that two cells be equated (in case of 0)
     */
    public int compareTo( LCell c ) {
	if ( cell.getStrength() > c.getCell().getStrength() ) return 1;
	else return -1;/*if ( cell.getStrength() < c.getCell().getStrength() ) return -1;
			 else return 0;*/
    }

    Alignment getAlignment() { return alignment; }
    Cell getCell() { return cell; }
}
