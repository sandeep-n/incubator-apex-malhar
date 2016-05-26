/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.lib.statistics;

import com.yahoo.sketches.hll.HllSketch;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.OperatorAnnotation;
import com.datatorrent.common.util.BaseOperator;

/**
 * An implementation of BaseOperator that estimates the cardinality (= number of distinct elements)
 * of the input data stream, while using only a small amount of memory. <br>
 * <p>
 * <b>Input Port(s) : </b><br>
 * <b>data : </b> Data values input port. <br>
 * <br>
 * <b>Output Port(s) : </b> <br>
 * <b>median : </b>Cardinality output port. <br>
 * <br>
 * <b>StateFull : Yes</b>, value are aggregated over application window. <br>
 * <b>Partitions : No</b>, will yield wrong results. <br>
 * <br>+
 * @displayName Hyperloglog
 * @category Stats and Aggregations
 * @tags Hyperloglog operator
 * @since 0.3.4
 */
@OperatorAnnotation(partitionable = false)
public class HyperloglogOperator extends BaseOperator
{

  private transient HllSketch hllSketch = HllSketch.builder().build();

  /**
   * Number of standard deviations determines the error bars. SD defaults to 2.0.
   */
  private double numStdDevs = 2.0;

  public double getNumStdDevs()
  {
    return numStdDevs;
  }

  public void setNumStdDevs(double numStdDevs)
  {
    this.numStdDevs = numStdDevs;
  }

  /**
   * Output port that emits number of distinct elements seen in stream so far, including error estimates.
   * Output is of the form {cardinality_estimate, lower_bound, upper_bound}
   */
  public final transient DefaultOutputPort<double[]> countDistinct = new DefaultOutputPort<>();

  /**
   * The hyperloglog data structure takes
   */
  public final transient DefaultInputPort<Object> data = new DefaultInputPort<Object>()
  {
    @Override
    public void process(Object input)
    {
// can update hllSketch with long, double, String, byte[], int[], long[]
      if (input instanceof String || input instanceof byte[] || input instanceof int[] || input instanceof long[]){
        hllSketch.update(input);
      }

      double[] estimateWithError = {hllSketch.getEstimate(), hllSketch.getLowerBound(numStdDevs),
        hllSketch.getUpperBound(numStdDevs)};

      countDistinct.emit(estimateWithError);
    }
  };


}
