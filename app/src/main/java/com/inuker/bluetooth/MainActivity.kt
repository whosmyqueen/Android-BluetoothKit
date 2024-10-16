package com.inuker.bluetooth

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.view.PullRefreshListView
import com.inuker.bluetooth.view.PullRefreshListView.OnRefreshListener
import com.inuker.bluetooth.view.PullToRefreshFrameLayout

class MainActivity : Activity() {
    private var mRefreshLayout: PullToRefreshFrameLayout? = null
    private var mListView: PullRefreshListView? = null
    private var mAdapter: DeviceListAdapter? = null
    private var mTvTitle: TextView? = null

    private var mDevices: MutableList<SearchResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        XXPermissions.with(this)
            .permission(Permission.BLUETOOTH_SCAN)
            .permission(Permission.BLUETOOTH_CONNECT)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                    } else {
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    }
                }
            })
        mDevices = ArrayList()

        mTvTitle = findViewById<View>(R.id.title) as TextView

        mRefreshLayout = findViewById<View>(R.id.pulllayout) as PullToRefreshFrameLayout

        mListView = mRefreshLayout!!.pullToRefreshListView
        mAdapter = DeviceListAdapter(this)
        mListView?.setAdapter(mAdapter)

        mListView?.setOnRefreshListener(OnRefreshListener { // TODO Auto-generated method stub
            searchDevice()
        })

        searchDevice()

        ClientManager.getClient().registerBluetoothStateListener(object : BluetoothStateListener() {
            override fun onBluetoothStateChanged(openOrClosed: Boolean) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed))
            }
        })
    }

    private fun searchDevice() {
        val request = SearchRequest.Builder()
            .searchBluetoothLeDevice(5000, 2).build()

        ClientManager.getClient().search(request, mSearchResponse)
    }

    private val mSearchResponse: SearchResponse = object : SearchResponse {
        override fun onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted")
            mListView!!.onRefreshComplete(true)
            mRefreshLayout!!.showState(AppConstants.LIST)
            mTvTitle!!.setText(R.string.string_refreshing)
            mDevices!!.clear()
        }

        override fun onDeviceFounded(device: SearchResult) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices!!.contains(device)) {
                mDevices!!.add(device)
                mAdapter!!.setDataList(mDevices)

                //                Beacon beacon = new Beacon(device.scanRecord);
//                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));

//                BeaconItem beaconItem = null;
//                BeaconParser beaconParser = new BeaconParser(beaconItem);
//                int firstByte = beaconParser.readByte(); // 读取第1个字节
//                int secondByte = beaconParser.readByte(); // 读取第2个字节
//                int productId = beaconParser.readShort(); // 读取第3,4个字节
//                boolean bit1 = beaconParser.getBit(firstByte, 0); // 获取第1字节的第1bit
//                boolean bit2 = beaconParser.getBit(firstByte, 1); // 获取第1字节的第2bit
//                beaconParser.setPosition(0); // 将读取起点设置到第1字节处
            }

            if (mDevices!!.size > 0) {
                mRefreshLayout!!.showState(AppConstants.LIST)
            }
        }

        override fun onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped")
            mListView!!.onRefreshComplete(true)
            mRefreshLayout!!.showState(AppConstants.LIST)

            mTvTitle!!.setText(R.string.devices)
        }

        override fun onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled")

            mListView!!.onRefreshComplete(true)
            mRefreshLayout!!.showState(AppConstants.LIST)

            mTvTitle!!.setText(R.string.devices)
        }
    }

    override fun onPause() {
        super.onPause()
        ClientManager.getClient().stopSearch()
    }

    companion object {
        private const val MAC = "B0:D5:9D:6F:E7:A5"
    }
}
