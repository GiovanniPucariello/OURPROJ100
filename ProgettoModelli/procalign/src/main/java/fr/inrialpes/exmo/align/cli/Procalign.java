/*
 * $Id: Procalign.java 1820 2013-03-06 10:13:00Z euzenat $
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2008, 2010-2013 INRIA
 * Copyright (C) 2004, Université de Montréal
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

/* This program is an adaptation of the Processor.java class of the
   initial release of the OWL-API
*/
package fr.inrialpes.exmo.align.cli;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A basic class for an OWL ontology alignment processing. The processor
    will parse ontologies, align them and renderings the resulting alignment.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.Procalign [options] onto1 onto2 [output]
    </pre>

    or better
    <pre>
    java -jar procalign.jar onto1 onto2
    </pre>

    where the options are:
    <pre>
        --alignment=filename -a filename Start from an XML alignment file
	--params=filename -p filename   Read the parameters in file
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --impl=className -i classname           Use the given alignment implementation.
        --renderer=className -r className       Specifies the alignment renderer
        --help -h                       Print this message
    </pre>

    <CODE>onto1</CODE> and <CODE>onto2</CODE> should be URLs. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: Procalign.java 1820 2013-03-06 10:13:00Z euzenat $
</pre>

@author Sean K. Bechhofer
@author Jérôme Euzenat
    */

public class Procalign {

    public static void testProcalign(String[] args) {
	try { new Procalign().run( args ); }
	catch ( Exception ex ) { ex.printStackTrace(); };
    }

    public Alignment run(String[] args) throws Exception {
    	Container cont = new Container();
		cont.onto1 = null;
		cont.onto2 = null;
		cont.result = null;
		cont.cutMethod = "hard";
		cont.initName = null;
		cont.init = null;
		cont.alignmentClassName = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		cont.filename = null;
		cont.paramfile = null;
		cont.rendererClass = "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor";
		cont.writer = null;
		cont.renderer = null;
		cont.debug = 0;
		cont.threshold = 0;
		cont.params = new Properties();

		cont.longopts = new LongOpt[10];

		cont.longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		cont.longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
		cont.longopts[2] = new LongOpt("alignment", LongOpt.REQUIRED_ARGUMENT, null, 'a');
		cont.longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
		cont.longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
		cont.longopts[5] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
		cont.longopts[6] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
		cont.longopts[7] = new LongOpt("cutmethod", LongOpt.REQUIRED_ARGUMENT, null, 'T');
		cont.longopts[8] = new LongOpt("params", LongOpt.REQUIRED_ARGUMENT, null, 'p');
		// Is there a way for that in LongOpt ???
		cont.longopts[9] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

		cont.g = new Getopt("", args, "ho:a:p:d::r:t:T:i:D:", cont.longopts);

		setParams(cont);


	try {

	   createAlignmentObject(cont, args);

	    if (cont.debug > 0) System.err.println(" Alignment structure created");
	    // Compute alignment
	    long time = System.currentTimeMillis();
		cont.result.align(  cont.init, cont.params ); // add opts
	    long newTime = System.currentTimeMillis();
		cont.result.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );

	    // Thresholding
	    if (cont.threshold != 0) cont.result.cut( cont.cutMethod, cont.threshold );

	    if (cont.debug > 0) System.err.println(" Matching performed");
	    
	    // Set output file
		setOutputFile(cont);

	    // Result printing (to be reimplemented with a default value)
		resultPrinting(cont);
	    
	    // Output
		cont.result.render(cont.renderer);
	} catch (Exception ex) {
	    throw ex;
	} finally {
	    if ( cont.writer != null ) {
			cont.writer.flush();
			cont.writer.close();
	    }
	}
	return cont.result;
    }

	private class Container{
		public URI onto1;
		public URI onto2;
		public AlignmentProcess result;
		public String cutMethod;
		public String initName;
		public Alignment init;
		public String alignmentClassName;
		public String filename;
		public String paramfile;
		public String rendererClass;
		public PrintWriter writer;
		public AlignmentVisitor renderer;
		public int debug;
		public double threshold;
		public Properties params;
		public Getopt g;
		public LongOpt[] longopts;
		public String arg;
		public URI uri1;
		public URI uri2;
	}


	private interface Handler{
		void handle(Container cont);
	}

	private class CaseH implements Handler{
		public void handle(Container cont){
			usage();
		}
	}

	private class CaseO implements Handler{
		public void handle(Container cont){
			/* Use filename instead of stdout */
			filename = g.getOptarg();
		}
	}

	private class CaseP implements Handler{
		public void handle(Container cont){
			/* Read parameters from filename */
			paramfile = g.getOptarg();
			params.loadFromXML( fi );
		}
	}

	private class CaseR implements Handler{
		public void handle(Container cont){
			/* Use the given class for rendering */
			rendererClass = g.getOptarg();
		}
	}

	private class CaseI implements Handler{
		public void handle(Container cont){
			/* Use the given class for the alignment */
			alignmentClassName = g.getOptarg();
		}
	}

	private class CaseA implements Handler{
		public void handle(Container cont){
			/* Use the given file as a partial alignment */
			initName = g.getOptarg();
		}
	}

	private class CaseT implements Handler{
		public void handle(Container cont){
			/* Threshold */
			threshold = Double.parseDouble(g.getOptarg());
		}
	}

	private class CaseMaiuscT implements Handler{
		public void handle(Container cont){
			/* Cut method */
			cont.cutMethod = cont.g.getOptarg();
		}
	}

	private class CaseD implements Handler{
		public void handle(Container cont){
			/* Debug level  */
			cont.arg = cont.g.getOptarg();
			if ( cont.arg != null ) cont.debug = Integer.parseInt(cont.arg.trim());
			else cont.debug = 4;
		}
	}

	private class CaseMaiuscD implements Handler{
		public void handle(Container cont){
			/* Parameter definition */
			cont.arg = cont.g.getOptarg();
			int index = cont.arg.indexOf('=');
			if ( index != -1 ) {
				cont.params.setProperty( cont.arg.substring( 0, index),
						cont.arg.substring(index+1));
			} else {
				System.err.println("Bad parameter syntax: "+cont.g);
				usage();
				System.exit(0);
			}
		}
	}

    private void createAlignmentObject(Container cont, String[] args) throws Exception {
		int i = cont.g.getOptind();
    	URI uri1 = null;
		URI uri2 = null;

		if (args.length > i + 1) {
			uri1 = new URI(args[i++]);
			uri2 = new URI(args[i]);
		} else if (cont.initName == null) {
			System.err.println("Two URIs required");
			usage();
			System.exit(0);
		}

		if (cont.debug > 0) System.err.println(" Ready");

    	try {
			if (cont.initName != null) {
				AlignmentParser aparser = new AlignmentParser(cont.debug);
				Alignment al = aparser.parse( cont.initName );
				cont.init = al;
				if (cont.debug > 0) System.err.println(" Init parsed");
			}

			// Create alignment object
			Object[] mparams = {};
			Class<?> alignmentClass = Class.forName(cont.alignmentClassName);
			Class[] cparams = {};
			java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
			cont.result = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
			cont.result.init( uri1, uri2 );
		} catch (Exception ex) {
			System.err.println("Cannot create alignment "+cont.alignmentClassName+"\n"
					+ex.getMessage());
			usage();
			throw ex;
		}
	}

	private void setOutputFile(Container cont) throws Exception{
		OutputStream stream = null;
		if (cont.filename == null) {
			stream = System.out;
		} else {
			try {
				stream = new FileOutputStream(cont.filename);
			} catch (Exception ex) {ex.printStackTrace();}
			finally {
				if (stream != null) {
					try {
						stream.close ();
					} catch (java.io.IOException e3) {
						System.out.println("I/O Exception");
					}
				}
			}
		}
		cont.writer = new PrintWriter (
				new BufferedWriter(
						new OutputStreamWriter( stream, "UTF-8" )), true);
	}

	private void resultPrinting(Container cont) throws Exception{
		try {
			Object[] mparams = {(Object) cont.writer };
			java.lang.reflect.Constructor[] rendererConstructors =
					Class.forName(cont.rendererClass).getConstructors();
			cont.renderer =
					(AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
		} catch (Exception ex) {
			System.err.println("Cannot create renderer "+cont.rendererClass+"\n"
					+ ex.getMessage());
			usage();
			throw ex;
		}
	}

	private void setParams(Container cont){
		int c;
		FileInputStream fi = null;
		HashMap<Character, Handler> map = new HashMap<Character, Handler>();
		map.put('h', new CaseH());
		map.put('o', new CaseO());
		map.put('p', new CaseP());
		map.put('r', new CaseR());
		map.put('i', new CaseI());
		map.put('a', new CaseA());
		map.put('t', new CaseT());
		map.put('T', new CaseMaiuscT());
		map.put('d', new CaseD());
		map.put('D', new CaseMaiuscD());
    	try {

			fi = new FileInputStream(cont.paramfile);
			c = cont.g.getopt();
			while (c != -1){
				map.get(c).handle(cont);
				c = cont.g.getopt();
			}

			if (cont.debug > 0) {
				cont.params.setProperty( "debug", Integer.toString(cont.debug) );
			} else if ( cont.params.getProperty("debug") != null ) {
				cont.debug = Integer.parseInt( cont.params.getProperty("debug") );
			}
		} catch (Exception ex) {ex.printStackTrace();}
		finally {
			if (fi != null) {
				try {
					fi.close ();
				} catch (java.io.IOException e3) {
					System.out.println("I/O Exception");
				}
			}
		}
	}


    public void usage() {
	System.err.println(Procalign.class.getPackage().getImplementationTitle()+" "+Procalign.class.getPackage().getImplementationVersion());
	System.err.println("\nusage: Procalign [options] URI1 URI2");
	System.err.println("options are:");
	System.err.println("\t--impl=className -i classname\t\tUse the given alignment implementation.");
	System.err.println("\t--renderer=className -r className\tSpecifies the alignment renderer");
	System.err.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.err.println("\t--threshold=double -t double\tFilters the similarities under threshold");
	System.err.println("\t--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span\tmethod for computing the threshold");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
    }
}
