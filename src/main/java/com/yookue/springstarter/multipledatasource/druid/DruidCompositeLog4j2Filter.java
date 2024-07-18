/*
 * Copyright (c) 2020 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.multipledatasource.druid;


import jakarta.annotation.Nullable;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.sql.SQLUtils;
import com.yookue.commonplexus.javaseutil.util.BeautifulFormatWraps;


/**
 * Druid filter for log4j2
 *
 * @author David Hsing
 */
public class DruidCompositeLog4j2Filter extends Log4j2Filter {
    public DruidCompositeLog4j2Filter() {
        super.setStatementSqlFormatOption(new SQLUtils.FormatOption(false, false));
    }

    @Override
    protected void statementLog(@Nullable String message) {
        message = BeautifulFormatWraps.combine2Singleton(message);
        super.statementLog(message);
    }
}
