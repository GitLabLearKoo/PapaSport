package qs.read_nfc;

import NFC.NdefMessageParser;
import NFC.ParsedNdefRecord;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import com.adrian.nfcmodule.NFCUtils;
import com.adrian.nfcmodule.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NFCActivity extends Activity {

	private static final DateFormat TIME_FORMAT = SimpleDateFormat
			.getDateTimeInstance();
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private NdefMessage mNdefPushMessage;
	private TextView promt;
	private AlertDialog mDialog;

    private NFCUtils nfcUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);
		promt = (TextView) findViewById(R.id.promt);

        nfcUtils = new NFCUtils(this, new NFCUtils.INFCListener() {

            @Override
            public void showNfcData(NdefMessage[] msgs) {
                promt.setText("");
                buildTagViews(msgs);
            }

            @Override
            public void getDecTagId(long decTagId) {
                Toast.makeText(NFCActivity.this, "tagID:" + decTagId, Toast.LENGTH_SHORT).show();
            }
        });

        nfcUtils.resolveIntent(getIntent());

		mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null)
				.create();

		// ��ȡĬ�ϵ�NFC������
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		//����ϵͳ����NFCɨ�裬����ɨ������
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{nfcUtils.newTextRecord("",
				Locale.ENGLISH, true) });

//		verifyStoragePermissions(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter == null) {
			if (!mAdapter.isEnabled()) {
				showWirelessSettingsDialog();
			}

			showMessage("error", " NO NFC");

			promt.setText("�豸��֧��NFC��");

			return;
		}
		if (!mAdapter.isEnabled()) {
			promt.setText("����ϵͳ������������NFC���ܣ�");
			return;
		}

		if (mAdapter != null) {
			//��ʽ����
			mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
			mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdapter != null) {
			//��ʽ����
			mAdapter.disableForegroundDispatch(this);
			mAdapter.disableForegroundNdefPush(this);
		}
	}
	private void showMessage(String title, String message) {
		mDialog.setTitle(title);
		mDialog.setMessage(message);
		mDialog.show();
	}

	private void showWirelessSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("nfc_disabled");
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(
								Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(intent);
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						finish();
					}
				});
		builder.create().show();
		return;
	}

	//��ʾNFCɨ�������
	private void buildTagViews(NdefMessage[] msgs) {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		// Parse the first message in the list
		// Build views for all of the sub records
//		Date now = new Date();
		List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
		final int size = records.size();
		for (int i = 0; i < size; i++) {
//			TextView timeView = new TextView(this);
//			timeView.setText(TIME_FORMAT.format(now));
			ParsedNdefRecord record = records.get(i);
			promt.append(record.getViewText());
		}
	}

	//��ȡϵͳ��ʽ������
	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
        nfcUtils.resolveIntent(intent);
	}
//	private static final int REQUEST_EXTERNAL_STORAGE = 1;
//	private static String[] PERMISSIONS_STORAGE = {
//			"android.permission.READ_EXTERNAL_STORAGE",
//			"android.permission.WRITE_EXTERNAL_STORAGE" };
//	// ����дȨ��
//	public static void verifyStoragePermissions(Activity activity) {
//		try {
//			// ����Ƿ���д��Ȩ��
//			int permission = ActivityCompat.checkSelfPermission(activity,
//					"android.permission.WRITE_EXTERNAL_STORAGE");
//			if (permission != PackageManager.PERMISSION_GRANTED) {
//				// û��д��Ȩ�ޣ�ȥ����д��Ȩ�ޣ��ᵯ���Ի���
//				ActivityCompat.requestPermissions(activity,
//						PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
