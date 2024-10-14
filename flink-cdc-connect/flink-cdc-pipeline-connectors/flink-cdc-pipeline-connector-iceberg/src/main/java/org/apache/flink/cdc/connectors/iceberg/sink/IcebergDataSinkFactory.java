package org.apache.flink.cdc.connectors.iceberg.sink;

import org.apache.flink.cdc.common.configuration.ConfigOption;
import org.apache.flink.cdc.common.factories.DataSinkFactory;
import org.apache.flink.cdc.common.sink.DataSink;

import java.util.Collections;
import java.util.Set;

public class IcebergDataSinkFactory implements DataSinkFactory {

  @Override
  public DataSink createDataSink(Context context) {
    return null;
  }

  @Override
  public String identifier() {
    return "iceberg";
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    return Collections.emptySet();
  }
}
