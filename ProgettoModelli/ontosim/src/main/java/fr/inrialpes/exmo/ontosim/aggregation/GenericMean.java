package fr.inrialpes.exmo.ontosim.aggregation;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

import java.util.HashMap;
import java.util.Map;


/**
 * Generic mean implementation
 * It follows the formula m=inversePhi(sum(phi(x)/n)
 * phi(x)=x -> arithmetic mean -> use new GenericMean(GenericMean.ARITHMETIC)
 * phi(x)=x^ -> quadratic mean -> use new GenericMean(GenericMean.QUADRATIC)
 * phi(x) = ln(x) -> geometric mean -> use new GenericMean(GenericMean.GEOMETRIC)
 * phi(x) = 1/x -> harmonic mean -> use new GenericMean(GenericMean.HARMONIC)
 * ...
 * To implement a specific weighted average, it is enough to
 * override method protected int weight(double x) and add the specific 
 * weighting formula
 * 
 * To implement another mean, implement a new Type
 * 
 * @author Jerome David
 *
 */
public class GenericMean extends AggregationScheme {
    
    private static final Map<Type,GenericMean> INSTANCES=new HashMap<Type,GenericMean>();

	/**
	 *
	 * @param t
	 * @return
	 */
	public synchronized static GenericMean getInstance(Type t) {
	/*Map<Type,GenericMean> mapType = INSTANCES.get(c);
	if (mapType==null) {
	    mapType=new HashMap<Type,GenericMean>();
	    INSTANCES.put(c, mapType);
	}*/
	GenericMean m = INSTANCES.get(t);//mapType.get(t);
	if (m==null) {
	    m=new GenericMean(t);
	    INSTANCES.put(t,m);
	}
	return m;
    }

	/**
	 * interface Type
	 */
	public interface Type {
	public double phi(double x);
	public double inversePhi(double x);
    }


    public static final Type ARITHMETIC = new  Type() {
		/**
		 *
		 * @param x
		 * @return
		 */
		@Override
	public final double inversePhi(double x) {
	    return x;
	}

		/**
		 *
		 * @param x
		 * @return
		 */
	@Override
	public final double phi(double x) {
	    return x;
	}
    };

    public static final Type QUADRATIC = new  Type() {
		/**
		 *
		 * @param x
		 * @return
		 */
    	@Override
	public final double inversePhi(double x) {
	    return Math.sqrt(x);
	}

		/**
		 *
		 * @param x
		 * @return
		 */
	@Override
	public double phi(double x) {
	    return x*x;
	}
    };
    
    
    public static final Type GEOMETRIC = new  Type() {
		/**
		 *
		 * @param x
		 * @return
		 */
	@Override
	public final double inversePhi(double x) {
	    return Math.exp(x);
	}

		/**
		 *
		 * @param x
		 * @return
		 */
	@Override
	public final double phi(double x) {
	    return Math.log(x);
	}
    };
    
    public static final Type HARMONIC = new  Type() {

		/**
		 *
		 * @param x
		 * @return
		 */
		@Override
	public final double inversePhi(double x) {
	    return 1/x;
	}

		/**
		 *
		 * @param x
		 * @return
		 */
	@Override
	public final double phi(double x) {
	    return 1/x;
	}
    };
    

    protected Type p;

	/**
	 *
	 * @param t
	 */
    protected GenericMean(Type t) {
	this.p=t;
    }
    
    protected GenericMean() {
	this(GenericMean.ARITHMETIC);
    }

	/**
	 *
	 * @param vals
	 * @return
	 */
    @Override
    public final double getValue(double[] vals) {
	double sum=0;
	double sumW=0;
	for (double v : vals) {
	    int w = weight(v);
	    sum+=w*p.phi(v);
	    sumW+=w;
	}
	return p.inversePhi(sum/sumW);
    }

	/**
	 *
	 * @param x
	 * @return
	 */
    protected int weight(double x) {return 1;}

	/**
	 *
	 * @param measure
	 * @param matching
	 * @param <O>
	 * @return
	 */
    public final <O> double getValue(Measure<O> measure, Matching<O> matching) {
	double sum=0;
	double sumW=0;
	for (Matching.Entry<O> entry : matching) {
	    double v = measure.getMeasureValue(entry.getSource(), entry.getTarget());
	    int w = weight(v);
	    sum+=w*p.phi(v);
	    sumW+=w;
	}
	return p.inversePhi(sum/sumW);
    }        
}
