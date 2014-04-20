package it.divito.touristexplorer;

import it.divito.touristexplorer.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * This activity allows the user to edit the options for tracking 
 * and its export to KMZ
 * 
 * @author Stefano Di Vito
 *
 */

@SuppressWarnings("deprecation")
public class OptionsActivity extends PreferenceActivity {
	
	Intent mIntent;
	// Per determinare se si vuole il colore del tracciamento automatico o meno
	CheckBoxPreference automaticColor;		
	// Lista dei colori disponibili per il tracciamento
	ListPreference listLineColorsMap;
	// Lista dei possibili tipi di movimento (a piedi, in bicicletta, in auto)
	ListPreference movingType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mIntent = getIntent(); 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_tourist_explorer);
		
		automaticColor = (CheckBoxPreference) getPreferenceManager().findPreference("automaticColor");		
		listLineColorsMap = (ListPreference) getPreferenceManager().findPreference("listLineColorsMap");		
		movingType = (ListPreference) getPreferenceManager().findPreference("automaticColorSetting");		
		
		// Listener per la checkbox di attivazione/disattivazione del colore
		// della traccia automatico. Se viene attivato, allora conseguentemente 
		// si può modificare anche il tipo di movimento; se disattivato, si disattiva
		// anche la modifica del tipo di movimento
		automaticColor
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (newValue.toString().equals("true")) {
							listLineColorsMap.setEnabled(false);
							movingType.setEnabled(true);
						} else {
							listLineColorsMap.setEnabled(true);
							movingType.setEnabled(false);
						}
						return true;
					}
				});
	}
	
	@Override
	public void onBackPressed() {
		//getPrefs();
		setResult(RESULT_OK, mIntent);
		finish();
	}
	
	
	public void onStart(Intent intent, int startId) {
    }
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Sul resume dell'activity, si ripristina lo stato che si aveva alla
		// precedente chiusura dell'activity stessa
		if(automaticColor.isChecked()){
			listLineColorsMap.setEnabled(false);
			movingType.setEnabled(true);
		}
		else{
			listLineColorsMap.setEnabled(true);
			movingType.setEnabled(false);
		}
	
	}

}
