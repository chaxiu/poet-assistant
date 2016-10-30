/*
 * Copyright (c) 2016 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.dictionaries;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;
import java8.util.stream.StreamSupport;

/**
 * SharedPreferences and db-backed cursor to read and add suggestions.  Suggestions include
 * words which have been looked up before, as well as similar words in the database.
 */
class SuggestionsCursor extends MatrixCursor {
    private static final String[] COLUMNS =
            new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1};

    private final SettingsPrefs mSettingsPrefs;
    private final Dictionary mDictionary;
    private String mFilter;

    SuggestionsCursor(Context context) {
        super(COLUMNS);
        mSettingsPrefs = SettingsPrefs.get(context);
        mDictionary = Dictionary.getInstance(context);
    }

    void setFilter(String filter) {
        mFilter = filter;
    }

    void load() {
        loadHistory();
        loadSimilarWords();
    }

    private void loadHistory() {
        Set<String> suggestions = mSettingsPrefs.getSuggestedWords();
        TreeSet<String> sortedSuggestions = new TreeSet<>();
        sortedSuggestions.addAll(suggestions);
        StreamSupport.stream(sortedSuggestions)
                .filter(suggestion -> TextUtils.isEmpty(mFilter) || suggestion.contains(mFilter))
                .forEach(suggestion -> addSuggestion(suggestion, R.drawable.ic_search_history));
    }

    private void loadSimilarWords() {
        if (!TextUtils.isEmpty(mFilter)) {
            String[] similarSoundingWords = mDictionary.findWordsWithPrefix(mFilter.trim().toLowerCase(Locale.getDefault()));
            for (String similarSoundingWord : similarSoundingWords) {
                addSuggestion(similarSoundingWord, R.drawable.ic_action_search);
            }
        }
    }

    private void addSuggestion(String word, @DrawableRes int iconId) {
        addRow(new Object[]{getCount(), word, iconId});
    }

    void clear() {
        mFilter = null;
        mSettingsPrefs.removeSuggestedWords();
    }

    void saveNewSuggestion(String suggestion) {
        Set<String> suggestionsReadOnly = mSettingsPrefs.getSuggestedWords();
        if (!suggestionsReadOnly.contains(suggestion)) {
            addSuggestion(suggestion, R.drawable.ic_search_history);
            TreeSet<String> suggestions = new TreeSet<>();
            suggestions.addAll(suggestionsReadOnly);
            suggestions.add(suggestion);
            mSettingsPrefs.putSuggestedWords(suggestions);
        }
    }

}