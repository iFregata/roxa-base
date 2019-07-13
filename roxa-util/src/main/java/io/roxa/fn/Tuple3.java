/**
 * The MIT License
 * 
 * Copyright (c) 2016-2018 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.fn;

/**
 * @author Steven Chen
 *
 */
public class Tuple3<V0, V1, V2> extends Tuple2<V0, V1> {

	protected V2 val2;

	/**
	 * 
	 */
	public Tuple3(V0 val0, V1 val1, V2 val2) {
		super(val0, val1);
		this.val2 = val2;
	}

	public V2 $2() {
		return val2;
	}

}
