package com.ota.ibeacon4android.fw;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BeaconDetectiveCircle extends View {

	// ビューの端から外側の円までの余白
	private static final int GRAPH_PADDING = 20; // 20dp
	// 外側の円と内側の円の差
	private static final int GRAPH_WIDTH = 10; // 10dp
	// 外枠の幅
	private static final int STROKE_WIDTH = 1; // 1dp

	// 中心の座標
	private int mCenterX;
	private int mCenterY;

	// 外側の円の半径
	private float mOuterRadius;
	// 内側の円の半径
	private float mInnerRadius;
	// Indicatorの半径
	private float mIndicatorRadius;
	// Outer Indicatorの半径
	private float mIndicatorOuterRadius;

	// 外側の円が占める領域
	private RectF mOuterBounds;
	// 内側の円が占める領域
	private RectF mInnerBounds;

	// 表示文字
	private long mDistance;
	private int mrssi;
	private int mtxpower;

	// 塗りつぶし用
	private final Paint mFillPaint;
	// 枠線用
	private final Paint mStrokePaint;
	// 文字用(距離)
	private final Paint mStringPaint4Distance;
	// 文字用(単位)
	private final Paint mStringPaint4Unit;
	// 文字用(Proximity)
	private final Paint mStringPaint4Proximity;
	// 文字用(Rssi)
	private final Paint mStringPaint4rssi;
	// 文字用(Rssi説明)
	private final Paint mStringPaint4rssiexp;
	// インディケーターサークル用
	private final Paint mIndicatorCerclePaint;
	// インディケーターアウターサークル用
	private final Paint mIndicatorOuterCerclePaint;

	private final Path mPath = new Path();

	private boolean isLost = true;
	
	private boolean isIBeacon;

	public BeaconDetectiveCircle(Context context) {
		this(context, null);
	}

	public BeaconDetectiveCircle(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BeaconDetectiveCircle(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		float density = getContext().getResources().getDisplayMetrics().density;

		// オブジェクトの生成はコンストラクタで行い、onDraw()で行わない
		mFillPaint = new Paint();
		mFillPaint.setAntiAlias(true);

		mStrokePaint = new Paint();
		mStrokePaint.setAntiAlias(true);
		mStrokePaint.setStrokeWidth(STROKE_WIDTH * density);
		mStrokePaint.setStyle(Paint.Style.STROKE);

		mStringPaint4Distance = new Paint();
		mStringPaint4Distance.setAntiAlias(true);
		//mStringPaint4Distance.setTextSize(170);
		mStringPaint4Distance.setTextAlign(Paint.Align.CENTER);
		mStringPaint4Distance.setColor(Color.parseColor("#e56200"));

		mStringPaint4Unit = new Paint();
		mStringPaint4Unit.setAntiAlias(true);
		//mStringPaint4Unit.setTextSize(45);
		mStringPaint4Unit.setColor(Color.parseColor("#8095ad"));

		mStringPaint4Proximity = new Paint();
		mStringPaint4Proximity.setAntiAlias(true);
		//mStringPaint4Proximity.setTextSize(70);
		mStringPaint4Proximity.setTextAlign(Paint.Align.CENTER);
		mStringPaint4Proximity.setColor(Color.parseColor("#8095ad"));

		mStringPaint4rssi = new Paint();
		mStringPaint4rssi.setAntiAlias(true);
		//mStringPaint4rssi.setTextSize(45);
		mStringPaint4rssi.setColor(Color.parseColor("#8095ad"));

		mStringPaint4rssiexp = new Paint();
		mStringPaint4rssiexp.setAntiAlias(true);
		//mStringPaint4rssiexp.setTextSize(45);
		mStringPaint4rssiexp.setColor(Color.parseColor("#8095ad"));

		mIndicatorCerclePaint = new Paint();
		mIndicatorCerclePaint.setColor(Color.parseColor("#3399CC"));
		mIndicatorCerclePaint.setAntiAlias(true);
		mIndicatorCerclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mIndicatorOuterCerclePaint = new Paint();
		mIndicatorOuterCerclePaint.setColor(Color.parseColor("#8095ad"));
		mIndicatorOuterCerclePaint.setAntiAlias(true);
		mIndicatorOuterCerclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mIndicatorOuterCerclePaint.setAlpha(60);
	}

	private static class GraphItem {
		public long value;
		public int color;

		public GraphItem(long value, int color) {
			this.value = value;
			this.color = color;
		}
	}

	private ArrayList<GraphItem> mData = new ArrayList<GraphItem>();

	public void addData(long value, int color) {
		mData.add(new GraphItem(value, color));
	}

	public void setDistance(long distance) {
		this.mDistance = distance;
	}

	public void setTxPower(int txpower) {
		this.mtxpower = txpower;
	}

	public void setRssi(int rssi) {
		this.mrssi = rssi;
	}

	public void setLost(boolean bool) {
		this.isLost = bool;
	}

	public void setIBeacon(boolean bool) {
		this.isIBeacon = bool;
	}
	
	public void clearData() {
		mData.clear();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCenterX = w / 2;
		mCenterY = h / 2;

		float density = getContext().getResources().getDisplayMetrics().density;
		// 縦横の小さい方から余白を引いた分を外側の円の大きさとする
		mOuterRadius = (w > h ? mCenterY : mCenterX) - GRAPH_PADDING * density;
		// 内側の円
		mInnerRadius = mOuterRadius - GRAPH_WIDTH * density;
		// Indicatorの円
		mIndicatorRadius = GRAPH_WIDTH / 2 * density;

		// OuterIndicatorの円
		mIndicatorOuterRadius = (GRAPH_WIDTH + 30) / 2 * density;

		// 外側の円の占める領域
		mOuterBounds = new RectF(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius, mCenterY + mOuterRadius);

		// 内側の円の占める領域
		mInnerBounds = new RectF(mCenterX - mInnerRadius, mCenterY - mInnerRadius, mCenterX + mInnerRadius, mCenterY + mInnerRadius);
		
		// xxdpi基準値（参考）
		//mStringPaint4Distance.setTextSize(168);
		//mStringPaint4Unit.setTextSize(45);
		//mStringPaint4Proximity.setTextSize(69);
		//mStringPaint4rssi.setTextSize(45);
		//mStringPaint4rssiexp.setTextSize(45);
		mStringPaint4Distance.setTextSize(56 * density);
		mStringPaint4Unit.setTextSize(15 * density);
		mStringPaint4Proximity.setTextSize(23 * density);
		mStringPaint4rssi.setTextSize(15 * density);
		mStringPaint4rssiexp.setTextSize(15 * density);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int total = 0;
		for (GraphItem data : mData) {
			total += data.value;
		}

		if (total <= 0) {
			return;
		}

		float startAngle = 0;
		for (GraphItem data : mData) {
			// 各データの占める角度を計算し描画
			float sweepAngle = 360f * data.value / total;
			drawPie(canvas, startAngle, sweepAngle, data.color);
			startAngle += sweepAngle;
		}

		float density = getContext().getResources().getDisplayMetrics().density;
		// Distance
		if (isLost || !isIBeacon) {
			canvas.drawText("-", mCenterX, mCenterY, mStringPaint4Distance);
			canvas.drawText("m", mCenterX + (48 * density), mCenterY, mStringPaint4Unit);
		} else if (mDistance <= 100) {
			canvas.drawText(Long.toString(mDistance), mCenterX, mCenterY, mStringPaint4Distance);
			canvas.drawText("cm", mCenterX + (48 * density), mCenterY, mStringPaint4Unit);
		} else {
			canvas.drawText(Long.toString(mDistance / 100), mCenterX, mCenterY, mStringPaint4Distance);
			canvas.drawText("m", mCenterX + (48 * density), mCenterY, mStringPaint4Unit);
		}
		// Rssi
		canvas.drawText("Rssi:", mCenterX - (106 * density), mCenterY - (66 * density), mStringPaint4rssiexp);
		canvas.drawText(Integer.toString(mrssi), mCenterX - (66 * density), mCenterY - (66 * density), mStringPaint4rssi);
		canvas.drawText("/", mCenterX - (33 * density), mCenterY - (66 * density), mStringPaint4rssiexp);
		canvas.drawText("TxPower:", mCenterX - (20 * density), mCenterY - (66 * density), mStringPaint4rssiexp);
		canvas.drawText(Integer.toString(mtxpower), mCenterX + (50 * density), mCenterY - (66 * density), mStringPaint4rssi);
		// Proximity
		canvas.drawText(this.getProximity(mDistance), mCenterX, mCenterY + (33 * density), mStringPaint4Proximity);
		
//		if (isLost || !isIBeacon) {
//			canvas.drawText("-", mCenterX, mCenterY, mStringPaint4Distance);
//			canvas.drawText("M", mCenterX + 145, mCenterY, mStringPaint4Unit);
//		} else if (mDistance <= 100) {
//			canvas.drawText(Long.toString(mDistance), mCenterX, mCenterY, mStringPaint4Distance);
//			canvas.drawText("CM", mCenterX + 145, mCenterY, mStringPaint4Unit);
//		} else {
//			canvas.drawText(Long.toString(mDistance / 100), mCenterX, mCenterY, mStringPaint4Distance);
//			canvas.drawText("M", mCenterX + 145, mCenterY, mStringPaint4Unit);
//		}
//		// Rssi
//		canvas.drawText("Rssi:", mCenterX - 320, mCenterY - 200, mStringPaint4rssiexp);
//		canvas.drawText(Integer.toString(mrssi), mCenterX - 200, mCenterY - 200, mStringPaint4rssi);
//		canvas.drawText("/", mCenterX - 100, mCenterY - 200, mStringPaint4rssiexp);
//		canvas.drawText("TxPower:", mCenterX - 60, mCenterY - 200, mStringPaint4rssiexp);
//		canvas.drawText(Integer.toString(mtxpower), mCenterX + 150, mCenterY - 200, mStringPaint4rssi);
//		// Proximity
//		canvas.drawText(this.getProximity(mDistance), mCenterX, mCenterY + 100, mStringPaint4Proximity);
	}

	private String getProximity(long distance) {
		if (isLost) {
			return "lost";
		} else if (!isIBeacon) {
			return "unmeasurable";
		} else if (distance < 100) {
			return "immediate";
		} else if (distance < 300) {
			return "near";
		} else {
			return "far";
		}
	}

	private void drawPie(Canvas canvas, float startAngle, float sweepAngle, int color) {

		// 円グラフの開始地点を上部にするため、90°引いておく
		// 左右に1°ずつ隙間がはいるようにしておく
		startAngle = startAngle + 1 - 90;
		// sweepAngle = sweepAngle - 2;

		mPath.reset();

		// 内側の線
		mPath.arcTo(mInnerBounds, startAngle, sweepAngle);

		// 内側から外側に向かう線
		int x = (int) (Math.cos((startAngle + sweepAngle) * Math.PI / 180) * mOuterRadius);
		int y = (int) (Math.sin((startAngle + sweepAngle) * Math.PI / 180) * mOuterRadius);
		mPath.lineTo(x + mCenterX, y + mCenterY);

		// 外側の線
		mPath.arcTo(mOuterBounds, startAngle + sweepAngle, -sweepAngle);

		// 外側から内側に向かう線
		x = (int) (Math.cos((startAngle) * Math.PI / 180) * mInnerRadius);
		y = (int) (Math.sin((startAngle) * Math.PI / 180) * mInnerRadius);
		mPath.lineTo(x + mCenterX, y + mCenterY);

		// 外側のポイント
		int x2 = (int) (Math.cos((startAngle) * Math.PI / 180) * mOuterRadius);
		int y2 = (int) (Math.sin((startAngle) * Math.PI / 180) * mOuterRadius);

		// 塗りつぶしを描画
		mFillPaint.setColor(color);
		mFillPaint.setAlpha(160);
		canvas.drawPath(mPath, mFillPaint);

		// 枠線を描画
		mStrokePaint.setColor(color);
		canvas.drawPath(mPath, mStrokePaint);

		// Indicatorを描画
		if (startAngle != -89) {
			canvas.drawCircle((x + mCenterX + x2 + mCenterX) / 2, (y + mCenterY + y2 + mCenterY) / 2, mIndicatorRadius, mIndicatorCerclePaint);
			canvas.drawCircle((x + mCenterX + x2 + mCenterX) / 2, (y + mCenterY + y2 + mCenterY) / 2, mIndicatorOuterRadius, mIndicatorOuterCerclePaint);
			// canvas.drawCircle(x + mCenterX, y + mCenterY, 5,
			// mIndicatorCerclePaint);
			// canvas.drawCircle(x2 + mCenterX, y2 + mCenterY, 5,
			// mIndicatorCerclePaint);
		}

	}
}
