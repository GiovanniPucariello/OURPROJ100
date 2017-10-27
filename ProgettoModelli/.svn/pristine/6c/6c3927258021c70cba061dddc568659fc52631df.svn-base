/*
 * $Id: EvalAlign.java 1805 2013-02-08 14:25:15Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2008, 2010-2013
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.cli;

import fr.inrialpes.exmo.align.impl.AlignmentTransformer;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;

//Imported JAVA classes
import java.io.IOException;
import java.lang.Integer;
import java.util.Properties;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

/** A really simple utility that loads and alignment and prints it.
    A basic class for an OWL ontology alignment processing. The processor
    will parse ontologies, align them and renderings the resulting alignment.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.EvalAlign [options] input [output]
    </pre>

    where the options are:
    <pre>
        --alignment=filename -a filename Start from an XML alignment file
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --help -h                       Print this message
    </pre>

    The <CODE>input</CODE> is a filename. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: EvalAlign.java 1805 2013-02-08 14:25:15Z euzenat $
</pre>

@author Jérôme Euzenat
    */

public class EvalAlign {

    public static void testEvalAlign(String[] args) {
	new EvalAlign().run( args );
    }


    public void run(String[] args) {
	Properties params = new Properties();
	Evaluator eval = null;

	Container cont = new Container();

	LongOpt[] longopts = new LongOpt[7];
	cont.debug = 0;
	
	// abcdefghijklmnopqrstuvwxyz?
	// x  x    i      x x x x    x 
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	longopts[4] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	
	Getopt g = new Getopt("", args, "ho:d::i:D:", longopts);

	switchOpt(g, cont, params);
	
	int i = g.getOptind();

	params.setProperty( "debug", Integer.toString( cont.debug ) );
	// debug = Integer.parseInt( params.getProperty("debug") );
	
	if (args.length > i+1 ) {
	    cont.alignName1 = args[i];
	    cont.alignName2 = args[i+1];
	} else {
	    System.err.println("Require two alignement filenames");
	    usage();
	    return;
	}

	if ( cont.debug > 1 ) System.err.println(" Filename"+cont.alignName1+"/"+cont.alignName2);
	loadAlignments(cont);
	boolean totry = true;
	try {
	while ( totry ) {
	    totry = false;

	    eval = createEvaluatorObject(cont);
	    if(eval==null) return;
	    // Compare
		compare(eval, params, totry, cont);
	}
	} catch ( Exception ex ) { ex.printStackTrace(); }

	setOutputFile(cont, eval);
    }

    private void loadAlignments(Container cont){
		try {
			// Load alignments
			AlignmentParser aparser = new AlignmentParser( cont.debug );
			cont.align1 = aparser.parse( cont.alignName1 );
			if ( cont.debug > 0 ) System.err.println(" Alignment structure1 parsed");
			aparser.initAlignment( null );
			cont.align2 = aparser.parse( cont.alignName2 );
			if ( cont.debug > 0 ) System.err.println(" Alignment structure2 parsed");
		} catch ( Exception ex ) { ex.printStackTrace(); }
	}

    private boolean compare(Evaluator eval, Properties params, boolean totry, Container cont) throws AlignmentException {
		try {
			eval.eval(params) ;
		} catch ( AlignmentException aex ) {
			if ( cont.align1 instanceof ObjectAlignment ) {
				throw aex;
			} else {
				try {
					cont.align1 = AlignmentTransformer.toObjectAlignment((URIAlignment) cont.align1);
					cont.align2 = AlignmentTransformer.toObjectAlignment((URIAlignment) cont.align2);
					totry = true;
				} catch ( AlignmentException aaex ) { throw aex; }
			}
		}
		return totry;
	}

    private Evaluator createEvaluatorObject(Container cont) throws AlignmentException {
		Evaluator eval = null;
    	if ( cont.classname != null ) {
    	try {
			Object [] mparams = {(Object)cont.align1, (Object)cont.align2};
			Class<?> oClass = Class.forName("Alignment");
			Class[] cparams = { oClass, oClass };
			Class<?> evaluatorClass =  Class.forName(cont.classname);
			java.lang.reflect.Constructor evaluatorConstructor = evaluatorClass.getConstructor(cparams);
			eval = (Evaluator)evaluatorConstructor.newInstance(mparams);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
			usage();
		}
		} else { eval = new PRecEvaluator( cont.align1, cont.align2 ); }
		return eval;
	}

    private void setOutputFile(Container cont, Evaluator eval ){
		OutputStream stream = null;
		PrintWriter writer = null;
		try {
			if (cont.filename == null) {
				//writer = (PrintStream) System.out;
				stream = System.out;
			} else {
				//writer = new PrintStream(new FileOutputStream(filename));
				stream = new FileOutputStream(cont.filename);
			}
			writer = new PrintWriter (
					new BufferedWriter(
							new OutputStreamWriter( stream, "UTF-8" )), true);
			eval.write( writer );
		} catch ( IOException ex ) {
			ex.printStackTrace();
		} finally {
			closeWriterResource(writer);
			closeStreamResource(stream);
		}
	}

    private void closeStreamResource(OutputStream stream ){
		if (stream != null) {
			try {
				stream.close();
			} catch (java.io.IOException e3) {
				System.out.println("I/O Exception");
			}
		}
	}

	private void closeWriterResource(PrintWriter writer ){
		if (writer != null) {
			try {
				writer.close();
			} catch (Exception e3) {
				System.out.println("PrintWriter Exception");
			}
		}
	}

    private class Container{
    	public String filename;
    	public String classname;
    	public Alignment align1;
    	public Alignment align2;
		public String alignName1;
		public String alignName2;
		public int debug;
		public String arg;
	}

    private void switchOpt(Getopt g, Container cont, Properties params ){
		int c = g.getopt();
		while (c  != -1) {
			switch(c) {
				case 'h':
					usage();
				case 'o':
		/* Output */
					cont.filename = g.getOptarg();
					break;
				case 'i':
		/* Evaluator class */
					cont.classname = g.getOptarg();
					break;
				case 'd':
		/* Debug level  */
					cont.arg = g.getOptarg();
					if ( cont.arg != null ) cont.debug = Integer.parseInt(cont.arg.trim());
					else cont.debug = 4;
					break;
				case 'D' :
		/* Parameter definition */

					break;
			}
			c = g.getopt();
		}
	}

	private void handleCaseD(Getopt g, Container cont, Properties params ){
		cont.arg = g.getOptarg();
		int index = cont.arg.indexOf('=');
		if ( index != -1 ) {
			params.setProperty( cont.arg.substring( 0, index),
					cont.arg.substring(index+1));
		} else {
			System.err.println("Bad parameter syntax: "+g);
			usage();
			System.exit(0);
		}
	}

    public void usage() {
	System.err.println("usage: EvalAlign [options] file1 file2");
	System.err.println("options are:");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t--impl=className -i classname\t\tUse the given evaluator implementation.");
	System.err.println("\t--output=filename -o filename\tOutput the result in filename");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.print("\n"+EvalAlign.class.getPackage().getImplementationTitle()+" "+EvalAlign.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: EvalAlign.java 1805 2013-02-08 14:25:15Z euzenat $)\n");

    }
}
