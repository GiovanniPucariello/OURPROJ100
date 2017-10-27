/*
 * Created on 18 2004
 */
package org.ivml.alimo;

/**
 * @author Giorgos Stoilos
 * 
 * This class implements the string matching method proposed in the paper
 * "A String Metric For Ontology Alignment", published in ISWC 2005 
 *
 * Jérôme Euzenat: added normalization
 */


public class ISub {

    public void startingPoint(int p, int j, int k, int l2,int l1, String s1, String s2, int i, int best,int startS1, int startS2,int endS1, int endS2){
        p = j;
        char c1 = s1.charAt(k);
        char c2 = s2.charAt(j);
        for (j++, k++;
             (j < l2) && (k < l1) && (c1 == c2);
             j++, k++){
            c1 = s1.charAt(k+1);
            c2 = s2.charAt(j+1);
        };
        if( k-i > best){
            best = k-i;
            startS1 = i;
            endS1 = k;
            startS2 = p;
            endS2 = j;
        }
    }

    public void forCicle(int j, int l2, int l1, String s1, String s2, int best, int i,int p, int startS1,int startS2,int endS1, int endS2){
        j = 0;
        while (l2 - j > best) {
            int k = i;
            char c1 = s1.charAt(k);
            char c2 = s2.charAt(j);
            for(;(j < l2) && (c1 != c2); j++){
                c2 = s2.charAt(j+1);
            };
            //System.out.println( s1.charAt( k ) + " " + s2.charAt( j ) );

            if (j != l2) { // we have found a starting point
                this.startingPoint(p,j,k,l2,l1,s1,s2,i,best,startS1,startS2,endS1,endS2);
                //best = Math.max(best, k - i);
            }
        }
    }

    public void forS1S2(int j, char[]newString, int i, int startS1, int endS1, int endS2, int startS2, String s1,String s2){
        j=0;
        int length = s1.length();
        for( i=0 ;i<length ; i++ ){
            if( i>=startS1 && i< endS1 )
                continue;
            newString[j++] = s1.charAt( i );
        }

        s1 = new String( newString );

        newString = new char[ s2.length() - ( endS2 - startS2 ) ];
        j=0;
        length = s2.length();
        for( i=0 ;i<length ; i++ ){
            if( i>=startS2 && i< endS2 )
                continue;
            newString[j++] = s2.charAt( i );
        }
        s2 = new String( newString );
    }

    public double hachmacherProduct(double unmatchedS1, double unmatchedS2, double dissimilarity, double commonality, double winklerImprovement ){
        double suma = unmatchedS1 + unmatchedS2;
        double product = unmatchedS1 * unmatchedS2;
        double p = 0.6;   //For 1 it coincides with the algebraic product
        if( (suma-product) == 0 )
            dissimilarity = 0;
        else
            dissimilarity = (product)/(p+(1-p)*(suma-product));

        // Modification JE: returned normalization (instead of [-1 1])
        double result = commonality - dissimilarity + winklerImprovement;
        return (result+1)/2;
    }

    public void whileBlock(String s1, String s2 ,int best, int l1, int l2, double common){
        int length1 = s1.length();
        int length2 = s2.length();
        while( length1 >0 && length2 >0 && best !=0 ){
            best = 0;

            l1 = s1.length();
            l2 = s2.length();

            int i = 0; // iterates through s1
            int j = 0; // iterates through s2

            int startS2 = 0;
            int endS2 = 0;
            int startS1 = 0;
            int endS1 = 0;
            int p=0;

            for( i = 0; (i < l1) && (l1 - i > best); i++) {
                this.forCicle(j,l2,l1,s1,s2,best,i,p,startS1,startS2,endS1,endS2);
            }

            char[] newString = new char[ s1.length() - (endS1 - startS1) ];

            this.forS1S2(j,newString,i,startS1,endS1,endS2,startS2,s1,s2);

            if( best > 2 )
                common += best;
            else
                best = 0;
            length1 = s1.length();
            length2 = s2.length();
            //System.out.println( s1 + ":" + s2 );
            //System.out.println( "StartS1 : " + startS1 + " EndS1: " + endS1 );
            //System.out.println( "StartS2 : " + startS2 + " EndS2: " + endS2 );
        }
    }

	public double score( String st1 , String st2 ){
		
		// JE: This should throw an error
		if ( st1 == null || st2 == null ) return -1;
		
		String s1 = st1.toLowerCase();
		String s2 = st2.toLowerCase();
		
		s1 = normalizeString( s1 , '.' );
		s2 = normalizeString( s2 , '.' );
		s1 = normalizeString( s1 , '_' );
		s2 = normalizeString( s2 , '_' );
		s1 = normalizeString( s1 , ' ' );
		s2 = normalizeString( s2 , ' ' );
 
		int l1 = s1.length(); // length of s
		int l2 = s2.length(); // length of t
		
		int L1 = l1;
		int L2 = l2;

		if ((L1 == 0) && (L2 == 0)) return 1;
		// Modification JE: giorgos put -1 instead of 0
		if ((L1 == 0) || (L2 == 0)) return 0; 
		
		double common = 0;
		int best = 2;

		int max = Math.min(l1, l2); // the maximal length of a subs
	
		this.whileBlock(s1,s2,best,l1,l2,common);

		double commonality = 0;
		double scaledCommon = (2*common)/(L1+L2);
		commonality = scaledCommon;

		double winklerImprovement = winklerImprovement( st1 , st2 , commonality );
		double dissimilarity = 0;

		double rest1 = L1 - common;
		double rest2 = L2 - common;

		double unmatchedS1 = Math.max( rest1 , 0 );
		double unmatchedS2 = Math.max( rest2 , 0 );
		unmatchedS1 = rest1/L1;
		unmatchedS2 = rest2/L2;
		
		/**
		 * Hamacher Product
		 */
		return this.hachmacherProduct(unmatchedS1,unmatchedS2,dissimilarity,commonality,winklerImprovement);
	}
	
	private double winklerImprovement( String s1 , String s2 , double commonality ){
		
		int i;
		//int n = Math.min( 4 , Math.min( s1.length() , s2.length() ) );
		int n = Math.min( s1.length() , s2.length() );
		for( i=0 ; i<n ; i++ )
			if( s1.charAt( i ) != s2.charAt( i ) )
				break;
		
		double commonPrefixLength = Math.min( 4 , i );
		//double commonPrefixLength = i;
		double winkler = commonPrefixLength*0.1*(1-commonality);

		return winkler;
	}

	/* (non-Javadoc)
	 * @see com.wcohen.ss.AbstractStringDistance#explainScore(com.wcohen.ss.api.StringWrapper, com.wcohen.ss.api.StringWrapper)
	 */
	public String explainScore(String s, String t) {
		return null;
	}
	
	public String normalizeString( String str , char remo ){
		
		StringBuffer strBuf = new StringBuffer();
		int j=0;
		int length = str.length();
		for( int i=0 ; i<length ; i++ ){
			if( str.charAt( i ) != remo )
				strBuf.append( str.charAt( i ) );
		}
		return strBuf.toString();		
	}
}
