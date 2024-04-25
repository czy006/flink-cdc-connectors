/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.cdc.connectors.mysql.debezium.converters;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cdc.connectors.mysql.MySqlValidatorTest;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSourceBuilder;
import org.apache.flink.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.cdc.connectors.mysql.testutils.MySqlContainer;
import org.apache.flink.cdc.connectors.mysql.testutils.MySqlVersion;
import org.apache.flink.cdc.connectors.mysql.testutils.UniqueDatabase;
import org.apache.flink.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.connector.mysql.converters.MysqlDebeziumTimeConverter;
import io.debezium.data.Envelope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startables;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.flink.cdc.connectors.mysql.source.MySqlSourceTestBase.assertEqualsInAnyOrder;

/** Test for {@link MysqlDebeziumTimeConverter}. */
public class MysqlDebeziumTimeConverterITCase {

    private static TemporaryFolder tempFolder;
    private static File resourceFolder;

    private final StreamExecutionEnvironment env =
            StreamExecutionEnvironment.getExecutionEnvironment();

    private static final Logger LOG =
            LoggerFactory.getLogger(MysqlDebeziumTimeConverterITCase.class);

    private static final String defaultZone = "GMT+8";

    @Rule public final Timeout timeoutPerTest = Timeout.seconds(300);

    @Before
    public void setup() throws Exception {
        resourceFolder =
                Paths.get(
                                Objects.requireNonNull(
                                                MySqlValidatorTest.class
                                                        .getClassLoader()
                                                        .getResource("."))
                                        .toURI())
                        .toFile();
        tempFolder = new TemporaryFolder(resourceFolder);
        tempFolder.create();
        env.setParallelism(1);
    }

    @Test
    public void testReadDateConvertDataStreamInAsia() throws Exception {
        testReadDateConvertDataStreamSource("Asia/Shanghai", true);
    }

    @Test
    public void testReadDateConvertDataStreamInBerlin() throws Exception {
        testReadDateConvertDataStreamSource("Europe/Berlin", false);
    }

    @Test
    public void testReadDateConvertSQLSourceInAsia() throws Exception {
        testTemporalTypesWithMySqlServerTimezone("Asia/Shanghai", true);
    }

    @Test
    public void testReadDateConvertSQLSourceInBerlin() throws Exception {
        testTemporalTypesWithMySqlServerTimezone("Europe/Berlin", false);
    }

    private void testReadDateConvertDataStreamSource(
            String timezone, boolean connectionTimeZoneEnable) throws Exception {
        MySqlContainer mySqlContainer = createMySqlContainer(timezone);
        startContainers(mySqlContainer, timezone);
        UniqueDatabase db = getUniqueDatabase(mySqlContainer);
        db.createAndInitialize();
        env.enableCheckpointing(1000L);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        MySqlSourceBuilder<String> builder =
                MySqlSource.<String>builder()
                        .hostname(db.getHost())
                        .port(db.getDatabasePort())
                        .databaseList(db.getDatabaseName())
                        .tableList(db.getDatabaseName() + ".date_convert_test")
                        .startupOptions(StartupOptions.initial())
                        .serverTimeZone(timezone)
                        .username(db.getUsername())
                        .password(db.getPassword())
                        .debeziumProperties(
                                getDebeziumConfigurations(
                                        connectionTimeZoneEnable ? timezone : defaultZone));
        builder.deserializer(new JsonDebeziumDeserializationSchema());
        DataStreamSource<String> convertDataStreamSource =
                env.fromSource(
                        builder.build(),
                        WatermarkStrategy.noWatermarks(),
                        "testDataStreamSourceConvertData");
        List<String> result = convertDataStreamSource.executeAndCollect(3);
        validTimestampValue(result);
    }

    private void validTimestampValue(List<String> result) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String[] timestampValues = new String[] {"14:23:00", "00:00:00", "00:00:00"};
        for (String after : result) {
            JsonNode jsonNode = mapper.readTree(after);
            Assert.assertEquals(
                    timestampValues[jsonNode.get(Envelope.FieldName.AFTER).get("id").asInt() - 1],
                    jsonNode.get("after").get("test_timestamp").asText());
        }
    }

    private void testTemporalTypesWithMySqlServerTimezone(
            String timezone, boolean connectionTimeZoneEnable) throws Exception {
        MySqlContainer mySqlContainer = createMySqlContainer(timezone);
        startContainers(mySqlContainer, timezone);
        UniqueDatabase db = getUniqueDatabase(mySqlContainer);
        db.createAndInitialize();
        env.enableCheckpointing(1000L);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);
        String sourceDDL =
                format(
                        "CREATE TABLE customers ("
                                + " id BIGINT NOT NULL,"
                                + " test_timestamp STRING,"
                                + " test_datetime STRING,"
                                + " test_date STRING,"
                                + " test_time STRING, "
                                + "primary key (id) not enforced"
                                + ") WITH ("
                                + " 'connector' = 'mysql-cdc',"
                                + " 'scan.incremental.snapshot.enabled' = 'true',"
                                + " 'hostname' = '%s',"
                                + " 'port' = '%s',"
                                + " 'username' = '%s',"
                                + " 'password' = '%s',"
                                + " 'database-name' = '%s',"
                                + " 'table-name' = 'date_convert_test',"
                                + " 'scan.startup.mode' = '%s',"
                                + " 'server-time-zone' = '%s',"
                                + " 'debezium.converters' = 'datetime',"
                                + " 'debezium.datetime.type' = '%s',"
                                + " 'debezium.database.connectionTimeZone' = '%s',"
                                + " 'debezium.datetime.format.date' = 'yyyy-MM-dd',"
                                + " 'debezium.datetime.format.time' = 'HH:mm:ss',"
                                + " 'debezium.datetime.format.datetime' = 'yyyy-MM-dd HH:mm:ss',"
                                + " 'debezium.datetime.format.default.value.convert' = 'true'"
                                + ")",
                        mySqlContainer.getHost(),
                        mySqlContainer.getDatabasePort(),
                        db.getUsername(),
                        db.getPassword(),
                        db.getDatabaseName(),
                        "initial",
                        timezone,
                        MysqlDebeziumTimeConverter.class.getName(),
                        connectionTimeZoneEnable ? timezone : defaultZone);
        tEnv.executeSql(sourceDDL);
        TableResult tableResult = tEnv.executeSql("select * from customers");
        checkData(tableResult);
    }

    private Properties getDebeziumConfigurations(String timezone) {
        Properties debeziumProperties = new Properties();
        // set properties
        debeziumProperties.setProperty("converters", "datetime");
        debeziumProperties.setProperty("datetime.type", MysqlDebeziumTimeConverter.class.getName());
        debeziumProperties.setProperty("datetime.format.date", "yyyy-MM-dd");
        debeziumProperties.setProperty("datetime.format.time", "HH:mm:ss");
        debeziumProperties.setProperty("datetime.format.datetime", "yyyy-MM-dd HH:mm:ss");
        debeziumProperties.setProperty("datetime.format.timestamp", "yyyy-MM-dd HH:mm:ss");
        debeziumProperties.setProperty("datetime.format.default.value.convert", "false");
        // If not set time convert maybe error
        debeziumProperties.setProperty("database.connectionTimeZone", timezone);
        LOG.info("Supplied debezium properties: {}", debeziumProperties);
        return debeziumProperties;
    }

    private void checkData(TableResult tableResult) {
        String[] snapshotForSingleTable =
                new String[] {
                    "+I[1, 14:23:00, 2023-04-01 14:24:00, 2023-04-01, 14:25:00]",
                    "+I[3, 00:00:00, null, null, 00:01:20]",
                    "+I[2, 00:00:00, null, null, 00:00:00]"
                };

        List<String> expectedSnapshotData = new ArrayList<>(Arrays.asList(snapshotForSingleTable));
        CloseableIterator<Row> collect = tableResult.collect();
        tableResult.getJobClient().get().getJobID();
        assertEqualsInAnyOrder(
                expectedSnapshotData, fetchRows(collect, expectedSnapshotData.size()));
    }

    private static List<String> fetchRows(Iterator<Row> iter, int size) {
        List<String> rows = new ArrayList<>(size);
        while (size > 0 && iter.hasNext()) {
            Row row = iter.next();
            rows.add(row.toString());
            size--;
        }
        return rows;
    }

    protected MySqlContainer createMySqlContainer(String timezone) {
        return (MySqlContainer)
                new MySqlContainer(MySqlVersion.V5_7)
                        .withConfigurationOverride(buildMySqlConfigWithTimezone(timezone))
                        .withSetupSQL("docker/setup.sql")
                        .withDatabaseName("flink-test")
                        .withUsername("flinkuser")
                        .withPassword("flinkpw")
                        .withLogConsumer(new Slf4jLogConsumer(LOG));
    }

    protected void startContainers(MySqlContainer mySqlContainer, String timezone) {
        LOG.info("Starting containers with timezone {} ...", timezone);
        Startables.deepStart(Stream.of(mySqlContainer)).join();
        LOG.info("Containers are started.");
    }

    protected UniqueDatabase getUniqueDatabase(MySqlContainer mySqlContainer) {
        return new UniqueDatabase(mySqlContainer, "date_convert_test", "mysqluser", "mysqlpw");
    }

    private String buildMySqlConfigWithTimezone(String timezone) {
        try {
            File folder = tempFolder.newFolder(String.valueOf(UUID.randomUUID()));
            Path cnf = Files.createFile(Paths.get(folder.getPath(), "my.cnf"));
            String mysqldConf =
                    "[mysqld]\n"
                            + "binlog_format = row\n"
                            + "log_bin = mysql-bin\n"
                            + "server-id = 223344\n"
                            + "binlog_row_image = FULL\n";
            String timezoneConf = "default-time_zone = '" + timezone + "'\n";
            Files.write(
                    cnf,
                    Collections.singleton(mysqldConf + timezoneConf),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
            return Paths.get(resourceFolder.getAbsolutePath()).relativize(cnf).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create my.cnf file.", e);
        }
    }
}
