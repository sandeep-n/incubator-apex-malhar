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

import java.util.ArrayList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.lang.ArrayUtils;

import com.datatorrent.lib.testbench.CollectorTestSink;
import com.datatorrent.lib.util.TestUtils;

public class KernelDensityEstimatorTest
{

  @Test
  public void testRetention()
  {
    KernelDensityEstimator kde = new KernelDensityEstimator();

    CollectorTestSink<Double> sink = new CollectorTestSink<>();
    TestUtils.setSink(kde.probability, sink);

    int N = 1000;
    Random rand = new Random(1234L);
    double[] randArray = new double[N];

    kde.setup(null);
    kde.beginWindow(0);

    for (int i = 0; i < N; i++) {
      double r = rand.nextGaussian();
      randArray[i] = r;
      kde.data.process(r);
    }

    int numKernels = kde.getNumKernels();

    kde.endWindow();

    Assert.assertEquals("One kernel per tuple", N, numKernels);
    Assert.assertArrayEquals("Kernel means correspond to input data points", randArray, kde.getMeans());
  }

  @Test
  public void testProbs()
  {
    KernelDensityEstimator kde = new KernelDensityEstimator();

    CollectorTestSink<Double> sink = new CollectorTestSink<>();
    TestUtils.setSink(kde.probability, sink);

    int N = 1000;
    Random rand = new Random(1234L);
    double[] randArray = new double[N];

    kde.setup(null);
    kde.beginWindow(0);

    for (int i = 0; i < N; i++) {
      double r = rand.nextGaussian();
      randArray[i] = r;
      kde.data.process(r);
    }

    kde.endWindow();

    for (int i = 0; i < N; i++) {
      Assert.assertTrue("valid probabilities", sink.collectedTuples.get(i) >= 0.0);
    }

  }
}
