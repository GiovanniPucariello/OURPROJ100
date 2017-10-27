/*
 * $Id: GraphPatternRendererVisitor.java 1833 2013-03-15 10:26:19Z euzenat $
 *
 * Copyright (C) INRIA, 2012-2013
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

import fr.inrialpes.exmo.align.impl.edoal.*;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.*;

/**
 * Translate correspondences into Graph Patterns
 *
 */

// JE: create a string... problem with increment.

/**
 * class GraphPatternRendererVisitor
 */
public abstract class GraphPatternRendererVisitor extends IndentedRendererVisitor implements EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    protected boolean ignoreerrors = false;
    protected static boolean blanks = false;
    protected boolean weakens = false;
    protected boolean corese = false;
    private boolean inClassRestriction = false;
    private String instance = null;
    private String value = "";
    private String uriType = null;
    private String datatype = "";
    private Object valueRestriction = null;        
    private static int flagRestriction;
    private Constructor op = null;          
    private Integer nbCardinality = null;
    private String opOccurence = "";    
    private static int numberNs;
	private static int number = 1;	
    private static String sub = ""; 
    private static String obj = "";
    private String strBGP = "";
    private String strBGP_Weaken = "";
    protected List<String> listBGP = new ArrayList<String>();
    private Set<String> subjectsRestriction = new HashSet<String>();
    private Set<String> objectsRestriction = new HashSet<String>();
    protected Hashtable<String,String> prefixList = new Hashtable<String,String>();
    
    private static int count = 1;

	/**
	 *
	 * @param writer
	 */
	public GraphPatternRendererVisitor( PrintWriter writer ){
		super( writer );
    }

	/**
	 *
	 * @param s
	 * @param o
	 */
    public static void resetVariablesName( String s, String o ) {
    	count = 1;
    	sub = "?" + s;
    	obj = "?" + o + count;    	
    }

	/**
	 *
	 * @param s
	 * @param o
	 */
	public void resetVariables( String s, String o ) {
    	resetVariablesName(s, o);
    	strBGP = "";
		strBGP_Weaken = "";
		listBGP.clear();
		objectsRestriction.clear();
		flagRestriction = 0;
    }

	/**
	 *
	 * @return
	 */
	public String getGP(){
    	return strBGP;
    }

	/**
	 *
	 * @return
	 */
	public List<String> getBGP() {
    	return listBGP;
    }

	/**
	 *
	 * @param u
	 * @return
	 */
	public String getPrefixDomain( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring(0, index+1);
    }

	/**
	 *
	 * @param u
	 * @return
	 */
	public String getPrefixName( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring( index+1 );
    }

	/**
	 *
	 * @return
	 */
	public static String getNamespace(){
    	return "ns" + numberNs++;
    }

	/**
	 *
	 * @param dir
	 * @param query
	 */
    public void createQueryFile( String dir, String query ) {
    	BufferedWriter out = null;
		FileWriter writer = null;
    	try {
	    writer = new FileWriter( dir+"query"+number +".rq" );
	    out = new BufferedWriter( writer );
	    number++;
	    out.write( query );
	    if ( out != null ) // there was at least one file
		out.close();
	} catch(IOException ioe) {
	    System.err.println( ioe );
	} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (java.io.IOException e3) {
					System.out.println("I/O Exception");
				}
			}
		}
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final ClassId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {
    		String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		prefixList.put( "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf" );
    		if( !prefixList.containsKey(prefix) && !prefix.equals("") ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}

			if ( !subjectsRestriction.isEmpty() ) {
				Iterator<String> listSub = subjectsRestriction.iterator();
				while ( listSub.hasNext() ) {
					String str = listSub.next();
					strBGP += str + " rdf:type " + shortCut + ":"+ tag + " ." + NL;			
					strBGP_Weaken += str + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
				}
				subjectsRestriction.clear();
			}
			else {
				strBGP += sub + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
				strBGP_Weaken += sub + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
			}
    	}
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final ClassConstruction e ) throws AlignmentException {    	
    	op = e.getOperator();
		if (op == Constructor.OR) {			
			int size = e.getComponents().size();			
			for ( final ClassExpression ce : e.getComponents() ) {
			    strBGP += "{" + NL;
			    strBGP_Weaken += "{" + NL;
			    ce.accept( this );			    			    
			    size--;
			    if( size != 0 ) {
			    	strBGP += "}" + " UNION " + NL;
			    	strBGP_Weaken += "}" + " UNION " + NL;
			    }
			    else {
			    	strBGP += "}" + NL;
			    	strBGP_Weaken += "}" + NL;
			    }
			}			
		}
		else if ( op == Constructor.NOT ) {			
		    strBGP += "FILTER (NOT EXISTS {" + NL;
		    strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final ClassExpression ce : e.getComponents() ) {			    
			    ce.accept( this );				
			}
		    strBGP += "})" + NL;
		    strBGP_Weaken += "})" + NL;
		}
		else {			
			for ( final ClassExpression ce : e.getComponents() ) {			    			    
			    ce.accept( this );
			    if ( !strBGP_Weaken.equals("") ) {
			    	listBGP.add(strBGP_Weaken);
			    	strBGP_Weaken = "";
			    }
			}
		}
    }

	/**
	 *
	 * @param objectsRestriction
	 * @param op
	 * @param str
	 * @param opOccurence
	 * @param valueRestriction
	 * @param strBGP
	 * @param strBGP_Weaken
	 */
    public static void visitInnerA(Set<String> objectsRestriction, Constructor op, String str, String opOccurence, Object valueRestriction,
								   String strBGP, String strBGP_Weaken) {
		Iterator<String> listObj = objectsRestriction.iterator();
		if (op == Constructor.COMP) {
			String tmp = "";
			while ( listObj.hasNext() )
				tmp = listObj.next();
			str = "FILTER (" + tmp + opOccurence + valueRestriction + ")" +NL;
		}
		else {
			while ( listObj.hasNext() ) {
				str += "FILTER (" + listObj.next() + opOccurence + valueRestriction + ")" +NL;
			}
		}
		strBGP += str;
		strBGP_Weaken += str;
	}

	/**
	 *
	 * @param op
	 */
	public static void visitInnerI(Constructor op) {
		if( op == Constructor.AND ){
			if ( blanks ) {
				obj = "_:o" + ++count;
			}
			else {
				obj = "?o" + ++count;
			}
		}
	}

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final ClassValueRestriction c ) throws AlignmentException {
    	String str = "";
    	instance = "";
	    value = "";
	    flagRestriction = 1;
	    c.getValue().accept( this );
	    flagRestriction = 0;
	    
	    if( !instance.equals("") )
	    	valueRestriction = instance;
	    else if( !value.equals("") )
	    	valueRestriction = value;
	    
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			opOccurence = ">";
			inClassRestriction = true;
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			opOccurence = "<";
			inClassRestriction = true;
		}
		flagRestriction = 1;
	    c.getRestrictionPath().accept( this );
	    flagRestriction = 0;
		String temp = obj;
		if ( inClassRestriction && !objectsRestriction.isEmpty() ) {
			visitInnerA(objectsRestriction, op, str, opOccurence,valueRestriction, strBGP, strBGP_Weaken);
		}
		valueRestriction = null;
		inClassRestriction = false;		
		obj = temp;
		visitInnerI(op);
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final ClassTypeRestriction c ) throws AlignmentException {	
    	String str = "";
    	datatype = "";
    	inClassRestriction = true;
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;
			
			strBGP += str;
			strBGP_Weaken += str;
		}
		objectsRestriction.clear();
		inClassRestriction = false;
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final ClassDomainRestriction c ) throws AlignmentException {					
    	inClassRestriction = true;
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
    	Iterator<String> listObj = objectsRestriction.iterator();
    	while ( listObj.hasNext() ) {
			subjectsRestriction.add(listObj.next());			
		}
    	c.getDomain().accept( this );    	
    	objectsRestriction.clear();
    	inClassRestriction = false;
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
		String str="";
		inClassRestriction = true;
    	if( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "=";
		}
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = ">";
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "<";
		}
		flagRestriction = 1;
		c.getRestrictionPath().accept( this );	
		flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			if (op == Constructor.COMP) {			
				String tmp = "";
				while ( listObj.hasNext() )
					tmp = listObj.next();
				str += "FILTER(COUNT(" + tmp + ")" + opOccurence + nbCardinality + ")" +NL;	    
			}
			else{
				while ( listObj.hasNext() ) {
					str += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" +NL;	
				}
			}			
			
			strBGP += str;
			strBGP_Weaken += str;
		}
		nbCardinality = null;
		opOccurence = "";
		inClassRestriction = false;
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final PropertyId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {	
    		String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if( !prefixList.containsKey(prefix) && !prefix.equals("") ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
    		String temp = obj;
    		if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )
    			obj = "\"" + valueRestriction.toString() + "\"";
    		if ( flagRestriction == 1 && inClassRestriction )
				objectsRestriction.add(obj);
    		
		    strBGP += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
		    strBGP_Weaken += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
    		obj = temp;    		
		}
    }

	/**
	 *
	 * @param strBGP
	 * @param strBGP_Weaken
	 * @param re
	 * @param size
	 */
    public static void visitInnerB(String strBGP, String strBGP_Weaken, PathExpression re, int size) {
		strBGP += "{" +NL;
		strBGP_Weaken += "{" +NL;
		re.accept( this );
		size--;
		if( size != 0 ){
			strBGP += "}" + " UNION " + NL;
			strBGP_Weaken += "}" + " UNION " + NL;
		}
		else {
			strBGP += "}" +NL;
			strBGP_Weaken += "}" +NL;
		}
	}

	/**
	 *
	 * @param size
	 * @param valueRestriction
	 * @param inClassRestriction
	 * @param e
	 */
	public static void visitInnerC(int size, Object valueRestriction, boolean inClassRestriction, PropertyConstruction e) {
		for ( final PathExpression re : e.getComponents() ) {
			re.accept( this );
			size--;
			if ( size != 0 ) {
				sub = obj;
				if( size == 1 && valueRestriction != null && !inClassRestriction ) {
					obj = "\"" + valueRestriction.toString() + "\"";
				}
				else {
					if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
						obj = "_:o" + ++count;
					}
					else {
						obj = "?o" + ++count;
					}
				}
			}
		}
	}

	/**
	 *
	 * @param e
	 * @param size
	 * @param objectsRestriction
	 * @param valueRestriction
	 * @param strBGP_Weaken
	 * @param inClassRestriction
	 * @param listBGP
	 */
	public static void visitInnerD(PropertyConstruction  e, int size, Set<String> objectsRestriction, Object valueRestriction,
								   String strBGP_Weaken, boolean inClassRestriction, List<String> listBGP) {
		for ( final PathExpression re : e.getComponents() ) {
			re.accept( this );
			size--;
			objectsRestriction.add( obj );
			if( size != 0 && valueRestriction == null ){
				obj = "?o" + ++count;
			}
			if ( !strBGP_Weaken.equals("") && !inClassRestriction ) {
				listBGP.add(strBGP_Weaken);
				strBGP_Weaken = "";
			}
		}
	}

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final PropertyConstruction e ) throws AlignmentException {

		HashMap<Constructor, Handler> map = new HashMap<Constructor, Handler>();
		map.put(Constructor.OR, new CaseOR1());
		map.put(Constructor.NOT, new CaseNOT1());
		map.put(Constructor.COMP, new CaseCOMP1());

    	op = e.getOperator();

    	if(map.get(op) != null){
    		map.get(op).execute(e);
		}else {
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			visitInnerD(e, size, objectsRestriction, valueRestriction, strBGP_Weaken, inClassRestriction, listBGP);
		}

		obj = "?o" + ++count;    	
    }

	/**
	 * interface Handler
	 */
    private interface Handler {
    	void execute(PropertyConstruction e);
	}

	/**
	 * interface SecondHandler
	 */
	private interface secondHandler {
    	void execute(RelationConstruction e);
	}

	/**
	 * class CaseOR1
	 */
	private class CaseOR1 implements Handler {
		public void execute(PropertyConstruction e) {
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
				visitInnerB(strBGP, strBGP_Weaken, re, size);
			}
			objectsRestriction.add( obj );
		}
	}

	/**
	 * class CaseNOT1
	 */
	private class CaseNOT1 implements Handler {
		public void execute(PropertyConstruction e) {
			strBGP += "FILTER (NOT EXISTS {" + NL;
			strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final PathExpression re : e.getComponents() ) {
				re.accept( this );
			}
			strBGP += "})" + NL;
			strBGP_Weaken += "})" + NL;
		}
	}

	/**
	 * class CaseCOMP1
	 */
	private class CaseCOMP1 implements Handler {
		public void execute(PropertyConstruction e) {
			int size = e.getComponents().size();
			String tempSub = sub;
			if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
				obj = "_:o" + ++count;
			}
			visitInnerC(size, valueRestriction, inClassRestriction, e);
			objectsRestriction.add( obj );
			sub = tempSub;
		}
	}

	/**
	 * class CaseOR2
	 */
	private class CaseOR2 implements secondHandler {
		public void execute(RelationConstruction e) {
			visitInnerF(e, valueRestriction, inClassRestriction, strBGP, strBGP_Weaken, objectsRestriction);
		}
	}

	/**
	 * class CaseNOT2
	 */
	private class CaseNOT2 implements secondHandler {
		public void execute(RelationConstruction e) {
			strBGP += "FILTER (NOT EXISTS {" + NL;
			strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final PathExpression re : e.getComponents() ) {
				re.accept( this );
			}
			strBGP += "})" + NL;
			strBGP_Weaken += "})" + NL;
		}
	}

	/**
	 * class CaseCOMP2
	 */
	private class CaseCOMP2 implements secondHandler {
		public void execute(RelationConstruction e) {
			int size = e.getComponents().size();
			String temp = sub;
			if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
				obj = "_:o" + ++count;
			}
			visitInnerG(size, valueRestriction, inClassRestriction, objectsRestriction, e, temp);
		}
	}

	/**
	 * class CaseINVERSE
	 */
	private class CaseINVERSE implements secondHandler {
		public void execute(RelationConstruction e) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
				String temp = sub;
				sub = obj;
				obj = temp;
				re.accept( this );
				sub = tempSub;
			}
		}
	}

	/**
	 * class CaseSYMMETRIC
	 */
	private class CaseSYMMETRIC implements secondHandler {
		public void execute(RelationConstruction e) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
				strBGP += "{" + NL;
				re.accept( this );
				objectsRestriction.add( obj );
				String temp = sub;
				sub = obj;
				obj = temp;
				strBGP += "} UNION {" + NL;
				re.accept( this );
				objectsRestriction.add( obj );
				strBGP +="}" + NL;
			}
			sub = tempSub;
		}
	}

	/**
	 * class CaseTRANSITIVE
	 */
	private class CaseTRANSITIVE implements secondHandler {
		public void execute(RelationConstruction e) {
			for ( final PathExpression re : e.getComponents() ) {
				flagRestriction = 1;
				re.accept( this );
				flagRestriction = 0;
			}
		}
	}

	/**
	 * class CaseREFLEXIVE
	 */
	private class CaseREFLEXIVE implements secondHandler {
		public void execute(RelationConstruction e) {
			for ( final PathExpression re : e.getComponents() ) {
				strBGP += "{" + NL;
				re.accept( this );
				strBGP += "} UNION {" + NL + "FILTER(" + sub + "=" + obj + ")" + NL + "}";
			}
		}
	}


	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final PropertyValueRestriction c ) throws AlignmentException {
    	String str = "";
    	value = "";
    	uriType = "";
    	flagRestriction = 1;
		c.getValue().accept( this );
		flagRestriction = 0;
  		if ( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") = ";    		
    	}
    	else if ( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") > ";			
    	}
    	else {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") < ";
    	}
    	str += "\"" + value + "\")" + NL;
		
		strBGP += str;
		strBGP_Weaken += str;
    	value = "";
    	uriType = "";
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final PropertyDomainRestriction c ) throws AlignmentException {
    	flagRestriction = 1;
		c.getDomain().accept( this );    	
    	flagRestriction = 0;	
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit( final PropertyTypeRestriction c ) throws AlignmentException {
    	String str = "";		
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;			
			strBGP += str;
			strBGP_Weaken += str;
		}
		objectsRestriction.clear();
    }

	/**
	 *
	 * @param strBGP
	 * @param strBGP_Weaken
	 * @param prefix
	 * @param shortCut
	 * @param prefixList
	 * @param op
	 * @param tag
	 */
    public static void visitInnerE(String strBGP, String strBGP_Weaken, String prefix, String shortCut, Hashtable<String,String> prefixList,
								   Constructor op, String tag) {
		if ( !prefixList.containsKey(prefix) && !prefix.equals("") ) {
			shortCut = getNamespace();
			prefixList.put( prefix, shortCut );
		}
		else {
			shortCut = prefixList.get( prefix );
		}
		strBGP += sub + " " + shortCut + ":"+ tag + "";
		strBGP_Weaken += sub + " " + shortCut + ":"+ tag + "";

		if ( op == Constructor.TRANSITIVE && flagRestriction == 1 ) {
			strBGP += "*";
			strBGP_Weaken += "*";
		}
	}

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final RelationId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut = null;
    		visitInnerE(strBGP, strBGP_Weaken, prefix, shortCut, prefixList, op, tag);
		    if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )			    
					obj = valueRestriction.toString();
		    if ( flagRestriction == 1 && inClassRestriction && op != Constructor.COMP )
					objectsRestriction.add(obj);
	    	
		    strBGP += " " + obj + " ." + NL;
		    strBGP_Weaken += " " + obj + " ." + NL;		    
		}
    }

	/**
	 *
	 * @param e
	 * @param valueRestriction
	 * @param inClassRestriction
	 * @param strBGP
	 * @param strBGP_Weaken
	 * @param objectsRestriction
	 */
    public static void visitInnerF(RelationConstruction e, Object valueRestriction, boolean inClassRestriction, String strBGP, String strBGP_Weaken,
								   Set<String> objectsRestriction) {
		int size = e.getComponents().size();
		if ( valueRestriction != null && !inClassRestriction )
			obj = valueRestriction.toString();
		String temp = obj;
		for ( final PathExpression re : e.getComponents() ) {
			strBGP += "{" + NL;
			strBGP_Weaken += "{" + NL;
			re.accept( this );
			obj = temp;
			size--;
			if ( size != 0 ) {
				strBGP += "}" + "UNION " + NL;
				strBGP_Weaken += "}" + "UNION " + NL;
			}
			else {
				strBGP += "}" + NL;
				strBGP_Weaken += "}" + NL;
			}
		}
		objectsRestriction.add( obj );
	}

	/**
	 *
	 * @param size
	 * @param valueRestriction
	 * @param inClassRestriction
	 * @param objectsRestriction
	 * @param e
	 * @param temp
	 */
	public static void visitInnerG(int size, Object valueRestriction, boolean inClassRestriction, Set<String> objectsRestriction,
									RelationConstruction e, String temp) {
		for ( final PathExpression re : e.getComponents() ) {
			re.accept( this );
			size--;
			if( size != 0 ) {
				sub = obj;
				if ( size == 1 && valueRestriction != null && !inClassRestriction ) {
					obj = valueRestriction.toString();
				}
				else {
					if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
						obj = "_:o" + ++count;
					}
					else {
						obj = "?o" + ++count;
					}
					objectsRestriction.add( obj );
				}
			}
		}
		sub = temp;
	}

	/**
	 *
	 * @param e
	 * @param valueRestriction
	 * @param inClassRestriction
	 * @param objectsRestriction
	 * @param strBGP_Weaken
	 * @param listBGP
	 */
	public static void visitInnerH(RelationConstruction e, Object valueRestriction, boolean inClassRestriction, Set<String> objectsRestriction,
								   String strBGP_Weaken, List<String> listBGP) {
		int size = e.getComponents().size();
		if ( valueRestriction != null && !inClassRestriction )
			obj = valueRestriction.toString();
		for ( final PathExpression re : e.getComponents() ) {
			re.accept( this );
			size--;
			objectsRestriction.add( obj );
			if ( size != 0 && valueRestriction == null ) {
				obj = "?o" + ++count;
			}
			if ( !strBGP_Weaken.equals("") && !inClassRestriction ) {
				listBGP.add(strBGP_Weaken);
				strBGP_Weaken = "";
			}
		}
	}

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final RelationConstruction e ) throws AlignmentException {

		op = e.getOperator();

		HashMap<Constructor, secondHandler> map = new HashMap<Constructor, secondHandler>();
		map.put(Constructor.OR, new CaseOR2());
		map.put(Constructor.NOT, new CaseNOT2());
		map.put(Constructor.COMP, new CaseCOMP2());
		map.put(Constructor.INVERSE, new CaseINVERSE());
		map.put(Constructor.SYMMETRIC, new CaseSYMMETRIC());
		map.put(Constructor.REFLEXIVE, new CaseREFLEXIVE());
		map.put(Constructor.TRANSITIVE, new CaseTRANSITIVE());

		if(map.get(op) != null) {
			map.get(op).execute(e);
		} else {
			visitInnerH(e, valueRestriction, inClassRestriction, objectsRestriction, strBGP_Weaken, listBGP);
		}

		obj = "?o" + ++count;    	
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
    	sub = obj;
    	flagRestriction = 1;		
    	c.getCoDomain().accept( this );    	
    	flagRestriction = 0;
    }

	/**
	 *
	 * @param c
	 * @throws AlignmentException
	 */
    public void visit(final RelationDomainRestriction c) throws AlignmentException {
    	flagRestriction = 1;
		c.getDomain().accept( this );
    	flagRestriction = 0;
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final InstanceId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if ( !prefixList.containsKey( prefix) ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
			if ( flagRestriction != 1 )
				strBGP += shortCut + ":"+ tag + " ?p ?o1 ." +NL;
			else
				instance = shortCut + ":"+ tag;
		}
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final Value e ) throws AlignmentException {
    	if (e.getType() != null) {
	    	String str = e.getType().toString();
	    	int index;
	    	if ( str.contains("#") )
	    		index = str.lastIndexOf("#");
	    	else
	    		index = str.lastIndexOf("/");
	    	uriType = str.substring( index+1 );
    	}
    	value = e.getValue();
    	if ( uriType != null && uriType.equals("") ) {
    		uriType = "string";
    	}
    	
    }

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final Apply e ) throws AlignmentException {}

	/**
	 *
	 * @param transf
	 * @throws AlignmentException
	 */
    public void visit( final Transformation transf ) throws AlignmentException {}

	/**
	 *
	 * @param e
	 * @throws AlignmentException
	 */
    public void visit( final Datatype e ) throws AlignmentException {
    	int index;
    	if ( e.getType().contains("#") )
    		index = e.getType().lastIndexOf("#");
    	else
    		index = e.getType().lastIndexOf("/");
    	datatype = e.getType().substring( index+1 );
    }

}
