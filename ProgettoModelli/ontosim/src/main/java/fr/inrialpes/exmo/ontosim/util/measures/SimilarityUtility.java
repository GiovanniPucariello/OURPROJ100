package fr.inrialpes.exmo.ontosim.util.measures;

import fr.inrialpes.exmo.ontosim.Measure;

/**
 * final class SimilarityUtility
 */
public final class SimilarityUtility extends MeasureUtility {
    public static <O> double getVal(Measure<O> m, O x, O y) {
	return m.getSim(x, y);
    }

	/**
	 *
	 * @param m
	 * @param <O>
	 * @return
	 */
    public static <O> Measure<O> convert(final Measure<O> m) {
	return new Measure<O>() {
		/**
		 *
		 * @param o1
		 * @param o2
		 * @return
		 */
	    @Override
	    public double getDissim(O o1, O o2) {
		return m.getDissim(o1, o2);
	    }

		/**
		 *
		 * @return
		 */
	    @Override
	    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	    }

		/**
		 *
		 * @param o1
		 * @param o2
		 * @return
		 */
	    @Override
	    public double getMeasureValue(O o1, O o2) {
		return m.getSim(o1, o2);
	    }

		/**
		 *
		 * @param o1
		 * @param o2
		 * @return
		 */
	    @Override
	    public double getSim(O o1, O o2) {
		return m.getSim(o1, o2);
	    }

	};
    }
}
