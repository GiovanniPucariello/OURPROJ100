package fr.inrialpes.exmo.ontosim.vector;

import fr.inrialpes.exmo.ontosim.OntoSimException;

import java.util.Arrays;

/**
 * class KendallTau
 */
public class KendallTau extends VectorMeasure {
	/**
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
    @Override
    public double getMeasureValue(double[] v1, double[] v2) {
	int sum=0;
	for (int i=0 ; i<v1.length; i++) {
	    for (int j=i+1 ; j<v1.length ; j++) {
		double vij=(v1[i]-v1[j])*(v2[i]-v2[j]);
		if (vij>0) sum++;
		else if (vij<0) sum--;
	    }
	}

	int sumT1 = calculate(v1);
	int sumT2 = calculate(v2);
	
	//System.out.println(sumT1+" - "+sumT2);
	int over=(v1.length*(v1.length-1));;
	
	return ((double)2*sum)/Math.sqrt((over-sumT1)*(over-sumT2));
    }

	/**
	 *
	 * @param vector
	 * @return
	 */
    private int calculate(double[] vector){
		double oldv=Double.NaN;
		int sumT=0;
		int nbT=0;
		double[] vectorCopy = vector.clone();
		Arrays.sort(vectorCopy);
		for (double v : vectorCopy) {
			if (v==oldv) nbT++;
			else  {
				sumT+=nbT*(nbT+1);
				nbT=0;
				oldv=v;
			}
		}
		return sumT;
	}

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
    @Override
    public double getDissim(double[] o1, double[] o2) {
	throw new OntoSimException("Not a dissimilarity");
    }

	/**
	 *
	 * @return
	 */
    @Override
    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return TYPES.other;
    }

	/**
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
    @Override
    public double getSim(double[] o1, double[] o2) {
	throw new OntoSimException("Not a similarity");
    }

}
