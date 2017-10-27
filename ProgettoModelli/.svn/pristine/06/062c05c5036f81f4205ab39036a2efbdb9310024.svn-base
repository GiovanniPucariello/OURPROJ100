/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   BatchMeasures.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package fr.inrialpes.exmo.ontosim.util;

import java.io.*;
import java.util.*;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.align.gen.NetworkAlignmentDropper;
import fr.inrialpes.exmo.align.gen.NetworkAlignmentWeakener;
import fr.inrialpes.exmo.align.gen.OntologyNetworkWeakener;
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.AlignmentSpaceMeasure;
import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.VectorSpaceMeasure;
import fr.inrialpes.exmo.ontosim.vector.model.DocumentCollection;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
<pre>
java fr.inrialpes.exmo.ontosim.util.BatchMeasures [options] ontodir measurefile
</pre>

where the options are:
<pre>
    --aligndir=dirname -a dirname Use alignments contained in this directory (load all .rdf or .owl files)
    --output=filename -o filename Output the results in filename (stdout by default)
    --factory=[OWL|JENA|OntologyFactory subclass] -f [OWL|JENA|OntologyFactory subclass] Use the specified factory for loading ontologies
    --weaken=n -w n Suppress n% of the correspondences at random in all alignments
    --threshold -t Tells if the correspondences are suppressed at random of by suppressing the n% of lower confidence
    --help -h                       Print this message
</pre>

<CODE>ontodir</CODE> is a directory which contains only the ontologies to compare (ontologies filename must finish by extension .owl or .rdf)

<CODE>measurefile</CODE> is a text file where each line is the name of a measure to compute. examples : 
 	VectorSpaceMeasure(fr.inrialpes.exmo.ontosim.vector.CosineVM,vector.model.DocumentCollection$WEIGHT=TFIDF)
	OntologySpaceMeasure(set.MaxCoupling(entity.EntityLexicalMeasure))

@author jerome D
 */

//07-06-10
//for storing pair of ontology and its similarity value to given ontology
class Pair {
	String ontology="";
	double sim=0.0;
	
	public Pair(String ontology, double sim) {
		this.ontology=ontology;
		this.sim=sim;
	}
	
	public String toString() {
		return this.ontology+":"+this.sim+" ";
	}
}

/**
 * class BatchMeasures
 */
public class BatchMeasures {

    private final static String SEP=" & ";
    public final static REFilenameFilter filter=new REFilenameFilter(".*\\.((rdf)|(owl))");


    public static void testBatchMeasures(String[] args) throws Exception {
		Container cont = new Container();
		cont.weakenT=false; // is weakening random or by threshold
		cont.weaken=-1;         // do I weaken the network
		cont.drop=-1;           // do I drop alignments from the network
		cont.invert = false; // do the invertion closure of the network before
		cont.close_matrix = false; //OZ,18-08-09:do matrix of closest ontologies for all ontologies and for all measures
		cont.robustness = false; //OZ,18-08-09:prepare matrixes for robustness estimation of all measures
		cont.max_sim = 0.0; //OZ:for getting the highest value for each measure and each ontology
		cont.current_sim = 0.0; //OZ
		cont.logAppend=null; //OZ, for printing closeness_matrixes for all measures where degradation is gradullay applied
		cont.threshold=0.0;//OZ, for printing purposes

		String basePackage="fr.inrialpes.exmo.ontosim.";
		cont.alignDir=null;
		// output device
		cont.out= System.out;

		LongOpt[] longopts = new LongOpt[10];

		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("outputfile", LongOpt.REQUIRED_ARGUMENT, null, 'o');
		longopts[2] = new LongOpt("aligndir", LongOpt.REQUIRED_ARGUMENT, null, 'a');
		longopts[3] = new LongOpt("factory", LongOpt.REQUIRED_ARGUMENT, null, 'f');
		longopts[4] = new LongOpt("weaken", LongOpt.REQUIRED_ARGUMENT, null, 'w');
		longopts[5] = new LongOpt("threshold", LongOpt.NO_ARGUMENT, null, 't');
		longopts[6] = new LongOpt("invert", LongOpt.NO_ARGUMENT, null, 'i');
		longopts[7] = new LongOpt("drop", LongOpt.REQUIRED_ARGUMENT, null, 'd');
		longopts[8] = new LongOpt("close_matrix", LongOpt.NO_ARGUMENT, null, 'c');
		longopts[9] = new LongOpt("robustness", LongOpt.NO_ARGUMENT, null, 'r');

		cont.opts = new Getopt(BatchMeasures.class.getCanonicalName(), args, "rcihf:a:o:w:d:t", longopts);
		int c = cont.opts.getopt();

		HashMap<Character, Handler> map = new HashMap<Character, Handler>();
		map.put('h', new CaseH());
		map.put('f', new CaseF());
		map.put('a', new CaseA());
		map.put('o', new CaseO());
		map.put('i', new CaseI());
		map.put('c', new CaseC());
		map.put('r', new CaseR());
		map.put('w', new CaseW());
		map.put('d', new CaseD());
		map.put('t', new CaseT());

		while (c != -1) {
			map.get(c).handle(cont);
			c = cont.opts.getopt();
		}

		// test parameters
		int a = cont.opts.getOptind();
		if (args.length<a+1) {
			printUsage();

			System.exit(-1);
		}

		cont.ontoFactory = OntologyFactory.getFactory();
		cont.alignFiles=null;
		cont.on=null;

		if (cont.alignDir!=null) {
			initializeAlignDir(cont);
		}

		cont.measures = new ArrayList<Measure<LoadedOntology<?>>>();
		cont.br = null;
		try {
			addAlignment(cont, args);
		} catch (Exception ex) {ex.printStackTrace();}
		finally {
			closeBufferedReader(cont.br);
		}
		if (!cont.close_matrix) cont.out.println();

		cont.br.close();

		if (!cont.close_matrix) {
			notClosestMatrix(cont);
		}
		else {//OZ,18-08-09, header and rows names
			//OZ:header
	  /*if (!robustness) {
		  out.print(SEP);
		  for (int k=0 ; k<ontFiles.length ; k++) {
			  LoadedOntology<?> o1 = ontoFactory.loadOntology(ontFiles[k].toURI());
			  if (!robustness) out.print(o1.getURI()+SEP);
		  }
		out.println();
	  }*/

			firstIteration(cont);
			cont.out.close();
		}
	}

	private static class Container{
		public Getopt opts;
		public File alignDir;
		public PrintStream out;
		public boolean invert;
		public boolean close_matrix;
		public boolean robustness;
		public int weaken;
		public boolean weakenT;
		public int drop;
		public double max_sim;
		public double current_sim;
		public BufferedWriter logAppend;
		public double threshold;
		public OntologyFactory ontoFactory;
		public File[] alignFiles;
		public OntologyNetwork on;
		public BufferedReader br;
		public List<Measure<LoadedOntology<?>>> measures;
		public File[] ontFiles;
	}

    private interface Handler{
    	void handle(Container cont);
	}

	private static class CaseH implements Handler{
		public void handle(Container cont){
			printUsage();
		}
	}

	private static class CaseF implements Handler{
		public void handle(Container cont){
			if (cont.opts.getOptarg().equals("JENA"))
				OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory");
			else if (cont.opts.getOptarg().equals("OWLAPI1"))
				OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi10.OWLAPIOntologyFactory");
			else {
				try {
					OntologyFactory.setDefaultFactory(cont.opts.getOptarg());
				}
				catch (Exception e) {
					System.err.println("No such ontology factory available, it will use "+OntologyFactory.getDefaultFactory()+" instead");
				}
			}
		}
	}

	private static class CaseA implements Handler{
		public void handle(Container cont){
			cont.alignDir = new File( cont.opts.getOptarg() );
			if (! cont.alignDir.isDirectory()) {
				System.err.println(cont.opts.getOptarg()+" is not a directory");
			}
		}
	}

	private static class CaseO implements Handler{
		public void handle(Container cont){
			cont.out = new PrintStream(new File(cont.opts.getOptarg()));
		}
	}

	private static class CaseI implements Handler{
		public void handle(Container cont){
			cont.invert = true;
		}
	}

	private static class CaseC implements Handler{
		public void handle(Container cont){
			cont.close_matrix = true;
		}
	}

	private static class CaseR implements Handler{
		public void handle(Container cont){
			cont.robustness = true;
			cont.close_matrix = true;
		}
	}

	private static class CaseW implements Handler{
		public void handle(Container cont){
			try {
				cont.weaken=Integer.parseInt(cont.opts.getOptarg());
				//if (weaken==100) weaken=99; //OZ,21-08-09: workaround, weaken=100 does not work now
				//out.print(weaken);
				if (cont.weaken < 0 || cont.weaken >100) {
					System.err.println(cont.opts.getOptarg()+" value must be between 0 and 100, it will not weaken alignment network");
					cont.weaken=-1;
				}
			}
			catch (NumberFormatException e) {
				System.err.println(cont.opts.getOptarg()+" is not a valid number, it will not weaken alignment network");
			}
		}
	}

	private static class CaseD implements Handler{
		public void handle(Container cont){
			try {
				cont.drop=Integer.parseInt(cont.opts.getOptarg());
				//out.print(drop);
				if (cont.drop < 0 || cont.drop >100) {
					System.err.println(cont.opts.getOptarg()+" value must be between 0 and 100, it will not weaken alignment network");
					cont.drop=-1;
				}
			}
			catch (NumberFormatException e) {
				System.err.println(cont.opts.getOptarg()+" is not a valid number, it will not weaken alignment network");
			}
		}
	}

	private static class CaseT implements Handler{
		public void handle(Container cont){
			cont.weakenT=true;
		}
	}

	private static void initializeAlignDir(Container cont){
		cont.alignFiles = cont.alignDir.listFiles(filter);
		AlignmentParser ap = new AlignmentParser(0);
		cont.on = new BasicOntologyNetwork();
		for (File af : cont.alignFiles) {
			ap.initAlignment(null);
			Alignment al = ap.parse(af.toURI().toString());
			cont.on.addAlignment(al);
		}
	    /*
	    for(Alignment al : on.getAlignments()) {
	    	out.print(al.getOntology1URI()+" "+al.getOntology2URI());
	    }*/
		// not clear if this action must be done before or after...
		if (cont. invert ) cont.on.invert();
		if (cont.weaken>0) {
			cont.threshold = ((double)cont.weaken)/100;
			OntologyNetworkWeakener weakener = new NetworkAlignmentWeakener();
			Properties p = new Properties();
			p.put("threshold", cont.weakenT);
			cont.on = weakener.weaken(cont.on, cont.threshold, p);
		}
		if (cont.drop>0) {
			cont.threshold = ((double)cont.drop)/100;
			OntologyNetworkWeakener dropper = new NetworkAlignmentDropper();
			cont.on = dropper.weaken(cont.on, cont.threshold, null);
		}
	}

	private static void closeBufferedReader(BufferedReader br ){
		if (br != null) {
			try {
				br.close ();
			} catch (java.io.IOException e3) {
				System.out.println("I/O Exception");
			}
		}
	}

	private static void addAlignment(Container cont, String[] args) throws IOException {
		MeasureFactory mf = new MeasureFactory(true);
		int a = cont.opts.getOptind();
		File ontDir = new File(args[a]);
		File mFile= new File(args[a+1]);
		cont.ontFiles = ontDir.listFiles(filter);
		cont.br = new BufferedReader(new FileReader(mFile));
		String line = cont.br.readLine();
		if (!cont.close_matrix)
			cont.out.print(SEP);
		while (line!=null) {
			if (line.charAt(0)=='#') continue;
			try {
				if (!cont.close_matrix) cont.out.print(SEP+line);
				Measure<LoadedOntology<?>> m = mf.getOntologyMeasure(line,basePackage);
				cont.measures.add(m);

				// case VectorSpaceMeasure with TFIDF weights: add all ontologies before
				caseVector(cont, m);

				// case of AlignmentSpaceMeasure : add all alignments
				caseAlignment(cont, m);
			}
			catch (OntoSimException e) {
				e.printStackTrace();
			}
			line = cont.br.readLine();
		}
	}

	private static void caseVector(Container cont, Measure<LoadedOntology<?>> m ){
		if (m instanceof VectorSpaceMeasure && ((VectorSpaceMeasure) m).getVectorType()==DocumentCollection.WEIGHT.TFIDF) {
			for (File of : cont.ontFiles)
				((VectorSpaceMeasure)m).addOntology(cont.ontoFactory.loadOntology(of.toURI()));
		}
	}

	private static void caseAlignment(Container cont, Measure<LoadedOntology<?>> m ){
		if (m instanceof AlignmentSpaceMeasure && cont.alignFiles!=null) {
			((AlignmentSpaceMeasure<?>) m).setAlignmentSpace(cont.on);
		}
	}

	private static void notClosestMatrix(Container cont){
		for (int i=0 ; i<cont.ontFiles.length ; i++) {
			LoadedOntology<?> o1 = cont.ontoFactory.loadOntology(cont.ontFiles[i].toURI());
			for (int j=i ; j<cont.ontFiles.length ; j++) {
				LoadedOntology<?> o2 = cont.ontoFactory.loadOntology(cont.ontFiles[j].toURI());
				cont.out.print(o1.getURI()+SEP+o2.getURI());
				for (Measure<LoadedOntology<?>> m : cont.measures) {
					if (m !=null)
						cont.out.print(SEP+m.getSim(o1, o2));
					else
						cont.out.print(SEP+"err");
				}
				cont.out.println();
			}
		}
		cont.out.close();
	}

	private static void matrixRobustness(Container cont, Measure<LoadedOntology<?>> m ){
		//out = new PrintStream(new File(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)));
		try {
			cont.logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+".cls", true));
			//logAppend.write("\n");//1st row emtpy for header
					/*
					if (drop>0) {
						logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+"Drop", true));
					}
					if (weaken>0) {
						logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+"Weaken", true));
					}
					if (weaken==-1&&drop==-1) {
						System.err.println("robustness feature must be used with --drop=n or --weaken=n");
						break; //OZ:probably better way how to stop processing here
						//throw NullPointerException;
					}
					*/
			cont.logAppend.write(new Double(cont.threshold).toString());
			cont.logAppend.write(SEP);

		} catch (Exception ex) {ex.printStackTrace();}
		finally {
			if (cont.logAppend != null) {
				try {
					cont.logAppend.close ();
				} catch (java.io.IOException e3) {
					System.out.println("I/O Exception");
				}
			}
		}
	}

	private static void firstIteration(Container cont){
		for (Measure<LoadedOntology<?>> m : cont.measures) {//1st iteration - measures
			if (cont.robustness) {
				matrixRobustness(cont, m);
			}
			else cont.out.print(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+SEP);

			secondIteration(cont, m);

			if (cont.robustness) cont.logAppend.write("\\\\ \n");
			else cont.out.println("\\\\");
			if (cont.robustness) cont.logAppend.close();
		}
	}

	private static void secondIteration(Container cont, Measure<LoadedOntology<?>> m ){

		for (int i=0 ; i<cont.ontFiles.length ; i++) {//2nd iteration - ontologies (columns)
			cont.max_sim=0.0;
			ArrayList<Pair> meas = new ArrayList<Pair>(15);
			LoadedOntology<?> o1 = cont.ontoFactory.loadOntology(cont.ontFiles[i].toURI());

			thirdIteration(count, m, i, o1);

			//07-06-10, make a sort the pairs ontology+sim according to sim
			Collections.sort(meas,new Comparator<Pair>() {
				public int compare(Pair p1, Pair p2) {
					return -(Double.compare(p1.sim, p2.sim));
				}});
			for(Pair p : meas)
				if (cont.robustness) cont.logAppend.write(p+" ");
				else cont.out.print(p+" ");
			if (cont.robustness) {
				if(i!=(cont.ontFiles.length-1)) cont.logAppend.write(SEP);
			}
			else if(i!=(cont.ontFiles.length-1)) cont.out.print(SEP);
		}
	}

	private static void thirdIteration(Container cont, Measure<LoadedOntology<?>> m, int i, ArrayList<Pair> meas,  LoadedOntology<?> o1){
		for (int j=0 ; j<cont.ontFiles.length ; j++) {//3rd iteration - similarity of each pair
			if (i==j) continue;
			LoadedOntology<?> o2 = cont.ontoFactory.loadOntology(cont.ontFiles[j].toURI());
			if (m !=null) {
				cont.current_sim=m.getSim(o1, o2);
				meas.add(new Pair(o2.getURI().toString(), cont.current_sim));
			}
			else
			if (cont.robustness) cont.logAppend.write(SEP+"err");
			else cont.out.print(SEP+"err");
		}
	}

    public static final void printUsage() {

	System.err.println("java "+BatchMeasures.class.getCanonicalName()+" [options] ontodir measurefile");
	System.err.println("where the options are:");
	System.err.println("\t--aligndir=dirname -a dirname Use alignments contained in this directory (load all .rdf or .owl files)");
	System.err.println("\t--output=filename -o filename Output the results in filename (stdout by default)");
	System.err.println("\t--factory=[OWL|JENA|OntologyFactory subclass] -f [OWL|JENA|OntologyFactory subclass] Use the specified factory for loading ontologies");
	System.err.println("\t--weaken=n -w n Suppress n% of the correspondences at random in all alignments");
	System.err.println("\t--threshold -t Tells if the correspondences are suppressed at random of by suppressing the n% of lower confidence");
	System.err.println("\t--drop=n -d n Suppress n% of the alignments at random in the network");
	System.err.println("\t--close_matrix -c It will generate closeness_matrix for assessing degree of agreement");
	System.err.println("\t--robustness -r It will generate several (degradated) closeness_matrixes for each measure (in combination with -d or -w), robustness");
	System.err.println("\t--invert -i Use the reflexive closure of the network");
	System.err.println("\t--help -h                       Print this message");
	System.err.println("ontodir is a directory which contains only the ontologies to compare (ontologies filename must finish by extension .owl or .rdf)");
	System.err.println("measurefile is a text file where each line is the name of a measure to compute. examples : \n" +
			"\t VectorSpaceMeasure(fr.inrialpes.exmo.ontosim.vector.CosineVM,vector.model.DocumentCollection$WEIGHT=TFIDF) \n" +
			"\t OntologySpaceMeasure(set.MaxCoupling(entity.EntityLexicalMeasure))");
	
    }

}
