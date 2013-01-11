package com.kaushik.sample.paypaldonation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;
import java.math.BigDecimal;

public class PaypalActivity extends Activity implements OnClickListener{
    
    private String TAG = PaypalActivity.class.getSimpleName();
    private static final int request = 1; // the value used is up to you
    
    protected static final int INITIALIZE_SUCCESS = 0;
    protected static final int INITIALIZE_FAILURE = 1;

    ScrollView scroller;
    TextView labelPayment;
    EditText donationAmount;

    LinearLayout layoutPayment;

    CheckoutButton mCheckoutButton;
    
    // These are used to display the results of the transaction
    public static String resultTitle;
    public static String resultInfo;
    public static String resultExtra;
    
    // This handler will allow us to properly update the UI. You cannot touch Views from a non-UI thread.
    Handler hRefresh = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case INITIALIZE_SUCCESS:
                    setupButtons();
                    break;
                case INITIALIZE_FAILURE:
                    showFailure();
                    break;
            }
        }
    };

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the library. We'll do it in a separate thread because it requires communication with the server
        // which may take some time depending on the connection strength/speed.
        
        new PaymentTask(PaypalActivity.this, paypalHandler).execute();
        
        // Setup our UI.
        setContentView(R.layout.activity_paypal);
        initUI();
        
    }
    
    private void initUI() {
        labelPayment = (TextView) findViewById(R.id.paypal_support_initiative);
        layoutPayment = (LinearLayout) findViewById(R.id.paypal_button);
        donationAmount = (EditText) findViewById(R.id.donation_amount);
        labelPayment.setText("Support the initiative");    
        labelPayment.setVisibility(View.GONE);
        
    }
    
    private Handler paypalHandler = new Handler(){
     
        public void handleMessage(android.os.Message msg) {// The library is initialized so let's create our CheckoutButton and update the UI.
            if (PayPal.getInstance().isLibraryInitialized()) {
                hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
            }
            else {
                hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
            }};
    
    }; 
     
    
    /**
     * Create our CheckoutButton and update the UI.
     */
    public void setupButtons() {
        PayPal pp = PayPal.getInstance();
        // Get the CheckoutButton. There are five different sizes. The text on the button can either be of type TEXT_PAY or TEXT_DONATE.
        mCheckoutButton = pp.getCheckoutButton(this, PayPal.BUTTON_194x37, CheckoutButton.TEXT_DONATE);
        // You'll need to have an OnClickListener for the CheckoutButton. For this application, MPL_Example implements OnClickListener and we
        // have the onClick() method below.
        mCheckoutButton.setOnClickListener(this);
        // The CheckoutButton is an android LinearLayout so we can add it to our display like any other View.
        layoutPayment.addView(mCheckoutButton);
                
        // Show our labels and the preapproval EditText.
        labelPayment.setVisibility(View.VISIBLE);
        //appVersion.setVisibility(View.VISIBLE);
        
    }
    
    /**
     * Show a failure message because initialization failed.
     */
    public void showFailure() {
        Toast.makeText(PaypalActivity.this, "error occured while initializing paypal payment. please try later", Toast.LENGTH_SHORT).show();
    }
    
    public void onClick(View v) {
        
        /**
         * For each call to checkout() and preapprove(), we pass in a ResultDelegate. If you want your application
         * to be notified as soon as a payment is completed, then you need to create a delegate for your application.
         * The delegate will need to implement PayPalResultDelegate and Serializable. See our ResultDelegate for
         * more details.
         */     
        
        if(v == mCheckoutButton) {
            
            Log.v(TAG, "amt = "+donationAmount.getText().toString().trim());
            Log.v(TAG, "hint = "+donationAmount.getHint().toString());
            
            if(donationAmount.getText().toString().length() > 0 || donationAmount.getHint().equals("1.00 (default amount in USD)")){
                
                if(donationAmount.getText().toString().length() == 0 && donationAmount.getHint().equals("1.00 (default amount in USD)")){
                    donationAmount.setText("1");
                }

                PayPalPayment newPayment = new PayPalPayment();
                newPayment.setSubtotal(new BigDecimal(Integer.parseInt(donationAmount.getText().toString())));
                newPayment.setCurrencyType("USD");
                newPayment.setRecipient("kaushik.atul@gmail.com");
                newPayment.setPaymentType(PayPal.PAYMENT_SUBTYPE_DONATIONS);
                newPayment.setMerchantName("Support the initiative");
                // Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
                newPayment.setMemo("Thanks for contributing generously!");
                Intent paypalIntent = PayPal.getInstance().checkout(newPayment, this);
                startActivityForResult(paypalIntent, request);
            }else{
                Toast.makeText(PaypalActivity.this, "Please enter the amount you want to donate", Toast.LENGTH_LONG).show();
                donationAmount.setHint("1.00 (default amount in USD)");
            }
            
            mCheckoutButton.updateButton();
        }/* else if(v == exitApp) {
            // The exit button was pressed, so close the application.
            finish();
        }*/
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != request)
            return;
        
        /**
         * If you choose not to implement the PayPalResultDelegate, then you will receive the transaction results here.
         * Below is a section of code that is commented out. This is an example of how to get result information for
         * the transaction. The resultCode will tell you how the transaction ended and other information can be pulled
         * from the Intent using getStringExtra.
         */
        switch(resultCode) {
        case Activity.RESULT_OK:
            Toast.makeText(PaypalActivity.this, "You have successfully completed this trasaction", Toast.LENGTH_SHORT).show();
            
           // resultTitle = "SUCCESS";
           // resultInfo = "You have successfully completed this " + (isPreapproval ? "preapproval." : "payment.");
            //resultExtra = "Transaction ID: " + data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
            break;
        case Activity.RESULT_CANCELED:
            Toast.makeText(PaypalActivity.this, "You have cancelled this trasaction", Toast.LENGTH_SHORT).show();
            
           // resultTitle = "CANCELED";
           // resultInfo = "The transaction has been cancelled.";
           // resultExtra = "";
            break;
        case PayPalActivity.RESULT_FAILURE:
            Toast.makeText(PaypalActivity.this, "This trasaction has failed", Toast.LENGTH_SHORT).show();
            
           // resultTitle = "FAILURE";
           // resultInfo = data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
           // resultExtra = "Error ID: " + data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
        }
        
        mCheckoutButton.updateButton();
    }
}
