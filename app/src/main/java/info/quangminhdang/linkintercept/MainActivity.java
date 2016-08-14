package info.quangminhdang.linkintercept;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.system.StructPollfd;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();

        final LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.buttons_linearLayout);
        final EditText url_editText = (EditText) findViewById(R.id.interceptedLink_editText);
        final TextView urlLengthLabel = (TextView) findViewById(R.id.urlLength_textView);
        final TextView urlStateLabel = (TextView) findViewById(R.id.urlState_textView);
        final Button openButton = (Button) findViewById(R.id.open_button);
        final Button clearButton = (Button) findViewById(R.id.clear_button);


        // This case happens when opening the app from the dashboard for example
        if (intent.getDataString() == null) {
            urlLengthLabel.setText(GetStringFromResources(R.string.pleaseOpenWithALink));
            ((ViewGroup) urlStateLabel.getParent()).removeView(urlStateLabel);
            ((ViewGroup) url_editText.getParent()).removeView(url_editText);
            ((ViewGroup) buttonsLayout.getParent()).removeView(buttonsLayout);
            return;
        }


        // Display URL
        url_editText.setText(intent.getDataString());


        // This will configure properly buttons activation states and info
        CheckURLValidity(intent.getDataString(), urlLengthLabel, urlStateLabel, openButton, clearButton);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(url_editText.getText().toString());
                Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(openLinkIntent);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url_editText.setText("");
            }
        });


        url_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CheckURLValidity(s.toString(), urlLengthLabel, urlStateLabel, openButton, clearButton);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private String GetStringFromResources(int _stringId) {
        return getResources().getString(_stringId);
    }

    private URLState CheckURLValidity(String _urlToValidate, TextView _urlLengthLabel, TextView _results_textView, Button _openButton, Button _clearButton) {
        _urlLengthLabel.setText(String.format(GetStringFromResources(R.string.mainLabelForValidUrl), _urlToValidate.length()));

        if (_urlToValidate.length() == 0){
            // Empty string
            _results_textView.setText(String.format(GetStringFromResources(R.string.urlState_textView), GetStringFromResources(R.string.urlState_emptyString)));
            _clearButton.setEnabled(false);
            _openButton.setEnabled(false);
            return URLState.EMPTY;
        }

        _clearButton.setEnabled(true);

        if (Patterns.WEB_URL.matcher(_urlToValidate).matches()) {
            if (URLUtil.isHttpUrl(_urlToValidate) || URLUtil.isHttpsUrl(_urlToValidate)) {
                // Valid
                _results_textView.setText(String.format(GetStringFromResources(R.string.urlState_textView), GetStringFromResources(R.string.urlState_ok)));
                _openButton.setEnabled(true);
                return URLState.VALID;
            } else {
                // Missing http:// or https://. We need to check this because the patterns matcher will evaluate URLs without URI scheme valid.
                _results_textView.setText(String.format(GetStringFromResources(R.string.urlState_textView), GetStringFromResources(R.string.urlState_uriSchemeMissing)));
                _openButton.setEnabled(false);
                return URLState.MISSING_URI_SCHEME;
            }
        } else {
            // Undefined invalidity
            _results_textView.setText(String.format(GetStringFromResources(R.string.urlState_textView), GetStringFromResources(R.string.urlState_invalid)));
            _openButton.setEnabled(false);
            return URLState.INVALID;
        }
    }



    private enum URLState{
        INVALID,
        VALID,
        EMPTY,
        MISSING_URI_SCHEME
    }
}
