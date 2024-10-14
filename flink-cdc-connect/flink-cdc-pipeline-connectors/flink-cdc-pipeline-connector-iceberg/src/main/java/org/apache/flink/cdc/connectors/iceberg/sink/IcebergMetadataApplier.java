package org.apache.flink.cdc.connectors.iceberg.sink;

import org.apache.flink.cdc.common.event.AddColumnEvent;
import org.apache.flink.cdc.common.event.AlterColumnTypeEvent;
import org.apache.flink.cdc.common.event.CreateTableEvent;
import org.apache.flink.cdc.common.event.DropColumnEvent;
import org.apache.flink.cdc.common.event.RenameColumnEvent;
import org.apache.flink.cdc.common.event.SchemaChangeEvent;
import org.apache.flink.cdc.common.event.SchemaChangeEventType;
import org.apache.flink.cdc.common.exceptions.SchemaEvolveException;
import org.apache.flink.cdc.common.exceptions.UnsupportedSchemaChangeEventException;
import org.apache.flink.cdc.common.schema.Column;
import org.apache.flink.cdc.common.sink.MetadataApplier;
import org.apache.flink.cdc.common.types.DataType;
import org.apache.flink.cdc.common.types.utils.DataTypeUtils;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.FlinkCatalog;
import org.apache.iceberg.flink.FlinkCatalogFactory;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IcebergMetadataApplier implements MetadataApplier {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergMetadataApplier.class);

  // Catalog is unSerializable.
  private transient Catalog catalog;

  private final Map<String, String> catalogOptions;

  public IcebergMetadataApplier(Map<String, String> catalogOptions) {
    this.catalogOptions = catalogOptions;
  }

  @Override
  public void applySchemaChange(SchemaChangeEvent schemaChangeEvent) throws SchemaEvolveException {
    if (catalog == null) {
      catalog = CatalogUtil.buildIcebergCatalog("123",catalogOptions,null);
    }
    try {
      if (schemaChangeEvent instanceof CreateTableEvent) {
        applyCreateTable((CreateTableEvent) schemaChangeEvent);
      } else if (schemaChangeEvent instanceof AddColumnEvent) {
        applyAddColumn((AddColumnEvent) schemaChangeEvent);
      } else if (schemaChangeEvent instanceof DropColumnEvent) {
        applyDropColumn((DropColumnEvent) schemaChangeEvent);
      } else if (schemaChangeEvent instanceof RenameColumnEvent) {
        applyRenameColumn((RenameColumnEvent) schemaChangeEvent);
      } else if (schemaChangeEvent instanceof AlterColumnTypeEvent) {
        applyAlterColumn((AlterColumnTypeEvent) schemaChangeEvent);
      } else {
        throw new UnsupportedSchemaChangeEventException(schemaChangeEvent);
      }
    } catch (Exception e) {
      throw new SchemaEvolveException(schemaChangeEvent, "schema change applying failure", e);
    }
  }

  private void applyCreateTable(CreateTableEvent event) {
    Namespace namespace = Namespace.of(event.tableId().getNamespace());
    TableIdentifier tableIdentifier = TableIdentifier.of(
            namespace
            , event.tableId().getSchemaName());
    org.apache.flink.cdc.common.schema.Schema cdcSchema = event.getSchema();
    Table table = catalog.loadTable(tableIdentifier);
    Schema icebergSchema = new Schema();
    for (Column column : cdcSchema.getColumns()) {
      table.updateSchema().addColumn(column.getName(),
              FlinkSchemaUtil.convert(DataTypeUtils.toFlinkDataType(column.getType()).getLogicalType())
              ,column.getComment());
    }
    catalog.createTable(tableIdentifier, icebergSchema);
  }


  @Override
  public MetadataApplier setAcceptedSchemaEvolutionTypes(Set<SchemaChangeEventType> schemaEvolutionTypes) {
    return MetadataApplier.super.setAcceptedSchemaEvolutionTypes(schemaEvolutionTypes);
  }

  @Override
  public boolean acceptsSchemaEvolutionType(SchemaChangeEventType schemaChangeEventType) {
    return MetadataApplier.super.acceptsSchemaEvolutionType(schemaChangeEventType);
  }

  @Override
  public Set<SchemaChangeEventType> getSupportedSchemaEvolutionTypes() {
    return MetadataApplier.super.getSupportedSchemaEvolutionTypes();
  }


}
