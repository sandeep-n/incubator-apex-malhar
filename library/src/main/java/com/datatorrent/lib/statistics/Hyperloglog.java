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
import com.datatorrent.common.util.BaseOperator;

public class Hyperloglog extends BaseOperator
{

  private transient HllSketch hllSketch = HllSketch.builder().build();

  /**
   * Number of standard deviations determines the error bars
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
   * Output port that emits number of distinct elements seen in stream so far, with error bars.
   * Output is of the form []
   */
  public final transient DefaultOutputPort<double[]> countDistinct = new DefaultOutputPort<>();

  /**
   * Input port takes a string. Data should be appropriately formatted, e.g., "126.0" and "126.00" are different strings
   * representing the same number, so care should be taken to avoid overcounting.
   */
  public final transient DefaultInputPort<String> data = new DefaultInputPort<String>()
  {
    @Override
    public void process(String input)
    {
      hllSketch.update(input);

      double[] estimateWithError = {hllSketch.getEstimate(), hllSketch.getLowerBound(numStdDevs),
        hllSketch.getUpperBound(numStdDevs)};

      countDistinct.emit(estimateWithError);
    }
  };


}
