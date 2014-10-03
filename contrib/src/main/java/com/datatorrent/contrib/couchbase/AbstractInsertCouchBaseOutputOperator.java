/*
 * Copyright (c) 2014 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.contrib.couchbase;

import com.datatorrent.common.util.DTThrowable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractInsertCouchBaseOutputOperator which extends AbstractCouchBaseOutputOperator and implements set functionality of couchbase.
 */
public abstract class AbstractInsertCouchBaseOutputOperator<T> extends AbstractCouchBaseOutputOperator<T>
{

  private transient OperationFuture<Boolean> future;
  private static final Logger logger = LoggerFactory.getLogger(AbstractInsertCouchBaseOutputOperator.class);

  @Override
  public void insertOrUpdate(T input)
  {

    String key = generateKey(input);
    Object tuple = getObject(input);
    ObjectMapper mapper = new ObjectMapper();
    String value = new String();
    try {
      value = mapper.writeValueAsString(tuple);
    }
    catch (IOException ex) {
      logger.error("IO Exception", ex);
      DTThrowable.rethrow(ex);
    }

    final CountDownLatch countLatch = new CountDownLatch(store.batch_size);

    future = store.getInstance().set(key, value);
    future.addListener(new OperationCompletionListener()
    {

      @Override
      public void onComplete(OperationFuture<?> f) throws Exception
      {
        countLatch.countDown();
        if (!((Boolean)f.get())) {
          throw new RuntimeException("Operation set failed");
        }

      }

    });

    if (num_tuples < store.batch_size) {
      try {
        countLatch.await();
      }
      catch (InterruptedException ex) {
        logger.error("Interrupted exception" + ex);
        DTThrowable.rethrow(ex);
      }
    }

  }

}
