package org.apache.flink.cdc.connectors.iceberg.sink.v2;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.cdc.common.event.Event;
import org.apache.flink.streaming.api.connector.sink2.WithPreWriteTopology;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.io.IOException;

public class IcebergEventSink implements WithPreWriteTopology<Event> {

  @Override
  public DataStream<Event> addPreWriteTopology(DataStream<Event> dataStream) {
    return null;
  }

  @Override
  public SinkWriter<Event> createWriter(InitContext initContext) throws IOException {
    return null;
  }
}
