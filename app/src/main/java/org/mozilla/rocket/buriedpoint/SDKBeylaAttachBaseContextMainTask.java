package org.mozilla.rocket.buriedpoint;
import com.anysitebrowser.base.core.stats.Stats;
import com.anysitebrowser.beyla.BeylaTracker;
import com.anysitebrowser.taskdispatcher.task.impl.MainThreadTask;

/**
 * SDKBeylaAttachBaseContextMainTask主线程任务
 * 加入到任务调度器后被执行
 */
public class SDKBeylaAttachBaseContextMainTask extends MainThreadTask {
    @Override
    public void run() {

        Stats.init(mContext, new AnalyticsCollectorsFactory());
        BeylaTracker.enableUploadByStoragePem(false);
    }
}