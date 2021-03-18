package com.wpe.wpe.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.wpe.wpe.external.SurfaceWrapper;
import com.wpe.wpe.IWPEService;
import com.wpe.wpe.Page;

public class WPEServiceConnection implements ServiceConnection {
    private static final String LOGTAG = "WPEServiceConnection";

    private final int m_processType;
    private final Page m_page;
    private Parcelable[] m_fds;
    private IWPEService m_service;

    static public final int PROCESS_TYPE_WEBPROCESS = 0;
    static public final int PROCESS_TYPE_NETWORKPROCESS = 1;

    public WPEServiceConnection(int processType, Page page, Parcelable[] fds)
    {
        m_processType = processType;
        m_page = page;
        m_fds = fds;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        Log.i("WPEServiceConnection", "onServiceConnected() name " + name.toString() + " m_fds " + m_fds);
        m_service = IWPEService.Stub.asInterface(service);

        Bundle bundle = new Bundle();
        bundle.putParcelableArray("fds", m_fds);
        m_fds = null;

        try {
            m_service.connect(bundle);
            if (m_processType == PROCESS_TYPE_WEBPROCESS) {
                m_service.provideSurface(new SurfaceWrapper(m_page.view().createSurface()));
            }
        } catch (android.os.RemoteException e) {
            Log.e(LOGTAG, "Failed to connect to service", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        Log.i(LOGTAG, "onServiceDisconnected()");
        // FIXME We need to notify WebKit about the Service being killed.
        //       What should WebKit do in this case?
        m_page.stopService(this);
    }
}
