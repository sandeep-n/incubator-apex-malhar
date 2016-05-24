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

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.OperatorAnnotation;
import com.datatorrent.common.util.BaseOperator;

import weka.estimators.KernelEstimator;

@OperatorAnnotation(partitionable = false)
public class KernelDensityEstimator extends BaseOperator
{

  /**
   * All data values are rounded to this precision. This value is also used to calculate the probability mass in the
   * range [value - precision, value + precision]
   */
  private double precision = 0.1;

  public double getPrecision()
  {
    return precision;
  }

  public void setPrecision(double precision)
  {
    this.precision = precision;
  }

  /**
   * Weka's kernel estimator sets its own bandwidth. In the Gaussian setting, the bandwidth is identical to the
   * standard deviation
   * @return
   */
  public double getBandwidth()
  {
    return kde.getStdDev();
  }

  /**
   * Get the number of kernels in the estimator. With weight 1.0, this should be the number of data pts seen thus far.
   */
  public int getNumKernels()
  {
    return kde.getNumKernels();
  }

  public double[] getMeans()
  {
    return kde.getMeans();
  }

  private transient KernelEstimator kde = new KernelEstimator(precision);

  public final transient DefaultOutputPort<Double> probability = new DefaultOutputPort<>();

  public final transient DefaultInputPort<Double> data = new DefaultInputPort<Double>()
  {
    @Override
    public void process(Double input)
    {
      // All values are added with weight 1.0
//      TODO: Extend operator to accommodate different weights
      kde.addValue(input, 1.0);

      probability.emit(kde.getProbability(input));

    }
  };
}
