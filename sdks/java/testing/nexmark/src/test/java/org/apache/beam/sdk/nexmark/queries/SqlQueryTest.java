/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.nexmark.queries;

import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.nexmark.NexmarkConfiguration;
import org.apache.beam.sdk.nexmark.NexmarkUtils;
import org.apache.beam.sdk.nexmark.model.Event;
import org.apache.beam.sdk.nexmark.model.KnownSize;
import org.apache.beam.sdk.nexmark.queries.sql.SqlQuery1;
import org.apache.beam.sdk.nexmark.queries.sql.SqlQuery2;
import org.apache.beam.sdk.nexmark.queries.sql.SqlQuery3;
import org.apache.beam.sdk.nexmark.queries.sql.SqlQuery5;
import org.apache.beam.sdk.nexmark.queries.sql.SqlQuery7;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.UsesStatefulParDo;
import org.apache.beam.sdk.testing.UsesTimersInParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test the various NEXMark queries yield results coherent with their models. */
@RunWith(JUnit4.class)
public class SqlQueryTest {
  private static final NexmarkConfiguration CONFIG = NexmarkConfiguration.DEFAULT.copy();

  static {
    // careful, results of tests are linked to numEventGenerators because of timestamp generation
    CONFIG.numEventGenerators = 1;
    CONFIG.numEvents = 5000;
  }

  @Rule public TestPipeline p = TestPipeline.create();

  /** Test {@code query} matches {@code model}. */
  private <T extends KnownSize> void queryMatchesModel(
      String name,
      NexmarkQueryTransform<T> query,
      NexmarkQueryModel<T> model,
      boolean streamingMode) {
    NexmarkUtils.setupPipeline(NexmarkUtils.CoderStrategy.HAND, p);

    PCollection<Event> events =
        p.apply(
            name + ".Read",
            streamingMode
                ? NexmarkUtils.streamEventsSource(CONFIG)
                : NexmarkUtils.batchEventsSource(CONFIG));
    PCollection<TimestampedValue<T>> results =
        (PCollection<TimestampedValue<T>>) events.apply(new NexmarkQuery<>(CONFIG, query));
    PAssert.that(results).satisfies(model.assertionFor());
    PipelineResult result = p.run();
    result.waitUntilFinish();
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery1MatchesModelBatch() {
    queryMatchesModel("SqlQuery1TestBatch", new SqlQuery1(), new Query1Model(CONFIG), false);
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery1MatchesModelStreaming() {
    queryMatchesModel("SqlQuery1TestStreaming", new SqlQuery1(), new Query1Model(CONFIG), true);
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery2MatchesModelBatch() {
    queryMatchesModel(
        "SqlQuery2TestBatch", new SqlQuery2(CONFIG.auctionSkip), new Query2Model(CONFIG), false);
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery2MatchesModelStreaming() {
    queryMatchesModel(
        "SqlQuery2TestStreaming", new SqlQuery2(CONFIG.auctionSkip), new Query2Model(CONFIG), true);
  }

  @Test
  @Category({NeedsRunner.class, UsesStatefulParDo.class, UsesTimersInParDo.class})
  public void sqlQuery3MatchesModelBatch() {
    queryMatchesModel("SqlQuery3TestBatch", new SqlQuery3(CONFIG), new Query3Model(CONFIG), false);
  }

  @Test
  @Category({NeedsRunner.class, UsesStatefulParDo.class, UsesTimersInParDo.class})
  public void sqlQuery3MatchesModelStreaming() {
    queryMatchesModel(
        "SqlQuery3TestStreaming", new SqlQuery3(CONFIG), new Query3Model(CONFIG), true);
  }

  @Test
  @Category(NeedsRunner.class)
  @Ignore("https://jira.apache.org/jira/browse/BEAM-7072")
  public void sqlQuery5MatchesModelBatch() {
    queryMatchesModel("SqlQuery5TestBatch", new SqlQuery5(CONFIG), new Query5Model(CONFIG), false);
  }

  @Test
  @Category(NeedsRunner.class)
  @Ignore("https://jira.apache.org/jira/browse/BEAM-7072")
  public void sqlQuery5MatchesModelStreaming() {
    queryMatchesModel(
        "SqlQuery5TestStreaming", new SqlQuery5(CONFIG), new Query5Model(CONFIG), true);
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery7MatchesModelBatch() {
    queryMatchesModel("SqlQuery7TestBatch", new SqlQuery7(CONFIG), new Query7Model(CONFIG), false);
  }

  @Test
  @Category(NeedsRunner.class)
  public void sqlQuery7MatchesModelStreaming() {
    queryMatchesModel(
        "SqlQuery7TestStreaming", new SqlQuery7(CONFIG), new Query7Model(CONFIG), true);
  }
}
