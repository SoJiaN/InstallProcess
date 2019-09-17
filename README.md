## code to install
### to resove the Intent
* 跳转进行安装的Activity
```
val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
val apkFile = File(this.cacheDir.path + "/CloudMusic_official_5.6.0.314967.apk")
install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
val contentUri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)

// 关键函数 最后会被解析成 intent.data 和 intent.type
install.setDataAndType(
            contentUri,
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(apkFile).toString()))
            )
startActivity(install)
```
* 到 ActivityStarter.java 的 startActivityMayWait 方法
```
final int startActivityMayWait( /*... ignore some params ... */) {
    ResolveInfo rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId);
    if (rInfo == null) {
         if (profileLockedAndParentUnlockingOrUnlocked) {
                    // 关键方法，调用到 ActivityStackSupervisor 类里，在这里得到 List<ResolveInfo>
                    rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId,
                            PackageManager.MATCH_DIRECT_BOOT_AWARE
                            | PackageManager.MATCH_DIRECT_BOOT_UNAWARE);
                }
    }
    // Collect information about the target of the Intent.
    // convert resolveInfo to activityInfo
    ActivityInfo aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags, profilerInfo);
    
    // 这里得到的 activityInfo ResoveInfo 会随着 startActivity 流程 传递下去，一直到 成功启动 InstallStart.java 这个 Activity 文件
    int res = startActivityLocked(aInfo, rInfo   /* ... ignore some params ... */);
}
```
* 到 PackageManagerService 类
* --> resolveIntent(PackageManagerInternal) --> (resolveIntent )PackageManagerInternalImpl (是唯一的实现类，是PackageManagerService 的私有子类)
```
    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType,
            int flags, int userId) {
        return resolveIntentInternal(intent, resolvedType, flags, 
                                            userId, true /*resolveForStart*/);
                
    }
```
```
    private ResolveInfo resolveIntentInternal(Intent intent, String resolvedType,
            int flags, int userId, boolean resolveForStart) {
            // 通过 intent 查找符合要求的Activities，ActivityInfo是ResolveInfo的一个变量，这里返回的是符合条件的装有ResolveInfo的集合
        final List<ResolveInfo> query = queryIntentActivitiesInternal(intent, resolvedType,
                flags, callingUid, userId, resolveForStart, true /*allowDynamicSplits*/);
        final ResolveInfo bestChoice =
                chooseBestActivity(intent, resolvedType, flags, query, userId);
        return bestChoice;
    }
```
* chooseBestActivity()  选择并返回返回最合适的ResolveInfo
```
            private ResolveInfo chooseBestActivity(Intent intent, String resolvedType,
            int flags, List<ResolveInfo> query, int userId) {
                if (N == 1) {
                        return query.get(0);
                    }else{
                        // 选出前两个
                        ResolveInfo r0 = query.get(0);
                        ResolveInfo r1 = query.get(1);
                        // 比较优先级 等 进行选择
                        return ri;
                    }
            }
```
* queryIntentActivitiesInternal() 解析 intent
```
    private @NonNull List<ResolveInfo> queryIntentActivitiesInternal(Intent intent,
            String resolvedType, int flags, int filterCallingUid, int userId,
            boolean resolveForStart, boolean allowDynamicSplits) {
        synchronized (mPackages) {
            // pkgName 就是 intent.mPackage 
            // intent.mPackage == null
            if (pkgName == null) {
            // 调用 ActivityStackSupervisor  
                List<CrossProfileIntentFilter> matchingFilters =
                        getMatchingCrossProfileIntentFilters(intent, resolvedType, userId);
                // Check for results that need to skip the current profile.
                ResolveInfo xpResolveInfo  = querySkipCurrentProfileIntents(matchingFilters, intent,
                        resolvedType, flags, userId);
                if (xpResolveInfo != null) {
                    List<ResolveInfo> xpResult = new ArrayList<ResolveInfo>(1);
                    xpResult.add(xpResolveInfo);
                    return applyPostResolutionFilter(
                            filterIfNotSystemUser(xpResult, userId), instantAppPkgName,
                            allowDynamicSplits, filterCallingUid, userId);
                }
                // hasWebURI(Intent) 返回了一个false 不会走进来
                if (hasWebURI(intent)) {
                    // We have more than one candidate (combining results from current and parent
                    // profile), so we need filtering and sorting.
                    result = filterCandidatesWithDomainPreferredActivitiesLPr(
                            intent, flags, result, xpDomainInfo, userId);
                    sortResult = true;
                }
            } else {
                // intent 指定了package,不会走进这里来
            }
        }
        return applyPostResolutionFilter(
                result, instantAppPkgName, allowDynamicSplits, filterCallingUid, userId);
    }
```
* 到 getMatchingCrossProfileIntentFilters 方法中调用到 IntentResolver 类的 queryIntent 方法
```
// resolvedType
public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly,
            int userId) {
        String scheme = intent.getScheme();

        ArrayList<R> finalList = new ArrayList<R>();

        // 如果 intent
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf('/');
            if (slashpos > 0) {
                final String baseType = resolvedType.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    if (resolvedType.length() != slashpos+2
                            || resolvedType.charAt(slashpos+1) != '*') {
                        firstTypeCut = mTypeToFilter.get(resolvedType);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                    } else {
                        // We can match anything with our base type.
                        firstTypeCut = mBaseTypeToFilter.get(baseType);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                    }
                    thirdTypeCut = mWildTypeToFilter.get("*");
                } else if (intent.getAction() != null) {
                    // The intent specified any type ({@literal *}/*).  This
                    // can be a whole heck of a lot of things, so as a first
                    // cut let's use the action instead.
                    firstTypeCut = mTypedActionToFilter.get(intent.getAction());
                }
            }
        }

        // If the intent includes a data URI, then we want to collect all of
        // the filters that match its scheme (we will further refine matches
        // on the authority and path by directly matching each resulting filter).
        if (scheme != null) {
            schemeCut = mSchemeToFilter.get(scheme);
        }

        // If the intent does not specify any data -- either a MIME type or
        // a URI -- then we will only be looking for matches against empty
        // data.
        if (resolvedType == null && scheme == null && intent.getAction() != null) {
            firstTypeCut = mActionToFilter.get(intent.getAction());
        }

        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        if (firstTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType,
                    scheme, firstTypeCut, finalList, userId);
        }
        if (secondTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType,
                    scheme, secondTypeCut, finalList, userId);
        }
        if (thirdTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType,
                    scheme, thirdTypeCut, finalList, userId);
        }
        if (schemeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType,
                    scheme, schemeCut, finalList, userId);
        }
        filterResults(finalList);
        sortResults(finalList);

        return finalList;
    }
```
* hasWebUri 方法  ---> 返回了一个false
```
    private static boolean hasWebURI(Intent intent) {
        // content://com.example.a22257.custominstallprocess.fileprovider/song/CloudMusic_official_5.6.0.314967.apk
        // intent.getData
        if (intent.getData() == null) {
            // do not enter
            return false;
        }
        // intent.getScheme() == "content"
        final String scheme = intent.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            // do not enter
            return false;
        }
        // "content" 不等于 "http","https"
        return scheme.equals(IntentFilter.SCHEME_HTTP) || scheme.equals(IntentFilter.SCHEME_HTTPS);
    }
```

* resolveIntent
* inent.getPackage = null
* intent.scheme = "content"
* intent.flalg = "Flag_grant_read_uri_permission"
* intent.action= "install_package"
* intent.data= "content://com.example.a22257.kotlintest.fileProvider/cache/Clo......apk"
* intent.compoent = null
* intent.categories = null
* intent.selector = null
* intent.type = application/vnd.android.package-archive
## from installStart.java to install finished
#### Cross Reference: PackageInstaller
* xref: /packages/apps/PackageInstaller/
    * 下的 AndroidManifest.xml 
    ```
        <activity android:name=".InstallStart" // 要启动的Activity
                android:exported="true"
                android:excludeFromRecents="true">
            <intent-filter android:priority="1">
                <action android:name="android.intent.action.VIEW" />
                <action android:name="android.intent.action.INSTALL_PACKAGE" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:scheme="file" />
                <data android:scheme="content" />
                <data android:mimeType="application/vnd.android.package-archive" />
            </intent-filter>
            <intent-filter android:priority="1">
                <action android:name="android.intent.action.INSTALL_PACKAGE" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:scheme="file" />
                <data android:scheme="package" />
                <data android:scheme="content" />
            </intent-filter>
            <intent-filter android:priority="1">
                <action android:name="android.content.pm.action.CONFIRM_PERMISSIONS" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>

        <activity android:name=".InstallStaging"
                android:exported="false" />

        <activity android:name=".PackageInstallerActivity"
                android:exported="false" />
    ```
    * InstallStart.java  activity 的 onCreate() 方法
    ```
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (PackageInstaller.ACTION_CONFIRM_PERMISSIONS.equals(intent.getAction())) {
            nextActivity.setClass(this, PackageInstallerActivity.class);
        } else {
            Uri packageUri = intent.getData();

            if (packageUri == null) {
               // 不会走进来 
               // ... ignore some code ...
            } else {
                // packageUri != null 
                // intent.data.scheme = "content"
                // private static final String SCHEME_CONTENT = "content"; 走进if
                if (packageUri.getScheme().equals(SCHEME_CONTENT)) {
                    nextActivity.setClass(this, InstallStaging.class);
                } else {
                    nextActivity.setClass(this, PackageInstallerActivity.class);
                }
            }
        }
        // 跳转到 InstallStaging.class
        if (nextActivity != null) {
            startActivity(nextActivity);
        }
        finish();
    }
    ```
    * 从installStart activity 跳转到 installStaging activity
        * 在 onResume 方法中执行
    ```
    @Override
    protected void onResume() {
        super.onResume();
            mStagingTask = new StagingAsyncTask();
            // getIntent.getData() == true
            mStagingTask.execute(getIntent().getData());
        }
    }
    ```
        * 跳转到 packageInstallerActivity  中
        ```
        private final class StagingAsyncTask extends AsyncTask<Uri, Void, Boolean> {
             @Override
                protected void onPostExecute(Boolean success) {
                    if (success) {
                        // Now start the installation again from a file 又回到了PackageInstallerActivity
                        Intent installIntent = new Intent(getIntent());
                        installIntent.setClass(InstallStaging.this, PackageInstallerActivity.class);
                        installIntent.setData(Uri.fromFile(mStagedFile));
                        installIntent
                                .setFlags(installIntent.getFlags() & ~Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivityForResult(installIntent, 0);
                    } else {
                        showError();
                    }
                }
        }
    ```
* PackageInstallerActivity.java（主要用于执行解析apk文件，解析manifest，解析签名等操作） 
    * PackageInstallerActivity 的 packageURI 的赋值（在oncreate 方法中）
    ```
    @Override
    protected void onCreate(Bundle icicle) {
        final Uri packageUri;
        // public static final String ACTION_CONFIRM_PERMISSIONS = "android.content.pm.action.CONFIRM_PERMISSIONS";
        // intent.getAction = "install_package"
        if (PackageInstaller.ACTION_CONFIRM_PERMISSIONS.equals(intent.getAction())){
        // 不会走进来
            final int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);
            final PackageInstaller.SessionInfo info = mInstaller.getSessionInfo(sessionId);
                if (info == null || !info.sealed || info.resolvedBaseCodePath == null) {
                    Log.w(TAG, "Session " + mSessionId + " in funky state; ignoring");
                    finish();
                    return;
                }
                
                mSessionId = sessionId;
                packageUri = Uri.fromFile(new File(info.resolvedBaseCodePath));
                mOriginatingURI = null;
                mReferrerURI = null;
        } else {
                mSessionId = -1;
                // uri.fromFile(mStagedFile);
                packageUri = intent.getData();
                mOriginatingURI
                intent.getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
                mReferrerURI = intent.getParcelableExtra(Intent.EXTRA_REFERRER);
        }
    }
    ```
        * 点击 确定 按钮执行 安装
            ```
                public void onClick(View v) {
                    if (v == mOk) {
                        startInstall();
                    } else if (v == mCancel) {
                        
                    }
                }
            ```
            * startInstall 方法
            ```
                private void startInstall() {
                    // Start subactivity to actually install the application
                    Intent newIntent = new Intent();
                    newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO,
                            mPkgInfo.applicationInfo);
                    newIntent.setData(mPackageURI);
                    // 跳转到 installInstalling 方法中
                    newIntent.setClass(this, InstallInstalling.class);
                    String installerPackageName = getIntent().getStringExtra(
                            Intent.EXTRA_INSTALLER_PACKAGE_NAME);
                    if (mOriginatingURI != null) {
                        newIntent.putExtra(Intent.EXTRA_ORIGINATING_URI, mOriginatingURI);
                    }
                    if (mReferrerURI != null) {
                        newIntent.putExtra(Intent.EXTRA_REFERRER, mReferrerURI);
                    }
                    if (mOriginatingUid != PackageInstaller.SessionParams.UID_UNKNOWN) {
                        newIntent.putExtra(Intent.EXTRA_ORIGINATING_UID, mOriginatingUid);
                    }
                    if (installerPackageName != null) {
                        newIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                        installerPackageName);
                    }
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        newIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    }
                    if(localLOGV) Log.i(TAG, "downloaded app uri="+mPackageURI);
                    startActivity(newIntent);
                    finish();
                }
            ```
*  跳转 到 InstallInstalling.class
        * 向packageManager发送信息，并处理包管理器的回调，
        * 用于执行安装apk逻辑，用于初始化安装界面，用于初始化用户UI。并调用PackageInstaller执行安装逻辑；
        * 内注册有观察者，当安装完成之后接收广播，更新UI。显示apk安装完成界面；
    * oncreate
        ```
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            // 设置layout xml
            setContentView(R.layout.install_installing);
            if ("package".equals(mPackageURI.getScheme())) {
                // getScheme() 是 “content”，这里不会走进来
                getPackageManager().installExistingPackage(appInfo.packageName);
                launchSuccess();
            } else {
                // 根据 packageURI 得到路径，
                final File sourceFile = new File(mPackageURI.getPath());
                PackageUtil.initSnippetForNewApp(this, PackageUtil.getAppSnippet(this, appInfo,
                        sourceFile), R.id.app_snippet);
    
                if (savedInstanceState != null) {
                    mSessionId = savedInstanceState.getInt(SESSION_ID);
                    mInstallId = savedInstanceState.getInt(INSTALL_ID);
    
                    // Reregister for result; might instantly call back if result was delivered while activity was destroyed
                    try {
                        InstallEventReceiver.addObserver(this, mInstallId,
                                this::launchFinishBasedOnResult);
                    } catch (EventResultPersister.OutOfIdsException e) {
                        // Does not happen
                    }
                } else {
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                            PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    params.referrerUri = getIntent().getParcelableExtra(Intent.EXTRA_REFERRER);
                    params.originatingUri = getIntent()
                            .getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
                    params.originatingUid = getIntent().getIntExtra(Intent.EXTRA_ORIGINATING_UID,
                            UID_UNKNOWN);
    
                    File file = new File(mPackageURI.getPath());
                    // parsePackageLite 初步解析安装文件
                    PackageParser.PackageLite pkg = PackageParser.parsePackageLite(file, 0);
                    params.setAppPackageName(pkg.packageName);
                    params.setInstallLocation(pkg.installLocation);
                    params.setSize(
                            PackageHelper.calculateInstalledSize(pkg, false, params.abiOverride));
    
                    // 注册观察者，接受安装成功，失败回调
                    mInstallId = InstallEventReceiver
                                .addObserver(this, EventResultPersister.GENERATE_NEW_ID,
                                        this::launchFinishBasedOnResult);
                    // 进行进程间通信 得到sessionId
                    mSessionId = getPackageManager().getPackageInstaller().createSession(params);
                }
            ｝
        }
        ```
        * launchFinishBasedOnResult
        ```
        private void launchFinishBasedOnResult(int statusCode, int legacyStatus, String statusMessage) {
                if (statusCode == PackageInstaller.STATUS_SUCCESS) {
                    launchSuccess();
                } else {
                    launchFailure(legacyStatus, statusMessage);
                }
        }
        ```
    * 在 onresume 中到PackageManagerService
    ```
    @Override
    protected void onResume() {
        super.onResume();
        if (mInstallingTask == null) {
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionInfo sessionInfo = installer.getSessionInfo(mSessionId);
            if (sessionInfo != null && !sessionInfo.isActive()) {
                // 调用 excute 方法
                mInstallingTask = new InstallingAsyncTask();
                mInstallingTask.execute();
            } else {
                // we will receive a broadcast when the install is finished
                mCancelButton.setEnabled(false);
                setFinishOnTouchOutside(false);
            }
        }
    }
    ```
    ```
    @Override
    protected void onPostExecute(PackageInstaller.Session session) {
        if (session != null) {
            Intent broadcastIntent = new Intent(BROADCAST_ACTION);
            broadcastIntent.setPackage(
                    getPackageManager().getPermissionControllerPackageName());
            broadcastIntent.putExtra(EventResultPersister.EXTRA_ID, mInstallId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    InstallInstalling.this,
                    mInstallId,
                    broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // 关键方法
            session.commit(pendingIntent.getIntentSender());
            mCancelButton.setEnabled(false);
            setFinishOnTouchOutside(false);
        } else {
            getPackageManager().getPackageInstaller().abandonSession(mSessionId);
            if (!isCancelled()) {
                launchFailure(PackageManager.INSTALL_FAILED_INVALID_APK, null);
            }
        }
    }
    ```
* PackageInstall.Session.java 
```
public void commit(@NonNull IntentSender statusReceiver) {
    try {
        // mSession 的类型为 IPackageInstallerSession.aidl 
        // 进行跨进程通信  packageManagerService.java
        mSession.commit(statusReceiver);
    } catch (RemoteException e) {
        throw e.rethrowFromSystemServer();
    }
}
```
* PackageInstallerSession.java 
```
    // IPackageInstallerSession 是 aidl 文件，在这里进行跨进程
    public class PackageInstallerSession extends IPackageInstallerSession.Stub {
```
* PackageInstallerSession.java
```
 @Override
public void commit(@NonNull IntentSender statusReceiver, boolean forTransfer) {
    // 封装包信息
    final PackageInstallObserverAdapter adapter = new PackageInstallObserverAdapter(
                    mContext, statusReceiver, sessionId, isInstallerDeviceOwnerLocked(), userId);
    // 从 Handler 发送 MSG_COMMIT
    mHandler.obtainMessage(MSG_COMMIT).sendToTarget();

}
```
* 处理该message 
```
private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COMMIT:
                    synchronized (mLock) {
                        try {
                            commitLocked();
                        } catch (PackageManagerException e) {
                            final String completeMsg = ExceptionUtils.getCompleteMessage(e);
                            Slog.e(TAG,
                                    "Commit of session " + sessionId + " failed: " + completeMsg);
                            destroyInternal();
                            dispatchSessionFinished(e.error, completeMsg, null);
                        }
                    }

                    break;
                case MSG_SESSION_FINISHED_WITH_EXCEPTION:
                    PackageManagerException e = (PackageManagerException) msg.obj;

                    dispatchSessionFinished(e.error, ExceptionUtils.getCompleteMessage(e),
                            null);
                    break;
            }

            return true;
        }
    };
```
* commitLocked 方法
```
private void commitLocked() throws PackageManagerException {
    mPm.installStage(mPackageName, stageDir, stageCid, localObserver, params,
                mInstallerPackageName, mInstallerUid, user, mCertificates);    
}
```
* 回到 PackageManagerService 类中
```
void installStage(String packageName, File stagedDir, String stagedCid,
            IPackageInstallObserver2 observer, PackageInstaller.SessionParams sessionParams,
            String installerPackageName, int installerUid, UserHandle user,
            Certificate[][] certificates) {
            // 构建 INIT_COPY 类型的message ，并发送出去
        final Message msg = mHandler.obtainMessage(INIT_COPY);
        final int installReason = fixUpInstallReason(installerPackageName, installerUid,
                sessionParams.installReason);
        final InstallParams params = new InstallParams(origin, null, observer,
                sessionParams.installFlags, installerPackageName, sessionParams.volumeUuid,
                verificationInfo, user, sessionParams.abiOverride,
                sessionParams.grantedRuntimePermissions, certificates, installReason);
        params.setTraceMethod("installStage").setTraceCookie(System.identityHashCode(params));
        msg.obj = params;
        // 发送 INIT_COPY 的 Handler
        mHandler.sendMessage(msg);
    }
```
* 到 PackageHandler 这个 Handler中
```
void doHandleMessage(Message msg) {
            switch (msg.what) {
                case INIT_COPY: {
                    // 取出数据
                    HandlerParams params = (HandlerParams) msg.obj;
                    if (!mBound) {
                        // 连接到安装服务
                        if (!connectToService()) { // ... ignore some code ...
                            params.serviceError();
                            if (params.traceMethod != null) {
                                Trace.asyncTraceEnd(TRACE_TAG_PACKAGE_MANAGER, params.traceMethod,
                                        params.traceCookie);
                            }
                            return;
                        } else {
                            //绑定服务成功后，将新的请求加入到mPendingIntalls中，等待处理
                            mPendingInstalls.add(idx, params);
                        }
                    } else {
                        //如果之前已经绑定过服务，同样将新的请求加入到mPendingIntalls中，等待处理
                        mPendingInstalls.add(idx, params);
                        if (idx == 0) {
                            // 关键步骤
                            // 如果是第一个请求，则直接发送事件MCS_BOUND，触发处理流程
                            mHandler.sendEmptyMessage(MCS_BOUND);
                        }
                    }
                    break;
                }
        }
}
```
* 查看 PackageHandler 的 MCS_BOUND 行为
```
case MCS_BOUND: {
                    if (mContainerService == null) {
                        // 
                    } else if (mPendingInstalls.size() > 0) {
                        HandlerParams params = mPendingInstalls.get(0);
                        if (params != null) {
                            // 关键步骤
                            // 调用 InstallParams.startCopy 处理安装请求
                            if (params.startCopy()) {
                                if (mPendingInstalls.size() > 0) {
                                    mPendingInstalls.remove(0);
                                }
                                if (mPendingInstalls.size() == 0) {
                                    if (mBound) {
                                        removeMessages(MCS_UNBIND);
                                        Message ubmsg = obtainMessage(MCS_UNBIND);
                                        // 如果队列为空，就在等待一段时间后，断开和安装服务的绑定
                                        sendMessageDelayed(ubmsg, 10000);
                                    }
                                } else {
                                    // 如果队列里还有数据，继续发送 MCS_BOUND 信息
                                    mHandler.sendEmptyMessage(MCS_BOUND);
                                }
                            }
                            Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
                        }
                    } else {
                        // Should never happen ideally.
                        Slog.w(TAG, "Empty queue");
                    }
                    break;
                }
```
* 查看 startCopy 方法
```
final boolean startCopy() {
            boolean res;
            try {
                // ... ignore some code ...
                // 关键方法
                handleStartCopy();
                res = true;
            } catch (RemoteException e) {
                if (DEBUG_INSTALL) Slog.i(TAG, "Posting install MCS_RECONNECT");
                mHandler.sendEmptyMessage(MCS_RECONNECT);
                res = false;
            }
            // 关键方法
            handleReturnCode();
            return res;
        }
```
* 跳转到 InstallParams 继承自 HandlerParams 类
```
    class InstallParams extends HandlerParams {
```
```
    public void handleStartCopy() throws RemoteException {
        // 确定 APK 的安装位置
        if (origin.file != null) {
            installFlags |= PackageManager.INSTALL_INTERNAL;
            installFlags &= ~PackageManager.INSTALL_EXTERNAL;
        } else if (origin.cid != null) {
            installFlags |= PackageManager.INSTALL_EXTERNAL;
            installFlags &= ~PackageManager.INSTALL_INTERNAL;
        } else {
            throw new IllegalStateException("Invalid stage location");
        }
        // 复制APK
        ret = args.copyApk(mContainerService, true);
    }
```
* 查看 handleReturnCode 方法
```
    @Override
    void handleReturnCode() {
        if (mArgs != null) {
            processPendingInstall(mArgs, mRet);
        }
    }
```
```
private void processPendingInstall(final InstallArgs args, final int currentStatus) {
        // Queue up an async operation since the package installation may take a little while.
        mHandler.post(new Runnable() {
            public void run() {
                PackageInstalledInfo res = new PackageInstalledInfo();
                if (res.returnCode == PackageManager.INSTALL_SUCCEEDED) {
                    // 预安装，检查包裹状态
                    args.doPreInstall(res.returnCode);
                    synchronized (mInstallLock) {
                        // 安装 APK
                        installPackageTracedLI(args, res);
                    }
                    // 安装收尾
                    args.doPostInstall(res.returnCode, res.uid);
                }
            }
        }
}
```
* installPackageTracedLI ---> installPackageLI 方法
```
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        // 方法比较长 有 491 行代码
        PackageParser pp = new PackageParser();
        // 解析package
        pkg = pp.parsePackage(tmpPackageFile, parseFlags);
        // 加载证书，获取签名信息
        PackageParser.populateCertificates(pkg, args.certificates);
        // 检测 是否已经安装过
        synchronized (mPackages) {
            // Check if installing already existing package
            if ((installFlags & PackageManager.INSTALL_REPLACE_EXISTING) != 0) {
            }
        }
        // 选择更新已经存在的APK，还是安装新的APK
        if (replace) {
                // 更新
                replacePackageLIF(pkg, parseFlags, scanFlags | SCAN_REPLACING, args.user,
                        installerPackageName, res, args.installReason);
            } else {
                // 安装
                installNewPackageLIF(pkg, parseFlags, scanFlags | SCAN_DELETE_DATA_ON_FAILURES,
                        args.user, installerPackageName, volumeUuid, res, args.installReason);
            }
    }
```
* installNewPackageLIF会调用scanPackageTracedLI去安装apk
```
private void installNewPackageLIF(PackageParser.Package pkg, final int policyFlags,
            int scanFlags, UserHandle user, String installerPackageName, String volumeUuid,
            PackageInstalledInfo res, int installReason) {
            // 1. 安装apk
            // 最终会调用scanPackageLI->scanPackageDirtyLI实际去将 apk 文件 中解析出的 activity ,service 等文件写入到 packageManagerService 的成员变量中。
            // 比如将每个APK的receivers列表里的元素，通过mReceivers.addActivity(a, “receiver”)添加到PMS成员列表mReceivers中:
            PackageParser.Package newPackage = scanPackageTracedLI(pkg, policyFlags, scanFlags,
                    System.currentTimeMillis(), user);
            // 2. 更新setting
            updateSettingsLI(newPackage, installerPackageName, null, res, user);
}
```
## PackageInstallerActivity.class (开机时的扫描)
* SystemServer.java - main() - run() - startBootstrapServices() - 
```
        // Start the package manager.
        if (!mRuntimeRestart) {
            MetricsLogger.histogram(null, "boot_package_manager_init_start",
                    (int) SystemClock.elapsedRealtime());
        }
        traceBeginAndSlog("StartPackageManagerService");
        mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
        mFirstBoot = mPackageManagerService.isFirstBoot();
        mPackageManager = mSystemContext.getPackageManager();
```
```
    // PackageManagerService.java main()
    public static PackageManagerService main(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {
        // Self-check for initial settings.
        PackageManagerServiceCompilerMapping.checkProperties();
        
        // 初始化
        PackageManagerService m = new PackageManagerService(context, installer,
                factoryTest, onlyCore);
        m.enableSystemUserPackages();
        ServiceManager.addService("package", m);
        final PackageManagerNative pmn = m.new PackageManagerNative();
        ServiceManager.addService("package_native", pmn);
        return m;
    }
```
```
    public PackageManagerService(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {
        synchronized (mInstallLock) {
        // writer
            synchronized (mPackages) {  
                // 初始化 PackageHandler 
                mHandler = new PackageHandler(mHandlerThread.getLooper());
                // 获取 data目录
                File dataDir = Environment.getDataDirectory();
                // 获取 /data/data 第三方软件目录 
                mAppInstallDir = new File(dataDir, "app");
                mAppLib32InstallDir = new File(dataDir, "app-lib");
                
                scanDirTracedLI(mAppInstallDir, 0, scanFlags | SCAN_REQUIRE_KNOWN, 0);
                
            }
        }
    }
```
```
    private void scanDirTracedLI(File dir, final int parseFlags, int scanFlags, long currentTime) {
            Trace.traceBegin(TRACE_TAG_PACKAGE_MANAGER, "scanDir [" + dir.getAbsolutePath() + "]");
            try {
                scanDirLI(dir, parseFlags, scanFlags, currentTime);
            } finally {
                Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
            }
    }
    private void scanDirLI(File dir, int parseFlags, int scanFlags, long currentTime) {
        final File[] files = dir.listFiles();
        int fileCount = 0;
        for(File file : files ){
            final boolean isPackage = (isApkFile(file) || file.isDirectory())
                    && !PackageInstallerService.isStageName(file.getName());
            if (!isPackage) {
                // Ignore entries which are not packages 如果不是package文件 忽略
                continue;
            }
            // 关键代码。调用 submit 方法
            parallelPackageParser.submit(file, parseFlags);
            fileCount++;
        }
        // Process results one by one
        for (; fileCount > 0; fileCount--) {
            if (errorCode == PackageManager.INSTALL_SUCCEEDED) {
                // 这个方法在接下来的解析 里会向 mPackages添加
                scanPackageLI(parseResult.pkg, parseResult.scanFile, parseFlags, scanFlags,
                                currentTime, null);
            }else{
                Slog.w(TAG, "Failed to scan " + parseResult.scanFile + ": " + e.getMessage());
            }
        }
    }
    
     /**
     * Submits the file for parsing
     * @param scanFile file to scan
     * @param parseFlags parse falgs
     */
    public void submit(File scanFile, int parseFlags) {
        mService.submit(() -> {
            ParseResult pr = new ParseResult();
            Trace.traceBegin(TRACE_TAG_PACKAGE_MANAGER, "parallel parsePackage [" + scanFile + "]");
            try {
                PackageParser pp = new PackageParser();
                pp.setSeparateProcesses(mSeparateProcesses);
                pp.setOnlyCoreApps(mOnlyCore);
                pp.setDisplayMetrics(mMetrics);
                pp.setCacheDir(mCacheDir);
                pp.setCallback(mPackageParserCallback);
                pr.scanFile = scanFile;
                // 关键代码
                pr.pkg = parsePackage(pp, scanFile, parseFlags);
            } catch (Throwable e) {
                pr.throwable = e;
            } finally {
                Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
            }
            try {
                mQueue.put(pr);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Propagate result to callers of take().
                // This is helpful to prevent main thread from getting stuck waiting on
                // ParallelPackageParser to finish in case of interruption
                mInterruptedInThread = Thread.currentThread().getName();
            }
        });
    }
    
    public Package parsePackage(File packageFile, int flags, boolean useCaches)
            throws PackageParserException {
        if (packageFile.isDirectory()) {
            // 解析目录中的所有 APKs 
            parsed = parseClusterPackage(packageFile, flags);
        } else {
            // 解析 单个 apk 已经被跑去
            parsed = parseMonolithicPackage(packageFile, flags);
        }
        return parsed;
    }
    
    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
            final AssetManager assets = assetLoader.getBaseAssetManager();
            final File baseApk = new File(lite.baseCodePath);
            final Package pkg = parseBaseApk(baseApk, assets, flags);
            if (pkg == null) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                        "Failed to parse base APK: " + baseApk);
            }

            if (!ArrayUtils.isEmpty(lite.splitNames)) {
                final int num = lite.splitNames.length;
                pkg.splitNames = lite.splitNames;
                pkg.splitCodePaths = lite.splitCodePaths;
                pkg.splitRevisionCodes = lite.splitRevisionCodes;
                pkg.splitFlags = new int[num];
                pkg.splitPrivateFlags = new int[num];
                pkg.applicationInfo.splitNames = pkg.splitNames;
                pkg.applicationInfo.splitDependencies = splitDependencies;
                pkg.applicationInfo.splitClassLoaderNames = new String[num];

                for (int i = 0; i < num; i++) {
                    final AssetManager splitAssets = assetLoader.getSplitAssetManager(i);
                    parseSplitApk(pkg, i, splitAssets, flags);
                }
            }

            pkg.setCodePath(packageDir.getAbsolutePath());
            pkg.setUse32bitAbi(lite.use32bitAbi);
            return pkg;     
    }
    
```
* 向 mPackages.put 的 流程分析
```
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, File scanFile,
            final int policyFlags, int scanFlags, long currentTime, @Nullable UserHandle user)
            throws PackageManagerException {
            PackageParser.Package scannedPkg = scanPackageInternalLI(pkg, scanFile, policyFlags,
                scanFlags, currentTime, user);
    }
```
* 调用到 scanPackageInternalLI() 方法
```
private PackageParser.Package scanPackageInternalLI(PackageParser.Package pkg, File scanFile,
            int policyFlags, int scanFlags, long currentTime, @Nullable UserHandle user)
            throws PackageManagerException {
             // Note that we invoke the following method only if we are about to unpack an application
        PackageParser.Package scannedPkg = scanPackageLI(pkg, policyFlags, scanFlags
                | SCAN_UPDATE_SIGNATURE, currentTime, user);            
}
```
* 调用 到 scanPackageLI() 方法
```
private PackageParser.Package scanPackageLI(PackageParser.Package pkg, final int policyFlags,
            int scanFlags, long currentTime, @Nullable UserHandle user)
                    throws PackageManagerException {
            final PackageParser.Package res = scanPackageDirtyLI(pkg, policyFlags, scanFlags,
                    currentTime, user);
}
```
* 调用到 scanPackageDirtyLI() 方法,主要就是把签名解析应用程序得到的package、provider、service、receiver和activity等信息保存在PackageManagerService相关的成员列表里
```
private PackageParser.Package scanPackageDirtyLI(PackageParser.Package pkg,
            final int policyFlags, final int scanFlags, long currentTime, @Nullable UserHandle user)
            throws PackageManagerException {
            
            for (i=0; i<N; i++) {
                PackageParser.Activity a = pkg.receivers.get(i);
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName);
                // 比如将每个APK的receivers列表里的元素，通过mReceivers.addActivity(a, “receiver”)添加到PMS成员列表mReceivers中:
                mReceivers.addActivity(a, "receiver");
            }
            
            if (nonMutatedPs != null) {
                // 向 Setting.java 中维护的 mPackages 中添加 PackageSetting
                // Map from package name to settings
                // final ArrayMap<String, PackageSetting> mPackages = new ArrayMap<>();
                synchronized (mPackages) {
                    mSettings.mPackages.put(nonMutatedPs.name, nonMutatedPs);
                }
            } else {
            // 调用到 commitPackageSettings, 在这个方法中执行 
            // Add the new setting to mPackages
            // mPackages.put(pkg.applicationInfo.packageName, pkg);
            commitPackageSettings(pkg, pkgSetting, user, scanFlags,
                    (policyFlags & PackageParser.PARSE_CHATTY) != 0 /*chatty*/);
            }
}
```
* parseSplitApk() 
```
    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags)
            throws PackageParserException {
        // 获取 AndroidManifest.xml 解析器
        parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
    
        pkg = parseSplitApk(pkg, res, parser, flags, splitIndex, outError);
    }
    
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags,
            int splitIndex, String[] outError) throws XmlPullParserException, IOException,
            PackageParserException {
        if (tagName.equals(TAG_APPLICATION)) {
                if (foundApp) {
                    if (RIGID_PARSER) {
                        outError[0] = "<manifest> has more than one <application>";
                        mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return null;
                    } else {
                        Slog.w(TAG, "<manifest> has more than one <application>");
                        XmlUtils.skipCurrentTag(parser);
                        continue;
                    }
                }

                foundApp = true;
                // 作为 apk 文件 来 解析 AndroidManifest.xml 文件
                if (!parseSplitApplication(pkg, res, parser, flags, splitIndex, outError)) {
                    return null;
                }

            } else if (RIGID_PARSER) {
                outError[0] = "Bad element under <manifest>: "
                    + parser.getName();
                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;

            } else {
                Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName()
                        + " at " + mArchiveSourcePath + " "
                        + parser.getPositionDescription());
                XmlUtils.skipCurrentTag(parser);
                continue;
        }
    }
```
```
    /**
     * Parse the {@code application} XML tree at the current parse location in a
     * <em>split APK</em> manifest.
     * <p>
     * Note that split APKs have many more restrictions on what they're capable
     * of doing, so many valid features of a base APK have been carefully
     * omitted here.
     */
     private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser,
            int flags, int splitIndex, String[] outError)
            throws XmlPullParserException, IOException {
        // 解析 Activity
        if (tagName.equals("activity")) {
            Activity a = parseActivity(owner, res, parser, flags, outError, cachedArgs, false,
                    owner.baseHardwareAccelerated);
            if (a == null) {
                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
            owner.activities.add(a);
            parsedComponent = a.info;
        } else if (tagName.equals("receiver")) {
        } else if (tagName.equals("service")) {
        } else if (tagName.equals("provider")) {
        } else if (tagName.equals("activity-alias")) {
        // ... ignore other....
    }
```
## packageInstall
* android:exported
    * Activity 是否可由其他应用的组件启动 —“true”表示可以，“false”表示不可以。若为“false”，则 Activity 只能由同一应用的组件或使用同一用户 ID 的不同应用启动。
    * 默认值取决于 Activity 是否包含 Intent 过滤器。没有任何过滤器意味着 Activity 只能通过指定其确切的类名称进行调用。 这意味着 Activity 专供应用内部使用（因为其他应用不知晓其类名称）。 因此，在这种情况下，默认值为“false”。另一方面，至少存在一个过滤器意味着 Activity 专供外部使用，因此默认值为“true”。

    * 该属性并非限制 Activity 对其他应用开放度的唯一手段。 您还可以利用权限来限制哪些外部实体可以调用 Activity（请参阅 permission 属性）。
* android:priority
    * 该值必须是整数，例如“ 100”。数字越大，优先级越高。默认值为0。
    * 当具有不同优先级的多个活动可以处理意图时，Android将仅考虑具有较高优先级值的那些作为意图的潜在目标


  [1]: https://upload-images.jianshu.io/upload_images/1417629-48b7b9d23288f0b9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
