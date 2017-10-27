/*
 * $Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2008, 2011-2013
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

/* 
*/
package fr.inrialpes.exmo.align.cli;

//Imported JAVA classes
import java.io.*;
import java.lang.Integer;
import java.lang.Double;
import java.util.HashMap;
import java.util.Properties;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A really simple utility that loads and alignment and prints it.
    A basic class for ontology alignment processing.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.ParserPrinter [options] input [output]
    </pre>

    where the options are:
    <pre>
	--renderer=className -r className  Use the given class for output.
	--parser=className -p className  Use the given class for input.
        --inverse -i              Inverse first and second ontology
	--threshold=threshold -t threshold      Trim the alugnment with regard to threshold
	--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span      Method to use for triming
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --help -h                       Print this message
    </pre>

    The <CODE>input</CODE> is a filename. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $
</pre>

    */

public class ParserPrinter {

    public static void testParserPrinter(String[] args) {
	try { new ParserPrinter().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
    	Container cont = new Container();
    	cont.result = null;
		cont.initName = null;
		cont.filename = null;
		cont.dirName = null;
		cont.writer = null;
		cont.renderer = null;
		cont.longopts = new LongOpt[11];
		cont.debug = 0;
		cont.rendererClass = null;
		cont.parserClass = null;
		cont.inverse = false;
		cont.embedded = false;
		cont.threshold = 0;
		cont.cutMethod = "hard";
		cont.params = new Properties();

		cont.longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		cont.longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
		cont.longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
		cont.longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
		cont.longopts[4] = new LongOpt("parser", LongOpt.REQUIRED_ARGUMENT, null, 'p');
		cont.longopts[5] = new LongOpt("inverse", LongOpt.NO_ARGUMENT, null, 'i');
		cont.longopts[6] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
		cont.longopts[7] = new LongOpt("cutmethod", LongOpt.REQUIRED_ARGUMENT, null, 'T');
		cont.longopts[8] = new LongOpt("embedded", LongOpt.NO_ARGUMENT, null, 'e');
		cont.longopts[9] = new LongOpt("dirName", LongOpt.REQUIRED_ARGUMENT, null, 'c');
		// Is there a way for that in LongOpt ???
		cont.longopts[10] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

		cont.g = new Getopt("", args, "ehio:t:T:d::r:p:c:D:", cont.longopts);

		int c = cont.g.getopt();

		HashMap<Character, Handler> map = new HashMap<Character, Handler>();
		map.put('h', new CaseH());
		map.put('i', new CaseI());
		map.put('e', new CaseE());
		map.put('o', new CaseO());
		map.put('c', new CaseC());
		map.put('r', new CaseR());
		map.put('p', new CaseP());
		map.put('t', new CaseT());
		map.put('T', new CaseMaiuscT());
		map.put('d', new CaseD());
		map.put('D', new CaseMaiuscD());


	while (c != -1){
		map.get(c).handle(cont);
		c = cont.g.getopt();
	}
	
	int i = cont.g.getOptind();
	
	if (args.length > i ) {
		cont.initName = args[i];
	} else {
	    System.out.println("Require the alignement filename");
	    usage();
	    return;
	}

	if ( cont.debug > 1 ) System.err.println(" Filename"+cont.initName);

	try {
        parser(cont);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

	private class Container{
		public Getopt g;
		public String arg;
		public Alignment result;
		public String initName;
		public String filename;
		public String dirName;
		public PrintWriter writer;
		public AlignmentVisitor renderer;
		public LongOpt[] longopts;
		public int debug;
		public String rendererClass;
		public String parserClass;
		public boolean inverse;
		public boolean embedded;
		public double threshold;
		public String cutMethod;
		public Properties params;

	}

	private interface Handler{
		void handle(Container cont);
	}

	private class CaseH implements Handler{
		public void handle(Container cont){
			usage();
		}
	}

	private class CaseI implements Handler{
		public void handle(Container cont){
			cont.inverse = true;
		}
	}

	private class CaseE implements Handler{
		public void handle(Container cont){
			cont.embedded = true;
		}
	}

	private class CaseO implements Handler{
		public void handle(Container cont){
			/* Write warnings to stdout rather than stderr */
			cont.filename = cont.g.getOptarg();
		}
	}

	private class CaseC implements Handler{
		public void handle(Container cont){
			cont.dirName = cont.g.getOptarg();
		}
	}

	private class CaseR implements Handler{
		public void handle(Container cont){
			/* Use the given class for rendernig */
			cont.rendererClass = cont.g.getOptarg();
		}
	}

	private class CaseP implements Handler{
		public void handle(Container cont){
			/* Use the given class for rendernig */
			cont.parserClass = cont.g.getOptarg();
		}
	}

	private class CaseT implements Handler{
		public void handle(Container cont){
			/* Threshold */
			cont.threshold = Double.parseDouble(cont.g.getOptarg());
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

	private void parser(Container cont) throws UnsupportedEncodingException, AlignmentException {
		// Create parser
		createParser(cont);

		// Set output file
		setOutputFile(cont);

		// Thresholding
		if (cont.threshold != 0) cont.result.cut( cont.cutMethod, cont.threshold );

		// Create renderer
		createParser(cont);

		// Render the alignment
		try {
			cont.result.render( cont.renderer );
		} catch ( AlignmentException aex ) {
			throw aex;
		} finally {
			cont.writer.flush();
			cont.writer.close();
		}
	}

	private void createParser(Container cont){
		AlignmentParser aparser = null;
		if ( cont.parserClass == null ) aparser = new AlignmentParser( cont.debug );
		else {
			try {
				Object[] mparams = { (Object)cont.debug };
				java.lang.reflect.Constructor[] parserConstructors =
						Class.forName(cont.parserClass).getConstructors();
				aparser = (AlignmentParser) parserConstructors[0].newInstance(mparams);
			} catch (Exception ex) {
				System.err.println("Cannot create parser " +
						cont.parserClass + "\n" + ex.getMessage() );
				usage();
				return;
			}
		}

		aparser.setEmbedded( cont.embedded );
		cont.result = aparser.parse( cont.initName );
		if ( cont.debug > 0 ) System.err.println(" Alignment structure parsed");
	}

	private void setOutputFile(Container cont){
		OutputStream stream = null;
		if (cont.filename == null) {
			//writer = (PrintStream) System.out;
			stream = System.out;
		}
		else {
			//writer = new PrintStream(new FileOutputStream(filename));
			try {
				stream = new FileOutputStream(cont.filename);
			} catch (Exception ex) {ex.printStackTrace();}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (java.io.IOException e3) {
						System.out.println("I/O Exception");
					}
				}
			}
		}
		if ( cont.dirName != null ) {
			File f = new File(cont.dirName);
			f.mkdir();
			cont.params.setProperty( "dir", cont.dirName );
			cont.params.setProperty( "split", "true" );
		}
		cont.writer = new PrintWriter (
				new BufferedWriter(
						new OutputStreamWriter( stream, "UTF-8" )), true);

		if ( cont.inverse ) cont.result = cont.result.inverse();
	}

	private void createRenderer(Container cont){
		if ( cont.rendererClass == null ) cont.renderer = new RDFRendererVisitor( cont.writer );
		else {
			try {
				Object[] mparams = {(Object) cont.writer };
				java.lang.reflect.Constructor[] rendererConstructors =
						Class.forName(cont.rendererClass).getConstructors();
				cont.renderer =
						(AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
			} catch (Exception ex) {
				System.err.println("Cannot create renderer " +
						cont.rendererClass + "\n" + ex.getMessage() );
				usage();
				return;
			}
		}

		cont.renderer.init( cont.params );
	}

    public void usage() {
	System.out.println("usage: ParserPrinter [options] URI");
	System.out.println("options are:");
	//System.out.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level ,");
	System.out.println("\t--renderer=className -r\t\tUse the given class for output.");
	System.out.println("\t--parser=className -p\t\tUse the given class for input.");
	System.out.println("\t--embedded -e\t\tRead the alignment as embedded in XML file");
	System.out.println("\t--inverse -i\t\tInverse first and second ontology");
	System.out.println("\t--threshold=threshold -t threshold\t\tTrim the alugnment with regard to threshold");
	System.out.println("\t--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span\t\tMethod to use for triming");
	System.out.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.out.println("\t--outputDir=dirName -c dirName\tSplit the output in a directory (SPARQL)");
	System.out.println("\t--help -h\t\t\tPrint this message");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.print("\n"+ParserPrinter.class.getPackage().getImplementationTitle()+" "+ParserPrinter.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $)\n");

    }
}
