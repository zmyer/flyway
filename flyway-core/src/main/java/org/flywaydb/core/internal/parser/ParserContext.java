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
package org.flywaydb.core.internal.parser;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.sqlscript.Delimiter;

public class ParserContext {
    private int parensDepth = 0;
    private int blockDepth = 0;
    private Delimiter delimiter;

    public ParserContext(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public void increaseParensDepth() {
        parensDepth++;
    }

    public void decreaseParensDepth() {
        parensDepth--;
    }

    public int getParensDepth() {
        return parensDepth;
    }

    public void increaseBlockDepth() {
        blockDepth++;
    }

    public void decreaseBlockDepth() {
        if (blockDepth == 0) {
            throw new FlywayException("Flyway parsing bug: unable to decrease block depth below 0");
        }
        blockDepth--;
    }

    public int getBlockDepth() {
        return blockDepth;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }
}