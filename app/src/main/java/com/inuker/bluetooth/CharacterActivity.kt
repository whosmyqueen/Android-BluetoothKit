package com.inuker.bluetooth

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.inuker.bluetooth.library.Constants
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.response.BleMtuResponse
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import com.inuker.bluetooth.library.connect.response.BleReadResponse
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse
import com.inuker.bluetooth.library.connect.response.BleWriteResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.library.utils.ByteUtils
import java.util.UUID

/**
 * Created by dingjikerbo on 2016/9/6.
 */
class CharacterActivity : Activity(), View.OnClickListener {
    private var mMac: String? = null
    private var mService: UUID? = null
    private var mCharacter: UUID? = null

    private var mTvTitle: TextView? = null

    private var mBtnRead: Button? = null

    private var mBtnWrite: Button? = null
    private var mEtInput: EditText? = null

    private var mBtnNotify: Button? = null
    private var mBtnUnnotify: Button? = null
    private var mEtInputMtu: EditText? = null
    private var mBtnRequestMtu: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.character_activity)

        val intent = intent
        mMac = intent.getStringExtra("mac")
        mService = intent.getSerializableExtra("service") as UUID?
        mCharacter = intent.getSerializableExtra("character") as UUID?

        mTvTitle = findViewById<View>(R.id.title) as TextView
        mTvTitle!!.text = String.format("%s", mMac)

        mBtnRead = findViewById<View>(R.id.read) as Button

        mBtnWrite = findViewById<View>(R.id.write) as Button
        mEtInput = findViewById<View>(R.id.input) as EditText

        mBtnNotify = findViewById<View>(R.id.notify) as Button
        mBtnUnnotify = findViewById<View>(R.id.unnotify) as Button

        mEtInputMtu = findViewById<View>(R.id.et_input_mtu) as EditText
        mBtnRequestMtu = findViewById<View>(R.id.btn_request_mtu) as Button

        mBtnRead!!.setOnClickListener(this)
        mBtnWrite!!.setOnClickListener(this)

        mBtnNotify!!.setOnClickListener(this)
        mBtnNotify!!.isEnabled = true

        mBtnUnnotify!!.setOnClickListener(this)
        mBtnUnnotify!!.isEnabled = false

        mBtnRequestMtu!!.setOnClickListener(this)
    }

    private val mReadRsp = BleReadResponse { code, data ->
        if (code == Constants.REQUEST_SUCCESS) {
            mBtnRead!!.text = String.format("read: %s", ByteUtils.byteToString(data))
            CommonUtils.toast("success")
        } else {
            CommonUtils.toast("failed")
            mBtnRead!!.text = "read"
        }
    }

    private val mWriteRsp = BleWriteResponse { code ->
        if (code == Constants.REQUEST_SUCCESS) {
            CommonUtils.toast("success")
        } else {
            CommonUtils.toast("failed")
        }
    }

    private val mNotifyRsp: BleNotifyResponse = object : BleNotifyResponse {
        override fun onNotify(service: UUID, character: UUID, value: ByteArray) {
            if (service == mService && character == mCharacter) {
                mBtnNotify!!.text = String.format("%s", ByteUtils.byteToString(value))
            }
        }

        override fun onResponse(code: Int) {
            if (code == Constants.REQUEST_SUCCESS) {
                mBtnNotify!!.isEnabled = false
                mBtnUnnotify!!.isEnabled = true
                CommonUtils.toast("success")
            } else {
                CommonUtils.toast("failed")
            }
        }
    }

    private val mUnnotifyRsp = BleUnnotifyResponse { code ->
        if (code == Constants.REQUEST_SUCCESS) {
            CommonUtils.toast("success")
            mBtnNotify!!.isEnabled = true
            mBtnUnnotify!!.isEnabled = false
        } else {
            CommonUtils.toast("failed")
        }
    }

    private val mMtuResponse = BleMtuResponse { code, data ->
        if (code == Constants.REQUEST_SUCCESS) {
            CommonUtils.toast("request mtu success,mtu = $data")
        } else {
            CommonUtils.toast("request mtu failed")
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.read -> ClientManager.getClient().read(mMac, mService, mCharacter, mReadRsp)
            R.id.write -> ClientManager.getClient().write(
                mMac, mService, mCharacter, ByteUtils.stringToBytes(mEtInput!!.text.toString()), mWriteRsp
            )

            R.id.notify -> ClientManager.getClient().notify(mMac, mService, mCharacter, mNotifyRsp)
            R.id.unnotify -> ClientManager.getClient().unnotify(mMac, mService, mCharacter, mUnnotifyRsp)
            R.id.btn_request_mtu -> {
                val mtuStr = mEtInputMtu!!.text.toString()
                if (TextUtils.isEmpty(mtuStr)) {
                    CommonUtils.toast("MTU不能为空")
                    return
                }
                val mtu = mtuStr.toInt()
                if (mtu < Constants.GATT_DEF_BLE_MTU_SIZE || mtu > Constants.GATT_MAX_MTU_SIZE) {
                    CommonUtils.toast("MTU不不在范围内")
                    return
                }
                ClientManager.getClient().requestMtu(mMac, mtu, mMtuResponse)
            }
        }
    }

    private val mConnectStatusListener: BleConnectStatusListener = object : BleConnectStatusListener() {
        override fun onConnectStatusChanged(mac: String, status: Int) {
            BluetoothLog.v(String.format("CharacterActivity.onConnectStatusChanged status = %d", status))

            if (status == Constants.STATUS_DISCONNECTED) {
                CommonUtils.toast("disconnected")
                mBtnRead!!.isEnabled = false
                mBtnWrite!!.isEnabled = false
                mBtnNotify!!.isEnabled = false
                mBtnUnnotify!!.isEnabled = false

                mTvTitle!!.postDelayed({ finish() }, 300)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ClientManager.getClient().registerConnectStatusListener(mMac, mConnectStatusListener)
    }

    override fun onPause() {
        super.onPause()
        ClientManager.getClient().unregisterConnectStatusListener(mMac, mConnectStatusListener)
    }
}
