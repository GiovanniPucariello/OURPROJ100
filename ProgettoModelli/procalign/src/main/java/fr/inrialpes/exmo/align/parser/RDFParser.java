/*
 * $Id: RDFParser.java 1712 2012-03-24 14:03:15Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.7 - 2008
 * Copyright (C) INRIA, 2008-2010, 2012
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

package fr.inrialpes.exmo.align.parser;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.*;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;
import fr.inrialpes.exmo.ontowrap.BasicOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Relation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

// Yes we are relying on Jena for parsing RDF

/**
 * <p>
 * Parser for the EDOAL syntax. The reader is Jena, input is an EDOALAlignment
 * The input document format shall be consistent with format document 
 * 
 * </p>
 * <p>
 * $Id: RDFParser.java 1712 2012-03-24 14:03:15Z euzenat $
 * </p>
 * 
 * @author Richard Pöttler
 * @version $Revision: 1.7 $
 */
public class RDFParser {

    private static Logger logger = Logger.getLogger(RDFParser.class.toString());

    private static Model rDFModel;

    private int debug = 0;

    private boolean isPattern = false; // I contain variables
    private boolean speedparse = false; // skip all checks

    private EDOALAlignment alignment;

    /** 
     * Creates an RDF Parser.
     */
    public RDFParser() {
	this(0);
    }

    /** 
     * Creates an RDF Parser.
     * @param debugMode The value of the debug mode
     */
    public RDFParser( int debugMode ) {
	debug = debugMode;
    }

    /**
     * Initialisation of the structures
     * This creates an RDF Model which contains all the syntactic elements.
     * This is to be called before parsing, i.e., before exploring RDF resources
     */
    public static void initSyntax() {
	if ( rDFModel == null ) {
	    rDFModel = ModelFactory.createDefaultModel();
	    for ( SyntaxElement el : SyntaxElement.values() ) {
		if ( el.isProperty == true ) {
		    el.resource = rDFModel.createProperty( el.id() );
		} else {
		    el.resource = rDFModel.createResource( el.id() );
		}
	    }
	}
    }

    /**
     * Parse the input model. The model shall include one statement that include (?,RDF.type,Alignment)
     * @param rdfmodel the rdfmodel containing the RDF representation of the parsed alignment
     * @return the result EDOALAlignment
     * @throws AlignmentException if there is any exception, throw AlignmentException that include describe infomation
     * and a caused exception.
     */
    public EDOALAlignment parse( final Model rdfmodel ) throws AlignmentException {
	// Initialize the syntax description
	initSyntax();
	// Shut up logging handling
	com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = true;
	// Get the statement including alignment resource as rdf:type
	StmtIterator stmtIt = rdfmodel.listStatements(null, RDF.type,(Resource)SyntaxElement.getResource("Alignment"));
	// Take the first one if it exists
	if ( !stmtIt.hasNext() ) throw new AlignmentException("There is no alignment in the RDF document");
	Statement alignDoc = stmtIt.nextStatement();
	// Step from this statement
	alignment = parseAlignment( alignDoc.getSubject() );
	// If necessary type-check the alignment
	if ( !speedparse ) alignment.accept( new TypeCheckingVisitor() );
	// Clean up memory
	rdfmodel.close(); // JE: I am not sure that I will not have trouble with initSyntax
	return alignment;
    }

    // Below is the plumbing:
    // Load the RDF under an RDFModel
    // Call the above parse: RDFModel -> EDOALAlignment

    public EDOALAlignment parse( final File file ) throws AlignmentException {
    	FileInputStream fi = null;
	try {
		fi = new FileInputStream(file);
	    return parse(fi);
	} catch ( FileNotFoundException fnfe ) {
	    throw new AlignmentException("RDFParser: There isn't such file: "+ file.getName(), fnfe);
	} finally {
		if (fi != null) {
			try {
				fi.close();
			} catch (java.io.IOException e3) {
				System.out.println("I/O Exception");
			}
		}
	}
    }

    public EDOALAlignment parse( final Reader is ) throws AlignmentException {
	if (is == null) throw new AlignmentException("The reader must not be null");
	Model align = ModelFactory.createDefaultModel();
	align.read( is, null );
	return parse( align );
    }
    
    public EDOALAlignment parse( final InputStream is ) throws AlignmentException {
	if (is == null) throw new AlignmentException("The inputstream must not be null");
	Model align = ModelFactory.createDefaultModel();
	align.read( is, null );
	return parse( align );
    }

    public EDOALAlignment parse( final String uri ) throws AlignmentException {
	Model align = ModelFactory.createDefaultModel();
	align.read( uri );
	return parse( align );
    }

    // Below is the real work


    public void ifStmtHasNext(StmtIterator stmtIt) throws AlignmentException {
		final String level = stmtIt.nextStatement().getString();
		if ((level != null) && (!level.equals(""))) {
			//if ( level.equals("0") ) {
			//	alignment.setLevel( level );
			//} else
			if (level.startsWith("2EDOAL")) {
				alignment.setLevel(level);
				if (level.equals("2EDOALPattern")) isPattern = true;
			} else {
				throw new AlignmentException("Cannot parse alignment of level " + level);
			}
		}
	}

	public void whileBlock(StmtIterator stmtIt){
		Statement stmt = stmtIt.nextStatement();
		if ( debug > 0 ) System.err.println( "  ---------------> "+stmt );
		try { alignment.addAlignCell( parseCell( stmt.getResource() ) ); }
		catch ( AlignmentException ae ) {
			System.err.println( "Error "+ae );
			ae.printStackTrace();
		}
	}

	public void ifMappingSource(final Resource node, Ontology source,StmtIterator stmtIt) throws AlignmentException {

		if ( stmtIt.hasNext() ) {
			source = parseOntology( stmtIt.nextStatement().getResource() );
		} else {
			throw new AlignmentException( "Missing ontology "+"onto1" );
		}
	}

	public void ifMappingTarger(StmtIterator stmtIt, Ontology target) throws AlignmentException {
		if ( stmtIt.hasNext() ) {
			target = parseOntology( stmtIt.nextStatement().getResource() );
		} else {
			throw new AlignmentException( "Missing ontology "+"onto2" );
		}
	}

	public void ifLevelResource(StmtIterator stmtIt) throws AlignmentException {
		if ( stmtIt.hasNext() ) {
			this.ifStmtHasNext(stmtIt);
		} else {
			throw new AlignmentException( "Missing level " );
		}
	}

	public void ifTypeResource(StmtIterator stmtIt) throws AlignmentException {
		if ( stmtIt.hasNext() ) {
			final String arity = stmtIt.nextStatement().getString();
			if ((arity != null) && (!arity.equals(""))) {
				alignment.setType( arity );
			}
		} else {
			throw new AlignmentException( "Missing type " );
		}
	}

	public void ifSourceTargetNotNull(Ontology source, Ontology target) throws AlignmentException {
		if ( source != null && target != null ) {
			alignment.init( source, target );
		} else {
			throw new IllegalArgumentException("Missing ontology description");
		}
	}
	/**
	 * Parses a mapping document. The resource passed to this method must be a
	 * <code>&lt;Alignment&gt;</code> tag.
	 *
	 * @param node    the alignment resource
	 * @return the parsed mapping document
	 * @throws AlignmentException
	 */


    public EDOALAlignment parseAlignment( final Resource node ) throws AlignmentException {
	if (node == null) throw new Error("Alignment must not be null");
	try {
	    Ontology source = null;
	    Ontology target = null;

	    
	    // getting the id of the document
	    final URI id = getNodeId( node );
	    
	    alignment = new EDOALAlignment();
	    if ( id != null )
		alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, id.toString() );
		StmtIterator stmtIt = node.listProperties((Property)SyntaxElement.MAPPING_SOURCE.resource );

		this.ifMappingSource(node,source,stmtIt);

	    stmtIt = node.listProperties((Property)SyntaxElement.MAPPING_TARGET.resource );

	    this.ifMappingTarger(stmtIt,target);

	    stmtIt = node.listProperties((Property)SyntaxElement.LEVEL.resource );

	    this.ifLevelResource(stmtIt);

	    stmtIt = node.listProperties((Property)SyntaxElement.TYPE.resource );

	    this.ifTypeResource(stmtIt);
	    
	    stmtIt = node.listProperties((Property)SyntaxElement.MAP.resource );

	     while (stmtIt.hasNext()) {
		this.whileBlock(stmtIt);
	    }

	    // Remaining resources...
	    // rdf:type must be forgotten
	    //else if ( !pred.equals( SyntaxElement.getResource("rdftype") ) ) { // Unknown is annotation
	    //	parseAnnotation( stmt, alignment );
	    //}

		this.ifSourceTargetNotNull(source,target);

	    return alignment;
	    
	} catch (AlignmentException e) {
	    throw e;
	} catch (Exception e) {
	    throw new AlignmentException("There is some error in parsing alignment: " + node.getLocalName(), e);
	}
    }

    /**
     * Parse an ontology node <code>&lt;onto1&gt;</code> or
     * <code>&lt;onto2&gt;</code> Node to an Ontology object. The node must
     * contain the <code>&lt;onto...&gt;</code> element.
     * 
     * @param node
     *            the ontology node
     * @return the Ontology object
     * @throws NullPointerException
     *             if the node is null
     */
    protected Ontology parseOntology(final Resource node) throws AlignmentException {
	if (node == null) throw new AlignmentException("The Ontology node must not be null");
	try {
	    Resource formu = node.getProperty((Property)SyntaxElement.FORMATT.resource).getResource();
	    final String formalismName = formu.getProperty((Property)SyntaxElement.NAME.resource).getString();
	    final String formalismUri = formu.getProperty((Property)SyntaxElement.URI.resource).getString();
	    final Statement location = node.getProperty((Property)SyntaxElement.LOCATION.resource);
	    Ontology onto = new BasicOntology();
	    onto.setURI( new URI( node.getURI() ) );
	    onto.setFormURI( new URI( formalismUri ) );
	    onto.setFormalism( formalismName );
	    if ( location != null ) onto.setFile( new URI( location.getString() ) );
	    return onto;
	} catch ( Exception e ) {
	    throw new AlignmentException("The Ontology node is not correct: "+ node.getLocalName(), e);
	}
    }


	public void ifTypeNull(final Relation type, final String relation){
		if (type == null) {	// I raise an error in this case anyway
			throw new IllegalArgumentException("Cannot parse the string \""+relation+"\" to a valid relation");
		}
	}

	public void ifStNull(Statement st, final Resource node) throws AlignmentException {
		if ( st == null ) {
			throw new AlignmentException("Correspondence must contain a measure" + node.getLocalName());
		}
	}

	public void debugMaj0(Expression s, Expression t){
		if ( debug > 0 ) {
			System.err.println(" s : "+s);
			System.err.println(" t : "+t);
		}
	}

	public void whileBlock(StmtIterator stmtIt,EDOALCell cell){
		while ( stmtIt.hasNext() ) {
			Statement stmt = stmtIt.nextStatement();
			try { cell.addTransformation( parseTransformation( stmt.getResource() ) ); }
			catch ( AlignmentException ae ) {
				System.err.println( "Error "+ae );
				ae.printStackTrace();
			}
		}
	}

    /**
     * Parses a mapping rule. The parsed node must be a Cell resource including the mandatory Statement.
     * <code>&lt;Cell&gt;</code> tag.
     * 
     * @param node
     *            the <code>&lt;Cell&gt;</code> tag
     * @return the parsed rule
     * @exception AlignmentException
     */
    protected EDOALCell parseCell( final Resource node ) throws AlignmentException {
	if (node == null) {
	    throw new Error("The node must not be null");
	}
	try {
	    // Should be better to use Alignment API relation recognition
	    // determine the relation, the relation shall be Literal
	    Statement st = node.getProperty((Property)SyntaxElement.RULE_RELATION.resource);

	    this.ifStNull(st,node);

	    final String relation = st.getString();
		final Relation type = BasicRelation.createRelation( relation );

	    this.ifTypeNull(type,relation);

	    // parse the measure, the node shall be Literal and it's a number
	    st = node.getProperty((Property)SyntaxElement.MEASURE.resource);

	    this.ifStNull(st,node);

	    final float m = st.getFloat();
	    // get the id
	    final String id = node.getURI();
	    //parsing the entity1 and entity2 
	    st = node.getProperty((Property)SyntaxElement.ENTITY1.resource);

	    this.ifStNull(st,node);

	    Resource entity1 = st.getResource();
	    st = node.getProperty((Property)SyntaxElement.ENTITY2.resource);

	    this.ifStNull(st,node);

	    Resource entity2 = st.getResource();
	    // JE2010:
	    // Here it would be better to check if the entity has a type.
	    // If both have none, then we get a old-style correspondence for free
	    // If it has one, let's go parsing
	    // I also assume that the factory only do dispatch
	    // JE2010 ~later: that would also allow for parsing old alignments
	    
	    Expression s = parseExpression( entity1 );
	    Expression t = parseExpression( entity2 );

	    this.debugMaj0(s,t);

	    EDOALCell cell = new EDOALCell( id, s, t, type, m );
	    // Parse the possible transformations
	    StmtIterator stmtIt = node.listProperties((Property)SyntaxElement.TRANSFORMATION.resource );

	    this.whileBlock(stmtIt,cell);

	    return cell;
	} catch (Exception e) {  //wrap other type exception
	    logger.log(java.util.logging.Level.SEVERE, "The cell isn't correct: " + node.getLocalName() + " "+e.getMessage());
	    throw new AlignmentException("Cannot parse correspondence " + node.getLocalName(), e);
	}
    }

    protected Transformation parseTransformation( final Resource node ) throws AlignmentException {
	if (node == null) throw new Error("The node must not be null");
	try {
	    // parsing type
	    Statement stmt = node.getProperty( (Property)SyntaxElement.TRDIR.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required "+SyntaxElement.TRDIR.print()+" property in Transformation" );
	    String type = stmt.getLiteral().toString();
	    // parsing entity1 and entity2 
	    Resource entity1 = node.getProperty((Property)SyntaxElement.TRENT1.resource).getResource();
	    Resource entity2 = node.getProperty((Property)SyntaxElement.TRENT2.resource).getResource();
	    ValueExpression s = parseValue( entity1 );
	    ValueExpression t = parseValue( entity2 );
	    if ( debug > 0 ) {
		System.err.println(" (Transf)s : "+s);	    
		System.err.println(" (Transf)t : "+t);
	    }
	    return new Transformation( type, s, t );
	} catch (Exception e) {  //wrap other type exception
	    logger.log(java.util.logging.Level.SEVERE, "The cell isn't correct:" + node.getLocalName() + " "+e.getMessage());
	    throw new AlignmentException("Cannot parse transformation " + node, e);
	}
    }

    public void parseExpressionA(Resource rdfType,final Resource node,Expression result) throws AlignmentException {
		if ( rdfType.equals( SyntaxElement.CLASS_EXPR.resource ) ||
				rdfType.equals( SyntaxElement.OCCURENCE_COND.resource ) ||
				rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ||
				rdfType.equals( SyntaxElement.TYPE_COND.resource ) ||
				rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
			result = parseClass( node );
		}else{
			this.parseExpressionB( rdfType,  node, result);
		}
	}

	public void parseExpressionB(Resource rdfType,final Resource node,Expression result) throws AlignmentException {
		if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ||
				rdfType.equals( SyntaxElement.PROPERTY_DOMAIN_COND.resource ) ||
				rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) ||
				rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {
			result = parseProperty( node );
		}else{
			this.parseExpressionC(rdfType,  node, result);
		}
	}

	public void parseExpressionC(Resource rdfType,final Resource node,Expression result) throws AlignmentException {
		if (rdfType.equals(SyntaxElement.RELATION_EXPR.resource) ||
				rdfType.equals(SyntaxElement.RELATION_DOMAIN_COND.resource) || // no chance
				rdfType.equals(SyntaxElement.RELATION_CODOMAIN_COND.resource)) {
			result = parseRelation(node);
		}else{
			this.parseExpressionD( rdfType,  node, result);
		}
	}

	public void parseExpressionD(Resource rdfType,final Resource node,Expression result) throws AlignmentException {
		if ( rdfType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
			result = parseInstance( node );
		} else {
			throw new AlignmentException("There is no parser for entity "+rdfType.getLocalName());
		}
	}

    // Here given the type of expression, this can be grand dispatch
    protected Expression parseExpression( final Resource node ) throws AlignmentException {
	Expression result= null;
	Resource rdfType = node.getProperty( RDF.type ).getResource();


	this.parseExpressionA(rdfType,node,result);

	if ( isPattern ) {
	    StmtIterator stmtIt = node.listProperties( (Property)SyntaxElement.VAR.resource );
	    if ( stmtIt.hasNext() ) {
		final String varname = stmtIt.nextStatement().getString();
		final Variable var = alignment.recordVariable( varname, result );
		result.setVariable( var );
	    }
	}
	return result;
    }

    public void parseClassA(final Resource node,Constructor op, Statement stmt) throws AlignmentException {
		if (node.hasProperty((Property) SyntaxElement.AND.resource)) {
			op = SyntaxElement.AND.getOperator();
			// listProperties would give them all
			stmt = node.getProperty((Property) SyntaxElement.AND.resource);
		}else{
			this.parseClassB(node,op,stmt);
		}
	}

	public void parseClassB(final Resource node,Constructor op, Statement stmt) throws AlignmentException {
		if (node.hasProperty((Property) SyntaxElement.OR.resource)) {
			op = SyntaxElement.OR.getOperator();
			stmt = node.getProperty((Property) SyntaxElement.OR.resource);
		}else{
			this.parseClassC(node,op,stmt);
		}
	}

	public void parseClassC(final Resource node,Constructor op, Statement stmt) throws AlignmentException {
		if (node.hasProperty((Property) SyntaxElement.NOT.resource)) {
			op = SyntaxElement.NOT.getOperator();
			stmt = node.getProperty((Property) SyntaxElement.NOT.resource);
		}else{
			this.parseClassD(node);
		}
	}

	public ClassExpression parseClassD(final Resource node) throws AlignmentException {
		if ( isPattern ) { // not necessarily with a variable (real patterns)
			return new ClassId();
		} else {
			throw new AlignmentException( "Class statement must containt one constructor or Id : "+node );
		}
	}

	public void ifForO(Object o,Resource coll) throws AlignmentException {
		if ( o instanceof Resource){
			coll = (Resource)o;
		}else if ( o instanceof Literal && !o.toString().equals("") )
			throw new AlignmentException( "Invalid content of constructor : "+o );
	}

	public void ifForOp(final Resource node, Resource coll,List<ClassExpression> clexpr ) throws AlignmentException {
		if ( coll == null )
			throw new AlignmentException( "NOT constructor cannot be empty : "+node );
		clexpr.add( parseClass( coll ) );
	}

	public void elseOp(Resource coll, List<ClassExpression> clexpr) throws AlignmentException {
		if ( coll != null ) {
			boolean isEqual = RDF.nil.getURI().equals( coll.getURI() );
			while ( !isEqual) {
				try {
					clexpr.add( parseClass( coll.getProperty( RDF.first ).getResource() ) );
				} catch (AlignmentException e) {
					e.printStackTrace();
				}
				coll = coll.getProperty( RDF.rest ).getResource(); // MUSTCHECK
				isEqual = RDF.nil.getURI().equals( coll.getURI() );
			}
		}
	}

	public void scndIf(Resource rdfType) throws AlignmentException {
		if ( !rdfType.equals( SyntaxElement.OCCURENCE_COND.resource ) &&
				!rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) &&
				!rdfType.equals( SyntaxElement.TYPE_COND.resource ) &&
				!rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
			throw new AlignmentException( "Bad class restriction type : "+rdfType );
		}
	}

	public ClassExpression ifRdfTypeEqTypeCond(final Resource node, Statement stmt ,PathExpression pe) throws AlignmentException {
		// Check that pe is a Property / Relation
		// ==> different treatment
		stmt = node.getProperty( (Property)SyntaxElement.EDATATYPE.resource );
		if ( stmt == null ) throw new AlignmentException( "Required "+SyntaxElement.EDATATYPE.print()+" property" );
		return new ClassTypeRestriction( pe, parseDatatype( stmt.getObject() ) );
	}

	public ClassExpression isRdfTypeEqDomainRex(final Resource node, Statement stmt ,PathExpression pe) throws AlignmentException {
		if ((stmt = node.getProperty((Property) SyntaxElement.TOCLASS.resource)) != null || (stmt = node.getProperty((Property) SyntaxElement.ALL.resource)) != null) {
			RDFNode nn = stmt.getObject();
			if (!nn.isResource()) throw new AlignmentException("Incorrect class expression " + nn);
			return new ClassDomainRestriction(pe, true, parseClass((Resource) nn));
		} else {
			return this.isRdfTypeEqDomainRex2(node, stmt, pe);
		}
	}

	public ClassExpression isRdfTypeEqDomainRex2(final Resource node, Statement stmt ,PathExpression pe) throws AlignmentException {
		if ( (stmt = node.getProperty( (Property)SyntaxElement.EXISTS.resource ) ) != null ) {
			RDFNode nn = stmt.getObject();
			if ( !nn.isResource() ) throw new AlignmentException( "Incorrect class expression "+nn );
			return new ClassDomainRestriction( pe, false, parseClass( (Resource)nn ) );
		} else throw new AlignmentException( "Required edoal:class property" );
	}

	public ClassExpression isRdfOccurrenceCond(final Resource node, Statement stmt ,PathExpression pe,Comparator comp,RDFNode nn ) throws AlignmentException {
		stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
		if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
		nn = stmt.getObject();
		if ( nn.isLiteral() ) {
			return new ClassOccurenceRestriction( pe, comp, ((Literal)nn).getInt() );
		} else {
			throw new AlignmentException( "Bad occurence specification : "+nn );
		}
	}

	public ClassExpression isRdfValueCond(final Resource node, Statement stmt ,PathExpression pe,Comparator comp) throws AlignmentException {

		stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
		if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
		ValueExpression v = parseValue( stmt.getObject() );
		return new ClassValueRestriction( pe, comp, v );
	}

	public ClassExpression elseIdDiffNull(final Resource node, Statement stmt, Constructor op  ) throws AlignmentException {

		// Using a List preserves the order... useful mostly for COMPOSE
		// Given the Jena encoding of Collection, LinkedList seems the best
		List<ClassExpression> clexpr = new LinkedList<ClassExpression>();

		this.parseClassA(node,op,stmt);

		// Jena encode these collections as first/rest statements
		Object o = stmt.getObject();
		Resource coll = null; // Errors if null tackled below

		this.ifForO(o,coll);

		if (op == SyntaxElement.NOT.getOperator()) {
			this.ifForOp(node,coll,clexpr);
		} else {
			this.elseOp(coll,clexpr);
		}
		return new ClassConstruction( op, clexpr );
	}

	public void elseComparator(final Resource node, Statement stmt , PathExpression pe, Comparator comp, RDFNode nn, Resource rdfType) throws AlignmentException {

		if ( stmt == null ) throw new AlignmentException( "Required edoal:comparator property" );


		URI id = getNodeId( stmt.getResource() );

		if ( id != null ) comp = Comparator.getComparator( id );

		else throw new AlignmentException("edoal:comparator requires a URI");

		if ( rdfType.equals( SyntaxElement.OCCURENCE_COND.resource ) ) {

			this.isRdfOccurrenceCond(node,stmt,pe,comp,nn);

		} else {
			throw new AlignmentException( "Bad occurence specification : "+nn );
		}
	}

	public  ClassExpression ifClassExpr(URI id, final  Resource node, Statement stmt, Constructor op) throws AlignmentException {
        if ( id != null ) {
            return new ClassId( id );
        } else {


            return this.elseIdDiffNull(node,stmt,op);

        }
    }

    public void ifDebugMaj1(final Resource node){
        if ( debug > 1 ) {
            StmtIterator it = node.listProperties();
            while ( it.hasNext() ) System.err.println( "   > "+it.next() );
        }
    }


	protected ClassExpression parseClass( final Resource node ) throws AlignmentException {

        this.ifDebugMaj1(node);

	Resource rdfType = node.getProperty(RDF.type).getResource();

	if ( rdfType.equals( SyntaxElement.CLASS_EXPR.resource ) ) {
	    URI id = getNodeId( node );
        Statement stmt=null;
        Constructor op= null;
	    this.ifClassExpr(id,node,stmt,op);

	} else {

		this.scndIf(rdfType);

		PathExpression pe;
	    Comparator comp=null;
	    // Find onAttribute
	    Statement stmt = node.getProperty( (Property)SyntaxElement.ONPROPERTY.resource );
	    //if ( stmt == null ) throw new AlignmentException( "Required edoal:onAttribute property" );
	    pe = parsePathExpression( stmt.getResource() ); // MUSTCHECK
	    if ( rdfType.equals( SyntaxElement.TYPE_COND.resource ) ) {

	    	this.ifRdfTypeEqTypeCond(node,stmt,pe);

	    } else if ( rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ) {

	    	this.isRdfTypeEqDomainRex(node,stmt,pe);

	    } else { // It is a Value or Occurence restruction
		// Find comparator
		RDFNode nn =stmt.getObject();

		stmt = node.getProperty( (Property)SyntaxElement.COMPARATOR.resource );

		this.elseComparator(node,stmt,pe,comp,nn,rdfType);

		}if ( rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
	    	this.isRdfValueCond(node,stmt,pe,comp);
		}
	    }
		return null;
	}



    // JE2010: Here is the problem again with DOMAIN (for instance)
    protected PathExpression parsePathExpression( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_DOMAIN_COND.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {
	    return parseProperty( node );
	} else if ( rdfType.equals( SyntaxElement.RELATION_EXPR.resource ) ||
	     rdfType.equals( SyntaxElement.RELATION_CODOMAIN_COND.resource ) ||
	     rdfType.equals( SyntaxElement.RELATION_DOMAIN_COND.resource ) ) {
	    return parseRelation( node );
	} else throw new AlignmentException( "Cannot parse path expression ("+rdfType+"): "+node );
	
    }

    // rdf:parseType="Collection" is supposed to preserve the order ()
    // Jena indeed always preserves the order so this can be used

    public void propertyExpressionA(final Resource node, Statement stmt, Constructor op) throws AlignmentException {
        if (node.hasProperty((Property) SyntaxElement.AND.resource)) {
            op = SyntaxElement.AND.getOperator();
            stmt = node.getProperty((Property) SyntaxElement.AND.resource);
        }else{
            this.propertyExpressionB(  node,  stmt,  op);
        }
    }

    public void propertyExpressionB(final Resource node, Statement stmt, Constructor op) throws AlignmentException {
        if (node.hasProperty((Property) SyntaxElement.OR.resource)) {
            op = SyntaxElement.OR.getOperator();
            stmt = node.getProperty((Property) SyntaxElement.OR.resource);
        }else{
            this.propertyExpressionC( node,  stmt,  op);
        }
    }

    public void propertyExpressionC(final Resource node, Statement stmt, Constructor op) throws AlignmentException {
        if (node.hasProperty((Property) SyntaxElement.COMPOSE.resource)) {
            op = SyntaxElement.COMPOSE.getOperator();
            stmt = node.getProperty((Property) SyntaxElement.COMPOSE.resource);
        }else{
            this.propertyExpressionD( node,  stmt,  op);
        }
    }

    public void propertyExpressionD(final Resource node, Statement stmt, Constructor op) throws AlignmentException {
        if (node.hasProperty((Property) SyntaxElement.NOT.resource)) {
            op = SyntaxElement.NOT.getOperator();
            stmt = node.getProperty((Property) SyntaxElement.NOT.resource);
        }else{
            this.propertyExpressionE(node);
        }
    }

    public void ifJena(Object o, Resource coll, Constructor op, List<PathExpression> clexpr, final Resource node) throws AlignmentException {
        if (o instanceof Resource) coll = (Resource) o;
        if (o instanceof Literal && !o.toString().equals(""))
            throw new AlignmentException("Invalid content of constructor : " + o);
        if (op == SyntaxElement.NOT.getOperator()) {
            if (coll == null)
                throw new AlignmentException("NOT constructor cannot be empty : " + node);
            clexpr.add(parseProperty(coll));
        }
    }

    public void ifOpCompose(Object o, Resource coll, Constructor op, List<PathExpression> clexpr, final Resource node) throws AlignmentException {
        if ( coll == null )
            throw new AlignmentException( "COMPOSE constructor for properties cannot be empty : "+node );
        boolean isEqual = RDF.nil.getURI().equals( coll.getURI() );
		while ( !isEqual) {
            // In this present case, it is a series of Relations followed by a Property
            Resource newcoll = coll.getProperty( RDF.rest ).getResource(); // MUSTCHECK
            if ( !RDF.nil.getURI().equals( newcoll.getURI() ) ) {
                clexpr.add( parseRelation( coll.getProperty( RDF.first ).getResource() ) );
            } else {
                clexpr.add( parseProperty( coll.getProperty( RDF.first ).getResource() ) );
            }
            coll = newcoll;
			isEqual = RDF.nil.getURI().equals( coll.getURI() );
        }
    }

    public PropertyExpression propertyExpressionE(final Resource node) throws AlignmentException {
        if ( isPattern ) { // not necessarily with a variable (real patterns)
            return new PropertyId();
        } else {
            throw new AlignmentException( "Property statement must containt one constructor or Id : "+node );
        }
    }

    public PropertyExpression ifPropDomain(final Resource node, Statement stmt, Constructor op) throws AlignmentException {

        stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
        if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
        RDFNode nn = stmt.getObject();
        if ( nn.isResource() ) {
            return new PropertyDomainRestriction( parseClass( (Resource)nn ) );
        } else {
            throw new AlignmentException( "Incorrect class expression "+nn );
        }
    }

    public PropertyExpression ifPropValueCond(final Resource node, Statement stmt, Constructor op) throws AlignmentException {

        // Find comparator
        stmt = node.getProperty( (Property)SyntaxElement.COMPARATOR.resource );
        if ( stmt == null ) throw new AlignmentException( "Required edoal:comparator property" );
        URI id = getNodeId( stmt.getResource() );
        if ( id == null ) throw new AlignmentException("edoal:comparator requires and URI");
        Comparator comp = Comparator.getComparator( id );
        stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
        if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
        ValueExpression v = parseValue( stmt.getObject() );
        return new PropertyValueRestriction( comp, v );
    }

    public PropertyExpression ifPropTypeCond(final Resource node, Statement stmt, Constructor op) throws AlignmentException {

        stmt = node.getProperty( (Property)SyntaxElement.EDATATYPE.resource );
        if ( stmt == null ) throw new AlignmentException( "Required "+SyntaxElement.EDATATYPE.print()+" property" );
        return new PropertyTypeRestriction( parseDatatype( stmt.getObject() ) );
    }

    public PropertyExpression ifPropExpr(final Resource node, Statement stmt, Constructor op) throws AlignmentException {

        URI id = getNodeId(node);
        if (id != null) {
            return new PropertyId(id);
        } else {
            this.propertyExpressionA(node, stmt, op);
        }
        return null;
    }

    public PropertyExpression elseCompose(final Resource node, Statement stmt, Constructor op,Resource coll, List<PathExpression> clexpr) throws AlignmentException {
        // This is a first/rest statements
        if ( coll != null ) {
        	boolean isEqual = RDF.nil.getURI().equals( coll.getURI() );
			while ( !isEqual) {
                clexpr.add( parseProperty( coll.getProperty( RDF.first ).getResource() ) );
                coll = coll.getProperty( RDF.rest ).getResource(); // MUSTCHECK
				isEqual = RDF.nil.getURI().equals( coll.getURI() );
            }
        }
        return new PropertyConstruction( op, clexpr );
    }


    protected PropertyExpression parseProperty( final Resource node ) throws AlignmentException {

        Resource rdfType = node.getProperty(RDF.type).getResource();
	    Statement stmt = null;
        Constructor op= null;
        List<PathExpression> clexpr = new LinkedList<PathExpression>();
        Object o = stmt.getObject();
        Resource coll = null; // Errors if null tackled below

	if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ) {

	    this.ifPropExpr(node,stmt,op);

		// Jena encode these collections as first/rest statements


        this.ifJena(o,coll,op,clexpr,node);

		} else if ( op == SyntaxElement.COMPOSE.getOperator() ) {

	    this.ifOpCompose(o,coll,op,clexpr,node);

		} else {
	    this.elseCompose(node,stmt,op,coll,clexpr);

		}
		if ( rdfType.equals( SyntaxElement.PROPERTY_DOMAIN_COND.resource ) ) {

	    this.ifPropDomain(node,stmt,op);

	} else if ( rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) ) {

	    this.ifPropTypeCond(node,stmt,op);


	} else if ( rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {

	    this.ifPropValueCond(node,stmt,op);

	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
     return null;
    }

    protected Datatype parseDatatype ( final RDFNode nn ) throws AlignmentException {
	String uri = null;
	if ( nn.isLiteral() ) { // Legacy
	    System.err.println( "Warning: datatypes must be Datatype objects ("+((Literal)nn).getString()+")" );
	    uri = ((Literal)nn).getString();
	} else if ( nn.isResource() ) {
	    if ( !((Resource)nn).getProperty(RDF.type).getResource().equals( SyntaxElement.DATATYPE.resource ) )
		throw new AlignmentException( "datatype requires a "+SyntaxElement.DATATYPE.print()+" value" );
	    uri = ((Resource)nn).getURI();
	    // check URI
	} else throw new AlignmentException( "Bad "+SyntaxElement.EDATATYPE.print()+" value" );
	return new Datatype( uri );
    }


    public void RelationExpressionA(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if (node.hasProperty((Property) SyntaxElement.AND.resource)) {
            op = SyntaxElement.AND.getOperator();
            stmt = node.getProperty((Property) SyntaxElement.AND.resource);
        }else{
            this.RelationExpressionB(node,op,stmt);
        }
    }
    public void RelationExpressionB(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( node.hasProperty( (Property)SyntaxElement.OR.resource ) ) {
            op = SyntaxElement.OR.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.OR.resource );
        }else{
            this.RelationExpressionC(node,op,stmt);
        }
    }
    public void RelationExpressionC(final Resource node, Constructor op, Statement stmt) throws AlignmentException{
        if ( node.hasProperty( (Property)SyntaxElement.COMPOSE.resource ) ) {
            op = SyntaxElement.COMPOSE.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.COMPOSE.resource );
        }else{
            this.RelationExpressionD(node,op,stmt);
        }
    }
    public void RelationExpressionD(final Resource node, Constructor op, Statement stmt) throws AlignmentException{
        if ( node.hasProperty( (Property)SyntaxElement.NOT.resource ) ) {
            op = SyntaxElement.NOT.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.NOT.resource );
        }else{
            this.RelationExpressionE(node,op,stmt);
        }
    }
    public void RelationExpressionE(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( node.hasProperty( (Property)SyntaxElement.INVERSE.resource ) ) {
            op = SyntaxElement.INVERSE.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.INVERSE.resource );
        }else{
            this.RelationExpressionF(node,op,stmt);
        }
    }
    public void RelationExpressionF(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( node.hasProperty( (Property)SyntaxElement.REFLEXIVE.resource ) ) {
            op = SyntaxElement.REFLEXIVE.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.REFLEXIVE.resource );
        }else{
            this.RelationExpressionG(node,op,stmt);
        }
    }
    public void RelationExpressionG(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( node.hasProperty( (Property)SyntaxElement.SYMMETRIC.resource ) ) {
            op = SyntaxElement.SYMMETRIC.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.SYMMETRIC.resource );
        }else{
            this.RelationExpressionH(node,op,stmt);
        }
    }
    public void RelationExpressionH(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( node.hasProperty( (Property)SyntaxElement.TRANSITIVE.resource ) ) {
            op = SyntaxElement.TRANSITIVE.getOperator();
            stmt = node.getProperty( (Property)SyntaxElement.TRANSITIVE.resource );
        }else{
            this.RelationExpressionI(node,op,stmt);
        }
    }
    public RelationExpression RelationExpressionI(final Resource node, Constructor op, Statement stmt) throws AlignmentException {
        if ( isPattern ) { // not necessarily with a variable (real patterns)
            return new RelationId();
        } else {
            throw new AlignmentException( "Relation statement must containt one constructor or Id : "+node );
        }
    }
    public void ifJena(final Resource node,Object o, Constructor op, List<PathExpression> clexpr, Resource coll) throws AlignmentException {
        if ( o instanceof Resource) coll = (Resource)o;
        if ( o instanceof Literal && !o.toString().equals("") )
            throw new AlignmentException( "Invalid content of constructor : "+o );
        if ( op == SyntaxElement.NOT.getOperator() ||
                op == SyntaxElement.INVERSE.getOperator() ||
                op == SyntaxElement.REFLEXIVE.getOperator() ||
                op == SyntaxElement.SYMMETRIC.getOperator() ||
                op == SyntaxElement.TRANSITIVE.getOperator() ) {
            this.ifNot(node,o,op,clexpr,coll);
        } else { // This is a first/rest statements
            this.elseJena(node,o,op,clexpr,coll);
        }
    }
    public  void ifNot(final Resource node,Object o, Constructor op, List<PathExpression> clexpr, Resource coll) throws AlignmentException {

        if ( coll == null )
            throw new AlignmentException( op+" constructor cannot be empty : "+node );
        clexpr.add( parseRelation( coll ) );
    }
    public void elseJena(final Resource node,Object o, Constructor op, List<PathExpression> clexpr, Resource coll) throws AlignmentException {
        if ( coll != null ) {
            boolean isEqual = RDF.nil.getURI().equals( coll.getURI() );
			while ( !isEqual) {
                clexpr.add( parseRelation( coll.getProperty( RDF.first ).getResource() ) );
                coll = coll.getProperty( RDF.rest ).getResource(); // MUSTCHECK
				isEqual = RDF.nil.getURI().equals( coll.getURI() );
			}
        }
    }
    public RelationExpression ifRelationDomain(final Resource node, Constructor op, Statement stmt) throws AlignmentException {

        stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
        if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
        RDFNode nn = stmt.getObject();
        if ( nn.isResource() ) {
            return new RelationDomainRestriction( parseClass( (Resource)nn ) );
        } else {
            throw new AlignmentException( "Incorrect class expression "+nn );
        }
    }
    public RelationExpression ifRelationCodomain(final Resource node, Constructor op, Statement stmt) throws AlignmentException {

        stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
        if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
        RDFNode nn = stmt.getObject();
        if ( nn.isResource() ) {
            return new RelationCoDomainRestriction( parseClass( (Resource)nn ) );
        } else {
            throw new AlignmentException( "Incorrect class expression "+nn );
        }
    }

    protected RelationExpression parseRelation( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	Statement stmt = null;
        Constructor op = null;
        // Remains a PathExpression (that this is a relation is checked by typing)
        List<PathExpression> clexpr = new LinkedList<PathExpression>();
	if ( rdfType.equals( SyntaxElement.RELATION_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) {
		return new RelationId( id );
	    } else {

		this.RelationExpressionA(node,op,stmt);
		}
		// Jena encode these collections as first/rest statements
		Object o = stmt.getObject();
		Resource coll = null; // Errors if null tackled below

        this.ifJena(node,o,op,clexpr,coll);

		return new RelationConstruction( op, clexpr );
	    }
	    if ( rdfType.equals( SyntaxElement.RELATION_DOMAIN_COND.resource ) ) {

	    this.ifRelationDomain(node,op,stmt);

	} else if ( rdfType.equals( SyntaxElement.RELATION_CODOMAIN_COND.resource ) ) {

	    this.ifRelationCodomain(node,op,stmt);

	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
        return new RelationId();
    }

    protected InstanceExpression parseInstance( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	if ( rdfType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) return new InstanceId( id );
	    else throw new AlignmentException("Cannot parse anonymous individual");
	} else if ( isPattern ) { // not necessarily with a variable (real patterns)
	    return new InstanceId();
	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
    }

    public void ifOpResour(final RDFNode node, URI op) throws AlignmentException {

        String operation = ((Resource)node).getProperty( (Property)SyntaxElement.OPERATOR.resource ).getLiteral().getString();
        try {
            op = new URI( operation );
        } catch (URISyntaxException e) {
            throw new AlignmentException( "edoal:Apply incorrect operation URI : "+operation );
        }
    }

    public void isArgResour(List<ValueExpression> valexpr , final RDFNode node) throws AlignmentException {

        Statement stmt = ((Resource)node).getProperty( (Property)SyntaxElement.ARGUMENTS.resource );
        Resource coll = stmt.getResource(); // MUSTCHECK
        boolean isEqual = RDF.nil.getURI().equals( coll.getURI() );
		while ( !isEqual) {
            valexpr.add( parseValue( coll.getProperty( RDF.first ).getResource() ) );
            coll = coll.getProperty( RDF.rest ).getResource();
			isEqual = RDF.nil.getURI().equals( coll.getURI() );
        }
    }

    public void isEtypeResour(final RDFNode node, URI u){

        try {
            u = new URI( ((Resource)node).getProperty( (Property)SyntaxElement.ETYPE.resource ).getLiteral().getString() );
        } catch (URISyntaxException urisex) {
            //throw new AlignmentException( "Incorect URI for edoal:type : "+ ((Resource)node).getProperty( (Property)SyntaxElement.TYPE.resource ).getLiteral().getString() );
            urisex.printStackTrace();
        }
    }

    public Value isStringRes(final RDFNode node) throws AlignmentException {

        if ( ((Resource)node).hasProperty( (Property)SyntaxElement.STRING.resource ) ) {
            URI u = null;
            if ( ((Resource)node).hasProperty( (Property)SyntaxElement.ETYPE.resource ) ) {
                this.isEtypeResour(node,u);
            }if ( u != null ) {
                return new Value( ((Resource)node).getProperty( (Property)SyntaxElement.STRING.resource ).getLiteral().getString(), u );
            } else {
                return new Value( ((Resource)node).getProperty( (Property)SyntaxElement.STRING.resource ).getLiteral().getString() );
            }
        } else {
            throw new AlignmentException( "edoal:Literal requires a edoal:value" );
        }
    }

    public void isLiteralRes(RDFNode node) throws AlignmentException {

        if ( ((Resource)node).hasProperty( (Property)SyntaxElement.STRING.resource ) ) {

            this.isStringRes(node);

        } else {
            throw new AlignmentException( "edoal:Literal requires a edoal:value" );
        }
    }

    public Apply isApplyRes(RDFNode node) throws AlignmentException {

        // Get the operation
        URI op= null;
        if ( ((Resource)node).hasProperty( (Property)SyntaxElement.OPERATOR.resource ) ) {

            this.ifOpResour(node,op);

        } else {
            throw new AlignmentException( "edoal:Apply requires an operation" );
        }
        // Get all arguments
        List<ValueExpression> valexpr = new LinkedList<ValueExpression>();
        if ( ((Resource)node).hasProperty( (Property)SyntaxElement.ARGUMENTS.resource ) ) {

            this.isArgResour(valexpr,node);

        }
        return new Apply( op, valexpr );
    }

    public ValueExpression nodeIsRes(RDFNode node) throws AlignmentException {
		Resource noder= (Resource) node;
        Resource nodeType = noder.getProperty(RDF.type).getResource();
        if ( nodeType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
            return parseInstance( noder );
        } else if ( nodeType.equals( SyntaxElement.LITERAL.resource ) ) {

            this.isLiteralRes(node);

        } else if ( nodeType.equals( SyntaxElement.APPLY.resource ) ) {

            this.isApplyRes(node);

        } else { // Check that pe is a Path??
            return parsePathExpression( noder );
        }
        return null;
    }


    protected ValueExpression parseValue( final RDFNode node ) throws AlignmentException {


        if (node.isLiteral()) { // should not appear anymore
            return new Value(((Literal) node).getString());
        } else if (node.isResource()) {
            return this.nodeIsRes(node);
        } else {
            throw new AlignmentException("Bad edoal:value value");
        }

    }

    protected URI getNodeId( final Resource node ) throws AlignmentException {
	final String idS = node.getURI();
	if ((idS != null) && (idS.length() > 0)) {
	    try {
		return new URI(idS);
	    } catch ( URISyntaxException usex ) {
		throw new AlignmentException( "Incorrect URI: "+idS );
	    }
	} else {
	    return null;
	}
    }

    /**
     * Parses a given annotaion in the the given node.
     * 
     * @param stmt
     *            the annotation statement
     * @param al
     *            the alignment in which the annotation is
     * @throws NullPointerException
     *             if the node or the element is null
     */
    //protected void parseAnnotation( final Statement stmt, EDOALAlignment al ) throws AlignmentException {
	//try {
	  //  final String anno = stmt.getString();
	    //if ((anno != null) && (anno.length() > 0)) {
		//URI uri = new URI ( stmt.getPredicate().getURI() );
		//String name = uri.getFragment();
		//String prefix = uri.getScheme()+":"+uri.getSchemeSpecificPart();
		//if ( name == null ) {
		  //  int pos = prefix.lastIndexOf('/');
		    //name = prefix.substring( pos+1 );
		    //prefix = prefix.substring( 0, pos+1 );
		//} else { prefix += "#"; }
		// This will not work for stuff like dc:creator which has no fragment!
		//al.setExtension( prefix, name, anno );
	    //}
	//} catch (Exception e1) {
	    // It would be better to silently ignore annotations
	    // Or report them in a bunch
	  //  throw new AlignmentException("The annotation is not correct", e1);
	//}

}
    


