/*
 * $Id: DistanceAlignment.java 1630 2011-09-15 20:29:40Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2011
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl;

import java.lang.ClassCastException;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.ontowrap.OntowrapException;

import fr.inrialpes.exmo.ontosim.util.HungarianAlgorithm;

/**
 * The mother class for distance or similarity-based alignments.
 * It is abstract because it does not provide an implemented similarity measure
 * Otherwise everything is fine.
 *
 * This class should work with similarity and distances, as soon as, the used
 * similarity structure is defined as such.
 *
 * @author Jérôme Euzenat
 * @version $Id: DistanceAlignment.java 1630 2011-09-15 20:29:40Z euzenat $
 */

public abstract class DistanceAlignment extends ObjectAlignment implements AlignmentProcess {
    Similarity sim;

    /** Creation **/
    public DistanceAlignment() {};

    public void setSimilarity( Similarity s ) { sim = s; }
    public Similarity getSimilarity() { return sim; }

    /**
     * Process matching
     * - create distance data structures,
     * - compute distance or similarity
     * - extract alignment
     **/
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
	loadInit( alignment );
	if (  params.getProperty("type") != null )
	    setType( params.getProperty("type") );
	// This is a 1:1 alignment in fact
	else setType("11");
	if ( sim == null )
	    throw new AlignmentException("DistanceAlignment: requires a similarity measure");

	sim.initialize( ontology1(), ontology2(), init );
	sim.compute( params );
	if ( params.getProperty("printMatrix") != null ) printDistanceMatrix(params);
	extract( getType(), params );
    }

    /**
     * Prints the distance matrix
     */
    public void printDistanceMatrix( Properties params ){
	String algName = params.getProperty("algName");
	String metric = "distance";
	if ( sim.getSimilarity() ) metric = "similarity";
	if ( algName == null ) algName = getClass().toString();
	System.out.println("\\documentclass{article}\n");
	System.out.println("\\usepackage{graphics}\n");
	System.out.println("\\begin{document}\n");
	System.out.println("\\begin{table}");
	sim.printClassSimilarityMatrix("tex");
	System.out.println("\\caption{Class "+metric+" with measure "+algName+".}" );
	System.out.println("\\end{table}");
	System.out.println();
	System.out.println("\\begin{table}");
	sim.printPropertySimilarityMatrix("tex");
	System.out.println("\\caption{Property "+metric+" with measure "+algName+".}" );
	System.out.println("\\end{table}");
	System.out.println();
	System.out.println("\\begin{table}");
	sim.printIndividualSimilarityMatrix("tex");
	System.out.println("\\caption{Individual "+metric+" with measure "+algName+".}" );
	System.out.println("\\end{table}");
	System.out.println("\n\\end{document}");
    }

    /**
     * Suppresses the distance matrix
     */
    public void cleanUp() {
	sim = null;
    }

    /**
     * Extract the alignment form the Similarity
     * There are theoretically 16 types of extractors composing the
     * characteristics
     * [q]estion mark = ?, one or zero relation
     * [s]tar = *, one, zero or many relations
     * [1] = 1, exactly one relation
     * [p]lus = +, one or many relations
     * for each place of the relation. Howerver, since it is not possible from a matrics to guarantee that one object will be in at least one relation, this is restricted to the four following types:
     * ?? (covering 11, 1? and ?1)
     * ** (covering ++, *+ and +*)
     * ?* (covering 1*, 1+ and ?+)
     * *? (covering +?, *1 and +1)
     */
    public Alignment extract(String type, Properties params) throws AlignmentException {
	double threshold = 0.;
	if (  params.getProperty("threshold") != null )
	    threshold = Double.parseDouble( params.getProperty("threshold") );

	//System.err.println("The type is "+type+" with length = "+type.length());
	return this.extractIfElseMethodA(type, params, threshold);

    }

	/**
	 *
	 * @param type
	 * @param params
	 * @param threshold
	 * @return
	 * @throws AlignmentException
	 */
    private Alignment extractIfElseMethodA(String type, Properties params, double threshold) throws AlignmentException {
		if ( type.equals("?*") || type.equals("1*") || type.equals("?+") || type.equals("1+") ) {
			return extractqs(threshold, params);
		}else{
			return this.extractIfElseMethodB(type, params, threshold);
		}
	}

	/**
	 *
	 * @param type
	 * @param params
	 * @param threshold
	 * @return
	 * @throws AlignmentException
	 */
	private Alignment extractIfElseMethodB(String type, Properties params, double threshold) throws AlignmentException {
		if ( type.equals("??") || type.equals("1?") || type.equals("?1") || type.equals("11") ) {
			return extractqq(threshold, params);
		}else{
			return this.extractIfElseMethodC(type, params, threshold);
		}
	}

	/**
	 *
	 * @param type
	 * @param params
	 * @param threshold
	 * @return
	 * @throws AlignmentException
	 */
	private Alignment extractIfElseMethodC(String type, Properties params, double threshold) throws AlignmentException {
		if (type.equals("*?") || type.equals("+?") || type.equals("*1") || type.equals("+1")) {
			return extractqs(threshold, params);
		}else{
			return this.extractIfElseMethodD(type, params, threshold);
		}
	}

	/**
	 *
	 * @param type
	 * @param params
	 * @param threshold
	 * @return
	 * @throws AlignmentException
	 */
	private Alignment extractIfElseMethodD(String type, Properties params, double threshold) throws AlignmentException {
		if (type.equals("**") || type.equals("+*") || type.equals("*+") || type.equals("++")) {
			return extractss(threshold, params);
		} else{
			throw new AlignmentException("Unknown alignment type: " + type);
		}
	}

    // JE: It is now certainly possible to virtualise extraction as it has
    // been done for printing matrix in MatrixMeasure (todo)

    /**
     * Extract the alignment of a ?* type
     * Non symmetric: for each entity of onto1, take the highest if superior to threshold
     * Complexity: O(n^2)
     */
    @SuppressWarnings("unchecked") //ConcatenatedIterator
    public Alignment extractqs( double threshold, Properties params) {
      double max = 0.;
      boolean found = false;
      double val = 0.;

      try {
	  // Extract for properties
	  ConcatenatedIterator pit1 = new
	      ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
				   ontology1().getDataProperties().iterator());
	  this.extractqsForMethodA(pit1, found, max, threshold, val);

	  // Extract for classes
		  this.extractqsForMethodB(found, max, threshold, val);

	  // Extract for individuals
	  if (  params.getProperty("noinst") == null ){
	      this.extractqsForMethodC(found, max, threshold, val);
	  }
      } catch (OntowrapException owex) { owex.printStackTrace(); //}
      } catch (AlignmentException alex) { alex.printStackTrace(); }
      return((Alignment)this);
    }

	/**
	 *
	 * @param pit1
	 * @param found
	 * @param max
	 * @param threshold
	 * @param val
	 * @throws AlignmentException
	 * @throws OntowrapException
	 */
    private void extractqsForMethodA(ConcatenatedIterator pit1, boolean found, double max, double threshold, double val) throws AlignmentException, OntowrapException {
		for( Object prop1 : pit1 ){
			found = false; max = threshold; val = 0.;
			Object prop2 = null;
			ConcatenatedIterator pit2 = new
					ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
					ontology2().getDataProperties().iterator());
			this.extractqsInnerForMethodA(pit2, prop1, prop2, found, max, threshold, val);
			if ( found ) addAlignCell(prop1,prop2, "=", max);
		}
	}

	/**
	 *
	 * @param pit2
	 * @param prop1
	 * @param prop2
	 * @param found
	 * @param max
	 * @param threshold
	 * @param val
	 */
	private void extractqsInnerForMethodA(ConcatenatedIterator pit2, Object prop1, Object prop2, boolean found, double max, double threshold, double val){
		for ( Object current : pit2 ){
			if ( sim.getSimilarity() ) val = sim.getPropertySimilarity(prop1,current);
			else val =  1. - sim.getPropertySimilarity(prop1,current);
			if ( val > max) {
				found = true; max = val; prop2 = current;
			}
		}
	}

	/**
	 *
	 * @param found
	 * @param max
	 * @param threshold
	 * @param val
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
	private void extractqsForMethodB( boolean found, double max, double threshold, double val) throws OntowrapException, AlignmentException {
		for ( Object class1 : ontology1().getClasses() ) {
			found = false; max = threshold; val = 0;
			Object class2 = null;
			this.extractqsInnerForMethodB(class1, class2, found, max, val);
			if ( found ) addAlignCell(class1, class2, "=", max);
		}
	}

	/**
	 *
	 * @param class1
	 * @param class2
	 * @param found
	 * @param max
	 * @param val
	 * @throws OntowrapException
	 */
	private void extractqsInnerForMethodB(Object class1, Object class2, boolean found, double max, double val) throws OntowrapException {
		for ( Object current : ontology2().getClasses() ) {
			if ( sim.getSimilarity() ) val = sim.getClassSimilarity(class1,current);
			else val = 1. - sim.getClassSimilarity(class1,current);
			if (val > max) {
				found = true; max = val; class2 = current;
			}
		}
	}

	/**
	 *
	 * @param found
	 * @param max
	 * @param threshold
	 * @param val
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
	private void extractqsForMethodC( boolean found, double max, double threshold, double val) throws OntowrapException, AlignmentException {
		for ( Object ind1 : ontology1().getIndividuals() ) {
			if ( ontology1().getEntityURI( ind1 ) != null ) {
				found = false; max = threshold; val = 0;
				Object ind2 = null;
				this.extractqsInnerForMethodC(ind1, ind2, found, max, val);
				if ( found ) addAlignCell(ind1,ind2, "=", max);
			}
		}
	}

	/**
	 *
	 * @param ind1
	 * @param ind2
	 * @param found
	 * @param max
	 * @param val
	 * @throws OntowrapException
	 */
	private void extractqsInnerForMethodC(Object ind1, Object ind2, boolean found, double max, double val) throws OntowrapException {
		for ( Object current : ontology2().getIndividuals() ) {
			if ( ontology2().getEntityURI( current ) != null ) {
				if ( sim.getSimilarity() ) val = sim.getIndividualSimilarity( ind1, current );
				else val = 1 - sim.getIndividualSimilarity( ind1, current );
				if (val > max) {
					found = true; max = val; ind2 = current;
				}
			}
		}
	}

    /**
     * Extract the alignment of a ** type
     * Symmetric: return all elements above threshold
     * Complexity: O(n^2)
     */
    @SuppressWarnings("unchecked") //ConcatenatedIterator
    public Alignment extractss( double threshold, Properties params) {
	double val = 0.;
	try {
	    // Extract for properties
	    ConcatenatedIterator pit1 = new
		ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
				     ontology1().getDataProperties().iterator());
	   this.extractssForMethodA( pit1, val, threshold);
	    // Extract for classes
	    this.extractssForMethodB(val, threshold);
	    // Extract for individuals

		if (  params.getProperty("noinst") == null ){
		this.extractssForMethodC(val, threshold);
	    }
	} catch (OntowrapException owex) { owex.printStackTrace(); //}
	} catch (AlignmentException alex) { alex.printStackTrace(); }
	return((Alignment)this);
    }

	/**
	 *
	 * @param pit1
	 * @param val
	 * @param threshold
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
    private void extractssForMethodA(ConcatenatedIterator pit1, double val, double threshold) throws OntowrapException, AlignmentException {
		for( Object prop1 : pit1 ){
			ConcatenatedIterator pit2 = new
					ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
					ontology2().getDataProperties().iterator());
			for ( Object prop2 : pit2 ){
				if ( sim.getSimilarity() ) val = sim.getPropertySimilarity(prop1,prop2);
				else val =  1. - sim.getPropertySimilarity(prop1,prop2);
				if ( val > threshold ) addAlignCell(prop1,prop2, "=", val);
			}
		}
	}

	/**
	 *
	 * @param val
	 * @param threshold
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
	private void extractssForMethodB(double val, double threshold) throws OntowrapException, AlignmentException {
		for ( Object class1 : ontology1().getClasses() ) {
			for ( Object class2 : ontology2().getClasses() ) {
				if ( sim.getSimilarity() ) val = sim.getClassSimilarity(class1,class2);
				else val = 1. - sim.getClassSimilarity(class1,class2);
				if (val > threshold ) addAlignCell(class1, class2, "=", val);
			}
		}
	}

	/**
	 *
	 * @param val
	 * @param threshold
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
	private void extractssForMethodC(double val, double threshold) throws OntowrapException, AlignmentException {
		for ( Object ind1 : ontology1().getIndividuals() ) {
			if ( ontology1().getEntityURI( ind1 ) != null ) {
				this.extractssInnerForMethodC(ind1, val, threshold);
			}
		}
	}

	/**
	 *
	 * @param ind1
	 * @param val
	 * @param threshold
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
	private void extractssInnerForMethodC(Object ind1, double val, double threshold) throws OntowrapException, AlignmentException {
		for ( Object ind2 : ontology2().getIndividuals() ) {
			if ( ontology2().getEntityURI( ind2 ) != null ) {
				if ( sim.getSimilarity() ) val = sim.getIndividualSimilarity( ind1, ind2 );
				else val = 1 - sim.getIndividualSimilarity( ind1, ind2 );
				if ( val > threshold ) addAlignCell(ind1,ind2, "=", val);
			}
		}
	}

    /**
     * Extract the alignment of a ?? type
     *
     * exact algorithm using the Hungarian method.
     * This algorithm contains several guards to prevent the HungarianAlgorithm to
     * raise problems:
     * - It invert column and rows when nbrows > nbcol (Hungarian loops)
     * - It prevents to generate alignments when one category has no elements.
     */
    @SuppressWarnings("unchecked") //ConcatenatedIterator
    public Alignment extractqq( double threshold, Properties params) {
	try {
	    // A STRAIGHTFORWARD IMPLEMENTATION
	    // (redoing the matrix instead of getting it)
	    // For each kind of stuff (cl, pr, ind)
	    // Create a matrix
	    int nbclasses1 = ontology1().nbClasses();
	    int nbclasses2 = ontology2().nbClasses();
	    this.extractqqIfElseMethodA(nbclasses1, nbclasses2, threshold);
	// For properties
	    int nbprop1 = ontology1().nbProperties();
	    int nbprop2 = ontology2().nbProperties();
	    if ( nbprop1 != 0 && nbprop2 != 0 ) {
		double[][] matrix = new double[nbprop1][nbprop2];
		Object[] prop1 = new Object[nbprop1];
		Object[] prop2 = new Object[nbprop2];
		int i = 0;
		ConcatenatedIterator pit1 = new
		    ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
					 ontology1().getDataProperties().iterator());
		for ( Object ob: pit1 ) prop1[i++] = ob;
		int j = 0;
		ConcatenatedIterator pit2 = new
		    ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
					 ontology2().getDataProperties().iterator());
		for ( Object ob: pit2 ) prop2[j++] = ob;
		this.extractqqForMethodB(i, j, nbprop1, nbprop2, matrix, prop1, prop2);
		// Pass it to the algorithm
		int[][] result = callHungarianMethod( matrix, nbprop1, nbprop2 );
		// Extract the result
			this.extractqqForMethodC(result, i,  prop1, prop2, threshold);
	    }
	} catch (AlignmentException alex) { alex.printStackTrace(); }
	catch (OntowrapException owex) { owex.printStackTrace(); }
	// For individuals
		this.extractqqIfElseMethodD(params, threshold );
	return((Alignment)this);
    }

    public int[][] callHungarianMethod( double[][] matrix, int i, int j ) {
	boolean transposed = false;
	if ( i > j ) { // transposed aray (because rows>columns).
	    matrix = HungarianAlgorithm.transpose(matrix);
	    transposed = true;
	}
	int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
	if ( transposed ) {
	    for( int k=0; k < result.length ; k++ ) {
		int val = result[k][0]; result[k][0] = result[k][1]; result[k][1] = val;
	    }
	}
	return result;
    }

	/**
	 *
	 * @param nbclasses1
	 * @param nbclasses2
	 * @param threshold
	 * @throws OntowrapException
	 * @throws AlignmentException
	 */
    private void extractqqIfElseMethodA(int nbclasses1, int nbclasses2, double threshold) throws OntowrapException, AlignmentException {
		if ( nbclasses1 != 0 && nbclasses2 != 0 ) {
			double[][] matrix = new double[nbclasses1][nbclasses2];
			Object[] class1 = new Object[nbclasses1];
			Object[] class2 = new Object[nbclasses2];
			int i = 0;
			int j = 0;
			this.extractqqIfElseMethodAInnerForA(class1, class2, i, j, nbclasses1, nbclasses2, matrix);

			// Pass it to the algorithm
			int[][] result = callHungarianMethod( matrix, nbclasses1, nbclasses2 );
			// Extract the result
			this.extractqqIfElseMethodAInnerForB(class1, class2, i, result, threshold);
		}
	}

	/**
	 *
	 * @param class1
	 * @param class2
	 * @param i
	 * @param j
	 * @param nbclasses1
	 * @param nbclasses2
	 * @param matrix
	 * @throws OntowrapException
	 */
	private void extractqqIfElseMethodAInnerForA(Object[] class1, Object[] class2, int i, int j, int nbclasses1, int nbclasses2, double[][] matrix) throws OntowrapException {
		for ( Object ob : ontology1().getClasses() ) {
			class1[i++] = ob;
		}
		for ( Object ob : ontology2().getClasses() ) {
			class2[j++] = ob;
		}
		for( i = 0; i < nbclasses1; i++ ){
			for( j = 0; j < nbclasses2; j++ ){
				if ( sim.getSimilarity() ) matrix[i][j] = sim.getClassSimilarity(class1[i],class2[j]);
				else matrix[i][j] = 1. - sim.getClassSimilarity(class1[i],class2[j]);
			}
		}
	}

	/**
	 *
	 * @param class1
	 * @param class2
	 * @param i
	 * @param result
	 * @param threshold
	 * @throws AlignmentException
	 */
	private void extractqqIfElseMethodAInnerForB(Object[] class1, Object[] class2, int i, int[][] result, double threshold) throws AlignmentException {
		for( i=0; i < result.length ; i++ ){
			// The matrix has been destroyed
			double val;
			if ( sim.getSimilarity() ) val = sim.getClassSimilarity(class1[result[i][0]],class2[result[i][1]]);
			else val = 1 - sim.getClassSimilarity(class1[result[i][0]],class2[result[i][1]]);
			// JE: here using strict-> is a very good idea.
			// it means that correspondences with 0. similarity
			// will be excluded from the best match.
			if( val > threshold ){
				addCell( new ObjectCell( (String)null, class1[result[i][0]], class2[result[i][1]], BasicRelation.createRelation("="), val ) );
			}
		}
	}

	/**
	 *
	 * @param i
	 * @param j
	 * @param nbprop1
	 * @param nbprop2
	 * @param matrix
	 * @param prop1
	 * @param prop2
	 */
	private void extractqqForMethodB(int i, int j, int nbprop1, int nbprop2, double[][] matrix, Object[] prop1, Object[] prop2){
		for( i = 0; i < nbprop1; i++ ){
			for( j = 0; j < nbprop2; j++ ){
				if ( sim.getSimilarity() ) matrix[i][j] = sim.getPropertySimilarity(prop1[i],prop2[j]);
				else matrix[i][j] = 1. - sim.getPropertySimilarity(prop1[i],prop2[j]);
			}
		}
	}

	/**
	 *
	 * @param result
	 * @param i
	 * @param prop1
	 * @param prop2
	 * @param threshold
	 * @throws AlignmentException
	 */
	private void extractqqForMethodC(int[][] result, int i, Object[] prop1, Object[] prop2, double threshold) throws AlignmentException {
		for( i=0; i < result.length ; i++ ){
			// The matrix has been destroyed
			double val;
			if ( sim.getSimilarity() ) val = sim.getPropertySimilarity(prop1[result[i][0]],prop2[result[i][1]]);
			else val = 1 - sim.getPropertySimilarity(prop1[result[i][0]],prop2[result[i][1]]);
			// JE: here using strict-> is a very good idea.
			// it means that alignments with 0. similarity
			// will be excluded from the best match.
			if( val > threshold ){
				addCell( new ObjectCell( (String)null, prop1[result[i][0]], prop2[result[i][1]], BasicRelation.createRelation("="), val ) );
			}
		}
	}

	/**
	 *
	 * @param params
	 * @param threshold
	 */
	private void extractqqIfElseMethodD(Properties params, double threshold ){
		if (  params.getProperty("noinst") == null ){
			try {
				// Create individual lists
				Object[] ind1 = new Object[ontology1().nbIndividuals()];
				Object[] ind2 = new Object[ontology2().nbIndividuals()];
				int nbind1 = 0;
				int nbind2 = 0;
				this.extractqqIfElseMethodDInnerForA(ind1, ind2, nbind1, nbind2);

				if ( nbind1 != 0 && nbind2 != 0 ) {
					double[][] matrix = new double[nbind1][nbind2];
					int i = 0;
					int j = 0;
					this.extractqqIfElseMethodDInnerForB(i, j, matrix, ind1, ind2, nbind1, nbind2);

					// Pass it to the algorithm
					int[][] result = callHungarianMethod( matrix, nbind1, nbind2 );
					// Extract the result
					this.extractqqIfElseMethodDInnerForC(i, result, ind1, ind2, threshold);
				}
			} catch (AlignmentException alex) { alex.printStackTrace(); //}
			} catch (OntowrapException owex) { owex.printStackTrace(); }
		}
	}

	/**
	 *
	 * @param ind1
	 * @param ind2
	 * @param nbind1
	 * @param nbind2
	 * @throws OntowrapException
	 */
	private void extractqqIfElseMethodDInnerForA(Object[] ind1, Object[] ind2, int nbind1, int nbind2) throws OntowrapException {
		for( Object ob : ontology2().getIndividuals() ){
			// We suppress anonymous individuals... this is not legitimate
			if ( ontology2().getEntityURI( ob ) != null ) {
				ind2[nbind2++] = ob;
			}
		}
		for( Object ob : ontology1().getIndividuals() ){
			// We suppress anonymous individuals... this is not legitimate
			if ( ontology1().getEntityURI( ob ) != null ) {
				ind1[nbind1++] = ob;
			}
		}
	}

	/**
	 *
	 * @param i
	 * @param j
	 * @param matrix
	 * @param ind1
	 * @param ind2
	 * @param nbind1
	 * @param nbind2
	 */
	private void extractqqIfElseMethodDInnerForB(int i,int  j, double[][] matrix, Object[] ind1, Object[] ind2, int nbind1, int nbind2){
		for( i=0; i < nbind1; i++ ){
			for( j=0; j < nbind2; j++ ){
				if ( sim.getSimilarity() ) matrix[i][j] = sim.getIndividualSimilarity(ind1[i],ind2[j]);
				else matrix[i][j] = 1 - sim.getIndividualSimilarity(ind1[i],ind2[j]);
			}
		}
	}

	/**
	 *
	 * @param i
	 * @param result
	 * @param ind1
	 * @param ind2
	 * @param threshold
	 * @throws AlignmentException
	 */
	private void extractqqIfElseMethodDInnerForC(int i, int[][] result, Object[] ind1, Object[] ind2, double threshold) throws AlignmentException {
		for( i=0; i < result.length ; i++ ){
			// The matrix has been destroyed
			double val;
			if ( sim.getSimilarity() ) val = sim.getIndividualSimilarity(ind1[result[i][0]],ind2[result[i][1]]);
			else val = 1 - sim.getIndividualSimilarity(ind1[result[i][0]],ind2[result[i][1]]);
			// JE: here using strict-> is a very good idea.
			// it means that alignments with 0. similarity
			// will be excluded from the best match.
			if( val > threshold ){
				addCell( new ObjectCell( (String)null, ind1[result[i][0]], ind2[result[i][1]], BasicRelation.createRelation("="), val ) );
			}
		}
	}

    /**
     * Greedy algorithm:
     * 1) dump the part of the matrix distance above threshold in a sorted set
     * 2) traverse the sorted set and each time a correspondence involving two
     *    entities that have no correspondence is encountered, add it to the
     *    alignment.
     * Complexity: O(n^2.logn)
     * Pitfall: no global optimality is warranted, nor stable marriage
     * for instance if there is the following matrix:
     * (a,a')=1., (a,b')=.9, (b,a')=.9, (b,b')=.1
     * This algorithm will select the first and last correspondances of
     * overall similarity 1.1, while the optimum is the second solution
     * with overall of 1.8.
     */
	@SuppressWarnings("unchecked") //ConcatenatedIterator
	public Alignment extractqqgreedy( double threshold, Properties params) {
		double val = 0;
		//TreeSet could be replaced by something else
		//The comparator must always tell that things are different!
		SortedSet<Cell> cellSet = null;
		this.cellSetMethod(cellSet);
		try {
			// Get all the matrix above threshold in the SortedSet
			// Plus a map from the objects to the cells
			// O(n^2.log n)
			// for classes
			try {
				this.extractqqgreedyForMethodA(cellSet, val, threshold);
			} catch (AlignmentException e) {
				e.printStackTrace();
			}

			// for properties
			ConcatenatedIterator pit1 = new
					ConcatenatedIterator(ontology1().getObjectProperties().iterator(),
					ontology1().getDataProperties().iterator());
			try {
				this.extractqqgreedyForMethodB(cellSet, pit1, val, threshold);
			} catch (AlignmentException e) {
				e.printStackTrace();
			}

			// for individuals
			try {
				this.extractqqgreedyIfElseMethodC(cellSet, params, val, threshold);
			} catch (AlignmentException e) {
				e.printStackTrace();
			}
			// O(n^2)
			this.extractqqgreedyForMethodD(cellSet);
		} catch (OntowrapException owex) {
			owex.printStackTrace();
		}
		return((Alignment)this);
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseA(Cell o1, Cell o2) throws OntowrapException {
		if ( o1.getStrength() > o2.getStrength() ){
			return -1;
		} else{
			return compareIfElseB(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseB(Cell o1, Cell o2) throws OntowrapException {
		if ( o1.getStrength() < o2.getStrength() ){
			return 1;
		} else{
			return compareIfElseC(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseC(Cell o1, Cell o2) throws OntowrapException {
		if ( ontology1().getEntityName( o1.getObject1() ) == null
				|| ontology2().getEntityName( o2.getObject1() ) == null ) {
			return -1;
		} else {
			return compareIfElseD(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseD(Cell o1, Cell o2) throws OntowrapException {
		if ( ontology1().getEntityName( o1.getObject1()).compareTo( ontology2().getEntityName( o2.getObject1() ) ) > 0 ) {
			return -1;
		} else {
			return compareIfElseE(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseE(Cell o1, Cell o2) throws OntowrapException {
		if ( ontology1().getEntityName( o1.getObject1()).compareTo( ontology2().getEntityName( o2.getObject1() ) ) < 0 ) {
			return 1;
		} else {
			return compareIfElseF(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseF(Cell o1, Cell o2) throws OntowrapException {
		if ( ontology1().getEntityName( o1.getObject2() ) == null
				|| ontology2().getEntityName( o2.getObject2() ) == null ) {
			return -1;
		} else {
			return compareIfElseG(o1, o2);
		}
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OntowrapException
	 */
	int compareIfElseG(Cell o1, Cell o2) throws OntowrapException {
		if ( ontology1().getEntityName( o1.getObject2()).compareTo( ontology2().getEntityName( o2.getObject2() ) ) > 0 ) {
			return -1;
			// Assume they have different names
		} else {
			return 1;
		}
	}

	/**
	 *
	 * @param cellSet
	 */
	private void cellSetMethod(SortedSet<Cell> cellSet){
		cellSet = new TreeSet<Cell>(
				new Comparator<Cell>() {
					public int compare( Cell o1, Cell o2 )
							throws ClassCastException{
						//System.err.println(o1.getObject1()+" -- "+o1.getObject2()+" // "+o2.getObject1()+" -- "+o2.getObject2());
						try {
							return compareIfElseA(o1, o2);
						} catch (OntowrapException e) {
							e.printStackTrace();
						}
						return -1;
					}
				}
		);
	}


	/**
	 * New Method extractqqgreedyForMethodA()
	 */
	private void extractqqgreedyForMethodA(SortedSet<Cell> cellSet, double val, double threshold) throws OntowrapException, AlignmentException {
		for ( Object ent1: ontology1().getClasses() ) {
			for ( Object ent2: ontology2().getClasses() ) {
				this.extractqqgreedyInnerForMethodA(cellSet, val, threshold, ent1, ent2);
			}
		}
	}

	/**
	 * New Method extractqqgreedyInnerForMethodA()
	 */
	private void extractqqgreedyInnerForMethodA(SortedSet<Cell> cellSet, double val, double threshold, Object ent1, Object ent2) throws AlignmentException {
		if ( sim.getSimilarity() ) val = sim.getClassSimilarity( ent1, ent2 );
		else val = 1 - sim.getClassSimilarity( ent1, ent2 );
		if ( val > threshold ){
			cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
		}
	}


	/**
	 * New Method extractqqgreedyForMethodB()
	 */
	private void extractqqgreedyForMethodB(SortedSet<Cell> cellSet, ConcatenatedIterator pit1, double val, double threshold) throws OntowrapException, AlignmentException {
		for ( Object ent1: pit1 ) {
			ConcatenatedIterator pit2 = new
					ConcatenatedIterator(ontology2().getObjectProperties().iterator(),
					ontology2().getDataProperties().iterator());
			for ( Object ent2: pit2 ) {
				this.extractqqgreedyInnerForMethodB(cellSet, val, threshold, ent1, ent2);

			}
		}
	}

	/**
	 * New Method extractqqgreedyInnerForMethodB()
	 */
	private void extractqqgreedyInnerForMethodB(SortedSet<Cell> cellSet, double val, double threshold, Object ent1, Object ent2) throws AlignmentException {
		if ( sim.getSimilarity() ) val = sim.getPropertySimilarity( ent1, ent2 );
		else val = 1 - sim.getPropertySimilarity( ent1, ent2 );
		if ( val > threshold ){
			cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
		}
	}

	/**
	 * New Method extractqqgreedyIfElseMethodC()
	 */
	private void extractqqgreedyIfElseMethodC(SortedSet<Cell> cellSet, Properties params, double val, double threshold) throws OntowrapException, AlignmentException {
		if (  params.getProperty("noinst") == null ){
			for( Object ent1: ontology1().getIndividuals() ) {
				this.extractqqgreedyInnerIfElseMethodC(cellSet, ent1, val, threshold);
			}
		}
	}

	/**
	 * New Method extractqqgreedyInnerIfElseMethodC()
	 */
	private void extractqqgreedyInnerIfElseMethodC(SortedSet<Cell> cellSet, Object ent1, double val, double threshold) throws OntowrapException, AlignmentException {
		if ( ontology1().getEntityURI( ent1 ) != null ) {
			for( Object ent2: ontology2().getIndividuals() ) {
				this.extractqqgreedyInnerIfElseMethodCInner(cellSet, ent1, ent2, val, threshold);
			}
		}
	}

	/**
	 * New Method extractqqgreedyInnerIfElseMethodCInner()
	 */
	private void extractqqgreedyInnerIfElseMethodCInner(SortedSet<Cell> cellSet, Object ent1, Object ent2, double val, double threshold) throws OntowrapException, AlignmentException {
		if ( ontology2().getEntityURI( ent2 ) != null ) {
			if ( sim.getSimilarity() ) val = sim.getIndividualSimilarity( ent1, ent2 );
			else val = 1 - sim.getIndividualSimilarity( ent1, ent2 );
			if ( val > threshold ){
				cellSet.add( new ObjectCell( (String)null, ent1, ent2, BasicRelation.createRelation("="), val ) );
			}
		}
	}


	/**
	 * New Method extractqqgreedyForMethodD()
	 */
	private void extractqqgreedyForMethodD(SortedSet<Cell> cellSet){
		try {
			for (Cell cell : cellSet) {
				Object ent1 = cell.getObject1();
				Object ent2 = cell.getObject2();
				if ((getAlignCells1(ent1) == null) && (getAlignCells2(ent2) == null)) {
					// The cell is directly added!
					addCell(cell);
				}
			}
		} catch (AlignmentException alex) {
			alex.printStackTrace();
		}
	}
}


