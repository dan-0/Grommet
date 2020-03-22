package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Locale;

public abstract class AdditionalInfo implements Parcelable, BaseColumns {


    public enum Type {
        LANGUAGE_PREF("preferred_language"),
        ASSISTANT_DECLARATION("assistant_declaration");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static Type fromString(String type) {
            for (Type val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return LANGUAGE_PREF;
        }
    }

    public enum PreferredLanguage {
        ENGLISH("English", "Ingles"),
        SPANISH("Spanish", "Español"),
        CHINESE("Chinese", "Chino"),
        GERMAN("German", "Aleman"),
        ITALIAN("Italian", "Italiano"),
        FRENCH("French", "Francés"),
        ARABIC("Arabic", "Arabe"),
        ARMENIAN("Armenian", "Armenio"),
        CREOLE("Creole", "Criollo"),
        CROATION("Croation", "Croata"),
        GREEK("Greek", "Griego"),
        GUJARATI("Gujarati", "Gujarati"),
        HEBREW("Hebrew", "Hebreo"),
        HINDI("Hindi", "Hindi"),
        HMOUNG("Hmoung", "Hmong"),
        HUNGARIAN("Hungarian", "Húngaro"),
        JAPANESE("Japanese", "Japonés"),
        KOREAN("Korean", "Coreano"),
        LAOTIAN("Laotian", "Laosiano"),
        KHMER("Khmer", "Khmer"),
        NAVAJO("Navajo", "Navajo"),
        PERSIAN("Persian", "Persa"),
        POLISH("Polish", "Polaco"),
        PORTUGUESE("Portuguese", "Portugués"),
        RUSSIAN("Russian", "Ruso"),
        TAGALOG("Tagalog", "Tagalo"),
        THAI("Thai", "Tailandés"),
        URDU("Urdu", "Urdu"),
        VIETNAMESE("Vietnamese", "Vietnamita");

        private final String enPref;
        private final String esPref;

        PreferredLanguage(String enPref, String esPref) {
            this.enPref = enPref;
            this.esPref = esPref;
        }

        @Override
        public String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esPref;
            } else {
                // default to english
                return enPref;
            }
        }

        public static PreferredLanguage fromString(String language) {
            for (PreferredLanguage val : values()) {
                if (val.enPref.equals(language) ||
                        val.esPref.equals(language)) {
                    return val;
                }
            }
            return ENGLISH;
        }
    }


}
