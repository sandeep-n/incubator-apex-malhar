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

import org.junit.Assert;
import org.junit.Test;

import com.datatorrent.lib.testbench.CollectorTestSink;
import com.datatorrent.lib.util.TestUtils;

public class HyperloglogTest
{

  @Test
  public void testEstimate()
  {
    HyperloglogOperator hll = new HyperloglogOperator();

    CollectorTestSink<double[]> sink = new CollectorTestSink<>();
    TestUtils.setSink(hll.countDistinct, sink);

    hll.setup(null);
    hll.beginWindow(0);

    for (int i = 0; i < 1000000; i++) {
      hll.data.process(String.valueOf(i));
    }

    hll.endWindow();

    // {estimate, lowerbound, upperbound}
    double[] finalEstimate = sink.collectedTuples.get(sink.collectedTuples.size() - 1);

    Assert.assertEquals("Captures all computed estimates", 1000000, sink.collectedTuples.size());
    Assert.assertTrue("Estimate is contained in bounds", finalEstimate[0] > finalEstimate[1]
      && finalEstimate[0] < finalEstimate[2]);
    Assert.assertTrue("Error is reasonable", Math.abs((1000000 - finalEstimate[0]) / 1000000) < 0.01);
  }

  @Test
  public void testErrorBars()
  {
    HyperloglogOperator hll = new HyperloglogOperator();

    CollectorTestSink<double[]> sink = new CollectorTestSink<>();
    TestUtils.setSink(hll.countDistinct, sink);

    /**
     * Setup HyperloglogOperator. Standard deviation has default value 2.0.
     */
    hll.setup(null);
    hll.beginWindow(0);

    for (int i = 0; i < 10000; i++) {
      hll.data.process(String.valueOf(i));
    }

    hll.endWindow();

    double[] errorBarsNarrow = sink.collectedTuples.get(sink.collectedTuples.size() - 1);

    /**
     * Repeat with a different value of standard deviation.
     */
    hll.setup(null);

    hll.setNumStdDevs(3.0);
    hll.beginWindow(0);

    for (int i = 0; i < 10000; i++) {
      hll.data.process(String.valueOf(i));
    }

    hll.endWindow();

    double[] errorBarsWide = sink.collectedTuples.get(sink.collectedTuples.size() - 1);

    Assert.assertTrue("Error bars are wider when SD is increased", errorBarsNarrow[1] > errorBarsWide[1]
      && errorBarsNarrow[2] < errorBarsWide[2]);
  }

  @Test
  public void testInputTypes()
  {
    
  }

}
