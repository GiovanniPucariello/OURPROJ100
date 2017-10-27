/*
 * $Id: MyApp.java 1838 2013-03-24 17:17:45Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010, 2013
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

// Alignment API classes

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

// Alignment API implementation classes
// Jena
// OWL API
// HermiT
// IDDL
// SAX standard classes
// DOM Standard classes
// Java standard classes

/**
 * MyApp
 *
 * Reconcile two ontologies in various ways
 */

public class MyApp {

    String RESTServ = "http://aserv.inrialpes.fr/rest/";

    public static void testMyApp(String[] args) {
		try {
			new MyApp().run( args );
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}

    public void matchOntology(URI uri1, URI uri2, Properties params, Alignment al){
		AlignmentProcess ap = new StringDistAlignment();
		try {
			ap.init( uri1, uri2 );
			params.setProperty("stringFunction","smoaDistance");
			params.setProperty("noinst","1");
			ap.align( (Alignment)null, params );
			al = ap;
			// Supplementary:
			// upload the result on the server
			// store it
		} catch (AlignmentException ae) { ae.printStackTrace(); }
	}

	public void parseAlign(Alignment al, String xmlString) {
        try {
            AlignmentParser aparser = new AlignmentParser(0);
            Alignment alu = aparser.parseString(xmlString);
            al = ObjectAlignment.toObjectAlignment((URIAlignment) alu);
        } catch (AlignmentException ae) {
            ae.printStackTrace();

        }
    }

    public void setUpOutput( Transformer trans ) throws TransformerConfigurationException {

        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
    }

    public void generateMerge(PrintWriter writer ,File merged, Alignment al){
        try {
            merged = File.createTempFile( "MyApp-results",".owl");
            merged.deleteOnExit();
            writer = new PrintWriter ( new FileWriter( merged, false ), true );
            AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
            al.render(renderer);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (AlignmentException ae) {
            ae.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if ( writer != null ) {
                writer.flush();
                writer.close();
            }
        }
    }

    public void tryCatchBlock(NodeList alset, String xmlString){
        try {
            // Set up the output transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            this.setUpOutput(trans);

            // Print the DOM node
            StringWriter sw = new StringWriter();
            DOMSource source = new DOMSource( alset.item(0) );
            trans.transform(source, new StreamResult(sw));
            xmlString = sw.toString();

            //System.out.println(xmlString);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void answerQuery(InputStream in, File merged, QueryExecution qe){
        try {
            in = new FileInputStream( merged );
            //OntModelSpec.OWL_MEM_RDFS_INF or no arguments to see the difference...
            Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF,null);
            model.read( in, "file:"+merged.getPath() );
            in.close();

            // Create a new query
            // Could also be selected by supervisor ???
            String queryString =
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX aa: <http://alignapi.gforge.inria.fr/tutorial/tutorial4/ontology1.owl#> " +
                            "SELECT ?fn ?ln ?t ?s " +
                            //"SELECT ?fn ?ln " +
                            "WHERE {" +
                            "      ?student rdf:type aa:Estudiante . " +
                            "      ?student aa:firstname  ?fn. " +
                            "      ?student aa:lastname  ?ln. " +
                            "OPTIONAL   {   ?student aa:affiliation ?t . } " +
                            "OPTIONAL   {   ?student aa:supervisor ?s . } " +
                            "      }";

            Query query = QueryFactory.create(queryString);

            // Execute the query and obtain results
            qe = QueryExecutionFactory.create(query, model);
            ResultSet results = qe.execSelect();

            // Output query results
            ResultSetFormatter.out(System.out, results, query);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            // Important - free up resources used running the query
            if ( qe != null ) qe.close();

            if (in != null) {
				try {
					in.close();
				} catch (java.io.IOException e3) {
					System.out.println("I/O Exception");
				}
			}
        }
    }

    public void controller(OWLReasoner reasoner) throws InterruptedException {
        if ( reasoner.isConsistent() ) {
            System.err.println( "The aligned ontologies are consistent" );
        } else {
            System.err.println( "The aligned ontologies are inconsistent" );
        }
    }

    public void run( String[] args ) throws IDDLException, UnsupportedEntailmentTypeException, InterruptedException {
	String myId = "Test";
	Alignment al = null;
	URI uri1 = null;
	URI uri2 = null;
	String u1 = "file:ontology1.owl";
	String u2 = "file:ontology2.owl";
	String method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	Properties params = new Properties();
	try {
	    uri1 = new URI( u1 );
	    uri2 = new URI( u2 );
	} catch (URISyntaxException use) { use.printStackTrace(); }

	// ***** First exercise: matching *****
	// (Sol1) Try to find an alignment between two ontologies from the server
	// ask for it
	String found = getFromURLString( RESTServ+"find?onto1="+u1+"&onto2="+u2, false );
	// retrieve it
	// If there exists alignments, ask for the first one
	NodeList alset = extractFromResult( found, "//findResponse/alignmentList/alid[1]/text()", false );

	// (Sol3) Match the ontologies on the server
	//if ( alset.getLength() == 0 ) {
	    // call for matching
	    // * tested (must add force = true)
	    //String match = getFromURLString( RESTServ+"match?onto1="+u1+"&onto2="+u2+"&method="+method+"&pretty="+myId+"&action=Match", true );
	    // This returns a URI
	//}

	if ( alset.getLength() > 0 ) {
	    // Ask it
	    String alid = alset.item(0).getNodeValue();
	    found = getFromURLString( RESTServ+"retrieve?method=fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor&id="+alid, false );
	    alset = extractFromResult( found, "//retrieveResponse/result/*[1]",true );

	    // This code is heavy
	    String xmlString = null;

	    this.tryCatchBlock(alset,xmlString);

	    // parse it as an alignment
	    this.parseAlign(al,xmlString);
	}

	// (Sol2) Match the ontologies with a local algorithm
	if ( al == null ){ // Unfortunatelly no alignment was available
	   this.matchOntology(uri1,uri2,params,al);
	}

	// Alternative: find an intermediate ontology between which there are alignments
	// find (basically a graph traversal operation)
	// retrieve them
	// parse them
	// compose them

	// ***** Second exercise: merging/transforming *****

	// (Sol1) generate a merged ontology between the ontologies (OWLAxioms)
	PrintWriter writer = null;
	File merged = null;

	this.generateMerge(writer,merged,al);

	// If merged is empty then destroy the file + exit

	// (Sol2) import the data from one ontology into the other

	// ***** Third exercise: querying and reasoning *****

	// (Sol1) Use SPARQL to answer queries (at the data level)
	InputStream in = null;
	QueryExecution qe = null;

	this.answerQuery(in,merged,qe);

	// (Sol2) Use the OWLReasoner to answer queries (at the ontology level)
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLReasoner reasoner = null;

	// Load the ontology 
	try {
	    OWLOntology ontology = manager.loadOntology( IRI.create( "file:"+merged.getPath() ) );
	    reasoner = new Reasoner( ontology );
	} catch (OWLOntologyCreationException ooce) {
	    ooce.printStackTrace(); 
	}

	this.controller(reasoner);
	// get the instances of a class
	OWLClass estud = manager.getOWLDataFactory().getOWLClass( IRI.create( "http://alignapi.gforge.inria.fr/tutorial/tutorial4/ontology1.owl#Estudiante" ) );   
	OWLClass person = manager.getOWLDataFactory().getOWLClass( IRI.create( "http://alignapi.gforge.inria.fr/tutorial/tutorial4/ontology2.owl#Person" ) );   
	OWLClass student = manager.getOWLDataFactory().getOWLClass( IRI.create( "http://alignapi.gforge.inria.fr/tutorial/tutorial4/ontology2.owl#Student" ) );   
	Set<OWLNamedIndividual> instances  = reasoner.getInstances( estud, false ).getFlattened();
	System.err.println("OWLReasoner(Merged): There are "+instances.size()+" students ("+clname(estud)+")");

	testOWLReasonerSubClass( manager, reasoner, estud, person );
	testOWLReasonerSubClass( manager, reasoner, estud, student );

	// (Sol3) reasoning with distributed semantics (IDDL)
	// test consistency of aligned ontologies
	ArrayList<Alignment> allist = new ArrayList<Alignment>();
	allist.add( al );
	IDDLReasoner dreasoner = new IDDLReasoner( allist, Semantics.DL );
	// Try Semantics.IDDL instead!
	if ( dreasoner.isConsistent() ) {
	    System.out.println( "IDDL: the alignment network is consistent");
	    testIDDLSubClass( dreasoner, uri1, uri2, estud, person );
	    testIDDLSubClass( dreasoner, uri1, uri2, estud, student );
         } else {
	    System.out.println( "IDDL: the alignment network is inconsistent");
	}
    }



    private String clname( OWLClassExpression cl ) {

        return cl.asOWLClass().getIRI().getFragment();
    }

    public void testOWLReasonerSubClass( OWLOntologyManager manager, OWLReasoner reasoner, OWLClassExpression d1, OWLClassExpression d2 ) throws UnsupportedEntailmentTypeException, InterruptedException {
	OWLAxiom axiom = manager.getOWLDataFactory().getOWLSubClassOfAxiom( d1, d2 );
	if ( reasoner.isEntailed( axiom ) ) {
	    System.out.println( "OWLReasoner(Merged): "+clname(d1)+" is subclass of "+clname(d2) );
	} else {
	    System.out.println( "OWLReasoner(Merged): "+clname(d1)+" is not necessarily subclass of "+clname(d2) );
	}
    }

    public void testIDDLSubClass( IDDLReasoner dreasoner, URI onto1, URI onto2, OWLClassExpression d1, OWLClassExpression d2 ) throws IDDLException {
	Alignment al2 = new ObjectAlignment();
	try {
	    al2.init( onto1, onto2 );
	    // add the cell
	    al2.addAlignCell( d1, d2, "&lt;", 1. );
	} catch (AlignmentException ae) { ae.printStackTrace(); }
	if ( dreasoner.isEntailed( al2 ) ) {
	    System.out.println( "IDDL: "+clname(d1)+" <= "+clname(d2)+" is entailed" );
	} else {
	    System.out.println( "IDDL: "+clname(d1)+" <= "+clname(d2)+" is not entailed" );
	}
    }

    public String getFromURLString( String u, boolean print ){
	URL url = null;
	String result = "<?xml version='1.0'?>";
	try {
	    url = new URL( u );
	    BufferedReader in = new BufferedReader(
    				new InputStreamReader(
    				  url.openStream()));
	    String inputLine = in.readLine();
	    while (inputLine != null) {
		if (print) System.out.println(inputLine);
		result += inputLine;
		inputLine= in.readLine();
	    }
	    in.close();
	}
	catch ( MalformedURLException mue ) { mue.printStackTrace(); }
	catch ( IOException mue ) { mue.printStackTrace(); }
	return result;
    }

    public NodeList extractFromResult( String found, String path, boolean print ){
	Document document = null;
	NodeList nodes = null;
	try { // Parse the returned stringAS XML
	    DocumentBuilder parser =
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    document = parser.parse(new ByteArrayInputStream( found.getBytes() ));
	} catch ( ParserConfigurationException pce ) { pce.printStackTrace(); }
	catch ( SAXException se ) { se.printStackTrace(); }
	catch ( IOException ioe ) { ioe.printStackTrace(); }

	try { // Apply the Xpath expression
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    //XPathExpression expr = xpath.compile("//book[author='Neal Stephenson']/title/text()");
	    XPathExpression expr = xpath.compile( path );
	    Object result = expr.evaluate( document, XPathConstants.NODESET );
	    nodes = (NodeList)result;
	    if ( print ) {
	    	int length =  nodes.getLength();
		for ( int i = 0; i < length; i++) {
		    System.out.println(nodes.item(i).getNodeValue()); 
		}
	    }
	} catch (XPathExpressionException xpee) { xpee.printStackTrace(); }
	return nodes;
    }
}
