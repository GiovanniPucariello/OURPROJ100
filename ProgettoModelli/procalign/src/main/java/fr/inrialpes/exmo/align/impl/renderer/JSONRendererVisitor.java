/*
 * $Id: JSONRendererVisitor.java 1771 2012-08-20 13:11:00Z euzenat $
 *
 * Copyright (C) INRIA, 2012
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

package fr.inrialpes.exmo.align.impl.renderer;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.*;
import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;
import fr.inrialpes.exmo.ontowrap.Ontology;
import org.semanticweb.owl.align.*;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

/**
 * Renders an alignment in JSON (and practically in JSON-LD)
 * http://json-ld.org/spec/latest/json-ld-syntax/
 *
 * application/json <========= media type available
 *
 * @author Jérôme Euzenat
 * @version $Id: JSONRendererVisitor.java 1771 2012-08-20 13:11:00Z euzenat $
 */

public class JSONRendererVisitor extends IndentedRendererVisitor implements AlignmentVisitor,EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;

    // We do not want a default namespace here
    private static Namespace DEF = Namespace.NONE;
    
    private boolean isPattern = false;
	
    public JSONRendererVisitor( PrintWriter writer ){
	super( writer );
    }

    public void init( Properties p ) {
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
    }

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	String extensionString = "";
	alignment = align;
	nslist = new Hashtable<String,String>();
        nslist.put( Namespace.ALIGNMENT.prefix , Namespace.ALIGNMENT.shortCut );
        nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
        nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
	// how does it get there for RDF?
        nslist.put( Namespace.EDOAL.prefix , Namespace.EDOAL.shortCut );
	// Get the keys of the parameter
	int gen = 0;

	this.visitInnerA(align, gen, extensionString);


	this.visitInnerB(align, extensionString);

	writer.print("{ \"@type\" : \""+SyntaxElement.ALIGNMENT.print(DEF)+"\","+NL );
	increaseIndent();
	indentedOutputln("\"@context\" : {");
	increaseIndent();

	this.visitInnerC();


	// Not sure that this is fully correct
	indentedOutputln("\""+SyntaxElement.MEASURE.print(DEF)+"\" : { \"@type\" : \"xsd:float\" }");
	decreaseIndent();
	indentedOutputln("},");
	String idext = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	if ( idext != null ) {
	    //indentedOutputln("\"rdf:about\" : \""+idext+"\",");
	    indentedOutputln("\"@id\" : \""+idext+"\",");
	}
	if ( alignment.getLevel().startsWith("2EDOALPattern") ) isPattern = true;
	indentedOutputln( "\""+SyntaxElement.LEVEL.print(DEF)+"\" : \""+align.getLevel()+"\",");
	indentedOutputln( "\""+SyntaxElement.TYPE.print(DEF)+"\" : \""+align.getType()+"\",");
	writer.print(extensionString);
	indentedOutputln( "\""+SyntaxElement.MAPPING_SOURCE.print(DEF)+"\" : " );
	increaseIndent();

	this.visitInnerD(align);

	decreaseIndent();
	writer.print( ","+NL );
	indentedOutputln( "\""+SyntaxElement.MAPPING_TARGET.print(DEF)+"\" : " );
	increaseIndent();

	this.visitInnerE(align);

	writer.print( ","+NL );
	decreaseIndent();
	indentedOutputln( "\""+SyntaxElement.MAP.print(DEF)+"\" : [" );
	increaseIndent();
	boolean first = true;

	this.visitInnerF(align, first);

	writer.print(NL);
	decreaseIndent();
	indentedOutputln( "]" );
	decreaseIndent();
	indentedOutputln("}");
    }

	/**
	 *
	 * @param align
	 * @param gen
	 * @param extensionString
	 */
	private void visitInnerA(Alignment align, int gen, String extensionString ){
		for ( String[] ext : align.getExtensions() ) {
			String prefix = ext[0];
			String name = ext[1];
			String tag = nslist.get(prefix);
			if ( tag == null ) {
				tag = "ns"+gen++;
				nslist.put( prefix, tag );
			}
			tag += ":"+name;
			extensionString += INDENT+"\""+tag+"\" : \""+ext[2]+"\","+NL;
		}
	}

	/**
	 *
	 * @param align
	 * @param extensionString
	 */
	private void visitInnerB(Alignment align, String extensionString){
		if ( align instanceof BasicAlignment ) {
			for ( String label : ((BasicAlignment)align).getXNamespaces().stringPropertyNames() ) {
				if ( !label.equals("rdf") && !label.equals("xsd")
						&& !label.equals("<default>") )
					extensionString += INDENT+"\""+label+"\" : \""+((BasicAlignment)align).getXNamespace( label )+"\","+NL;
			}
		}
	}

	/**
	 *
	 */
	private void visitInnerC(){
		for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
			String k = (String)e.nextElement();
			indentedOutputln("\""+nslist.get(k)+"\" : \""+k+"\",");
		}
	}

	/**
	 *
	 * @param align
	 * @throws AlignmentException
	 */
	private void visitInnerD(Alignment align) throws AlignmentException {
		if ( align instanceof BasicAlignment ) {
			printOntology( ((BasicAlignment)align).getOntologyObject1() );
		} else {
			printBasicOntology( align.getOntology1URI(), align.getFile1() );
		}
	}

	/**
	 *
	 * @param align
	 * @throws AlignmentException
	 */
	private void visitInnerE(Alignment align) throws AlignmentException {
		if ( align instanceof BasicAlignment ) {
			printOntology( ((BasicAlignment)align).getOntologyObject2() );
		} else {
			printBasicOntology( align.getOntology2URI(), align.getFile2() );
		}
	}

	/**
	 *
	 * @param align
	 * @param first
	 * @throws AlignmentException
	 */
	private void visitInnerF(Alignment align, boolean first) throws AlignmentException {
		for( Cell c : align ){
			if ( first ) { first = false; } else { writer.print(","+NL); }
			c.accept( this );
		}
	}

    private void printBasicOntology ( URI u, URI f ) {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.ONTOLOGY.print(DEF)+"\","+NL);
	increaseIndent();
	//indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	indentedOutput("\"@id\" : \""+u+"\","+NL);
	if ( f != null ) {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+f+"\","+NL);
	} else {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+u+"\","+NL);
	}
	decreaseIndent();
	indentedOutput("}");
    }

    public void printOntology( Ontology onto ) {
	URI u = onto.getURI();
	URI f = onto.getFile();
	indentedOutputln("{ \"@type\" : \""+SyntaxElement.ONTOLOGY.print(DEF)+"\",");
	increaseIndent();
	//indentedOutput("\"rdf:about\" : \""+u+"\","+NL);
	indentedOutput("\"@id\" : \""+u+"\","+NL);
	if ( f != null ) {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+f+"\"");
	} else {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+u+"\"");
	}
	if ( onto.getFormalism() != null ) {
	    writer.print(","+NL);
	    indentedOutputln("\""+SyntaxElement.FORMATT.print(DEF)+"\" : ");
	    increaseIndent();
	    indentedOutputln("{ \"@type\" : \""+SyntaxElement.FORMALISM.print(DEF)+"\"," );
	    increaseIndent();
	    indentedOutputln("\""+SyntaxElement.NAME.print(DEF)+"\" : \""+onto.getFormalism()+"\",");
	    indentedOutputln("\""+SyntaxElement.URI.print()+"\" : \""+onto.getFormURI()+"\"");
	    decreaseIndent();
	    indentedOutputln("}");
	    decreaseIndent();
	}
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	URI u1 = cell.getObject1AsURI(alignment);
	URI u2 = cell.getObject2AsURI(alignment);
	if ( ( u1 != null && u2 != null)
	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
	    indentedOutputln("{ \"@type\" : \""+SyntaxElement.CELL.print(DEF)+"\",");
	    increaseIndent();
	    this.visitMethodA();
	    this.visitMethodB(u1,u2);
	    indentedOutput("\""+SyntaxElement.RULE_RELATION.print(DEF)+"\" : \"");
	    cell.getRelation().accept( this );
	    writer.print("\","+NL);
	    indentedOutput("\""+SyntaxElement.MEASURE.print(DEF)+"\" : \""+cell.getStrength()+"\"");
	    this.visitMethodC();
	    decreaseIndent();
	    writer.print(NL);
	    indentedOutput("}");
	}
    }

	/**
	 *
	 */
	private void visitMethodA(){
		if ( cell.getId() != null && !cell.getId().equals("") ){
			//indentedOutputln("\"rdf:about\" : \""+cell.getId()+"\",");
			indentedOutputln("\"@id\" : \""+cell.getId()+"\",");
		}
	}

	/**
	 *
	 * @param u1
	 * @param u2
	 * @throws AlignmentException
	 */
	private void visitMethodB(URI u1, URI u2) throws AlignmentException {
		if ( alignment.getLevel().startsWith("2EDOAL") ) {
			indentedOutputln("\""+SyntaxElement.ENTITY1.print(DEF)+"\" : ");
			increaseIndent();
			((Expression)(cell.getObject1())).accept( this );
			decreaseIndent();
			writer.print(","+NL);
			indentedOutputln("\""+SyntaxElement.ENTITY2.print(DEF)+"\" : ");
			increaseIndent();
			((Expression)(cell.getObject2())).accept( this );
			decreaseIndent();
			writer.print(","+NL);
			this.visitMethodBInner();
		} else {
			indentedOutputln("\""+SyntaxElement.ENTITY1.print(DEF)+"\" : \""+u1.toString()+"\",");
			indentedOutputln("\""+SyntaxElement.ENTITY2.print(DEF)+"\" : \""+u2.toString()+"\",");
		}
	}

	/**
	 *
	 * @throws AlignmentException
	 */
	private void visitMethodBInner() throws AlignmentException {
		if ( cell instanceof EDOALCell ) { // Here put the transf
			Set<Transformation> transfs = ((EDOALCell)cell).transformations();
			if ( transfs != null ) {
				for ( Transformation transf : transfs ){
					indentedOutputln("\""+SyntaxElement.TRANSFORMATION.print(DEF)+"\" : ");
					increaseIndent();
					transf.accept( this );
					decreaseIndent();
					writer.print(","+NL);
				}
			}
		}
	}

	/**
	 *
	 */
	private void visitMethodC(){
		if ( cell.getSemantics() != null &&
				!cell.getSemantics().equals("") &&
				!cell.getSemantics().equals("first-order") ) {
			writer.print(","+NL);
			indentedOutput("\""+SyntaxElement.SEMANTICS.print(DEF)+"\" : \""+cell.getSemantics()+"\"");
		}
		if ( cell.getExtensions() != null ) {
			for ( String[] ext : cell.getExtensions() ){
				writer.print(","+NL);
				indentedOutputln(ext[1]+" : \""+ext[2]+"\"");
			}
		}
	}



    // DONE: could also be a qualified class name
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	rel.write( writer );
    };

    // ********** EDOAL

    public void renderVariables( Expression expr ) {
	if ( expr.getVariable() != null ) {
	    writer.print(","+NL);
	    indentedOutputln("\""+SyntaxElement.VAR.print(DEF)+"\" : \""+expr.getVariable().name()+"\"");
	}
    }

    public void visit( final ClassId e ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.CLASS_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    writer.print(","+NL);
	    indentedOutput("\"@id\" : \""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	decreaseIndent();
	writer.print(NL);
	indentedOutput("}");
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ \"@type\" : \""+SyntaxElement.CLASS_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	writer.print(","+NL);
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	boolean first = true;
	for ( final ClassExpression ce : e.getComponents() ) {
	    if ( first ) { first = false; } else { writer.print(","+NL); }
	    ce.accept( this );
	}
	writer.print(NL);
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.VALUE_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : ");
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.TYPE_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : ");
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(","+NL);
	c.getType().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.DOMAIN_RESTRICTION.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : ");
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	if ( c.isUniversal() ) {
	    indentedOutput("\""+SyntaxElement.ALL.print(DEF)+"\" : "+NL);
	} else {
	    indentedOutput("\""+SyntaxElement.EXISTS.print(DEF)+"\" : "+NL);
	}
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.OCCURENCE_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : ");
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	indentedOutputln("{ \"@type\" : \""+SyntaxElement.LITERAL.print(DEF)+"\",");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.ETYPE.print(DEF)+"\" : \"xsd:int\",");
	indentedOutputln("\""+SyntaxElement.STRING.print(DEF)+"\" : \""+c.getOccurence()+"\"");
	decreaseIndent();
	indentedOutputln("}");
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit(final PropertyId e) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.PROPERTY_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    writer.print(","+NL);
	    indentedOutput("\"@id\" : \""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyConstruction e) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ \"@type\" : \""+SyntaxElement.PROPERTY_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	writer.print(","+NL);
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	boolean first = true;
	for ( final PathExpression pe : e.getComponents() ) {
	    if ( first ) { first = false; } else { writer.print(","+NL); }
	    writer.print(linePrefix);
	    pe.accept( this );
	}
	writer.print(NL);
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.PROPERTY_VALUE_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.PROPERTY_DOMAIN_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.PROPERTY_TYPE_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	c.getType().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit( final RelationId e ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.RELATION_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( e.getURI() != null ){
	    writer.print(","+NL);
	    //indentedOutput("\rdf:about\" : \""+u+"\"");
	    indentedOutput("\"@id\" : \""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ \"@type\" : \""+SyntaxElement.RELATION_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	writer.print(","+NL);
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	boolean first = true;
	for ( final PathExpression re : e.getComponents() ) {
	    if ( first ) { first = false; } else { writer.print(","+NL); }
	    re.accept( this );
	}
	writer.print(NL);
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.RELATION_CODOMAIN_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getCoDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.RELATION_DOMAIN_COND.print(DEF)+"\"");
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final InstanceId e ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.INSTANCE_EXPR.print(DEF)+"\"");
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\"");
	    writer.print(","+NL);
	    indentedOutput("\"@id\" : \""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit( final Value e ) throws AlignmentException {
	indentedOutput("{ \"@type\" : \""+SyntaxElement.LITERAL.print(DEF)+"\"");
	increaseIndent();
	if ( e.getType() != null ) {
	    writer.print(","+NL);
	    indentedOutput("\""+SyntaxElement.ETYPE.print(DEF)+"\" : \""+e.getType()+"\"");
	}
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.STRING.print(DEF)+"\" : \""+e.getValue()+"\"");
	decreaseIndent();
	indentedOutput("}");
    }
	
    public void visit( final Apply e ) throws AlignmentException {
	indentedOutputln("{ \"@type\" : \""+SyntaxElement.APPLY.print(DEF)+"\",");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.OPERATOR.print(DEF)+"\" : \""+e.getOperation()+"\",");
	indentedOutputln("\""+SyntaxElement.ARGUMENTS.print(DEF)+"\" : [");
	increaseIndent();
	boolean first = true;
	for ( final ValueExpression ve : e.getArguments() ) {
	    if ( first ) { first = false; } else { writer.print(","+NL); }
	    ve.accept( this );
	}
	writer.print(NL);
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final Transformation transf ) throws AlignmentException {
	indentedOutputln("{ \"@type\" : \""+SyntaxElement.TRANSF.print(DEF)+"\",");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.TRDIR.print(DEF)+"\" : \""+transf.getType()+"\",");
	indentedOutputln("\""+SyntaxElement.TRENT1.print(DEF)+"\" : ");
	increaseIndent();
	transf.getObject1().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.TRENT2.print(DEF)+"\" : ");
	increaseIndent();
	transf.getObject2().accept( this );
	decreaseIndent();
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final Datatype e ) throws AlignmentException {
	indentedOutputln("\""+SyntaxElement.EDATATYPE.print(DEF)+"\" : ");
	increaseIndent();
	indentedOutputln("{ \"@type\" : \""+SyntaxElement.DATATYPE.print(DEF)+"\",");
	increaseIndent();
	indentedOutput("\"@id\" : \""+e.getType()+"\" }");
	decreaseIndent();
	decreaseIndent();
    }
	
}
