package org.mozilla.rocket.buriedpoint;

import android.content.Context;

import com.anysitebrowser.analytics.collector.BeylaCollector;
import com.anysitebrowser.base.core.ccf.CloudConfig;
import com.anysitebrowser.base.core.settings.Settings;
import com.anysitebrowser.base.core.stats.BaseAnalyticsCollector;
import com.anysitebrowser.base.core.stats.IAnalyticsCollectorFactory;
import com.anysitebrowser.beyla.BeylaTracker;

import java.util.ArrayList;
import java.util.List;

public final class AnalyticsCollectorsFactory implements IAnalyticsCollectorFactory {
    public List<BaseAnalyticsCollector> createCollectors(Context context) {
        List<BaseAnalyticsCollector> collectors = new ArrayList<>();
        // zhuwenliang Add Anysite Browser beyla collector according to cloud configuration
        try {
            BeylaTracker.setConfig(CloudConfig.getStringConfig(context, "beyla_params"),
                    CloudConfig.getBooleanConfig(context, "beyla_support_backend", true),
                    CloudConfig.getBooleanConfig(context, "beyla_use_https", false));
            BeylaCollector beylaCollector = new BeylaCollector(context, null, true, true);
            collectors.add(beylaCollector);
        } catch(Exception e) {}
        return collectors;
    }
}