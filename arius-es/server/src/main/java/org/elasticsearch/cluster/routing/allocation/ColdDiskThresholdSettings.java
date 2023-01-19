/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cluster.routing.allocation;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.RatioValue;
import org.elasticsearch.common.unit.TimeValue;

import java.util.*;

/**
 * A container to keep settings for disk thresholds up to date with cluster setting changes.
 */
public class ColdDiskThresholdSettings extends DiskThresholdSettings {
    public static final Setting<String> CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING =
        new Setting<>("cluster.routing.allocation.cold.disk.watermark.low", "85%",
            (s) -> validWatermarkSetting(s, "cluster.routing.allocation.cold.disk.watermark.low"),
            new LowDiskWatermarkValidator(),
            Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<String> CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING =
        new Setting<>("cluster.routing.allocation.cold.disk.watermark.high", "90%",
            (s) -> validWatermarkSetting(s, "cluster.routing.allocation.cold.disk.watermark.high"),
            new HighDiskWatermarkValidator(),
            Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<String> CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING =
        new Setting<>("cluster.routing.allocation.cold.disk.watermark.flood_stage", "95%",
            (s) -> validWatermarkSetting(s, "cluster.routing.allocation.cold.disk.watermark.flood_stage"),
            new FloodStageValidator(),
            Setting.Property.Dynamic, Setting.Property.NodeScope);

    public ColdDiskThresholdSettings(Settings settings, ClusterSettings clusterSettings) {
        super();

        final String lowWatermark = CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING.get(settings);
        final String highWatermark = CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING.get(settings);
        final String floodStage = CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING.get(settings);
        setHighWatermark(highWatermark);
        setLowWatermark(lowWatermark);
        setFloodStage(floodStage);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING, this::setLowWatermark);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING, this::setHighWatermark);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING, this::setFloodStage);

        this.includeRelocations = CLUSTER_ROUTING_ALLOCATION_INCLUDE_RELOCATIONS_SETTING.get(settings);
        this.rerouteInterval = CLUSTER_ROUTING_ALLOCATION_REROUTE_INTERVAL_SETTING.get(settings);
        this.enabled = CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED_SETTING.get(settings);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_INCLUDE_RELOCATIONS_SETTING, this::setIncludeRelocations);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_REROUTE_INTERVAL_SETTING, this::setRerouteInterval);
        clusterSettings.addSettingsUpdateConsumer(CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED_SETTING, this::setEnabled);
    }

    static final class LowDiskWatermarkValidator implements Setting.Validator<String> {

        @Override
        public void validate(String value) {

        }

        @Override
        public void validate(final String value, final Map<Setting<?>, Object> settings) {
            final String highWatermarkRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING);
            final String floodStageRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING);
            doValidate(value, highWatermarkRaw, floodStageRaw);
        }

        @Override
        public Iterator<Setting<?>> settings() {
            final List<Setting<?>> settings = Arrays.asList(
                    CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING,
                    CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING);
            return settings.iterator();
        }

    }

    static final class HighDiskWatermarkValidator implements Setting.Validator<String> {

        @Override
        public void validate(final String value) {

        }

        @Override
        public void validate(final String value, final Map<Setting<?>, Object> settings) {
            final String lowWatermarkRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING);
            final String floodStageRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING);
            doValidate(lowWatermarkRaw, value, floodStageRaw);
        }

        @Override
        public Iterator<Setting<?>> settings() {
            final List<Setting<?>> settings = Arrays.asList(
                    CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING,
                    CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING);
            return settings.iterator();
        }

    }

    static final class FloodStageValidator implements Setting.Validator<String> {

        @Override
        public void validate(final String value) {

        }

        @Override
        public void validate(final String value, final Map<Setting<?>, Object> settings) {
            final String lowWatermarkRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING);
            final String highWatermarkRaw = (String) settings.get(CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING);
            doValidate(lowWatermarkRaw, highWatermarkRaw, value);
        }

        @Override
        public Iterator<Setting<?>> settings() {
            final List<Setting<?>> settings = Arrays.asList(
                    CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING,
                    CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING);
            return settings.iterator();
        }

    }

    private static void doValidate(String low, String high, String flood) {
        try {
            doValidateAsPercentage(low, high, flood);
            return; // early return so that we do not try to parse as bytes
        } catch (final ElasticsearchParseException e) {
            // swallow as we are now going to try to parse as bytes
        }
        try {
            doValidateAsBytes(low, high, flood);
        } catch (final ElasticsearchParseException e) {
            final String message = String.format(
                    Locale.ROOT,
                    "unable to consistently parse [%s=%s], [%s=%s], and [%s=%s] as percentage or bytes",
                    CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING.getKey(),
                    low,
                    CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING.getKey(),
                    high,
                    CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING.getKey(),
                    flood);
            throw new IllegalArgumentException(message, e);
        }
    }

    private static void doValidateAsBytes(final String low, final String high, final String flood) {
        final ByteSizeValue lowWatermarkBytes =
                thresholdBytesFromWatermark(low, CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING.getKey(), false);
        final ByteSizeValue highWatermarkBytes =
                thresholdBytesFromWatermark(high, CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING.getKey(), false);
        final ByteSizeValue floodStageBytes =
                thresholdBytesFromWatermark(flood, CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING.getKey(), false);
        if (lowWatermarkBytes.getBytes() < highWatermarkBytes.getBytes()) {
            throw new IllegalArgumentException(
                    "low disk watermark [" + low + "] less than high disk watermark [" + high + "]");
        }
        if (highWatermarkBytes.getBytes() < floodStageBytes.getBytes()) {
            throw new IllegalArgumentException(
                    "high disk watermark [" + high + "] less than flood stage disk watermark [" + flood + "]");
        }
    }

    private void setLowWatermark(String lowWatermark) {
        // Watermark is expressed in terms of used data, but we need "free" data watermark
        this.lowWatermarkRaw = lowWatermark;
        this.freeDiskThresholdLow = 100.0 - thresholdPercentageFromWatermark(lowWatermark);
        this.freeBytesThresholdLow = thresholdBytesFromWatermark(lowWatermark,
            CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING.getKey());
    }

    private void setHighWatermark(String highWatermark) {
        // Watermark is expressed in terms of used data, but we need "free" data watermark
        this.highWatermarkRaw = highWatermark;
        this.freeDiskThresholdHigh = 100.0 - thresholdPercentageFromWatermark(highWatermark);
        this.freeBytesThresholdHigh = thresholdBytesFromWatermark(highWatermark,
            CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING.getKey());
    }

    private void setFloodStage(String floodStageRaw) {
        // Watermark is expressed in terms of used data, but we need "free" data watermark
        this.freeDiskThresholdFloodStage = 100.0 - thresholdPercentageFromWatermark(floodStageRaw);
        this.freeBytesThresholdFloodStage = thresholdBytesFromWatermark(floodStageRaw,
            CLUSTER_ROUTING_ALLOCATION_COLD_DISK_FLOOD_STAGE_WATERMARK_SETTING.getKey());
    }

    public Setting getLowWatermarkSetting() {
        return CLUSTER_ROUTING_ALLOCATION_LOW_COLD_DISK_WATERMARK_SETTING;
    }

    public Setting getHighWatermarkSetting() {
        return CLUSTER_ROUTING_ALLOCATION_HIGH_COLD_DISK_WATERMARK_SETTING;
    }

}
