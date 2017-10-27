/*
 * $Id: RenameThings.java 1676 2012-02-15 12:16:50Z euzenat $
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntClass;

//Google API classes
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

/*
//WordNet API classes
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
*/

// JE: reengineer this class... (what have we to rename)

//activeRandomString is true -> we replace the label with a random string
//activeTranslateString is true -> we translate the label

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is only an abstract class gathering the renaming routines
 */
public abstract class RenameThings extends BasicAlterator {

    // -------------------------
    // Label replacement

    //replaces the label of the property
    public void replacePropertyLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntProperty prop = modifiedModel.getOntProperty( uri );
        if ( prop.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                prop.setLabel( newLabel, "fr" );
            } else {
                prop.setLabel( newLabel, "en" );
            }
        }
    }

    public void forBuildList(List<OntProperty> properties,List<OntProperty> notRenamedProperties){
        for ( OntProperty p : properties ) {
            String local = getLocalName( p.getURI() );
            if ( alignment.containsKey( local ) ) {
                if ( alignment.getProperty( local ).equals( local ) )
                    notRenamedProperties.add( p );      //add the property to not renamed properties
            }
        }
    }

    public void forBuildListProp(int toBeRenamed,List<OntProperty> notRenamedProperties, List<OntProperty> propertiesTo ,List<String> propertiesName,int[] n){
        for ( int i=0; i<toBeRenamed; i++ ) {
            OntProperty p = notRenamedProperties.get(n[i]);
            propertiesTo.add(p);
            if ( getNameSpace( p.getURI() ).equals( modifiedOntologyNS ) )
                propertiesName.add( getLocalName( p.getURI() ) );
        }
    }

    public void getPropertiesIdA(List<String> propertiesName,boolean activeRandomString, HashMap<String, String> propertiesIdentifiers,int activeStringOperation,String localName,boolean activeTranslateString,OntProperty prop, boolean activeSynonym ){
        if ( activeTranslateString ) {                          //replace the URI with the translated one
            String translateStrg = parseString ( localName, true, false);
            propertiesIdentifiers.put( localName , translateStrg );
            replacePropertyLabel( prop.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );

            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                alignment.put( localName, translateStrg );//the reference alignment
            }
        }else{
            this.getPropertiesIdB( propertiesName,activeRandomString,  propertiesIdentifiers, activeStringOperation, localName, activeTranslateString, prop,  activeSynonym);
        }
    }

    public void getPropertiesIdB(List<String> propertiesName,boolean activeRandomString, HashMap<String, String> propertiesIdentifiers,int activeStringOperation,String localName,boolean activeTranslateString,OntProperty prop, boolean activeSynonym) {
        if (activeRandomString) {                        //replace the URI with a random string
            String newStrg = getRandomString();
            propertiesIdentifiers.put(localName, newStrg);
            replacePropertyLabel(prop.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
            if (alignment.containsKey(localName)) {        //alignment.remove( prop.getURI() );
                alignment.put(localName, newStrg);//the reference alignment
            }
        }else{
            this.getPropertiesIdC(activeRandomString,  propertiesIdentifiers, activeStringOperation, localName, activeTranslateString, prop,  activeSynonym,propertiesName);
        }
    }

    public void getPropertiesIdC(boolean activeRandomString, HashMap<String, String> propertiesIdentifiers,int activeStringOperation,String localName,boolean activeTranslateString,OntProperty prop, boolean activeSynonym,List<String> propertiesName) {
        if (activeSynonym) {
            String synonym = parseString(localName, false, true);
            if (propertiesName.contains(synonym))
                propertiesIdentifiers.put(localName, localName);
            else {
                propertiesIdentifiers.put(localName, synonym);
                replacePropertyLabel(prop.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
                if (alignment.containsKey(localName)) {    //alignment.remove( prop.getURI() );
                    alignment.put(localName, synonym);    //the reference alignment
                }
            }
        }else{
            this.getPropertiesIdD(activeRandomString,  propertiesIdentifiers, activeStringOperation, localName, activeTranslateString, prop,  activeSynonym,propertiesName);
        }
    }

    public void getPropertiesIdD(boolean activeRandomString, HashMap<String, String> propertiesIdentifiers,int activeStringOperation,String localName,boolean activeTranslateString,OntProperty prop, boolean activeSynonym,List<String> propertiesName){
        if ( activeStringOperation == 1 ) {                //replace the URI with the UpperCase URI
            propertiesIdentifiers.put( localName , localName.toUpperCase() );
            replacePropertyLabel( prop.getURI(), localName.toUpperCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                alignment.put( localName, localName.toUpperCase() ); //the reference alignment
            }
        }else{
            this.getPropertiesIdE(activeRandomString,  propertiesIdentifiers, activeStringOperation, localName, activeTranslateString, prop,  activeSynonym,propertiesName);
        }
    }

    public void getPropertiesIdE(boolean activeRandomString, HashMap<String, String> propertiesIdentifiers,int activeStringOperation,String localName,boolean activeTranslateString,OntProperty prop, boolean activeSynonym,List<String> propertiesName){
        if ( activeStringOperation == 2 ) {
            propertiesIdentifiers.put( localName , localName.toLowerCase() );
            replacePropertyLabel( prop.getURI(), localName.toLowerCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {        // alignment.remove( prop.getURI() );
                alignment.put( localName, localName.toLowerCase() ); //the reference alignment
            }
        }else {
            propertiesIdentifiers.put( localName,  localName + "PROPERTY" );
            replacePropertyLabel( prop.getURI(), localName + "PROPERTY", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                alignment.put( localName, localName + "PROPERTY" );
            }
        }
    }

    // get the URIs of the properties and their translation
    public HashMap<String, String> getPropertiesIdentifiers( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        HashMap<String, String> propertiesIdentifiers = new HashMap<String, String>();	//the HashMap of the properties identifiers
	List<String> propertiesName = new ArrayList<String>();                  //the properties identifiers

        List<OntProperty> propertiesTo = new ArrayList<OntProperty>();          //the list of properties to be renamed
	List<OntProperty> notRenamedProperties = new ArrayList<OntProperty>();  //the list of not renamed properties
        List<OntProperty> properties = getOntologyProperties();                 //the list of all the properties

        int nbProperties, toBeRenamed, renamedProperties;

        // build the list of all unrenamed properties from the model

        this.forBuildList(properties,notRenamedProperties);

        nbProperties = properties.size();                                       //the number of renamed properties
        renamedProperties = nbProperties - notRenamedProperties.size();
        toBeRenamed = Math.round(percentage*nbProperties) - renamedProperties;       // -renamedProperties -> for Benchmark

	// toBeRenamed is negative when properties have been added to the model
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //builds the list of properties to be renamed
	int [] n = randNumbers(notRenamedProperties.size(), toBeRenamed);

	this.forBuildListProp(toBeRenamed,notRenamedProperties,propertiesTo,propertiesName,n);

	for ( OntProperty prop : propertiesTo ) {
	    String prefix = getNameSpace( prop.getURI() );
		String localName = getLocalName( prop.getURI() );
		//has the same Namespace as the Ontology Namespace
                if ( prefix.equals( modifiedOntologyNS ) ) {
                    if ( !propertiesIdentifiers.containsKey( localName ) ) {
                        this.getPropertiesIdA(propertiesName,activeRandomString,propertiesIdentifiers,activeStringOperation,localName,activeTranslateString,prop,activeSynonym);
                    }
                }
        }
        return propertiesIdentifiers;
    }

    //replaces the label of the class
    public void replaceClassLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntClass c = modifiedModel.getOntClass( uri );

        if ( c.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                c.setLabel( newLabel, "fr" );
            } else
                c.setLabel( newLabel, "en" );
        }
    }


    public void forOntoClass(List<OntClass> classes,List<OntClass> notRenamedClasses){
        for ( OntClass c : classes ) {
            String local = getLocalName( c.getURI() );
            if ( alignment.containsKey( local ) ) {
                if ( alignment.getProperty( local ).equals( local ) )
                    notRenamedClasses.add( c ); //add the class to not renamed classes
            }
        }
    }

    public void forBuildListClasses(List<OntClass> classesTo,int toBeRenamed,int n[], List<OntClass> notRenamedClasses){
        for ( int i=0; i<toBeRenamed; i++ ) {
            OntClass cls = notRenamedClasses.get(n[i]);
            classesTo.add(cls);
        }

    }

    public void getClassIdA(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){
        if ( activeTranslateString ) {			//replace the URI with the translated one
            String translateStrg = parseString (localName, true, false);
            classesIdentifiers.put( localName , translateStrg );
            replaceClassLabel( cls.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                alignment.put( localName, translateStrg);	//the reference alignment
            }
        }else{
            this.getClassIdB(cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);
        }
    }

    public void getClassIdB(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){
        if ( activeRandomString )	{		//replace the URI with a random string
            String newStrg = getRandomString();
            classesIdentifiers.put( localName , newStrg );
            replaceClassLabel( cls.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                alignment.put( localName, newStrg );	//the reference alignment
            }
        }else{
            this.getClassIdC(cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);
        }
    }

    public void getClassIdC(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){
        if ( activeSynonym ) {                         //replace the URI with a synonym
            String synonym = parseString (localName, false, true);
            classesIdentifiers.put( localName, synonym );
            replaceClassLabel( cls.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                alignment.put( localName, synonym );//the reference alignment
            }
        }else {
            this.getClassIdD(cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);
        }
    }

    public void getClassIdD(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){
        if ( activeSynonym ) {                         //replace the URI with a synonym
            String synonym = parseString (localName, false, true);
            classesIdentifiers.put( localName, synonym );
            replaceClassLabel( cls.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                alignment.put( localName, synonym );//the reference alignment
            }
        }else{
            this.getClassIdE(cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);
        }
    }

    public void getClassIdE(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){

        if ( activeStringOperation == 2 ){             //replace the URI with the LowerCase URI
            classesIdentifiers.put( localName , localName.toLowerCase() );
            replaceClassLabel( cls.getURI(), localName.toLowerCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
            if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                alignment.put( localName, localName.toLowerCase() );     //the reference alignment
            }
        }else{
            this.getClassIdF( cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);
        }
    }

    public void getClassIdF(OntClass cls,boolean activeTranslateString, boolean activeRandomString, boolean activeSynonym, int activeStringOperation,String localName,HashMap<String, String> classesIdentifiers){
        classesIdentifiers.put( localName, localName + "CLASS" );
        replaceClassLabel( cls.getURI(), localName + "CLASS", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
        if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
            alignment.put( localName, localName + "CLASS" );
        }
    }

    //gets the URIs of the classes and their translation
    public HashMap<String, String> getClassesIdentifiers ( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        HashMap<String, String> classesIdentifiers = new HashMap<String, String>(); //the HashMap of classes identifiers

        int nbClasses, toBeRenamed, renamedClasses;

        List<OntClass> notRenamedClasses = new ArrayList<OntClass>();           //the list of not renamed classes
        List<OntClass> classes = getOntologyClasses();                     //the list of ontology classes
        List<OntClass> classesTo = new ArrayList<OntClass>();                   //the list of classes to be renamed

	// alignment contains those classes which have already been renamed
        //builds the list of all unrenamed classes from the model
        this.forOntoClass(classes,notRenamedClasses);
        
        nbClasses = classes.size();                           
        renamedClasses = nbClasses - notRenamedClasses.size();                  //the number of renamed classes
        toBeRenamed = Math.round(percentage*nbClasses) - renamedClasses;             // -renamedClasses -> for Benchmark

	//System.err.println( "NbClasses = "+nbClasses+ " YetToRename = "+notRenamedClasses.size()+" I will rename = "+toBeRenamed );

	// toBeRenamed is negative when classes have been added to the model
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //build the list of classes to be renamed
        int[] n = randNumbers( notRenamedClasses.size(), toBeRenamed );

        this.forBuildListClasses(classesTo,toBeRenamed,n,notRenamedClasses);

        for ( OntClass cls : classesTo ) {
            if ( !cls.isRestriction() ) {
                if ( !cls.isAnon() ) {
                    String prefix = getNameSpace( cls.getURI() );
                    String localName = getLocalName( cls.getURI() );

                    //has the same Namespace as the Ontology Namespace
                    if ( prefix.equals( modifiedOntologyNS ) ) {
                        if ( !classesIdentifiers.containsKey( localName ) ) {

                            this.getClassIdA(cls, activeTranslateString,  activeRandomString,  activeSynonym,  activeStringOperation, localName, classesIdentifiers);

                        }
                    }
                }
            }
        }
        return classesIdentifiers;
    }

    //renames percentage properties and classes
    //activeProperties -> if true, then rename properties
    //activeClasses -> if true, then rename classes

    public void ifActiveProp(boolean isSubj, HashMap<String, String> propertiesIdentifiers,String subjectLocalName, Resource subj,OntModel newModel,String subjectNameSpace){
        if ( propertiesIdentifiers.containsKey( subjectLocalName ) ) {
            //if the namespace of the subject is the same as the namespace of the property identifier
            if ( subjectNameSpace.equals( modifiedOntologyNS ) ) {//that we want to remove
                isSubj = true;
                subj = newModel.createResource( subjectNameSpace + propertiesIdentifiers.get( subjectLocalName ) );
            }
        }
    }

    public void ifActiveClass( HashMap<String, String> classesIdentifiers,boolean isSubj, HashMap<String, String> propertiesIdentifiers,String subjectLocalName, Resource subj,OntModel newModel,String subjectNameSpace){

        if ( classesIdentifiers.containsKey( subjectLocalName ) ) {
            //if the namespace of the subject is the same as the namespace of the property identifier
            //that we want to remove
            if ( subjectNameSpace.equals( modifiedOntologyNS ) ) {
                isSubj = true;
                subj = newModel.createResource( subjectNameSpace + classesIdentifiers.get( subjectLocalName ) );
            }
        }
    }

    public void getPropAndClassId(HashMap<String, String> propertiesIdentifiers,HashMap<String, String> classesIdentifiers,boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation){
        if ( activeProperties )
            propertiesIdentifiers = getPropertiesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
        if ( activeClasses )
            classesIdentifiers = getClassesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );

    }

    public void ifSubStatement(OntModel newModel,HashMap<String, String> propertiesIdentifiers,HashMap<String, String> classesIdentifiers,boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation,String subjectLocalName, String subjuri,Resource subj, boolean isSubj){
        if ( subjectLocalName != null ) {
            String subjectNameSpace = getNameSpace( subjuri );
            if ( activeProperties ) {
                this.ifActiveProp(isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectNameSpace);
            }

            if ( activeClasses ) {
                this.ifActiveClass(classesIdentifiers,isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectNameSpace);
            }
        }
    }

    public void ifObjIsUri(Resource object,OntModel newModel,HashMap<String, String> propertiesIdentifiers,HashMap<String, String> classesIdentifiers,boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation,String subjectLocalName, String subjuri,Resource subj, boolean isSubj){

        String uri =  object.asResource().getURI();
        String objectLocalName = getLocalName( uri );
        String objectNameSpace = getNameSpace( uri );
        if ( activeProperties ) {

            this.ifActiveProp(isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectLocalName);
        }

        if ( activeClasses ) {

            this.ifActiveClass(classesIdentifiers,isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectLocalName);
        }
    }

    public void ifIsSubj(boolean isPred, boolean isObj, OntModel newModel, RDFNode object, Resource obj, Property pred, Property predicate, Resource subj ){

        if ( isPred ) {
            if ( isObj )
                newModel.add( subj, pred, obj );
            else
                newModel.add( subj, pred, object );
        }
        else {
            if ( isObj )
                newModel.add( subj, predicate, obj );
            else
                newModel.add( subj, predicate, object );
        }
    }

    public OntModel renameResource( boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        List<Statement> statements = null;                                      //the list of all statements
        HashMap<String, String> propertiesIdentifiers = null;                   //the HashMap of the properties identifiers
        HashMap<String, String> classesIdentifiers = null;                      //the HashMap of the classes identifiers

        OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );//create new Model
        //get properties and classes identifiers
        this.getPropAndClassId(propertiesIdentifiers,classesIdentifiers,activeProperties,activeClasses,percentage,activeRandomString,activeTranslateString,activeSynonym,activeStringOperation);
        //iterate and modify the identifiers
        for ( Statement stm : modifiedModel.listStatements().toList() ) {

            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object

	    boolean isPred, isSubj, isObj;
            isPred = isSubj = isObj = false;

	    String subjuri = subject.getURI();
	    String subjectLocalName = getLocalName( subjuri );
            Resource subj = null;
            //if it is the subject of the statement

            this.ifSubStatement(newModel,propertiesIdentifiers,classesIdentifiers,activeProperties,
                    activeClasses,percentage,activeRandomString,activeTranslateString,activeSynonym,
                    activeStringOperation,subjectLocalName,subjuri,subj,isSubj);

            //if it is the predicate of the statement
	    String preduri =  predicate.getURI();
	    String predicateLocalName = getLocalName( preduri );
	    String predicateNameSpace = getNameSpace( preduri );
            Property pred = null;
            if ( activeProperties ) {
                this.ifActiveProp(isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectLocalName);
            }

            if ( activeClasses ) {
                this.ifActiveClass(classesIdentifiers,isSubj,propertiesIdentifiers,subjectLocalName,subj,newModel,subjectLocalName);
            }
            
            Resource obj  = null;
            //if it is the object of the statement
            if ( object.canAs( Resource.class ) )
                if ( object.isURIResource() ) {
                this.ifObjIsUri((Resource) object,newModel,propertiesIdentifiers,classesIdentifiers,activeProperties,activeClasses,percentage
                ,activeRandomString,activeTranslateString,activeSynonym,activeStringOperation,subjectLocalName,subjuri,subj,isSubj);
                }

            if ( isSubj ) {

                this.ifIsSubj(isPred,isObj,newModel,object,obj,pred,predicate,subj);
            } else {
                throw new AssertionError();
            }
        }
        if ( activeClasses ) {
            buildClassHierarchy();
            //we update the class hierarchy according to the new modifications
            classHierarchy.updateClassHierarchy( alignment );
	    //classHierarchy.printClassHierarchy();
        }
        return newModel;
    }

    // -------------------------
    // Utility (string) functions

    //removes spaces from a string
    public String removeSpaces ( String str ) {
        //return str.replaceAll("\\s+", "");
        if ( !str.contains( " " ) )
		return str;
	else {
		String aux = "", aux1="";
		int index;

                if ( str.contains( " " ) ) {
                    index = str.indexOf( " " );
                    while ( index != -1 ) {
                        aux += str.substring( 0, index );
                        aux1 = str.substring(index+2);
                        str = str.substring(index+1, index+2).toUpperCase().concat( aux1 );
                        index = str.indexOf( " " );
                    }
                    aux += str;
                    return aux;
                }
        }
        return str;
    }

    //translates the string from English to French
    public String translateString( String source ) {
        String translatedText = "";
        GoogleAPI.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        //Translate.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        try {
            translatedText = Translate.execute(source, Language.ENGLISH, Language.FRENCH);
        } catch (Exception e) {
            System.err.println( "Exception " + e.getMessage() );
        }
        return removeSpaces ( translatedText );
    }

    public String getSynonym( String source ) {
        return source;
    }
        /*
	//synonym of the word
	public String getSynonym ( String source ) {
		String synonym = "";
		//set this variable according to your WordNet installation folder
		//see : http://lyle.smu.edu/~tspell/jaws/index.html
		System.setProperty("wordnet.database.dir", "/usr/Wordnet/WordNet-3.0/dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets( source );
		if (synsets.length > 0) {
			for (int i = 0; i < synsets.length; i++) {
				String[] wordForms = synsets[i].getWordForms();
				for (int j = 0; j < wordForms.length; j++) {
					if ( !wordForms[j].equals( source ) )	{
						synonym = removeSpaces ( wordForms[j] );
						return synonym;
					}
				}
			}
		}
		else
			return source;
		return source;
	}
        */

    public String parseString (String str, boolean activeTranslateString, boolean activeSynonym) {
        // if ( debug ) System.err.println ( "str = [" + str + "]" );
        char [] parsed = str.toCharArray();
        String newString = "";

        for ( int i=1; i<parsed.length; i++ ) {
            if( Character.isUpperCase( parsed[i] ) ) {
                String aux = str.substring(0, i);

                if ( activeTranslateString )
                    newString = newString.concat( translateString( str.substring(0, i) ) );
                if ( activeSynonym )
                    newString = newString.concat( getSynonym( str.substring(0, i) ) );

                str = str.substring(i);
            }
        }

        if ( activeTranslateString )
            newString = newString.concat( translateString(str.substring(0)) );
        if ( activeSynonym )
            newString = newString.concat( getSynonym(str.substring(0)) );
        return newString;
    }

}
