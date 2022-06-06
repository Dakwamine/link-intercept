package info.quangminhdang.linkintercept;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Set;

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
        final TextView urlParametersTextView = (TextView) findViewById(R.id.urlParameters_textView);

        // Enable click on links.
        urlParametersTextView.setMovementMethod(LinkMovementMethod.getInstance());

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
        URLState urlState = CheckURLValidity(intent.getDataString(), urlLengthLabel, urlStateLabel, openButton, clearButton);

        // Update URL parameters
        UpdateUrlParameters(urlState, intent.getDataString(), urlParametersTextView);

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
                URLState urlState = CheckURLValidity(s.toString(), urlLengthLabel, urlStateLabel, openButton, clearButton);
                UpdateUrlParameters(urlState, s.toString(), urlParametersTextView);
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

        if (_urlToValidate.length() == 0) {
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


    private void UpdateUrlParameters(URLState _urlState, String _validatedUrl, TextView _urlParametersTextView) {
        switch (_urlState) {
            case VALID:
                StringBuilder sb = new StringBuilder();

                ParseQueryParameters(sb, _validatedUrl);

                _urlParametersTextView.setText(Html.fromHtml(sb.toString()));
                break;

            default:
                // Invalid URL, don't bother parsing it.
                _urlParametersTextView.setText(GetStringFromResources(R.string.urlParametersTextViewPlaceholder));
                break;
        }
    }

    /**
     * Recursive parser.
     *
     * @param _sb StringBuilder object which will contain the output.
     * @param _url Url string to parse.
     */
    private void ParseQueryParameters(StringBuilder _sb, String _url) {
        // Parse URL
        Uri uri = Uri.parse(_url);

        // This will prevent trying to get parameters from invalid URI (such as mailtos).
        if(!uri.isHierarchical())
            return;

        Set<String> queryParameterNames = uri.getQueryParameterNames();

        _sb.append("<ul>");

        // Host, aka URL without path.
        String host = uri.isAbsolute() ? uri.getScheme() + "://" : "";
        host += uri.getHost();

        if(!host.equals("null")) {
            _sb.append("<li><strong>Host:</strong> ");
            AppendAnchor(_sb, host, host);
            _sb.append("</li>");

            // Host + path
            _sb.append("<li><strong>Host and path:</strong> ");
            AppendAnchor(_sb, host + uri.getPath(), host + uri.getPath());
            _sb.append("</li>");
        }

        // No query (sub)parameter? No need to parse this item
        if(queryParameterNames.size() == 0)
            return;

        _sb.append("<li><strong>Parameters:</strong><ul>");

        for (String queryParameterName : queryParameterNames) {
            _sb.append("<li>");
            _sb.append(queryParameterName);
            _sb.append(": ");

            // If the value is a valid URL, display it in <a> tag.
            String value = uri.getQueryParameter(queryParameterName);

            boolean isURL = false;

            if (Patterns.WEB_URL.matcher(value).matches()) {
                // We don't care if the URI scheme is missing here.
                isURL = true;
            }

            if(isURL) {
                AppendAnchor(_sb, value, value);
            }
            else {
                _sb.append(value);
            }

            // Try to parse sub items if any.
            ParseQueryParameters(_sb, value);

            _sb.append("</li>");
        }

        _sb.append("</ul>");
        _sb.append("</li>");
        _sb.append("</ul>");
    }

    private void AppendAnchor(StringBuilder _sb, String _href, String _label) {
        _sb.append("<a href=\"");
        _sb.append(_href);
        _sb.append("\">");
        _sb.append(_label);
        _sb.append("</a>");
    }


    private enum URLState {
        INVALID,
        VALID,
        EMPTY,
        MISSING_URI_SCHEME
    }
}
