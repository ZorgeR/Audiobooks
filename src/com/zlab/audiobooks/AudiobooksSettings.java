package com.zlab.audiobooks;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AudiobooksSettings extends PreferenceActivity {
	
        @Override
        protected void onCreate(Bundle savedInstanceState) {
        	
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);

                
        }
}