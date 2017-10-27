/**
 *   Copyright 2008-2010 INRIA, Université Pierre Mendès France
 *   
 *   DegreeOfAgreement.java is part of OntoSim.
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
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * class DegreeOfAgreement
 */
public class DegreeOfAgreement {


    public static void testDegreeOfAgreement(String[] args) {
		if (args[0].equals("-r")) {
			argsCaseR();
		}
		else {
			argsCaseNotR();
		}
	}

    private static void argsCaseR(){
		PrintWriter logMatrix = null;
		ArrayList<ArrayList<String>> degrees;
		StringTokenizer st;
		ArrayList<String> measure;// = new ArrayList<String>();
		String[] out;
		String fileName;
    	//String[] headers = new String[91];
		File file = new File(".");
		out = file.list();
		//robustness scenario - for comparing each degradated measure 10 times with non-degradated one
		int j=0;
		for (int i=0; i<=out.length-1; i++) {
			//load closeness_matrixes from files
			fileName = out[i];
			degrees = new ArrayList<>();
			if (fileName.matches(".*.cls$")) {
				System.out.println("for robustness "+fileName.substring(0,fileName.indexOf(".")));
				System.out.println(fileName.substring(0,fileName.indexOf(".")));
				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader (fileName));
					String s="";
					//1st row headers
					//in.readLine();
					//int h=0;
					String line = in.readLine();
					while ((s = line) != null) {
						measure = new ArrayList<String>();
						st = new StringTokenizer(s,",");
						//headers[h++]=st.nextToken();
						st.nextToken();
						boolean hasMoreToken = st.hasMoreTokens();
						while(hasMoreToken) {
							measure.add(st.nextToken());
							hasMoreToken = st.hasMoreTokens();
						}
						degrees.add(measure);
						line = in.readLine();
					}
					//control printing:
			/*
			  for (ArrayList<String> ar : degrees) {
			  for(String ss : ar) {
			  System.out.println(ss+"|");
			  }
			  }
			*/
					//compare each degradation step (10%,20%,...) with non-degradated variant (0. position in degrees)
					logMatrix = new PrintWriter(new FileWriter(fileName.substring(0,fileName.indexOf("."))));
					//prepare header
					for(int r=1;r<=10;r++) logMatrix.print(",-");
					logMatrix.println(",avg,std");
					logMatrix.print("deg10%");
					//end of header
					compareDegradatedMeasureWithNondegradated(degrees, logMatrix);

					//end of computation one degree_matrix comparing degradated vs. nondegradated variants of measures
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(Exception e) {
					e.printStackTrace();
				} finally {
					closeBufferedReader(in);
					closePrintWriter(logMatrix);
				}
				logMatrix.close();
			}
		}
		j++;
	}

	private static void compareDegradatedMeasureWithNondegradated(ArrayList<ArrayList<String>> degrees, PrintWriter logMatrix ){
		int agreed=0;
		double sum=0;//for computation average and standard deviation
		double degree;
		double[] values = new double[10];//for storing degree values enable to count standard deviation
    	int size = degrees.size();
		for(int k=1;k<size;k++) {
			//compare degradated measure with nondegradated
			agreed=0;
			int sizeK = degrees.get(k).size();
			for(int p=0;p<sizeK;p++) {
				if(degrees.get(k).get(p).equals(degrees.get(0).get(p))) {
					agreed++;
				}
			}
			//#agreed/#pair of ontologies
			degree=(double)agreed/degrees.get(k).size();
			if (k<=10) values[k-1]=degree;
			else values[(k%10)]=degree;
			//System.out.print(","+degree);
			logMatrix.print(","+degree);
			sum+=degree;
			//System.out.println("("+k+",k,"+l+"l,"+agreed+" agreed)");
			//System.out.println();
			if ((k % 10) == 0) {
				double sd = standardDeviation(sum, values);
				//end of computation standard deviation
				logMatrix.print(","+sd);//standard deviation
				logMatrix.println();
				//printing header
				if (k < 81)
					logMatrix.print("deg"+(k+10)+"%");
				else if (k < 91) logMatrix.print("deg100%");
				values = new double[10];
				sum=0;
			}
		}
	}

	private static double standardDeviation(double sum, double[] values ){
		double average = sum/10;
		double sd=0;
		for (double d : values) {
			sd+=(d-average)*(d-average);
		}
		sd=Math.sqrt(sd/10);
		return sd;
	}

	private static void closeBufferedReader(BufferedReader in){
		if (in != null) {
			try {
				in.close ();
			} catch (java.io.IOException e3) {
				System.out.println("I/O Exception");
			}
		}
	}

	private static void closePrintWriter(PrintWriter logMatrix){
		if (logMatrix != null) {
			try {
				logMatrix.close ();
			} catch (Exception e3) {
				System.out.println("PrintWriter Exception");
			}
		}
	}

	public static void argsCaseNotR(){
		//for comparing each-to-each measure without any degradation (-c), we see directly their agreement, or clusters
		//out = new String[1];
		//out[0]="degree1";
		//System.out.println(fileName.substring(0,fileName.indexOf(".")));
		PrintWriter logMatrix = null;
		ArrayList<ArrayList<String>> degrees = new ArrayList<>();
		StringTokenizer st;
		ArrayList<String> measure;// = new ArrayList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader ("degree1"));
			String s="";
			//1st row headers
			//in.readLine();
			String line = in.readLine();
			while (line != null) {
				measure = new ArrayList<String>();
				st = new StringTokenizer(s,",");
				st.nextToken();
				boolean hasMoreTokens = st.hasMoreTokens();
				while(hasMoreTokens) {
					measure.add(st.nextToken());
					hasMoreTokens = st.hasMoreTokens();
				}
				degrees.add(measure);
				line = in.readLine();
			}
			//for one closeness_matrix compute degree of agreement between all measures
			logMatrix = new PrintWriter(new FileWriter("degree1.dgr"));
			compareTwoMeasure(degrees, logMatrix);
			//end of computation one degree_matrix
		}
		catch(IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(in);
			closePrintWriter(logMatrix);
		}
		logMatrix.close();
	}

	private static void compareTwoMeasure(ArrayList<ArrayList<String>> degrees, PrintWriter logMatrix ){
		int agreed=0;
		double degree;
		int size = degrees.size();
		for(int k=0;k<size;k++) {
			for(int l=0;l<size;l++) {
				//if (l<=k) System.out.print(",-");
				if (l<=k) logMatrix.print(",-");
				else {
					//compare two measures and compute degree of agreement
					agreed=0;
					int sizeK = degrees.get(k).size();
					for(int p=0;p<sizeK;p++) {
						if(degrees.get(k).get(p).equals(degrees.get(l).get(p))) {
							agreed++;
						}
					}
					//#agreed/#pair of ontologies
					degree=(double)agreed/degrees.get(k).size();
					//System.out.print(","+degree);
					logMatrix.print(","+degree);
					//System.out.println("("+k+",k,"+l+"l,"+agreed+" agreed)");
				}
			}
			//System.out.println();
			logMatrix.println();
		}
	}
}
