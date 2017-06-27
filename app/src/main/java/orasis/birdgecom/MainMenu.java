package orasis.birdgecom;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Locale;

public class MainMenu extends Activity {

    private TextView bridgeText;
    private TextToSpeech speech;
    private EditText textInput;
    private Intent voiceRecogniser;
    private final Locale defaultLocale = Locale.getDefault();
    private Spinner languageList;
    private Button readText;
    private Button listenToVoice;
    private Button submit;
    private SpeechRecognizer sr;
    private String lastCommand;
    private String history = "";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static final int speech_not_supported=0x7f060004;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        textInput = (EditText) findViewById(R.id.editTextInput);
        readText = (Button) findViewById(R.id.buttonReadText);
        listenToVoice = (Button) findViewById(R.id.buttonListen);
        submit = (Button) findViewById(R.id.buttonSubmit);
        speech = Intro.mTts;
        bridgeText = (TextView) findViewById(R.id.textViewBridge);
        bridgeText.setMovementMethod(new ScrollingMovementMethod());
        languageList = (Spinner) findViewById(R.id.languageList);

        languageList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Locale locale = defaultLocale;
                switch ((int) parent.getItemIdAtPosition(position)){
                    case 0: locale = Locale.ENGLISH; break;
                    case 1: locale = Locale.FRENCH; break;
                    case 2: locale = Locale.ITALIAN; break;
                    case 3: locale = Locale.GERMAN; break;
                    case 4: locale = Locale.CHINESE; break;
                    case 5: locale = Locale.JAPANESE; break;
                    case 6: locale = Locale.KOREAN; break;
                    case 7: locale = defaultLocale;
                }
                changeLanguage(locale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        sr = SpeechRecognizer.createSpeechRecognizer(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history += '\n' + textInput.getText().toString();
                bridgeText.setText(history);
                lastCommand = textInput.getText().toString();
                textInput.setText("");
            }
        });

        textInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (textInput.getImeActionId() == EditorInfo.IME_ACTION_DONE) {
                    history += '\n' + textInput.getText().toString();
                    bridgeText.setText(history);
                    lastCommand = textInput.getText().toString();
                    handled = true;
                }
                return handled;
            }
        });

        listenToVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speech.speak("Please speak after the beep sound.", TextToSpeech.QUEUE_FLUSH, null);
                while (speech.isSpeaking()) {

                }
                promptSpeechInput();
            }
        });

        readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speech.stop();
                speech.speak(lastCommand, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private void changeLanguage(Locale locale) {
        speech.setLanguage(locale);
        voiceRecogniser = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceRecogniser.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
        Resources res = getApplicationContext().getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        Configuration configuration = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        res.updateConfiguration(configuration, displayMetrics);
    }

    @SuppressWarnings("ResourceType")
    private void promptSpeechInput() {
        voiceRecogniser = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceRecogniser.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceRecogniser.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//                getString(speech_prompt));
        try {
            startActivityForResult(voiceRecogniser, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    lastCommand = result.get(0);
                    history += '\n' +result.get(0);
                    bridgeText.setText(history);
                }

                break;
            }

        }
    }
}
