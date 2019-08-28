/*
 * Copyright 2010-2019 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.util.concurrent.Callable;

/**
 * Handles Flyway's automatic schema creation.
 */
public class DbSchemas {
    private static final Log LOG = LogFactory.getLog(DbSchemas.class);

    /**
     * The database connection to use for accessing the schema history table.
     */
    private final Connection connection;

    /**
     * The schemas managed by Flyway.
     */
    private final Schema[] schemas;

    /**
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * Creates a new DbSchemas.
     *
     * @param database      The database to use.
     * @param schemas       The schemas managed by Flyway.
     * @param schemaHistory The schema history table.
     */
    public DbSchemas(Database database, Schema[] schemas, SchemaHistory schemaHistory) {
        this.connection = database.getMainConnection();
        this.schemas = schemas;
        this.schemaHistory = schemaHistory;
    }

    /**
     * Creates the schemas.
     *
     * @param baseline Whether to include the creation of a baseline marker.
     */
    public void create(final boolean baseline) {
        int retries = 0;
        while (true) {
            try {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Void call() {
                        for (Schema schema : schemas) {
                            if (schema.exists()) {
                                LOG.debug("Schema " + schema + " already exists. Skipping schema creation.");
                                return null;
                            }
                        }

                        for (Schema schema : schemas) {
                            schema.create();
                        }

                        schemaHistory.create(baseline);
                        schemaHistory.addSchemasMarker(schemas);

                        return null;
                    }
                });
                return;
            } catch (RuntimeException e) {
                if (++retries >= 10) {
                    throw e;
                }
                try {
                    LOG.debug("Schema creation failed. Retrying in 1 sec ...");
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            }
        }
    }
}