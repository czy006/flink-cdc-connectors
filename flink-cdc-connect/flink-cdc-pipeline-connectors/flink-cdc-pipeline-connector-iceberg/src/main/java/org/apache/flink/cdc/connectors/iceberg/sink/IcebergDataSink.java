package org.apache.flink.cdc.connectors.iceberg.sink;

import org.apache.flink.cdc.common.event.DataChangeEvent;
import org.apache.flink.cdc.common.function.HashFunctionProvider;
import org.apache.flink.cdc.common.sink.DataSink;
import org.apache.flink.cdc.common.sink.EventSinkProvider;
import org.apache.flink.cdc.common.sink.FlinkSinkProvider;
import org.apache.flink.cdc.common.sink.MetadataApplier;
import org.apache.flink.cdc.connectors.iceberg.sink.v2.IcebergEventSink;

import java.io.Serializable;

public class IcebergDataSink implements DataSink, Serializable {

  @Override
  public EventSinkProvider getEventSinkProvider() {
    return FlinkSinkProvider.of(
            new IcebergEventSink(options, commitUser, serializer, schemaOperatorUid, zoneId));
  }

  @Override
  public MetadataApplier getMetadataApplier() {
    return null;
  }

  @Override
  public HashFunctionProvider<DataChangeEvent> getDataChangeEventHashFunctionProvider() {
    return DataSink.super.getDataChangeEventHashFunctionProvider();
  }

  @Override
  public HashFunctionProvider<DataChangeEvent> getDataChangeEventHashFunctionProvider(int parallelism) {
    return DataSink.super.getDataChangeEventHashFunctionProvider(parallelism);
  }
}
