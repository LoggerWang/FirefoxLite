
    进入页面后设置Context, base_url, appId, userId, 代理模式
      1.  OpenVpnApi.setActivity(this);
      2.  OpenVpnApi.INSTANCE.setBaseUrl(OneClicklUrl.getVpnBaseUrl());
      3.  OpenVpnApi.INSTANCE.setAppIdUserId("","");
        // 设置模式为智能或者自定义的时候, 需要传入包名列表mSmartPkgNameList(反选)或mCustomPkgNameList(正选)   
      4.  OpenVpnApi.INSTANCE.setProxyMode(ProxyModeEnum.PROXY_CUSTOM);

    获取节点列表, 
    5. OpenVpnApi.getServerNode(), 对结果通过 OpenVpnApi.serverListLiveData进行监听

    获取节点后, 连接某个节点
    6. OpenVpnApi.setSelectNode(node, true) // 设置节点信息, 是否开启节点下载和连接, 如果为false, 可通过OpenVpnApi.checkFileConnect()进行节点下载和连接

    获取当前连接状态
    7. OpenVpnApi.getCurrentState() // 返回未连接, 准备中, 连接中, 已连接 , 共4种
    如果需要实时监听, 则OpenVpnApi.serverStateLiveData进行监听
        







