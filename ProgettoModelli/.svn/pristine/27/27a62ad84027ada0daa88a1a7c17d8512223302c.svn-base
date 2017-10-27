/*
 * $Id: RemoveProperties.java 1684 2012-02-20 22:12:39Z euzenat $
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

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.ontology.Restriction;

import java.sql.Statement;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;


public class RemoveProperties extends BasicAlterator {

    public RemoveProperties( Alterator om ) {
	initModel( om );
    };

    //@SuppressWarnings("unchecked")
    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.REMOVE_PROPERTIES );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
        List <OntProperty> properties = getOntologyProperties();           //the list of all properties from the model
        ArrayList <OntProperty> propertiesToBeRemoved = new ArrayList<OntProperty>();
        ArrayList<Restriction> restrictions = new ArrayList<Restriction>();
        ArrayList<OntClass> resources = new ArrayList<OntClass>();
        List<String> pr = new ArrayList<String>();
        boolean isObj = false;
        boolean isSubj = false;
        boolean isPred = false;
        int nbProperties = properties.size();				//the number of properties
	//System.err.println( percentage );
        int toBeRemoved = Math.round( percentage*nbProperties );			//the number of properties to be removed
        //build the list of classes to be removed
        int[] n = randNumbers( nbProperties, toBeRemoved );
        this.modifyInnerA(toBeRemoved, n, propertiesToBeRemoved, pr, restrictions, resources);
        this.modifyInnerB(resources, pr);
        this.modifyInnerC(isSubj, isObj, isPred, propertiesToBeRemoved);
	return this; // useless
    }

    /**
     *
     * @param toBeRemoved
     * @param n
     * @param propertiesToBeRemoved
     * @param pr
     * @param restrictions
     * @param resources
     */
    private void modifyInnerA(int toBeRemoved, int[] n, ArrayList <OntProperty> propertiesToBeRemoved,  List<String> pr, ArrayList<Restriction> restrictions, ArrayList<OntClass> resources){
        for ( int i=0; i<toBeRemoved; i++ ) {					//build the list of properties to be removed
            OntProperty property = properties.get( n[i] );
            propertiesToBeRemoved.add( property );
            pr.add( getLocalName( property.getURI() ) );
            //alignment.remove( p.getURI() );
            //get the restrictions of that property
            this.modifyInnerAInnerA(property, restrictions);
            //the domain of the property is a unionOf class
            this.modifyInnerAInnerB(property, resources);
            //the range of the property is a unionOf class
            this.modifyInnerAInnerC(property, resources);
        }
    }

    /**
     *
     * @param property
     * @param restrictions
     */
    private void modifyInnerAInnerA(OntProperty property, ArrayList<Restriction> restrictions){
        for ( Iterator it = property.listReferringRestrictions(); it.hasNext();  ) {
            restrictions.add( (Restriction)it.next() );
        }
        //delete all the restrictions
        for ( Restriction r : restrictions ) r.remove();
    }

    /**
     *
     * @param property
     * @param resources
     */
    private void modifyInnerAInnerB(OntProperty property, ArrayList<OntClass> resources){
        if ( property.hasDomain(null) ) {
            if ( property.getDomain().canAs( OntResource.class  ) ) {
                OntResource res = property.getDomain();
                if ( res.canAs( UnionClass.class ) ) {
                    OntClass cls = res.asClass();
                    resources.add(cls);
                }
            }
        }
    }

    /**
     *
     * @param property
     * @param resources
     */
    private void modifyInnerAInnerC(OntProperty property, ArrayList<OntClass> resources){
        if ( property.hasRange(null) ) {
            if ( property.getRange().canAs( OntResource.class ) ) {
                OntResource res = property.getRange();
                if ( res.canAs( UnionClass.class ) ) {
                    OntClass cls = res.asClass();
                    resources.add(cls);
                }
            }
        }
    }

    /**
     *
     * @param resources
     * @param pr
     */
    private void modifyInnerB(ArrayList<OntClass> resources, List<String> pr){
        for ( OntClass c : resources ) c.remove(); // Remove the class descriptions
        //remove that property from alignment
        for ( String key : alignment.stringPropertyNames() ) {
            if ( pr.contains( alignment.getProperty( key ) ) ) {
                alignment.remove( key );
            }
        }
    }

    /**
     *
     * @param isSubj
     * @param isObj
     * @param isPred
     * @param propertiesToBeRemoved
     */
    private void modifyInnerC(boolean isSubj, boolean isObj, boolean isPred, ArrayList <OntProperty> propertiesToBeRemoved){
        for ( Statement st : modifiedModel.listStatements().toList() ) {					//remove the declaration of properties from the model
            Resource subject   = st.getSubject();
            Property predicate = st.getPredicate();
            RDFNode object     = st.getObject();
            isSubj = isPred = isObj = false;
            this.modifyInnerCInnerA(propertiesToBeRemoved, subject, isSubj, isPred, predicate );
            this.modifyInnerCInnerB(object, propertiesToBeRemoved, isObj, isSubj, isPred, st);
        }
    }

    /**
     *
     * @param propertiesToBeRemoved
     * @param subject
     * @param isSubj
     * @param isPred
     * @param predicate
     */
    private void modifyInnerCInnerA(ArrayList <OntProperty> propertiesToBeRemoved, Resource subject, boolean isSubj, boolean isPred, Property predicate){
        if ( propertiesToBeRemoved.contains( subject ) )			//if appears as subject
            if ( getNameSpace( subject.getURI() ).equals( modifiedOntologyNS ) )
                isSubj = true;

        if ( propertiesToBeRemoved.contains( predicate ) )			//if appears as predicate
            if ( getNameSpace( predicate.getURI() ).equals( modifiedOntologyNS ) )
                isPred = true;
    }

    /**
     *
     * @param object
     * @param propertiesToBeRemoved
     * @param isObj
     * @param isSubj
     * @param isPred
     * @param st
     */
    private void modifyInnerCInnerB(RDFNode object, ArrayList <OntProperty> propertiesToBeRemoved, boolean isObj, boolean isSubj, boolean isPred, Statement st){
        if ( object.canAs( Resource.class ) )				//if appears as object
            if ( propertiesToBeRemoved.contains( object ) )
                if ( getNameSpace( object.asResource().getURI() ).equals( modifiedOntologyNS ) )
                    isObj = true;

        if ( isSubj || isPred || isObj )					//remove the statement in which the prop
            modifiedModel.remove( st );				//appears as subject, predicate or object
    }
}
