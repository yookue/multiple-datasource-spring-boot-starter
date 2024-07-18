/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.multipledatasource.druid;


import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Properties for druid stat
 *
 * @author <a href="mailto:89921218@qq.com">lihengming</a>
 * @author David Hsing
 */
@ConfigurationProperties(prefix = "spring.datasource.druid")
@Getter
@Setter
@ToString
public class DruidStatProperties implements Serializable {
    private final StatViewServlet statViewServlet = new StatViewServlet();
    private final WebStatFilter webStatFilter = new WebStatFilter();
    private String[] aopPatterns;


    /**
     * Properties for {@code com.yookue.springstarter.multipledatasource.druid.DruidStatProperties.StatViewServlet}
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class StatViewServlet implements Serializable {
        private Boolean enabled;
        private String urlPattern = "/druid/*";    // $NON-NLS-1$
        private String allow;
        private String deny;
        private String loginUsername;
        private String loginPassword;
        private String resetEnable;
    }


    /**
     * Properties for {@code com.yookue.springstarter.multipledatasource.druid.DruidStatProperties.WebStatFilter}
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class WebStatFilter implements Serializable {
        private Boolean enabled;
        private String urlPattern;
        private String exclusions;
        private String sessionStatMaxCount;
        private String sessionStatEnable;
        private String principalSessionName;
        private String principalCookieName;
        private String profileEnable;
        // David Hsing added on 2021-08-12
        private Boolean defaultExclusionsEnable;
        private Boolean excludeStatServlet;
    }
}
