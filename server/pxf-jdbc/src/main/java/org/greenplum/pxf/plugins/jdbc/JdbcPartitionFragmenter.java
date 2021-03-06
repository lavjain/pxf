package org.greenplum.pxf.plugins.jdbc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.SerializationUtils;
import org.greenplum.pxf.api.model.BaseFragmenter;
import org.greenplum.pxf.api.model.Fragment;
import org.greenplum.pxf.api.model.FragmentStats;
import org.greenplum.pxf.api.model.RequestContext;
import org.greenplum.pxf.plugins.jdbc.partitioning.JdbcFragmentMetadata;
import org.greenplum.pxf.plugins.jdbc.partitioning.PartitionType;

import java.util.List;

/**
 * JDBC fragmenter
 * <p>
 * Splits the query to allow multiple simultaneous SELECTs
 */
public class JdbcPartitionFragmenter extends BaseFragmenter {
    // A PXF engine to use as a host for fragments
    private static final String[] pxfHosts = {"localhost"};

    private PartitionType partitionType;
    private String column;
    private String range;
    private String interval;

    @Override
    public void initialize(RequestContext context) {
        super.initialize(context);

        String partitionByOption = context.getOption("PARTITION_BY");
        if (partitionByOption == null) return;

        try {
            String[] partitionBy = partitionByOption.split(":");
            column = partitionBy[0];
            partitionType = PartitionType.of(partitionBy[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The parameter 'PARTITION_BY' has incorrect format. The correct format is '<column_name>:{int|date|enum}'");
        }

        range = context.getOption("RANGE");
        interval = context.getOption("INTERVAL");
    }

    /**
     * getFragments() implementation.
     * Note that all partitionType parameters must be verified before calling this procedure.
     *
     * @return a list of getFragmentsMetadata to be passed to PXF segments
     */
    @Override
    public List<Fragment> getFragments() {
        if (partitionType == null) {
            fragments.add(createFragment(null));
        } else {
            List<JdbcFragmentMetadata> fragmentsMetadata = partitionType.getFragmentsMetadata(column, range, interval);
            for (JdbcFragmentMetadata fragmentMetadata : fragmentsMetadata) {
                fragments.add(createFragment(SerializationUtils.serialize(fragmentMetadata)));
            }
        }
        return fragments;
    }

    /**
     * Create {@link Fragment} from byte array.
     *
     * @param fragmentMetadata
     * @return {@link Fragment}
     */
    private Fragment createFragment(byte[] fragmentMetadata) {
        return new Fragment(context.getDataSource(), pxfHosts, fragmentMetadata);
    }

    /**
     * @return fragment stats
     * @throws UnsupportedOperationException ANALYZE for Jdbc plugin is not supported
     */
    @Override
    public FragmentStats getFragmentStats() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ANALYZE for JDBC plugin is not supported");
    }
}
