/*
 * $Id: FlattenLevel.java 1676 2012-02-15 12:16:50Z euzenat $
 *
 * Copyright (C) 2011-2012, INRIA
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

package fr.inrialpes.exmo.align.gen.alt;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.vocabulary.OWL;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class FlattenLevel extends BasicAlterator {

    public FlattenLevel( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.LEVEL_FLATTENED );
	if ( p == null ) return null;
	// Should be a float cast in int!!!
	int level = (int)Float.parseFloat( p );
        ArrayList<OntClass> levelClasses = new ArrayList<OntClass>();		//the list of classes from that level
        ArrayList<OntClass> parentLevelClasses = new ArrayList<OntClass>();	//the list of parent of the child classes from that level
        ArrayList<OntClass> superLevelClasses = new ArrayList<OntClass>();	//the list of parent of the parent classes from that level
        if ( level == 1 ) return this; //no change
        buildClassHierarchy();                                                  //check if the class hierarchy is built
	//classHierarchy.printClassHierarchy();
        classHierarchy.flattenClassHierarchy( modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);
	//classHierarchy.printClassHierarchy();
        final int size = levelClasses.size();

        /* remove duplicates from list */
        HashMap<String, ArrayList<Restriction>> restrictions = new HashMap<String, ArrayList<Restriction>>();
        List<String> parentURI = new ArrayList<String>();
        HashMap<String, String> unionOf = new HashMap<String, String>();
        this.modifyInnerA(unionOf, size, parentLevelClasses, levelClasses, superLevelClasses, restrictions, parentURI);
        int i = 0;
        this.modifyInnerB(i, levelClasses, parentURI, restrictions);
        //checks if the class appears like unionOf, someValuesFrom, allValuesFrom .. and replaces its appearence with the superclass
        modifiedModel = changeDomainRange(unionOf);

        //remove the parentClass from the alignment
	int baselength = initOntologyNS.length(); // key.indexOf+baselenght == baselenght...
       this.modifyInnerC(parentURI);
	return this; // useless
    }

    /**
     *
     * @param unionOf
     * @param size
     * @param parentLevelClasses
     * @param levelClasses
     * @param superLevelClasses
     * @param restrictions
     * @param parentURI
     */
    private void modifyInnerA(HashMap<String, String> unionOf, final int size, ArrayList<OntClass> parentLevelClasses, ArrayList<OntClass> levelClasses, ArrayList<OntClass> superLevelClasses, HashMap<String, ArrayList<Restriction>> restrictions, List<String> parentURI){
        for ( int i=0; i<size; i++ ) {
            OntClass childClass = levelClasses.get( i );			//child class
            OntClass parentClass = parentLevelClasses.get( i );                 //parent class
            //build the list of restrictions of the parent class
            ArrayList<Restriction> restr = new ArrayList<Restriction>();
            List<OntClass> supCls = parentClass.listSuperClasses().toList();
           this.modifyInnerAInnerA(supCls, restr);
            //if ( debug ) System.err.println( restr.size() );
            if ( !restrictions.containsKey( parentClass.getURI() ) ) {
                restrictions.put( parentClass.getURI(), restr );
            }
            parentURI.add( parentClass.getURI() );
            OntClass superClass = superLevelClasses.get( i );                //parent class of the child class parents
            if ( superClass == null ) superClass = modifiedModel.createClass( OWL.Thing.getURI() );	//Thing class
            //if ( debug ) System.err.println("SuperClass class [" + superClass.getURI() + "]");
            //if ( debug ) System.err.println("Parent class [" + parentClass.getURI() + "]");
            //if ( debug ) System.err.println("Child class [" + childClass.getURI() + "]");
            this.modifyInnerAInnerB(parentClass, unionOf, superClass, i, childClass, superLevelClasses);
            parentClass.removeSubClass( childClass );
        }
    }

    /**
     *
     * @param supCls
     * @param restr
     */
    private void modifyInnerAInnerA(List<OntClass> supCls, ArrayList<Restriction> restr){
        for ( OntClass cls : supCls ) {
            if ( cls.isRestriction() ) {
                Restriction r = cls.asRestriction();
                this.modifyInnerAInnerAInner(r, restr);
                this.modifyInnerAInnerAInnerB(r, restr);
            }
        }
    }

    /**
     *
     * @param r
     * @param restr
     */
    private void modifyInnerAInnerAInner( Restriction r, ArrayList<Restriction> restr){
        if ( r.isAllValuesFromRestriction() )
            restr.add(r);
        if ( r.isCardinalityRestriction() )
            restr.add(r);
        if ( r.isHasValueRestriction() )
            restr.add(r);
    }

    /**
     *
     * @param r
     * @param restr
     */
    private void modifyInnerAInnerAInnerB( Restriction r, ArrayList<Restriction> restr){
        if ( r.isMaxCardinalityRestriction() )
            restr.add(r);
        if ( r.isMinCardinalityRestriction() )
            restr.add(r);
        if ( r.isSomeValuesFromRestriction() )
            restr.add(r);
        //if ( debug ) System.err.println( cls.getURI() );
    }

    /**
     *
     * @param parentClass
     * @param unionOf
     * @param superClass
     * @param i
     * @param childClass
     * @param superLevelClasses
     */
    private void modifyInnerAInnerB(OntClass parentClass, HashMap<String, String> unionOf, OntClass superClass, int i, OntClass childClass, ArrayList<OntClass> superLevelClasses){
        if ( modifiedModel.containsResource( parentClass ) ) {
            //to check if the class appears as unionOf, someValuesFrom, allValuesFrom ..
            unionOf.put( parentClass.getURI(), superClass.getURI() );
            checkClassesRestrictions( parentClass, superClass );
            parentClass.remove();
        }
        if ( superLevelClasses.get( i ) != null ) childClass.addSuperClass( superClass );
    }

    /**
     *
     * @param i
     * @param levelClasses
     * @param parentURI
     * @param restrictions
     */
    private void modifyInnerB(int i, ArrayList<OntClass> levelClasses, List<String> parentURI, HashMap<String, ArrayList<Restriction>> restrictions){
        for ( String uri : parentURI ) {
            OntClass childClass = levelClasses.get( i );
            List<Restriction> restr = restrictions.get( uri );
            for ( Restriction r : restr ) {
                childClass.addSuperClass(r);
            }
            i++;
        }
    }

    /**
     *
     * @param parentURI
     */
    private void modifyInnerC(List<String> parentURI){
        for ( String key : alignment.stringPropertyNames() ) {
            String value = alignment.getProperty( key );
            if ( parentURI.contains( modifiedOntologyNS + key ) ) {        //this.classHierarchy.removeUri("Thing", key);
                alignment.remove( key );
            }
            // This is strange
            if ( parentURI.contains( modifiedOntologyNS + value ) ) {    //this.classHierarchy.removeUri("Thing", value);
                alignment.remove( key );
            }
        }
    }
}
