package com.example.hs20150126.day8_task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity {
	public static final String TAG = "CalendarMonthViewActivity";

	GridView monthView;
	CalendarMonthAdapter monthViewAdapter;

	TextView monthText;

	TextView editText;

	int curYear;
	int curMonth;

	int curPosition;
	EditText scheduleInput;
	Button saveButton;

	ListView scheduleList;
	ScheduleListAdapter scheduleAdapter;
	ArrayList outScheduleList;

	public static final int REQUEST_CODE_SCHEDULE_INPUT = 1001;
	public static final int WEATHER_PROGRESS_DIALOG = 1002;
	public static final int WEATHER_SAVED_DIALOG = 1003;

	private static final String BASE_URL = "http://www.google.com";
	private static String WEATHER_URL = "http://www.kma.go.kr/XML/weather/sfc_web_map.xml";

	private static boolean weatherCanceled;

	WeatherCurrentCondition weather = null;

	Handler handler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		monthView = (GridView) findViewById(R.id.monthView);
		monthViewAdapter = new CalendarMonthAdapter(this);
		monthView.setAdapter(monthViewAdapter);

		// set listener
		monthView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MonthItem curItem = (MonthItem) monthViewAdapter.getItem(position);
				int day = curItem.getDay();

				//Toast.makeText(getApplicationContext(), day + "일이 선택되었습니다.", 1000).show();

				monthViewAdapter.setSelectedPosition(position);
				monthViewAdapter.notifyDataSetChanged();

				// set schedule to the TextView
				curPosition = position;

				outScheduleList = monthViewAdapter.getSchedule(position);
				if (outScheduleList == null) {
					outScheduleList = new ArrayList<ScheduleListItem>();
				}
				scheduleAdapter.scheduleList = outScheduleList;

				scheduleAdapter.notifyDataSetChanged();
			}
		});

		monthText = (TextView) findViewById(R.id.monthText);
		setMonthText();

		Button monthPrevious = (Button) findViewById(R.id.monthPrevious);
		monthPrevious.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				monthViewAdapter.setPreviousMonth();
				monthViewAdapter.notifyDataSetChanged();

				setMonthText();
			}
		});

		Button monthNext = (Button) findViewById(R.id.monthNext);
		monthNext.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				monthViewAdapter.setNextMonth();
				monthViewAdapter.notifyDataSetChanged();

				setMonthText();
			}
		});


		curPosition = -1;

		scheduleList = (ListView)findViewById(R.id.scheduleList);
		scheduleAdapter = new ScheduleListAdapter(this);
		scheduleList.setAdapter(scheduleAdapter);


	}


	private void setMonthText() {
		curYear = monthViewAdapter.getCurYear();
		curMonth = monthViewAdapter.getCurMonth();

		monthText.setText(curYear + "년 " + (curMonth+1) + "월");
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		addOptionMenuItems(menu);

		return true;
	}

	private void addOptionMenuItems(Menu menu) {
		int id = Menu.FIRST;
		menu.clear();

		menu.add(id, id, Menu.NONE, "일정 추가");

		id = Menu.FIRST+1;
		menu.add(id, id, Menu.NONE, "오늘날씨 저장");
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case Menu.FIRST:
				showScheduleInput();

				return true;
			case Menu.FIRST+1:
				getCurrentWeather();

				return true;
			default:
				break;
		}

		return false;
	}

	/**
	 * get current weather
	 */
	private void getCurrentWeather() {
		weatherCanceled = false;

		showDialog(WEATHER_PROGRESS_DIALOG);

		CurrentWeatherSaveThread thread = new CurrentWeatherSaveThread();
		thread.start();

	}


	class CurrentWeatherSaveThread extends Thread {
		public CurrentWeatherSaveThread() {

		}

		public void run() {
			try {
				URL url = new URL(
						"http://www.kma.go.kr/XML/weather/sfc_web_map.xml");
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(url.openStream(), "utf-8");
				String Item = "";
				String ItemName = "";
				String ItemContents = "";
				boolean bSet = false;
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
						case XmlPullParser.START_DOCUMENT:
							break;
						case XmlPullParser.END_DOCUMENT:
							break;
						case XmlPullParser.START_TAG:
							String tag = parser.getName();
							if (tag.equals("local")) {
								ItemContents = "";
								String state = parser.getAttributeValue(null,
										"desc");
								String temperature = "섭씨";
								temperature += parser.getAttributeValue(null,
										"ta");
								temperature += "º";
								ItemContents = state + " , " + temperature
										+ "  ";
								bSet = true;
							}
							break;
						case XmlPullParser.END_TAG:
							break;
						case XmlPullParser.TEXT:
							if (bSet) {
								ItemName = "";
								String region = parser.getText();

								if(region.equals("서울"))
								{
									System.out.println("region = "+region+"    236   :");
									ItemName += region + "   - ";
									Item += ItemName + ItemContents;
									Item += "\n";
									bSet = false;

									editText.setText(region.toString());

								}


							}
							break;
					}
					eventType = parser.next();
				}
				//edtResult.setText(Item);

				System.out.println(Item.toString());
			} catch(Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private InputStream getInputStreamUsingHTTP(URL url) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		int resCode = conn.getResponseCode();
		Log.d(TAG, "Response Code : " + resCode);

		if (weatherCanceled) {
			return null;
		}

		InputStream instream = conn.getInputStream();
		return instream;
	}

	Runnable completedRunnable = new Runnable() {
		public void run() {
			removeDialog(WEATHER_PROGRESS_DIALOG);

			if (weatherCanceled) {
				weather = null;
			} else {
				Toast.makeText(getApplicationContext(), "Current Weather : " + weather.getCondition() + ", " + weather.getIconURL(), Toast.LENGTH_LONG).show();

				// set today weather
				int todayPosition = monthViewAdapter.getTodayPosition();
				Log.d(TAG, "today position : " + todayPosition);

				monthViewAdapter.putWeather(monthViewAdapter.todayYear, monthViewAdapter.todayMonth, todayPosition, weather);
				monthViewAdapter.notifyDataSetChanged();

				showDialog(WEATHER_SAVED_DIALOG);
			}
		}
	};


	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case WEATHER_PROGRESS_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("날씨정보 가져오는 중...");
				progressDialog.setCancelable(true);
				progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						weatherCanceled = true;
					}
				});

				return progressDialog;
			case WEATHER_SAVED_DIALOG:
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
				alertBuilder.setMessage("날씨정보를 저장하였습니다.");
				alertBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				AlertDialog alertDialog = alertBuilder.create();
				return alertDialog;
		}

		return null;
	}


	private void showScheduleInput() {
		Intent intent = new Intent(this, ScheduleInputActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SCHEDULE_INPUT);
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == REQUEST_CODE_SCHEDULE_INPUT) {
			if (intent == null) {
				return;
			}

			String time = intent.getStringExtra("time");
			String message = intent.getStringExtra("message");

			if (message != null) {
				//Toast toast = Toast.makeText(getBaseContext(), "result code : " + resultCode + ", time : " + time + ", message : " + message, Toast.LENGTH_LONG);
				//toast.show();

				ScheduleListItem aItem = new ScheduleListItem(time, message);


				if (outScheduleList == null) {
					outScheduleList = new ArrayList();
				}
				outScheduleList.add(aItem);

				monthViewAdapter.putSchedule(curPosition, outScheduleList);

				scheduleAdapter.scheduleList = outScheduleList;
				scheduleAdapter.notifyDataSetChanged();
			}
		}

	}

}