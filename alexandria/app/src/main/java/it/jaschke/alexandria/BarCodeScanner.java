package it.jaschke.alexandria;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class BarCodeScanner extends ActionBarActivity implements ZBarScannerView.ResultHandler{
    private ZBarScannerView mScannerView;
    private String TAG = BarCodeScanner.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.v(TAG, result.getContents());
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("scannedBarcode", result.getContents());
        startActivity(intent);
    }
}
