package org.apache.flink.cdc.connectors.iceberg.sink.v2;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;
import org.apache.iceberg.catalog.ImmutableTableCommit;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class IcebergWriter<InputT>
        implements TwoPhaseCommittingSink.PrecommittingSinkWriter<InputT, ImmutableTableCommit> {

  @Override
  public Collection<ImmutableTableCommit> prepareCommit() throws IOException, InterruptedException {
    return Collections.emptyList();
  }

  @Override
  public void write(InputT inputT, Context context) throws IOException, InterruptedException {

  }

  @Override
  public void flush(boolean b) throws IOException, InterruptedException {

  }

  @Override
  public void writeWatermark(Watermark watermark) throws IOException, InterruptedException {
    TwoPhaseCommittingSink.PrecommittingSinkWriter.super.writeWatermark(watermark);
  }

  @Override
  public void close() throws Exception {

  }
}
