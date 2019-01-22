/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.user.YamlUserTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrchestrationMasterSlaveDataSourceTest {
    
    private OrchestrationMasterSlaveDataSource masterSlaveDataSource;
    
    @Before
    @SneakyThrows
    public void setUp() {
        masterSlaveDataSource = new OrchestrationMasterSlaveDataSource(getMasterSlaveDataSource(), getOrchestrationConfiguration());
        
    }
    
    @SneakyThrows
    private MasterSlaveDataSource getMasterSlaveDataSource() {
        File yamlFile = new File(YamlUserTest.class.getResource("/yaml/unit/masterSlave.yaml").toURI());
        return (MasterSlaveDataSource) YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        RegistryCenterConfiguration registryCenterConfiguration = new RegistryCenterConfiguration();
        registryCenterConfiguration.setNamespace("test_ms");
        registryCenterConfiguration.setServerLists("localhost:2181");
        return new OrchestrationConfiguration("test", registryCenterConfiguration, true);
    }
    
    @Test
    public void assertRenewRule() {
        masterSlaveDataSource.renew(getMasterSlaveRuleChangedEvent());
        assertThat(masterSlaveDataSource.getDataSource().getMasterSlaveRule().getName(), is("new_ms"));
    }
    
    private MasterSlaveRuleChangedEvent getMasterSlaveRuleChangedEvent() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = new MasterSlaveRuleConfiguration("new_ms", "ds_m", Collections.singletonList("ds_s"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
        return new MasterSlaveRuleChangedEvent(ShardingConstant.LOGIC_SCHEMA_NAME, masterSlaveRuleConfiguration);
    }
    
    @Test
    public void assertRenewDataSource() {
        masterSlaveDataSource.renew(new DataSourceChangedEvent(ShardingConstant.LOGIC_SCHEMA_NAME, getDataSourceConfigurations()));
        assertThat(masterSlaveDataSource.getDataSource().getDataSourceMap().size(), is(1));
        
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return Collections.singletonMap("ds_test", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
    }
    
    @Test
    public void assertRenewProperties() {
    }
    
    @Test
    public void assertRenewConfigMap() {
    }
    
    @Test
    public void assertRenewDisabledState() {
    }
    
    @Test
    public void assertRenewCircuitState() {
    }
    
    @Test
    public void assertGetDataSource() {
    }
}
