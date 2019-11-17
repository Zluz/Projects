package jmr.util.math;

public enum FunctionParameter {

	SAMPLE_SIZE,
	
	DROP_TOP,
	DROP_BOTTOM,
	
	RAW_MIN,
	RAW_MAX,
	
	/** 
	 * Where to reset the trigger range chokes on the running sensor value.
	 * Higher = larger gap, more time before next trigger. 
	 */
	TRIGGER_NORM_DRIFT_THRESHOLD,
	TRIGGER_RAW_PERCENT_THRESHOLD,
	
	/** Used with IRL_MULTIPLIER to calculate IRL value from sensor value */
	IRL_INTERCEPT,
	/** Used with IRL_INTERCEPT to calculate IRL value from sensor value */
	IRL_MULTIPLIER,
	
	VAR_VALUE_LAST_POSTED,
	VAR_TIME_LAST_POSTED,
	VAR_VALUE_IRL_LAST_POSTED,
	;
}
