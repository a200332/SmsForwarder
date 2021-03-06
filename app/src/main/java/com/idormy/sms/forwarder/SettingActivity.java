package com.idormy.sms.forwarder;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.idormy.sms.forwarder.BroadCastReceiver.RebootBroadcastReceiver;
import com.idormy.sms.forwarder.utils.CacheUtil;
import com.idormy.sms.forwarder.utils.aUtil;
import com.xuexiang.xupdate.easy.EasyUpdate;
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker;

import java.util.HashMap;
import java.util.Map;


public class SettingActivity extends AppCompatActivity {
    private String TAG = "SettingActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.d(TAG, "onCreate: " + RebootBroadcastReceiver.class.getName());

        Switch check_with_reboot = (Switch) findViewById(R.id.switch_with_reboot);
        checkWithReboot(check_with_reboot);

        final TextView version_now = (TextView) findViewById(R.id.version_now);
        Button check_version_now = (Button) findViewById(R.id.check_version_now);
        try {
            version_now.setText(aUtil.getVersionName(SettingActivity.this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        check_version_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkNewVersion();
                try {
                    String updateUrl = "https://xupdate.bms.ink/update/checkVersion?appKey=com.idormy.sms.forwarder&versionCode=";
                    updateUrl += aUtil.getVersionCode(SettingActivity.this);

                    EasyUpdate.create(SettingActivity.this, updateUrl)
                            .updateChecker(new DefaultUpdateChecker() {
                                @Override
                                public void onBeforeCheck() {
                                    super.onBeforeCheck();
                                    Toast.makeText(SettingActivity.this, "查询中...", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onAfterCheck() {
                                    super.onAfterCheck();
                                }

                                @Override
                                public void noNewVersion(Throwable throwable) {
                                    super.noNewVersion(throwable);
                                    // 没有最新版本的处理
                                    Toast.makeText(SettingActivity.this, "已是最新版本！", Toast.LENGTH_LONG).show();
                                }
                            })
                            .update();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final TextView cache_size = (TextView) findViewById(R.id.cache_size);
        try {
            cache_size.setText(CacheUtil.getTotalCacheSize(SettingActivity.this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button clear_all_cache = (Button) findViewById(R.id.clear_all_cache);
        clear_all_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CacheUtil.clearAllCache(SettingActivity.this);
                try {
                    cache_size.setText(CacheUtil.getTotalCacheSize(SettingActivity.this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(SettingActivity.this, "缓存清理完成", Toast.LENGTH_LONG).show();
            }
        });

        Button join_qq_group = (Button) findViewById(R.id.join_qq_group);
        join_qq_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = "HvroJRfvK7GGfnQgaIQ4Rh1un9O83N7M";
                joinQQGroup(key);
            }
        });

    }

    //检查重启广播接受器状态并设置
    private void checkWithReboot(Switch withrebootSwitch) {
        //获取组件
        final ComponentName cm = new ComponentName(this.getPackageName(), RebootBroadcastReceiver.class.getName());

        final PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(cm);
        if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {

            withrebootSwitch.setChecked(true);
        } else {
            withrebootSwitch.setChecked(false);
        }
        withrebootSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int newState = (Boolean) isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                pm.setComponentEnabledSetting(cm, newState, PackageManager.DONT_KILL_APP);
                Log.d(TAG, "onCheckedChanged:" + isChecked);
            }
        });
    }

    private void checkNewVersion() {
        try {
            String updateUrl = "https://xupdate.bms.ink/update/checkVersion?appKey=com.idormy.sms.forwarder&versionCode=";
            updateUrl += aUtil.getVersionCode(SettingActivity.this);
            EasyUpdate.checkUpdate(SettingActivity.this, updateUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void feedbackcommit(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view1 = View.inflate(SettingActivity.this, R.layout.dialog_feedback, null);

        final EditText feedback_et_email = view1.findViewById(R.id.feedback_et_email);
        final EditText feedback_et_text = view1.findViewById(R.id.feedback_et_text);

        builder
                .setTitle(R.string.feedback_input_text)
                .setView(view1)
                .create();
        builder.setPositiveButton("提交反馈", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    Map<String, String> feedBackData = new HashMap<>();
                    feedBackData.put("email", feedback_et_email.getText().toString());
                    feedBackData.put("text", feedback_et_text.getText().toString());
                    /*new HttpUtil().asyncPost("https://api.sl.willanddo.com/api/tsms/feedBack", feedBackData, new HttpI.Callback() {
                        @Override
                        public void onResponse(String result) {
                            Log.i(TAG, "onResponse: " + result);
                            if (result != null) {
                                FeedBackResult feedBackResult = JSON.parseObject(result, FeedBackResult.class);
                                Log.i(TAG, "feedBackResult: " + feedBackResult);

                                if (feedBackResult != null) {
                                    JSONObject feedBackResultObject = JSON.parseObject(result);
                                    Toast.makeText(SettingActivity.this, feedBackResultObject.getString("message"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SettingActivity.this, "感谢您的反馈，我们将尽快处理！", Toast.LENGTH_LONG).show();

                                }
                            } else {
                                Toast.makeText(SettingActivity.this, "感谢您的反馈，我们将尽快处理！", Toast.LENGTH_LONG).show();

                            }

                        }

                        @Override
                        public void onError(String error) {
                            Log.i(TAG, "onError: " + error);
                            Toast.makeText(SettingActivity.this, error, Toast.LENGTH_LONG).show();

                        }
                    });*/

                } catch (Exception e) {
                    Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "feedback e: " + e.getMessage());
                }


            }
        }).show();
    }

    /****************
     *
     * 发起添加群流程。群号：idormy 多米互联(562854376) 的 key 为： HvroJRfvK7GGfnQgaIQ4Rh1un9O83N7M
     * 调用 joinQQGroup(HvroJRfvK7GGfnQgaIQ4Rh1un9O83N7M) 即可发起手Q客户端申请加群 idormy 多米互联(562854376)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            Toast.makeText(SettingActivity.this, "未安装手Q或安装的版本不支持！", Toast.LENGTH_LONG).show();
            return false;
        }
    }


}
