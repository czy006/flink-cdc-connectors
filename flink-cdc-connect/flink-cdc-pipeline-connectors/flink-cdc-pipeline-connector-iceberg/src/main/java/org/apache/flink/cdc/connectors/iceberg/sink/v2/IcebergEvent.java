package org.apache.flink.cdc.connectors.iceberg.sink.v2;

import org.apache.iceberg.catalog.TableIdentifierParser;
import org.apache.iceberg.data.GenericRecord;

public class IcebergEvent {

  // Identifier for the iceberg table to be written.
  TableIdentifierParser tableId;

  // The actual record to be written to iceberg table.
  GenericRecord genericRow;

  // if true, means that table schema has changed right before this genericRow.
  boolean shouldRefreshSchema;

  public IcebergEvent(TableIdentifierParser tableId, GenericRecord genericRow, boolean shouldRefreshSchema) {
    this.tableId = tableId;
    this.genericRow = genericRow;
    this.shouldRefreshSchema = shouldRefreshSchema;
  }

  public IcebergEvent(TableIdentifierParser tableId, GenericRecord genericRow) {
    this.tableId = tableId;
    this.genericRow = genericRow;
    this.shouldRefreshSchema = false;
  }

  public TableIdentifierParser getTableId() {
    return tableId;
  }

  public void setTableId(TableIdentifierParser tableId) {
    this.tableId = tableId;
  }

  public GenericRecord getGenericRow() {
    return genericRow;
  }

  public void setGenericRow(GenericRecord genericRow) {
    this.genericRow = genericRow;
  }

  public boolean isShouldRefreshSchema() {
    return shouldRefreshSchema;
  }

  public void setShouldRefreshSchema(boolean shouldRefreshSchema) {
    this.shouldRefreshSchema = shouldRefreshSchema;
  }
}
